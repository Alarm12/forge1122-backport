package dev.rezipped.zipline.server;

import dev.rezipped.zipline.api.Cable;
import dev.rezipped.zipline.api.CableReference;
import dev.rezipped.zipline.api.CableHit;
import dev.rezipped.zipline.api.Cables;
import dev.rezipped.zipline.config.ZiplineConfig;
import dev.rezipped.zipline.network.MessageZiplineControl;
import dev.rezipped.zipline.network.MessageZiplineState;
import dev.rezipped.zipline.network.ZiplineNetworkHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class ZiplineServerHandler
{
    private static final Map<UUID, ZiplineState> STATES = new HashMap<UUID, ZiplineState>();
    private static final Map<UUID, Integer> COOLDOWNS = new HashMap<UUID, Integer>();

    public static void handleControl(EntityPlayerMP player, byte action, CableReference reference)
    {
        if (action == MessageZiplineControl.ATTACH)
        {
            attach(player, reference);
        }
        else if (action == MessageZiplineControl.JUMP)
        {
            detach(player, true);
        }
        else if (action == MessageZiplineControl.RELEASE)
        {
            detach(player, false);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote)
        {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        tickCooldown(player);

        ZiplineState state = STATES.get(player.getUniqueID());

        if (state == null)
        {
            return;
        }

        if (player.isDead || !ZiplineConfig.isAttachmentItem(player.getHeldItemMainhand()))
        {
            detach(player, false);
            return;
        }

        Cable cable = Cables.resolve(player.world, state.getReference());

        if (cable == null)
        {
            detach(player, false);
            return;
        }

        tickMovement(player, cable, state);
        tickDurability(player, state);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event)
    {
        STATES.remove(event.player.getUniqueID());
        COOLDOWNS.remove(event.player.getUniqueID());
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.player instanceof EntityPlayerMP)
        {
            detach((EntityPlayerMP) event.player, false);
        }
    }

    private static void attach(EntityPlayerMP player, CableReference reference)
    {
        if (reference == null || STATES.containsKey(player.getUniqueID()))
        {
            rejectAttach(player);
            return;
        }

        Integer cooldown = COOLDOWNS.get(player.getUniqueID());

        if (cooldown != null && cooldown.intValue() > 0)
        {
            rejectAttach(player);
            return;
        }

        if (!ZiplineConfig.isAttachmentItem(player.getHeldItemMainhand()))
        {
            rejectAttach(player);
            return;
        }

        Cable cable = Cables.resolve(player.world, reference);

        if (cable == null)
        {
            rejectAttach(player);
            return;
        }

        Vec3d eye = player.getPositionEyes(1.0F);
        CableHit hit = Cables.findLookedAt(
            player.world,
            eye,
            player.getLookVec(),
            ZiplineConfig.clickReach,
            ZiplineConfig.snapRadius
        );

        if (hit == null || !hit.getCable().getReference().getProviderId().equals(reference.getProviderId())
            || !hit.getCable().getReference().getData().equals(reference.getData()))
        {
            rejectAttach(player);
            return;
        }

        double t = hit.getT();

        Vec3d tangent = cable.getTangent(t);
        Vec3d look = player.getLookVec();
        double direction = look.dotProduct(tangent) >= 0.0D ? 1.0D : -1.0D;
        double velocity = 0.35D * ZiplineConfig.speedMultiplier * direction;

        STATES.put(player.getUniqueID(), new ZiplineState(reference, t, velocity));
        ZiplineNetworkHandler.CHANNEL.sendTo(new MessageZiplineState(true), player);
    }

    private static void rejectAttach(EntityPlayerMP player)
    {
        ZiplineNetworkHandler.CHANNEL.sendTo(new MessageZiplineState(false), player);
    }

    private static void tickMovement(EntityPlayerMP player, Cable cable, ZiplineState state)
    {
        double t = state.getT();
        Vec3d tangent = cable.getTangent(t);
        double velocity = state.getVelocity();

        if (ZiplineConfig.realisticPhysics)
        {
            double desiredDirection = player.getLookVec().dotProduct(tangent) >= 0.0D ? 1.0D : -1.0D;
            velocity += -0.08D * tangent.y;
            velocity += 0.012D * desiredDirection * ZiplineConfig.speedMultiplier;
            velocity *= 0.995D;
            double maximumSpeed = 1.2D * ZiplineConfig.speedMultiplier;
            velocity = MathHelper.clamp(velocity, -maximumSpeed, maximumSpeed);
        }
        else
        {
            double desiredDirection = player.getLookVec().dotProduct(tangent) >= 0.0D ? 1.0D : -1.0D;
            velocity = 0.35D * ZiplineConfig.speedMultiplier * desiredDirection;
        }

        double length = Math.max(0.001D, cable.approximateLength());
        double nextT = t + (velocity / length);

        if (nextT <= 0.0D || nextT >= 1.0D)
        {
            state.setVelocity(velocity);
            state.setT(MathHelper.clamp(nextT, 0.0D, 1.0D));
            detach(player, false);
            return;
        }

        Vec3d point = cable.getPoint(nextT);
        double targetX = point.x;
        double targetY = point.y - ZiplineConfig.hangOffset;
        double targetZ = point.z;
        double motionX = targetX - player.posX;
        double motionY = targetY - player.posY;
        double motionZ = targetZ - player.posZ;

        BlockPos targetBlock = new BlockPos(targetX, targetY, targetZ);

        if (!player.world.isBlockLoaded(targetBlock))
        {
            detach(player, false);
            return;
        }

        AxisAlignedBB targetBox = player.getEntityBoundingBox().offset(motionX, motionY, motionZ);

        if (!player.world.getCollisionBoxes(player, targetBox).isEmpty())
        {
            detach(player, false);
            return;
        }

        state.setVelocity(velocity);
        state.setT(nextT);

        player.fallDistance = 0.0F;
        player.motionX = motionX;
        player.motionY = motionY;
        player.motionZ = motionZ;
        player.connection.setPlayerLocation(
            targetX,
            targetY,
            targetZ,
            player.rotationYaw,
            player.rotationPitch
        );
    }

    private static void tickDurability(EntityPlayerMP player, ZiplineState state)
    {
        if (!ZiplineConfig.consumeDurability)
        {
            return;
        }

        if (state.incrementDurabilityTicks() < ZiplineConfig.durabilityInterval)
        {
            return;
        }

        state.resetDurabilityTicks();
        ItemStack heldStack = player.getHeldItemMainhand();

        if (!heldStack.isItemStackDamageable())
        {
            return;
        }

        heldStack.damageItem(1, player);
    }

    private static void detach(EntityPlayerMP player, boolean jump)
    {
        ZiplineState state = STATES.remove(player.getUniqueID());

        if (state == null)
        {
            return;
        }

        Cable cable = Cables.resolve(player.world, state.getReference());

        if (cable != null)
        {
            Vec3d tangent = cable.getTangent(MathHelper.clamp(state.getT(), 0.0D, 1.0D));
            double signedSpeed = state.getVelocity();
            Vec3d launch = tangent.scale(signedSpeed);

            player.motionX = launch.x;
            player.motionY = launch.y + (jump ? (0.42D * ZiplineConfig.exitJumpMultiplier) : 0.0D);
            player.motionZ = launch.z;
            player.velocityChanged = true;
        }

        COOLDOWNS.put(player.getUniqueID(), Integer.valueOf(ZiplineConfig.releaseCooldown));
        ZiplineNetworkHandler.CHANNEL.sendTo(new MessageZiplineState(false), player);
    }

    private static void tickCooldown(EntityPlayerMP player)
    {
        Integer cooldown = COOLDOWNS.get(player.getUniqueID());

        if (cooldown == null)
        {
            return;
        }

        int next = cooldown.intValue() - 1;

        if (next <= 0)
        {
            COOLDOWNS.remove(player.getUniqueID());
        }
        else
        {
            COOLDOWNS.put(player.getUniqueID(), Integer.valueOf(next));
        }
    }
}
