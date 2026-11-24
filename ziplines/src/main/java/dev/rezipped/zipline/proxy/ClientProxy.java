package dev.rezipped.zipline.proxy;

import dev.rezipped.zipline.client.ClientInputHandler;
import net.minecraftforge.common.MinecraftForge;

public final class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        MinecraftForge.EVENT_BUS.register(new ClientInputHandler());
    }
}
