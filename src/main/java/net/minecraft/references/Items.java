package net.minecraft.references;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class Items {
    public static final ResourceKey<Item> PUMPKIN_SEEDS = createKey("pumpkin_seeds");
    public static final ResourceKey<Item> MELON_SEEDS = createKey("melon_seeds");

    private static ResourceKey<Item> createKey(String param0) {
        return ResourceKey.create(Registries.ITEM, new ResourceLocation(param0));
    }
}
