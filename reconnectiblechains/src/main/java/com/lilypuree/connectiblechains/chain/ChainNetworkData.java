package com.lilypuree.connectiblechains.chain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public final class ChainNetworkData extends WorldSavedData
{
    private static final String DATA_NAME = "connectiblechains_network";
    private final List<ChainConnection> connections = new ArrayList<ChainConnection>();

    public ChainNetworkData()
    {
        super(DATA_NAME);
    }

    public ChainNetworkData(String name)
    {
        super(name);
    }

    public static ChainNetworkData get(World world)
    {
        MapStorage storage = world.getPerWorldStorage();
        ChainNetworkData data = (ChainNetworkData) storage.getOrLoadData(ChainNetworkData.class, DATA_NAME);

        if (data == null)
        {
            data = new ChainNetworkData();
            storage.setData(DATA_NAME, data);
        }

        return data;
    }

    public synchronized List<ChainConnection> getConnections()
    {
        List<ChainConnection> copy = new ArrayList<ChainConnection>();

        for (ChainConnection connection : this.connections)
        {
            copy.add(connection.copy());
        }

        return Collections.unmodifiableList(copy);
    }

    public synchronized ChainConnection find(net.minecraft.util.math.BlockPos first, net.minecraft.util.math.BlockPos second)
    {
        for (ChainConnection connection : this.connections)
        {
            if (connection.matches(first, second))
            {
                return connection;
            }
        }

        return null;
    }

    public synchronized boolean add(ChainConnection connection)
    {
        if (this.find(connection.getFirst(), connection.getSecond()) != null)
        {
            return false;
        }

        this.connections.add(connection);
        this.markDirty();
        return true;
    }

    public synchronized boolean remove(ChainConnection connection)
    {
        for (int index = 0; index < this.connections.size(); index++)
        {
            ChainConnection existing = this.connections.get(index);

            if (existing.matches(connection.getFirst(), connection.getSecond()))
            {
                this.connections.remove(index);
                this.markDirty();
                return true;
            }
        }

        return false;
    }

    public synchronized void updateSlack(ChainConnection connection, double slack)
    {
        ChainConnection existing = this.find(connection.getFirst(), connection.getSecond());

        if (existing != null)
        {
            existing.setSlack(slack);
            this.markDirty();
        }
    }

    @Override
    public synchronized void readFromNBT(NBTTagCompound compound)
    {
        this.connections.clear();
        NBTTagList list = compound.getTagList("Connections", 10);

        for (int index = 0; index < list.tagCount(); index++)
        {
            this.connections.add(ChainConnection.readFromNbt(list.getCompoundTagAt(index)));
        }
    }

    @Override
    public synchronized NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        NBTTagList list = new NBTTagList();

        for (ChainConnection connection : this.connections)
        {
            list.appendTag(connection.writeToNbt());
        }

        compound.setTag("Connections", list);
        return compound;
    }
}
