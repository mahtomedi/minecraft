package net.minecraft.data.models.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class TextureMapping {
    private final Map<TextureSlot, ResourceLocation> slots = Maps.newHashMap();
    private final Set<TextureSlot> forcedSlots = Sets.newHashSet();

    public TextureMapping put(TextureSlot param0, ResourceLocation param1) {
        this.slots.put(param0, param1);
        return this;
    }

    public TextureMapping putForced(TextureSlot param0, ResourceLocation param1) {
        this.slots.put(param0, param1);
        this.forcedSlots.add(param0);
        return this;
    }

    public Stream<TextureSlot> getForced() {
        return this.forcedSlots.stream();
    }

    public TextureMapping copySlot(TextureSlot param0, TextureSlot param1) {
        this.slots.put(param1, this.slots.get(param0));
        return this;
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

    public static TextureMapping flowerbed(Block param0) {
        return new TextureMapping().put(TextureSlot.FLOWERBED, getBlockTexture(param0)).put(TextureSlot.STEM, getBlockTexture(param0, "_stem"));
    }

    public static TextureMapping wool(ResourceLocation param0) {
        return singleSlot(TextureSlot.WOOL, param0);
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
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(param0))
            .put(TextureSlot.END, getBlockTexture(param0, "_top"))
            .put(TextureSlot.PARTICLE, getBlockTexture(param0));
    }

    public static TextureMapping column(ResourceLocation param0, ResourceLocation param1) {
        return new TextureMapping().put(TextureSlot.SIDE, param0).put(TextureSlot.END, param1);
    }

    public static TextureMapping fence(Block param0) {
        return new TextureMapping()
            .put(TextureSlot.TEXTURE, getBlockTexture(param0))
            .put(TextureSlot.SIDE, getBlockTexture(param0, "_side"))
            .put(TextureSlot.TOP, getBlockTexture(param0, "_top"));
    }

    public static TextureMapping customParticle(Block param0) {
        return new TextureMapping().put(TextureSlot.TEXTURE, getBlockTexture(param0)).put(TextureSlot.PARTICLE, getBlockTexture(param0, "_particle"));
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

    public static TextureMapping columnWithWall(Block param0) {
        ResourceLocation var0 = getBlockTexture(param0);
        return new TextureMapping()
            .put(TextureSlot.TEXTURE, var0)
            .put(TextureSlot.WALL, var0)
            .put(TextureSlot.SIDE, var0)
            .put(TextureSlot.END, getBlockTexture(param0, "_top"));
    }

    public static TextureMapping door(ResourceLocation param0, ResourceLocation param1) {
        return new TextureMapping().put(TextureSlot.TOP, param0).put(TextureSlot.BOTTOM, param1);
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

    public static TextureMapping campfire(Block param0) {
        return new TextureMapping().put(TextureSlot.LIT_LOG, getBlockTexture(param0, "_log_lit")).put(TextureSlot.FIRE, getBlockTexture(param0, "_fire"));
    }

    public static TextureMapping candleCake(Block param0, boolean param1) {
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(Blocks.CAKE, "_side"))
            .put(TextureSlot.BOTTOM, getBlockTexture(Blocks.CAKE, "_bottom"))
            .put(TextureSlot.TOP, getBlockTexture(Blocks.CAKE, "_top"))
            .put(TextureSlot.SIDE, getBlockTexture(Blocks.CAKE, "_side"))
            .put(TextureSlot.CANDLE, getBlockTexture(param0, param1 ? "_lit" : ""));
    }

    public static TextureMapping cauldron(ResourceLocation param0) {
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(Blocks.CAULDRON, "_side"))
            .put(TextureSlot.SIDE, getBlockTexture(Blocks.CAULDRON, "_side"))
            .put(TextureSlot.TOP, getBlockTexture(Blocks.CAULDRON, "_top"))
            .put(TextureSlot.BOTTOM, getBlockTexture(Blocks.CAULDRON, "_bottom"))
            .put(TextureSlot.INSIDE, getBlockTexture(Blocks.CAULDRON, "_inner"))
            .put(TextureSlot.CONTENT, param0);
    }

    public static TextureMapping sculkShrieker(boolean param0) {
        String var0 = param0 ? "_can_summon" : "";
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(Blocks.SCULK_SHRIEKER, "_bottom"))
            .put(TextureSlot.SIDE, getBlockTexture(Blocks.SCULK_SHRIEKER, "_side"))
            .put(TextureSlot.TOP, getBlockTexture(Blocks.SCULK_SHRIEKER, "_top"))
            .put(TextureSlot.INNER_TOP, getBlockTexture(Blocks.SCULK_SHRIEKER, var0 + "_inner_top"))
            .put(TextureSlot.BOTTOM, getBlockTexture(Blocks.SCULK_SHRIEKER, "_bottom"));
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

    public static TextureMapping layered(ResourceLocation param0, ResourceLocation param1) {
        return new TextureMapping().put(TextureSlot.LAYER0, param0).put(TextureSlot.LAYER1, param1);
    }

    public static TextureMapping layered(ResourceLocation param0, ResourceLocation param1, ResourceLocation param2) {
        return new TextureMapping().put(TextureSlot.LAYER0, param0).put(TextureSlot.LAYER1, param1).put(TextureSlot.LAYER2, param2);
    }

    public static ResourceLocation getBlockTexture(Block param0) {
        ResourceLocation var0 = BuiltInRegistries.BLOCK.getKey(param0);
        return var0.withPrefix("block/");
    }

    public static ResourceLocation getBlockTexture(Block param0, String param1) {
        ResourceLocation var0 = BuiltInRegistries.BLOCK.getKey(param0);
        return var0.withPath(param1x -> "block/" + param1x + param1);
    }

    public static ResourceLocation getItemTexture(Item param0) {
        ResourceLocation var0 = BuiltInRegistries.ITEM.getKey(param0);
        return var0.withPrefix("item/");
    }

    public static ResourceLocation getItemTexture(Item param0, String param1) {
        ResourceLocation var0 = BuiltInRegistries.ITEM.getKey(param0);
        return var0.withPath(param1x -> "item/" + param1x + param1);
    }
}
