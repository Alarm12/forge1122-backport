package dev.rezipped.zipline.integration;

import com.lilypuree.connectiblechains.chain.ChainConnection;
import com.lilypuree.connectiblechains.chain.ChainMath;
import dev.rezipped.zipline.api.Cable;
import dev.rezipped.zipline.api.CableReference;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

public final class ReconnectibleChainCable implements Cable
{
    private final ChainConnection connection;

    public ReconnectibleChainCable(ChainConnection connection)
    {
        this.connection = connection.copy();
    }

    @Override
    public Vec3d getPoint(double t)
    {
        return ChainMath.getPoint(this.connection, t);
    }

    @Override
    public Vec3d getTangent(double t)
    {
        return ChainMath.getTangent(this.connection, t);
    }

    @Override
    public double approximateLength()
    {
        return ChainMath.approximateLength(this.connection, 32);
    }

    @Override
    public CableReference getReference()
    {
        NBTTagCompound data = new NBTTagCompound();
        data.setLong("First", this.connection.getFirst().toLong());
        data.setLong("Second", this.connection.getSecond().toLong());
        return new CableReference(ReconnectibleChainProvider.ID, data);
    }
}
