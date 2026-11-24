package dev.rezipped.zipline.api;

import net.minecraft.nbt.NBTTagCompound;

public final class CableReference
{
    private final String providerId;
    private final NBTTagCompound data;

    public CableReference(String providerId, NBTTagCompound data)
    {
        this.providerId = providerId;
        this.data = data.copy();
    }

    public String getProviderId()
    {
        return this.providerId;
    }

    public NBTTagCompound getData()
    {
        return this.data.copy();
    }
}
