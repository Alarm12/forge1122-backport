package dev.rezipped.zipline.integration;

import com.lilypuree.connectiblechains.api.ChainApi;
import com.lilypuree.connectiblechains.chain.ChainConnection;
import com.lilypuree.connectiblechains.chain.ChainMath;
import dev.rezipped.zipline.api.Cable;
import dev.rezipped.zipline.api.CableProvider;
import dev.rezipped.zipline.api.CableReference;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class ReconnectibleChainProvider implements CableProvider
{
    public static final String ID = "connectiblechains";

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public List<Cable> getCables(World world, Vec3d origin, double radius)
    {
        List<Cable> result = new ArrayList<Cable>();
        List<ChainConnection> connections = ChainApi.getConnections(world);
        double radiusSquared = radius * radius;

        for (ChainConnection connection : connections)
        {
            Vec3d first = ChainMath.getAnchorPoint(connection.getFirst());
            Vec3d second = ChainMath.getAnchorPoint(connection.getSecond());
            Vec3d midpoint = first.add(second).scale(0.5D);
            double allowance = radius + (connection.getStraightLength() * 0.5D);

            if (midpoint.squareDistanceTo(origin) <= (allowance * allowance)
                || first.squareDistanceTo(origin) <= radiusSquared
                || second.squareDistanceTo(origin) <= radiusSquared)
            {
                result.add(new ReconnectibleChainCable(connection));
            }
        }

        return result;
    }

    @Override
    public Cable resolve(World world, CableReference reference)
    {
        NBTTagCompound data = reference.getData();

        if (!data.hasKey("First") || !data.hasKey("Second"))
        {
            return null;
        }

        BlockPos first = BlockPos.fromLong(data.getLong("First"));
        BlockPos second = BlockPos.fromLong(data.getLong("Second"));
        ChainConnection connection = ChainApi.findConnection(world, first, second);

        if (connection == null)
        {
            return null;
        }

        return new ReconnectibleChainCable(connection);
    }
}
