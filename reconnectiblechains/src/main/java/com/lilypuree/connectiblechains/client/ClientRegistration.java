package com.lilypuree.connectiblechains.client;

import com.lilypuree.connectiblechains.ReconnectibleChains;
import com.lilypuree.connectiblechains.item.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ReconnectibleChains.MODID)
public final class ClientRegistration
{
    private ClientRegistration()
    {
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        ModelLoader.setCustomModelResourceLocation(
            ModItems.CHAIN,
            0,
            new ModelResourceLocation(ModItems.CHAIN.getRegistryName(), "inventory")
        );
    }
}
