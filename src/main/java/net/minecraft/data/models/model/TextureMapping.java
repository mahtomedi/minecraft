package net.minecraft.data.models.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class TextureMapping {
    private final Map<TextureSlot, ResourceLocation> slots = Maps.newHashMap();
    private final Set<TextureSlot> forcedSlots = Sets.newHashSet();

    public TextureMapping put(TextureSlot param0, ResourceLocation param1) {
        this.slots.put(param0, param1);
        return this;
    }

    public Stream<TextureSlot> getForced() {
        return this.forcedSlots.stream();
    }

    public TextureMapping copyForced(TextureSlot param0, TextureSlot param1) {
        this.slots.put(param1, this.slots.get(param0));
        this.forcedSlots.add(param1);
        return this;
    }

    public ResourceLocation get(TextureSlot param0) {
        for(TextureSlot var0 = param0; var0 != null; var0 = var0.getParent()) {
            ResourceLocation var1 = this.slots.get(var0);
            if (var1 != null) {
                return var1;
            }
        }

        throw new IllegalStateException("Can't find texture for slot " + param0);
    }

    public TextureMapping copyAndUpdate(TextureSlot param0, ResourceLocation param1) {
        TextureMapping var0 = new TextureMapping();
        var0.slots.putAll(this.slots);
        var0.forcedSlots.addAll(this.forcedSlots);
        var0.put(param0, param1);
        return var0;
    }

    public static TextureMapping cube(Block param0) {
        ResourceLocation var0 = getBlockTexture(param0);
        return cube(var0);
    }

    public static TextureMapping defaultTexture(Block param0) {
        ResourceLocation var0 = getBlockTexture(param0);
        return defaultTexture(var0);
    }

    public static TextureMapping defaultTexture(ResourceLocation param0) {
        return new TextureMapping().put(TextureSlot.TEXTURE, param0);
    }

    public static TextureMapping cube(ResourceLocation param0) {
        return new TextureMapping().put(TextureSlot.ALL, param0);
    }

    public static TextureMapping cross(Block param0) {
        return singleSlot(TextureSlot.CROSS, getBlockTexture(param0));
    }

    public static TextureMapping cross(ResourceLocation param0) {
        return singleSlot(TextureSlot.CROSS, param0);
    }

    public static TextureMapping plant(Block param0) {
        return singleSlot(TextureSlot.PLANT, getBlockTexture(param0));
    }

    public static TextureMapping plant(ResourceLocation param0) {
        return singleSlot(TextureSlot.PLANT, param0);
    }

    public static TextureMapping rail(Block param0) {
        return singleSlot(TextureSlot.RAIL, getBlockTexture(param0));
    }

    public static TextureMapping rail(ResourceLocation param0) {
        return singleSlot(TextureSlot.RAIL, param0);
    }

    public static TextureMapping wool(Block param0) {
        return singleSlot(TextureSlot.WOOL, getBlockTexture(param0));
    }

    public static TextureMapping stem(Block param0) {
        return singleSlot(TextureSlot.STEM, getBlockTexture(param0));
    }

    public static TextureMapping attachedStem(Block param0, Block param1) {
        return new TextureMapping().put(TextureSlot.STEM, getBlockTexture(param0)).put(TextureSlot.UPPER_STEM, getBlockTexture(param1));
    }

    public static TextureMapping pattern(Block param0) {
        return singleSlot(TextureSlot.PATTERN, getBlockTexture(param0));
    }

    public static TextureMapping fan(Block param0) {
        return singleSlot(TextureSlot.FAN, getBlockTexture(param0));
    }

    public static TextureMapping crop(ResourceLocation param0) {
        return singleSlot(TextureSlot.CROP, param0);
    }

    public static TextureMapping pane(Block param0, Block param1) {
        return new TextureMapping().put(TextureSlot.PANE, getBlockTexture(param0)).put(TextureSlot.EDGE, getBlockTexture(param1, "_top"));
    }

    public static TextureMapping singleSlot(TextureSlot param0, ResourceLocation param1) {
        return new TextureMapping().put(param0, param1);
    }

    public static TextureMapping column(Block param0) {
        return new TextureMapping().put(TextureSlot.SIDE, getBlockTexture(param0, "_side")).put(TextureSlot.END, getBlockTexture(param0, "_top"));
    }

    public static TextureMapping cubeTop(Block param0) {
        return new TextureMapping().put(TextureSlot.SIDE, getBlockTexture(param0, "_side")).put(TextureSlot.TOP, getBlockTexture(param0, "_top"));
    }

    public static TextureMapping logColumn(Block param0) {
        return new TextureMapping().put(TextureSlot.SIDE, getBlockTexture(param0)).put(TextureSlot.END, getBlockTexture(param0, "_top"));
    }

    public static TextureMapping column(ResourceLocation param0, ResourceLocation param1) {
        return new TextureMapping().put(TextureSlot.SIDE, param0).put(TextureSlot.END, param1);
    }

    public static TextureMapping cubeBottomTop(Block param0) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(param0, "_side"))
            .put(TextureSlot.TOP, getBlockTexture(param0, "_top"))
            .put(TextureSlot.BOTTOM, getBlockTexture(param0, "_bottom"));
    }

    public static TextureMapping cubeBottomTopWithWall(Block param0) {
        ResourceLocation var0 = getBlockTexture(param0);
        return new TextureMapping()
            .put(TextureSlot.WALL, var0)
            .put(TextureSlot.SIDE, var0)
            .put(TextureSlot.TOP, getBlockTexture(param0, "_top"))
            .put(TextureSlot.BOTTOM, getBlockTexture(param0, "_bottom"));
    }

    public static TextureMapping door(Block param0) {
        return new TextureMapping().put(TextureSlot.TOP, getBlockTexture(param0, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(param0, "_bottom"));
    }

    public static TextureMapping particle(Block param0) {
        return new TextureMapping().put(TextureSlot.PARTICLE, getBlockTexture(param0));
    }

    public static TextureMapping particle(ResourceLocation param0) {
        return new TextureMapping().put(TextureSlot.PARTICLE, param0);
    }

    public static TextureMapping fire0(Block param0) {
        return new TextureMapping().put(TextureSlot.FIRE, getBlockTexture(param0, "_0"));
    }

    public static TextureMapping fire1(Block param0) {
        return new TextureMapping().put(TextureSlot.FIRE, getBlockTexture(param0, "_1"));
    }

    public static TextureMapping lantern(Block param0) {
        return new TextureMapping().put(TextureSlot.LANTERN, getBlockTexture(param0));
    }

    public static TextureMapping torch(Block param0) {
        return new TextureMapping().put(TextureSlot.TORCH, getBlockTexture(param0));
    }

    public static TextureMapping torch(ResourceLocation param0) {
        return new TextureMapping().put(TextureSlot.TORCH, param0);
    }

    public static TextureMapping particleFromItem(Item param0) {
        return new TextureMapping().put(TextureSlot.PARTICLE, getItemTexture(param0));
    }

    public static TextureMapping commandBlock(Block param0) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(param0, "_side"))
            .put(TextureSlot.FRONT, getBlockTexture(param0, "_front"))
            .put(TextureSlot.BACK, getBlockTexture(param0, "_back"));
    }

    public static TextureMapping orientableCube(Block param0) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(param0, "_side"))
            .put(TextureSlot.FRONT, getBlockTexture(param0, "_front"))
            .put(TextureSlot.TOP, getBlockTexture(param0, "_top"))
            .put(TextureSlot.BOTTOM, getBlockTexture(param0, "_bottom"));
    }

    public static TextureMapping orientableCubeOnlyTop(Block param0) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(param0, "_side"))
            .put(TextureSlot.FRONT, getBlockTexture(param0, "_front"))
            .put(TextureSlot.TOP, getBlockTexture(param0, "_top"));
    }

    public static TextureMapping orientableCubeSameEnds(Block param0) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(param0, "_side"))
            .put(TextureSlot.FRONT, getBlockTexture(param0, "_front"))
            .put(TextureSlot.END, getBlockTexture(param0, "_end"));
    }

    public static TextureMapping top(Block param0) {
        return new TextureMapping().put(TextureSlot.TOP, getBlockTexture(param0, "_top"));
    }

    public static TextureMapping craftingTable(Block param0, Block param1) {
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(param0, "_front"))
            .put(TextureSlot.DOWN, getBlockTexture(param1))
            .put(TextureSlot.UP, getBlockTexture(param0, "_top"))
            .put(TextureSlot.NORTH, getBlockTexture(param0, "_front"))
            .put(TextureSlot.EAST, getBlockTexture(param0, "_side"))
            .put(TextureSlot.SOUTH, getBlockTexture(param0, "_side"))
            .put(TextureSlot.WEST, getBlockTexture(param0, "_front"));
    }

    public static TextureMapping fletchingTable(Block param0, Block param1) {
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(param0, "_front"))
            .put(TextureSlot.DOWN, getBlockTexture(param1))
            .put(TextureSlot.UP, getBlockTexture(param0, "_top"))
            .put(TextureSlot.NORTH, getBlockTexture(param0, "_front"))
            .put(TextureSlot.SOUTH, getBlockTexture(param0, "_front"))
            .put(TextureSlot.EAST, getBlockTexture(param0, "_side"))
            .put(TextureSlot.WEST, getBlockTexture(param0, "_side"));
    }

    public static TextureMapping layer0(Item param0) {
        return new TextureMapping().put(TextureSlot.LAYER0, getItemTexture(param0));
    }

    public static TextureMapping layer0(Block param0) {
        return new TextureMapping().put(TextureSlot.LAYER0, getBlockTexture(param0));
    }

    public static TextureMapping layer0(ResourceLocation param0) {
        return new TextureMapping().put(TextureSlot.LAYER0, param0);
    }

    public static ResourceLocation getBlockTexture(Block param0) {
        ResourceLocation var0 = Registry.BLOCK.getKey(param0);
        return new ResourceLocation(var0.getNamespace(), "block/" + var0.getPath());
    }

    public static ResourceLocation getBlockTexture(Block param0, String param1) {
        ResourceLocation var0 = Registry.BLOCK.getKey(param0);
        return new ResourceLocation(var0.getNamespace(), "block/" + var0.getPath() + param1);
    }

    public static ResourceLocation getItemTexture(Item param0) {
        ResourceLocation var0 = Registry.ITEM.getKey(param0);
        return new ResourceLocation(var0.getNamespace(), "item/" + var0.getPath());
    }

    public static ResourceLocation getItemTexture(Item param0, String param1) {
        ResourceLocation var0 = Registry.ITEM.getKey(param0);
        return new ResourceLocation(var0.getNamespace(), "item/" + var0.getPath() + param1);
    }
}