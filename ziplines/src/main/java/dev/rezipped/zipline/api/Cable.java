package dev.rezipped.zipline.api;

import net.minecraft.util.math.Vec3d;

public interface Cable
{
    Vec3d getPoint(double t);

    Vec3d getTangent(double t);

    double approximateLength();

    CableReference getReference();
}
