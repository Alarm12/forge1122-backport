package dev.rezipped.zipline.server;

import dev.rezipped.zipline.api.CableReference;

public final class ZiplineState
{
    private final CableReference reference;
    private double t;
    private double velocity;
    private int durabilityTicks;

    public ZiplineState(CableReference reference, double t, double velocity)
    {
        this.reference = reference;
        this.t = t;
        this.velocity = velocity;
    }

    public CableReference getReference()
    {
        return this.reference;
    }

    public double getT()
    {
        return this.t;
    }

    public void setT(double t)
    {
        this.t = t;
    }

    public double getVelocity()
    {
        return this.velocity;
    }

    public void setVelocity(double velocity)
    {
        this.velocity = velocity;
    }

    public int incrementDurabilityTicks()
    {
        this.durabilityTicks++;
        return this.durabilityTicks;
    }

    public void resetDurabilityTicks()
    {
        this.durabilityTicks = 0;
    }
}
