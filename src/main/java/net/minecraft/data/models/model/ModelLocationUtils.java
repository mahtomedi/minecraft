package net.minecraft.data.models.model;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModelLocationUtils {
    @Deprecated
    public static ResourceLocation decorateBlockModelLocation(String param0) {
        return new ResourceLocation("minecraft", "block/" + param0);
    }

    public static ResourceLocation decorateItemModelLocation(String param0) {
        return new ResourceLocation("minecraft", "item/" + param0);
    }

    public static ResourceLocation getModelLocation(Block param0, String param1) {
        ResourceLocation var0 = BuiltInRegistries.BLOCK.getKey(param0);
        return var0.withPath(param1x -> "block/" + param1x + param1);
    }

    public static ResourceLocation getModelLocation(Block param0) {
        ResourceLocation var0 = BuiltInRegistries.BLOCK.getKey(param0);
        return var0.withPrefix("block/");
    }

    public static ResourceLocation getModelLocation(Item param0) {
        ResourceLocation var0 = BuiltInRegistries.ITEM.getKey(param0);
        return var0.withPrefix("item/");
    }

    public static ResourceLocation getModelLocation(Item param0, String param1) {
        ResourceLocation var0 = BuiltInRegistries.ITEM.getKey(param0);
        return var0.withPath(param1x -> "item/" + param1x + param1);
    }
}
