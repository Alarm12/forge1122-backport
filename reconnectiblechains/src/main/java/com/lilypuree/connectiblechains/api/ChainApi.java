package com.lilypuree.connectiblechains.api;

import com.lilypuree.connectiblechains.chain.ChainConnection;
import com.lilypuree.connectiblechains.chain.ChainNetworkData;
import com.lilypuree.connectiblechains.chain.ClientChainStore;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ChainApi
{
    private ChainApi()
    {
    }

    public static List<ChainConnection> getConnections(World world)
    {
        if (world.isRemote)
        {
            return ClientChainStore.get(world.provider.getDimension());
        }

        return ChainNetworkData.get(world).getConnections();
    }

    public static ChainConnection findConnection(World world, BlockPos first, BlockPos second)
    {
        List<ChainConnection> connections = getConnections(world);

        for (ChainConnection connection : connections)
        {
            if (connection.matches(first, second))
            {
                return connection;
            }
        }

        return null;
    }
}
