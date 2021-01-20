package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemModelGenerator {
    public static final List<String> LAYERS = Lists.newArrayList("layer0", "layer1", "layer2", "layer3", "layer4");

    public BlockModel generateBlockModel(Function<Material, TextureAtlasSprite> param0, BlockModel param1) {
        Map<String, Either<Material, String>> var0 = Maps.newHashMap();
        List<BlockElement> var1 = Lists.newArrayList();

        for(int var2 = 0; var2 < LAYERS.size(); ++var2) {
            String var3 = LAYERS.get(var2);
            if (!param1.hasTexture(var3)) {
                break;
            }

            Material var4 = param1.getMaterial(var3);
            var0.put(var3, Either.left(var4));
            TextureAtlasSprite var5 = param0.apply(var4);
            var1.addAll(this.processFrames(var2, var3, var5));
        }

        var0.put("particle", param1.hasTexture("particle") ? Either.left(param1.getMaterial("particle")) : var0.get("layer0"));
        BlockModel var6 = new BlockModel(null, var1, var0, false, param1.getGuiLight(), param1.getTransforms(), param1.getOverrides());
        var6.name = param1.name;
        return var6;
    }

    private List<BlockElement> processFrames(int param0, String param1, TextureAtlasSprite param2) {
        Map<Direction, BlockElementFace> var0 = Maps.newHashMap();
        var0.put(Direction.SOUTH, new BlockElementFace(null, param0, param1, new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)));
        var0.put(Direction.NORTH, new BlockElementFace(null, param0, param1, new BlockFaceUV(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0)));
        List<BlockElement> var1 = Lists.newArrayList();
        var1.add(new BlockElement(new Vector3f(0.0F, 0.0F, 7.5F), new Vector3f(16.0F, 16.0F, 8.5F), var0, null, true));
        var1.addAll(this.createSideElements(param2, param1, param0));
        return var1;
    }

    private List<BlockElement> createSideElements(TextureAtlasSprite param0, String param1, int param2) {
        float var0 = (float)param0.getWidth();
        float var1 = (float)param0.getHeight();
        List<BlockElement> var2 = Lists.newArrayList();

        for(ItemModelGenerator.Span var3 : this.getSpans(param0)) {
            float var4 = 0.0F;
            float var5 = 0.0F;
            float var6 = 0.0F;
            float var7 = 0.0F;
            float var8 = 0.0F;
            float var9 = 0.0F;
            float var10 = 0.0F;
            float var11 = 0.0F;
            float var12 = 16.0F / var0;
            float var13 = 16.0F / var1;
            float var14 = (float)var3.getMin();
            float var15 = (float)var3.getMax();
            float var16 = (float)var3.getAnchor();
            ItemModelGenerator.SpanFacing var17 = var3.getFacing();
            switch(var17) {
                case UP:
                    var8 = var14;
                    var4 = var14;
                    var6 = var9 = var15 + 1.0F;
                    var10 = var16;
                    var5 = var16;
                    var7 = var16;
                    var11 = var16 + 1.0F;
                    break;
                case DOWN:
                    var10 = var16;
                    var11 = var16 + 1.0F;
                    var8 = var14;
                    var4 = var14;
                    var6 = var9 = var15 + 1.0F;
                    var5 = var16 + 1.0F;
                    var7 = var16 + 1.0F;
                    break;
                case LEFT:
                    var8 = var16;
                    var4 = var16;
                    var6 = var16;
                    var9 = var16 + 1.0F;
                    var11 = var14;
                    var5 = var14;
                    var7 = var10 = var15 + 1.0F;
                    break;
                case RIGHT:
                    var8 = var16;
                    var9 = var16 + 1.0F;
                    var4 = var16 + 1.0F;
                    var6 = var16 + 1.0F;
                    var11 = var14;
                    var5 = var14;
                    var7 = var10 = var15 + 1.0F;
            }

            var4 *= var12;
            var6 *= var12;
            var5 *= var13;
            var7 *= var13;
            var5 = 16.0F - var5;
            var7 = 16.0F - var7;
            var8 *= var12;
            var9 *= var12;
            var10 *= var13;
            var11 *= var13;
            Map<Direction, BlockElementFace> var18 = Maps.newHashMap();
            var18.put(var17.getDirection(), new BlockElementFace(null, param2, param1, new BlockFaceUV(new float[]{var8, var10, var9, var11}, 0)));
            switch(var17) {
                case UP:
                    var2.add(new BlockElement(new Vector3f(var4, var5, 7.5F), new Vector3f(var6, var5, 8.5F), var18, null, true));
                    break;
                case DOWN:
                    var2.add(new BlockElement(new Vector3f(var4, var7, 7.5F), new Vector3f(var6, var7, 8.5F), var18, null, true));
                    break;
                case LEFT:
                    var2.add(new BlockElement(new Vector3f(var4, var5, 7.5F), new Vector3f(var4, var7, 8.5F), var18, null, true));
                    break;
                case RIGHT:
                    var2.add(new BlockElement(new Vector3f(var6, var5, 7.5F), new Vector3f(var6, var7, 8.5F), var18, null, true));
            }
        }

        return var2;
    }

    private List<ItemModelGenerator.Span> getSpans(TextureAtlasSprite param0) {
        int var0 = param0.getWidth();
        int var1 = param0.getHeight();
        List<ItemModelGenerator.Span> var2 = Lists.newArrayList();
        param0.getUniqueFrames().forEach(param4 -> {
            for(int var0x = 0; var0x < var1; ++var0x) {
                for(int var1x = 0; var1x < var0; ++var1x) {
                    boolean var2x = !this.isTransparent(param0, param4, var1x, var0x, var0, var1);
                    this.checkTransition(ItemModelGenerator.SpanFacing.UP, var2, param0, param4, var1x, var0x, var0, var1, var2x);
                    this.checkTransition(ItemModelGenerator.SpanFacing.DOWN, var2, param0, param4, var1x, var0x, var0, var1, var2x);
                    this.checkTransition(ItemModelGenerator.SpanFacing.LEFT, var2, param0, param4, var1x, var0x, var0, var1, var2x);
                    this.checkTransition(ItemModelGenerator.SpanFacing.RIGHT, var2, param0, param4, var1x, var0x, var0, var1, var2x);
                }
            }

        });
        return var2;
    }

    private void checkTransition(
        ItemModelGenerator.SpanFacing param0,
        List<ItemModelGenerator.Span> param1,
        TextureAtlasSprite param2,
        int param3,
        int param4,
        int param5,
        int param6,
        int param7,
        boolean param8
    ) {
        boolean var0 = this.isTransparent(param2, param3, param4 + param0.getXOffset(), param5 + param0.getYOffset(), param6, param7) && param8;
        if (var0) {
            this.createOrExpandSpan(param1, param0, param4, param5);
        }

    }

    private void createOrExpandSpan(List<ItemModelGenerator.Span> param0, ItemModelGenerator.SpanFacing param1, int param2, int param3) {
        ItemModelGenerator.Span var0 = null;

        for(ItemModelGenerator.Span var1 : param0) {
            if (var1.getFacing() == param1) {
                int var2 = param1.isHorizontal() ? param3 : param2;
                if (var1.getAnchor() == var2) {
                    var0 = var1;
                    break;
                }
            }
        }

        int var3 = param1.isHorizontal() ? param3 : param2;
        int var4 = param1.isHorizontal() ? param2 : param3;
        if (var0 == null) {
            param0.add(new ItemModelGenerator.Span(param1, var4, var3));
        } else {
            var0.expand(var4);
        }

    }

    private boolean isTransparent(TextureAtlasSprite param0, int param1, int param2, int param3, int param4, int param5) {
        return param2 >= 0 && param3 >= 0 && param2 < param4 && param3 < param5 ? param0.isTransparent(param1, param2, param3) : true;
    }

    @OnlyIn(Dist.CLIENT)
    static class Span {
        private final ItemModelGenerator.SpanFacing facing;
        private int min;
        private int max;
        private final int anchor;

        public Span(ItemModelGenerator.SpanFacing param0, int param1, int param2) {
            this.facing = param0;
            this.min = param1;
            this.max = param1;
            this.anchor = param2;
        }

        public void expand(int param0) {
            if (param0 < this.min) {
                this.min = param0;
            } else if (param0 > this.max) {
                this.max = param0;
            }

        }

        public ItemModelGenerator.SpanFacing getFacing() {
            return this.facing;
        }

        public int getMin() {
            return this.min;
        }

        public int getMax() {
            return this.max;
        }

        public int getAnchor() {
            return this.anchor;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static enum SpanFacing {
        UP(Direction.UP, 0, -1),
        DOWN(Direction.DOWN, 0, 1),
        LEFT(Direction.EAST, -1, 0),
        RIGHT(Direction.WEST, 1, 0);

        private final Direction direction;
        private final int xOffset;
        private final int yOffset;

        private SpanFacing(Direction param0, int param1, int param2) {
            this.direction = param0;
            this.xOffset = param1;
            this.yOffset = param2;
        }

        public Direction getDirection() {
            return this.direction;
        }

        public int getXOffset() {
            return this.xOffset;
        }

        public int getYOffset() {
            return this.yOffset;
        }

        private boolean isHorizontal() {
            return this == DOWN || this == UP;
        }
    }
}
