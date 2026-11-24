package dev.rezipped.zipline.api;

public final class CableHit
{
    private final Cable cable;
    private final double t;
    private final double distanceSquared;

    public CableHit(Cable cable, double t, double distanceSquared)
    {
        this.cable = cable;
        this.t = t;
        this.distanceSquared = distanceSquared;
    }

    public Cable getCable()
    {
        return this.cable;
    }

    public double getT()
    {
        return this.t;
    }

    public double getDistanceSquared()
    {
        return this.distanceSquared;
    }
}
