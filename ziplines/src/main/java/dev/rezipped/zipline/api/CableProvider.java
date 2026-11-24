package dev.rezipped.zipline.api;

import java.util.List;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface CableProvider
{
    String getId();

    List<Cable> getCables(World world, Vec3d origin, double radius);

    Cable resolve(World world, CableReference reference);
}
