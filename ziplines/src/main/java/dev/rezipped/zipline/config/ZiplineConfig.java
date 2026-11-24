package dev.rezipped.zipline.config;

import java.io.File;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;

public final class ZiplineConfig
{
    public static boolean realisticPhysics = false;
    public static boolean consumeDurability = true;
    public static double clickReach = 5.0D;
    public static double snapRadius = 0.45D;
    public static double speedMultiplier = 1.0D;
    public static double hangOffset = 1.35D;
    public static double exitJumpMultiplier = 1.0D;
    public static int releaseCooldown = 8;
    public static int durabilityInterval = 40;
    public static String[] attachmentItems = new String[0];

    private ZiplineConfig()
    {
    }

    public static void load(File file)
    {
        Configuration configuration = new Configuration(file);

        try
        {
            configuration.load();

            realisticPhysics = configuration.getBoolean(
                "realisticPhysics",
                Configuration.CATEGORY_GENERAL,
                false,
                "Whether gravity affects speed along the cable."
            );

            consumeDurability = configuration.getBoolean(
                "consumeDurability",
                Configuration.CATEGORY_GENERAL,
                true,
                "Whether the held attachment item loses durability while ziplining."
            );

            clickReach = configuration.getFloat(
                "clickReach",
                Configuration.CATEGORY_GENERAL,
                5.0F,
                1.0F,
                12.0F,
                "Maximum look distance used when manually attaching."
            );

            snapRadius = configuration.getFloat(
                "snapRadius",
                Configuration.CATEGORY_GENERAL,
                0.45F,
                0.05F,
                2.0F,
                "Maximum distance from the look ray to a cable sample."
            );

            speedMultiplier = configuration.getFloat(
                "speedMultiplier",
                Configuration.CATEGORY_GENERAL,
                1.0F,
                0.1F,
                5.0F,
                "Movement speed multiplier."
            );

            hangOffset = configuration.getFloat(
                "hangOffset",
                Configuration.CATEGORY_GENERAL,
                1.35F,
                0.0F,
                3.0F,
                "Vertical player offset below the cable."
            );

            exitJumpMultiplier = configuration.getFloat(
                "exitJumpMultiplier",
                Configuration.CATEGORY_GENERAL,
                1.0F,
                0.0F,
                5.0F,
                "Vertical launch multiplier when jumping off a cable."
            );

            releaseCooldown = configuration.getInt(
                "releaseCooldown",
                Configuration.CATEGORY_GENERAL,
                8,
                0,
                100,
                "Ticks before the player may reattach after release."
            );

            durabilityInterval = configuration.getInt(
                "durabilityInterval",
                Configuration.CATEGORY_GENERAL,
                40,
                1,
                1200,
                "Ticks between durability damage while attached."
            );

            attachmentItems = configuration.getStringList(
                "attachmentItems",
                Configuration.CATEGORY_GENERAL,
                new String[0],
                "Additional attachment tools as modid:item registry names."
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

    public static boolean isAttachmentItem(ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return false;
        }

        Item item = stack.getItem();

        if (item instanceof ItemPickaxe || item instanceof ItemHoe)
        {
            return true;
        }

        ResourceLocation registryName = item.getRegistryName();

        if (registryName == null)
        {
            return false;
        }

        String name = registryName.toString();

        for (String configured : attachmentItems)
        {
            if (name.equals(configured))
            {
                return true;
            }
        }

        return false;
    }
}
