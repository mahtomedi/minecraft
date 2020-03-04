package net.minecraft.data.models.model;

import net.minecraft.core.Registry;
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
        ResourceLocation var0 = Registry.BLOCK.getKey(param0);
        return new ResourceLocation(var0.getNamespace(), "block/" + var0.getPath() + param1);
    }

    public static ResourceLocation getModelLocation(Block param0) {
        ResourceLocation var0 = Registry.BLOCK.getKey(param0);
        return new ResourceLocation(var0.getNamespace(), "block/" + var0.getPath());
    }

    public static ResourceLocation getModelLocation(Item param0) {
        ResourceLocation var0 = Registry.ITEM.getKey(param0);
        return new ResourceLocation(var0.getNamespace(), "item/" + var0.getPath());
    }

    public static ResourceLocation getModelLocation(Item param0, String param1) {
        ResourceLocation var0 = Registry.ITEM.getKey(param0);
        return new ResourceLocation(var0.getNamespace(), "item/" + var0.getPath() + param1);
    }
}
