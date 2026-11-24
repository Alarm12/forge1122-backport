package dev.rezipped.zipline.network;

import dev.rezipped.zipline.Zipline;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class ZiplineNetworkHandler
{
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Zipline.MODID);

    private ZiplineNetworkHandler()
    {
    }

    public static void init()
    {
        CHANNEL.registerMessage(
            MessageZiplineControl.Handler.class,
            MessageZiplineControl.class,
            0,
            Side.SERVER
        );

        CHANNEL.registerMessage(
            MessageZiplineState.Handler.class,
            MessageZiplineState.class,
            1,
            Side.CLIENT
        );
    }
}
