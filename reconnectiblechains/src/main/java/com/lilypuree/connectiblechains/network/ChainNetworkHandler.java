package com.lilypuree.connectiblechains.network;

import com.lilypuree.connectiblechains.ReconnectibleChains;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class ChainNetworkHandler
{
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(ReconnectibleChains.MODID);

    private ChainNetworkHandler()
    {
    }

    public static void init()
    {
        CHANNEL.registerMessage(
            MessageSyncChains.Handler.class,
            MessageSyncChains.class,
            0,
            Side.CLIENT
        );
    }
}
