package com.lilypuree.connectiblechains.chain;

import net.minecraft.util.math.Vec3d;

public final class ChainMath
{
    private ChainMath()
    {
    }

    public static Vec3d getPoint(ChainConnection connection, double t)
    {
        double clampedT = clamp(t, 0.0D, 1.0D);
        Vec3d first = getAnchorPoint(connection.getFirst());
        Vec3d second = getAnchorPoint(connection.getSecond());
        double x = first.x + ((second.x - first.x) * clampedT);
        double y = first.y + ((second.y - first.y) * clampedT);
        double z = first.z + ((second.z - first.z) * clampedT);

        // A quadratic sag term keeps both endpoints fixed while allowing per-chain slack.
        double sag = connection.getStraightLength()
            * connection.getSlack()
            * 4.0D
            * clampedT
            * (1.0D - clampedT);

        return new Vec3d(x, y - sag, z);
    }

    public static Vec3d getTangent(ChainConnection connection, double t)
    {
        double clampedT = clamp(t, 0.0D, 1.0D);
        Vec3d first = getAnchorPoint(connection.getFirst());
        Vec3d second = getAnchorPoint(connection.getSecond());
        double sagScale = connection.getStraightLength() * connection.getSlack();
        double dx = second.x - first.x;
        double dy = (second.y - first.y) - (4.0D * sagScale * (1.0D - (2.0D * clampedT)));
        double dz = second.z - first.z;
        Vec3d tangent = new Vec3d(dx, dy, dz);

        if (tangent.lengthSquared() < 1.0E-8D)
        {
            return new Vec3d(1.0D, 0.0D, 0.0D);
        }

        return tangent.normalize();
    }

    public static double approximateLength(ChainConnection connection, int segments)
    {
        int segmentCount = Math.max(2, segments);
        Vec3d previous = getPoint(connection, 0.0D);
        double length = 0.0D;

        for (int index = 1; index <= segmentCount; index++)
        {
            double t = (double) index / (double) segmentCount;
            Vec3d current = getPoint(connection, t);
            length += current.distanceTo(previous);
            previous = current;
        }

        return length;
    }

    public static double findNearestT(ChainConnection connection, Vec3d point, int samples)
    {
        int sampleCount = Math.max(8, samples);
        double bestT = 0.0D;
        double bestDistance = Double.MAX_VALUE;

        for (int index = 0; index <= sampleCount; index++)
        {
            double t = (double) index / (double) sampleCount;
            double distance = getPoint(connection, t).squareDistanceTo(point);

            if (distance < bestDistance)
            {
                bestDistance = distance;
                bestT = t;
            }
        }

        return bestT;
    }

    public static Vec3d getAnchorPoint(net.minecraft.util.math.BlockPos position)
    {
        return new Vec3d(
            position.getX() + 0.5D,
            position.getY() + 0.625D,
            position.getZ() + 0.5D
        );
    }

    private static double clamp(double value, double minimum, double maximum)
    {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
