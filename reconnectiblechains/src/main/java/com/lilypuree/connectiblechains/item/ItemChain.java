package com.lilypuree.connectiblechains.item;

import com.lilypuree.connectiblechains.ReconnectibleChains;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public final class ItemChain extends Item
{
    public ItemChain()
    {
        this.setRegistryName(ReconnectibleChains.MODID, "chain");
        this.setUnlocalizedName(ReconnectibleChains.MODID + ".chain");
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setMaxStackSize(64);
    }
}
