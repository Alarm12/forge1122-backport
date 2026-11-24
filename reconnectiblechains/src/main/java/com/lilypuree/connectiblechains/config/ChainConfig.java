package com.lilypuree.connectiblechains.config;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public final class ChainConfig
{
    public static int maxChainRange = 128;
    public static double defaultSlack = 0.15D;
    public static double minimumSlack = 0.0D;
    public static double maximumSlack = 1.5D;
    public static double slackStep = 0.05D;
    public static String[] additionalAnchors = new String[0];

    private ChainConfig()
    {
    }

    public static void load(File file)
    {
        Configuration configuration = new Configuration(file);

        try
        {
            configuration.load();

            maxChainRange = configuration.getInt(
                "maxChainRange",
                Configuration.CATEGORY_GENERAL,
                128,
                1,
                512,
                "Maximum length of a chain connection in blocks."
            );

            defaultSlack = configuration.getFloat(
                "defaultSlack",
                Configuration.CATEGORY_GENERAL,
                0.15F,
                0.0F,
                1.5F,
                "Default sag amount as a fraction of the connection length."
            );

            minimumSlack = configuration.getFloat(
                "minimumSlack",
                Configuration.CATEGORY_GENERAL,
                0.0F,
                0.0F,
                1.5F,
                "Minimum per-chain slack."
            );

            maximumSlack = configuration.getFloat(
                "maximumSlack",
                Configuration.CATEGORY_GENERAL,
                1.5F,
                0.0F,
                4.0F,
                "Maximum per-chain slack."
            );

            slackStep = configuration.getFloat(
                "slackStep",
                Configuration.CATEGORY_GENERAL,
                0.05F,
                0.01F,
                0.5F,
                "Slack change per shears interaction."
            );

            additionalAnchors = configuration.getStringList(
                "additionalAnchors",
                Configuration.CATEGORY_GENERAL,
                new String[0],
                "Additional anchor blocks as modid:block registry names."
            );
        }
        finally
        {
            if (configuration.hasChanged())
            {
                configuration.save();
            }
        }
    }
}
