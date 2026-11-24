package dev.rezipped.zipline;

import dev.rezipped.zipline.config.ZiplineConfig;
import dev.rezipped.zipline.integration.ReconnectibleChainsIntegration;
import dev.rezipped.zipline.network.ZiplineNetworkHandler;
import dev.rezipped.zipline.proxy.CommonProxy;
import dev.rezipped.zipline.server.ZiplineServerHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = Zipline.MODID,
    name = Zipline.NAME,
    version = Zipline.VERSION,
    acceptedMinecraftVersions = "[1.12.2]",
    dependencies = "after:connectiblechains"
)
public final class Zipline
{
    public static final String MODID = "zipline";
    public static final String NAME = "Ziplines: Rezipped!";
    public static final String VERSION = "0.1.0-alpha";

    @SidedProxy(
        clientSide = "dev.rezipped.zipline.proxy.ClientProxy",
        serverSide = "dev.rezipped.zipline.proxy.CommonProxy"
    )
    public static CommonProxy PROXY;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        ZiplineConfig.load(event.getSuggestedConfigurationFile());
        ZiplineNetworkHandler.init();
        MinecraftForge.EVENT_BUS.register(new ZiplineServerHandler());
        PROXY.preInit();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        if (Loader.isModLoaded("connectiblechains"))
        {
            ReconnectibleChainsIntegration.register();
        }
    }
}
