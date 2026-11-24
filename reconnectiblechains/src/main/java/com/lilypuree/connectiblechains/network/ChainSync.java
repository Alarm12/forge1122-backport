package com.lilypuree.connectiblechains.network;

import com.lilypuree.connectiblechains.chain.ChainNetworkData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

public final class ChainSync
{
    private ChainSync()
    {
    }

    public static void sendToDimension(WorldServer world)
    {
        MessageSyncChains message = createMessage(world);
        ChainNetworkHandler.CHANNEL.sendToDimension(message, world.provider.getDimension());
    }

    public static void sendToPlayer(EntityPlayerMP player)
    {
        WorldServer world = player.getServerWorld();
        ChainNetworkHandler.CHANNEL.sendTo(createMessage(world), player);
    }

    private static MessageSyncChains createMessage(WorldServer world)
    {
        return new MessageSyncChains(
            world.provider.getDimension(),
            ChainNetworkData.get(world).getConnections()
        );
    }
}
