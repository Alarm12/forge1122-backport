package dev.rezipped.zipline.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class Cables
{
    private static final List<CableProvider> PROVIDERS = new ArrayList<CableProvider>();

    private Cables()
    {
    }

    public static synchronized void registerProvider(CableProvider provider)
    {
        for (CableProvider existing : PROVIDERS)
        {
            if (existing.getId().equals(provider.getId()))
            {
                return;
            }
        }

        PROVIDERS.add(provider);
    }

    public static synchronized List<CableProvider> getProviders()
    {
        return Collections.unmodifiableList(new ArrayList<CableProvider>(PROVIDERS));
    }

    public static Cable resolve(World world, CableReference reference)
    {
        for (CableProvider provider : getProviders())
        {
            if (provider.getId().equals(reference.getProviderId()))
            {
                return provider.resolve(world, reference);
            }
        }

        return null;
    }

    public static CableHit findLookedAt(
        World world,
        Vec3d eye,
        Vec3d look,
        double reach,
        double snapRadius
    )
    {
        Vec3d normalizedLook = look.normalize();
        CableHit best = null;
        double bestDistance = snapRadius * snapRadius;

        for (CableProvider provider : getProviders())
        {
            List<Cable> cables = provider.getCables(world, eye, reach + 8.0D);

            for (Cable cable : cables)
            {
                int samples = Math.max(16, Math.min(96, (int) Math.ceil(cable.approximateLength() * 2.0D)));

                for (int index = 0; index <= samples; index++)
                {
                    double t = (double) index / (double) samples;
                    Vec3d point = cable.getPoint(t);
                    Vec3d fromEye = point.subtract(eye);
                    double alongRay = fromEye.dotProduct(normalizedLook);

                    if (alongRay < 0.0D || alongRay > reach)
                    {
                        continue;
                    }

                    Vec3d closestOnRay = eye.add(normalizedLook.scale(alongRay));
                    double distanceSquared = point.squareDistanceTo(closestOnRay);

                    if (distanceSquared <= bestDistance)
                    {
                        bestDistance = distanceSquared;
                        best = new CableHit(cable, t, distanceSquared);
                    }
                }
            }
        }

        return best;
    }

    public static double findNearestT(Cable cable, Vec3d position, int samples)
    {
        int sampleCount = Math.max(16, samples);
        double bestT = 0.0D;
        double bestDistance = Double.MAX_VALUE;

        for (int index = 0; index <= sampleCount; index++)
        {
            double t = (double) index / (double) sampleCount;
            double distance = cable.getPoint(t).squareDistanceTo(position);

            if (distance < bestDistance)
            {
                bestDistance = distance;
                bestT = t;
            }
        }

        return MathHelper.clamp(bestT, 0.0D, 1.0D);
    }
}
