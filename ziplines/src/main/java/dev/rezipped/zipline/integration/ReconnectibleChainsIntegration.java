package dev.rezipped.zipline.integration;

import dev.rezipped.zipline.api.Cables;

public final class ReconnectibleChainsIntegration
{
    private ReconnectibleChainsIntegration()
    {
    }

    public static void register()
    {
        Cables.registerProvider(new ReconnectibleChainProvider());
    }
}
