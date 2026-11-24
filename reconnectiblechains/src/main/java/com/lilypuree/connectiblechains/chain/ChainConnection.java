package com.lilypuree.connectiblechains.chain;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public final class ChainConnection
{
    private final BlockPos first;
    private final BlockPos second;
    private double slack;

    public ChainConnection(BlockPos first, BlockPos second, double slack)
    {
        if (compare(first, second) <= 0)
        {
            this.first = first.toImmutable();
            this.second = second.toImmutable();
        }
        else
        {
            this.first = second.toImmutable();
            this.second = first.toImmutable();
        }

        this.slack = slack;
    }

    public BlockPos getFirst()
    {
        return this.first;
    }

    public BlockPos getSecond()
    {
        return this.second;
    }

    public double getSlack()
    {
        return this.slack;
    }

    public void setSlack(double slack)
    {
        this.slack = slack;
    }

    public boolean contains(BlockPos position)
    {
        return this.first.equals(position) || this.second.equals(position);
    }

    public boolean matches(BlockPos first, BlockPos second)
    {
        return (this.first.equals(first) && this.second.equals(second))
            || (this.first.equals(second) && this.second.equals(first));
    }

    public double getStraightLength()
    {
        double x = this.second.getX() - this.first.getX();
        double y = this.second.getY() - this.first.getY();
        double z = this.second.getZ() - this.first.getZ();
        return Math.sqrt((x * x) + (y * y) + (z * z));
    }

    public ChainConnection copy()
    {
        return new ChainConnection(this.first, this.second, this.slack);
    }

    public NBTTagCompound writeToNbt()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("First", this.first.toLong());
        tag.setLong("Second", this.second.toLong());
        tag.setDouble("Slack", this.slack);
        return tag;
    }

    public static ChainConnection readFromNbt(NBTTagCompound tag)
    {
        BlockPos first = BlockPos.fromLong(tag.getLong("First"));
        BlockPos second = BlockPos.fromLong(tag.getLong("Second"));
        double slack = tag.getDouble("Slack");
        return new ChainConnection(first, second, slack);
    }

    private static int compare(BlockPos first, BlockPos second)
    {
        if (first.getX() != second.getX())
        {
            return Integer.compare(first.getX(), second.getX());
        }

        if (first.getY() != second.getY())
        {
            return Integer.compare(first.getY(), second.getY());
        }

        return Integer.compare(first.getZ(), second.getZ());
    }
}
