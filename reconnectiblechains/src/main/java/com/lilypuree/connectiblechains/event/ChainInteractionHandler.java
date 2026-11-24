package com.lilypuree.connectiblechains.event;

import com.lilypuree.connectiblechains.chain.ChainConnection;
import com.lilypuree.connectiblechains.chain.ChainNetworkData;
import com.lilypuree.connectiblechains.config.ChainConfig;
import com.lilypuree.connectiblechains.item.ModItems;
import com.lilypuree.connectiblechains.network.ChainSync;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockWall;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class ChainInteractionHandler
{
    private static final String START_DIMENSION = "ConnectibleChainsStartDimension";
    private static final String START_POSITION = "ConnectibleChainsStartPosition";

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.getWorld().isRemote || event.getHand() != EnumHand.MAIN_HAND)
        {
            return;
        }

        ItemStack heldStack = event.getItemStack();
        BlockPos position = event.getPos();

        if (heldStack.getItem() == ModItems.CHAIN && this.isAnchor(event.getWorld().getBlockState(position).getBlock()))
        {
            this.handleChainUse((EntityPlayerMP) event.getEntityPlayer(), heldStack, position);
            event.setCancellationResult(EnumActionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (heldStack.getItem() == Items.SHEARS && this.isAnchor(event.getWorld().getBlockState(position).getBlock()))
        {
            this.adjustSlack((EntityPlayerMP) event.getEntityPlayer(), heldStack, position);
            event.setCancellationResult(EnumActionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        if (event.getWorld().isRemote)
        {
            return;
        }

        EntityPlayer player = event.getEntityPlayer();
        ItemStack heldStack = player.getHeldItemMainhand();

        if (heldStack.getItem() != Items.SHEARS)
        {
            return;
        }

        ChainNetworkData data = ChainNetworkData.get(event.getWorld());
        ChainConnection connection = this.findConnectionAt(data.getConnections(), event.getPos());

        if (connection == null)
        {
            return;
        }

        if (data.remove(connection))
        {
            int refund = this.getRequiredChainItems(connection.getStraightLength());
            this.giveChains(player, refund);
            heldStack.damageItem(1, player);
            ChainSync.sendToDimension((WorldServer) event.getWorld());
            event.setCanceled(true);
        }
    }

    private void handleChainUse(EntityPlayerMP player, ItemStack stack, BlockPos position)
    {
        WorldServer world = player.getServerWorld();
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null)
        {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        if (!tag.hasKey(START_POSITION))
        {
            tag.setInteger(START_DIMENSION, world.provider.getDimension());
            tag.setLong(START_POSITION, position.toLong());
            player.sendStatusMessage(new TextComponentTranslation("message.connectiblechains.start"), true);
            return;
        }

        int startDimension = tag.getInteger(START_DIMENSION);
        BlockPos start = BlockPos.fromLong(tag.getLong(START_POSITION));

        if (startDimension != world.provider.getDimension())
        {
            this.clearStart(tag);
            player.sendStatusMessage(new TextComponentTranslation("message.connectiblechains.dimension"), true);
            return;
        }

        if (start.equals(position))
        {
            this.clearStart(tag);
            player.sendStatusMessage(new TextComponentTranslation("message.connectiblechains.cancel"), true);
            return;
        }

        if (!world.isBlockLoaded(start) || !world.isBlockLoaded(position))
        {
            this.clearStart(tag);
            return;
        }

        if (!this.isAnchor(world.getBlockState(start).getBlock()))
        {
            this.clearStart(tag);
            player.sendStatusMessage(new TextComponentTranslation("message.connectiblechains.invalid_start"), true);
            return;
        }

        double distance = Math.sqrt(start.distanceSq(position));

        if (distance > ChainConfig.maxChainRange)
        {
            player.sendStatusMessage(
                new TextComponentTranslation("message.connectiblechains.too_far", Integer.valueOf(ChainConfig.maxChainRange)),
                true
            );
            return;
        }

        int required = this.getRequiredChainItems(distance);

        if (!player.capabilities.isCreativeMode && this.countChains(player) < required)
        {
            player.sendStatusMessage(
                new TextComponentTranslation("message.connectiblechains.not_enough", Integer.valueOf(required)),
                true
            );
            return;
        }

        ChainNetworkData data = ChainNetworkData.get(world);
        ChainConnection connection = new ChainConnection(start, position, ChainConfig.defaultSlack);

        if (!data.add(connection))
        {
            this.clearStart(tag);
            player.sendStatusMessage(new TextComponentTranslation("message.connectiblechains.exists"), true);
            return;
        }

        if (!player.capabilities.isCreativeMode)
        {
            this.consumeChains(player, required);
        }

        this.clearStart(tag);
        ChainSync.sendToDimension(world);
        player.sendStatusMessage(new TextComponentTranslation("message.connectiblechains.created"), true);
    }

    private void adjustSlack(EntityPlayerMP player, ItemStack shears, BlockPos position)
    {
        WorldServer world = player.getServerWorld();
        ChainNetworkData data = ChainNetworkData.get(world);
        ChainConnection connection = this.findConnectionAt(data.getConnections(), position);

        if (connection == null)
        {
            return;
        }

        double direction = player.isSneaking() ? -1.0D : 1.0D;
        double nextSlack = connection.getSlack() + (ChainConfig.slackStep * direction);
        nextSlack = Math.max(ChainConfig.minimumSlack, Math.min(ChainConfig.maximumSlack, nextSlack));
        data.updateSlack(connection, nextSlack);
        shears.damageItem(1, player);
        ChainSync.sendToDimension(world);
    }

    private ChainConnection findConnectionAt(List<ChainConnection> connections, BlockPos position)
    {
        for (ChainConnection connection : connections)
        {
            if (connection.contains(position))
            {
                return connection;
            }
        }

        return null;
    }

    private boolean isAnchor(Block block)
    {
        if (block instanceof BlockFence || block instanceof BlockWall || block == Blocks.IRON_BARS)
        {
            return true;
        }

        ResourceLocation registryName = block.getRegistryName();

        if (registryName == null)
        {
            return false;
        }

        String name = registryName.toString();

        for (String configured : ChainConfig.additionalAnchors)
        {
            if (name.equals(configured))
            {
                return true;
            }
        }

        return false;
    }

    private int getRequiredChainItems(double distance)
    {
        return Math.max(1, (int) Math.ceil(distance));
    }

    private int countChains(EntityPlayer player)
    {
        int count = 0;

        for (ItemStack inventoryStack : player.inventory.mainInventory)
        {
            if (!inventoryStack.isEmpty() && inventoryStack.getItem() == ModItems.CHAIN)
            {
                count += inventoryStack.getCount();
            }
        }

        return count;
    }

    private void consumeChains(EntityPlayer player, int amount)
    {
        int remaining = amount;

        for (ItemStack inventoryStack : player.inventory.mainInventory)
        {
            if (remaining <= 0)
            {
                break;
            }

            if (inventoryStack.isEmpty() || inventoryStack.getItem() != ModItems.CHAIN)
            {
                continue;
            }

            int removed = Math.min(remaining, inventoryStack.getCount());
            inventoryStack.shrink(removed);
            remaining -= removed;
        }
    }

    private void giveChains(EntityPlayer player, int amount)
    {
        int remaining = amount;

        while (remaining > 0)
        {
            int stackSize = Math.min(64, remaining);
            ItemStack refund = new ItemStack(ModItems.CHAIN, stackSize);

            if (!player.inventory.addItemStackToInventory(refund))
            {
                player.dropItem(refund, false);
            }

            remaining -= stackSize;
        }
    }

    private void clearStart(NBTTagCompound tag)
    {
        tag.removeTag(START_DIMENSION);
        tag.removeTag(START_POSITION);
    }
}
