package com.lilypuree.connectiblechains;

import com.lilypuree.connectiblechains.config.ChainConfig;
import com.lilypuree.connectiblechains.event.ChainInteractionHandler;
import com.lilypuree.connectiblechains.event.WorldSyncHandler;
import com.lilypuree.connectiblechains.network.ChainNetworkHandler;
import com.lilypuree.connectiblechains.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = ReconnectibleChains.MODID,
    name = ReconnectibleChains.NAME,
    version = ReconnectibleChains.VERSION,
    acceptedMinecraftVersions = "[1.12.2]"
)
public final class ReconnectibleChains
{
    public static final String MODID = "connectiblechains";
    public static final String NAME = "Reconnectible Chains";
    public static final String VERSION = "0.1.0-alpha";

    @SidedProxy(
        clientSide = "com.lilypuree.connectiblechains.proxy.ClientProxy",
        serverSide = "com.lilypuree.connectiblechains.proxy.CommonProxy"
    )
    public static CommonProxy PROXY;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        ChainConfig.load(event.getSuggestedConfigurationFile());
        ChainNetworkHandler.init();
        MinecraftForge.EVENT_BUS.register(new ChainInteractionHandler());
        MinecraftForge.EVENT_BUS.register(new WorldSyncHandler());
        PROXY.preInit();
    }
}
