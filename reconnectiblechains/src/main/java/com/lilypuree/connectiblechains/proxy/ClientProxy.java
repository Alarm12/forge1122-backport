package com.lilypuree.connectiblechains.proxy;

import com.lilypuree.connectiblechains.client.ClientChainRenderer;
import net.minecraftforge.common.MinecraftForge;

public final class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        MinecraftForge.EVENT_BUS.register(new ClientChainRenderer());
    }
}
