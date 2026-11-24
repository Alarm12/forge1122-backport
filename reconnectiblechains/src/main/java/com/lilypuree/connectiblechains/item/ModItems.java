package com.lilypuree.connectiblechains.item;

import com.lilypuree.connectiblechains.ReconnectibleChains;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ReconnectibleChains.MODID)
public final class ModItems
{
    public static final Item CHAIN = new ItemChain();

    private ModItems()
    {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(CHAIN);
    }
}
