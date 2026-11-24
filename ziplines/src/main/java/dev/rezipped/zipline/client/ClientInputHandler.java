package dev.rezipped.zipline.client;

import dev.rezipped.zipline.api.CableHit;
import dev.rezipped.zipline.api.Cables;
import dev.rezipped.zipline.config.ZiplineConfig;
import dev.rezipped.zipline.network.MessageZiplineControl;
import dev.rezipped.zipline.network.ZiplineNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class ClientInputHandler
{
    private boolean previousUse;
    private boolean previousJump;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP player = minecraft.player;

        if (player == null || minecraft.world == null)
        {
            this.previousUse = false;
            this.previousJump = false;
            ClientZiplineState.setAttached(false);
            return;
        }

        boolean use = minecraft.gameSettings.keyBindUseItem.isKeyDown();
        boolean jump = minecraft.gameSettings.keyBindJump.isKeyDown();
        ItemStack heldStack = player.getHeldItemMainhand();

        if (ClientZiplineState.isAttached())
        {
            if (jump && !this.previousJump)
            {
                ZiplineNetworkHandler.CHANNEL.sendToServer(MessageZiplineControl.jump());
            }
            else if (!use)
            {
                ZiplineNetworkHandler.CHANNEL.sendToServer(MessageZiplineControl.release());
            }
        }
        else if (use
            && !this.previousUse
            && !ClientZiplineState.isPendingAttach()
            && ZiplineConfig.isAttachmentItem(heldStack))
        {
            Vec3d eye = player.getPositionEyes(1.0F);
            Vec3d look = player.getLookVec();
            CableHit hit = Cables.findLookedAt(
                minecraft.world,
                eye,
                look,
                ZiplineConfig.clickReach,
                ZiplineConfig.snapRadius
            );

            if (hit != null)
            {
                ClientZiplineState.setPendingAttach(true);
                ZiplineNetworkHandler.CHANNEL.sendToServer(
                    MessageZiplineControl.attach(hit.getCable().getReference())
                );
            }
        }

        this.previousUse = use;
        this.previousJump = jump;
    }
}
