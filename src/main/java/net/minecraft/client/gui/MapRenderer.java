package net.minecraft.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MapRenderer implements AutoCloseable {
    private static final ResourceLocation MAP_ICONS_LOCATION = new ResourceLocation("textures/map/map_icons.png");
    static final RenderType MAP_ICONS = RenderType.text(MAP_ICONS_LOCATION);
    private static final int WIDTH = 128;
    private static final int HEIGHT = 128;
    final TextureManager textureManager;
    private final Int2ObjectMap<MapRenderer.MapInstance> maps = new Int2ObjectOpenHashMap<>();

    public MapRenderer(TextureManager param0) {
        this.textureManager = param0;
    }

    public void update(int param0, MapItemSavedData param1) {
        this.getOrCreateMapInstance(param0, param1).forceUpload();
    }

    public void render(PoseStack param0, MultiBufferSource param1, int param2, MapItemSavedData param3, boolean param4, int param5) {
        this.getOrCreateMapInstance(param2, param3).draw(param0, param1, param4, param5);
    }

    private MapRenderer.MapInstance getOrCreateMapInstance(int param0, MapItemSavedData param1) {
        return this.maps.compute(param0, (param1x, param2) -> {
            if (param2 == null) {
                return new MapRenderer.MapInstance(param1x, param1);
            } else {
                param2.replaceMapData(param1);
                return param2;
            }
        });
    }

    public void resetData() {
        for(MapRenderer.MapInstance var0 : this.maps.values()) {
            var0.close();
        }

        this.maps.clear();
    }

    @Override
    public void close() {
        this.resetData();
    }

    @OnlyIn(Dist.CLIENT)
    class MapInstance implements AutoCloseable {
        private MapItemSavedData data;
        private final DynamicTexture texture;
        private final RenderType renderType;
        private boolean requiresUpload = true;

        MapInstance(int param0, MapItemSavedData param1) {
            this.data = param1;
            this.texture = new DynamicTexture(128, 128, true);
            ResourceLocation param2 = MapRenderer.this.textureManager.register("map/" + param0, this.texture);
            this.renderType = RenderType.text(param2);
        }

        void replaceMapData(MapItemSavedData param0) {
            boolean var0 = this.data != param0;
            this.data = param0;
            this.requiresUpload |= var0;
        }

        public void forceUpload() {
            this.requiresUpload = true;
        }

        private void updateTexture() {
            for(int var0 = 0; var0 < 128; ++var0) {
                for(int var1 = 0; var1 < 128; ++var1) {
                    int var2 = var1 + var0 * 128;
                    this.texture.getPixels().setPixelRGBA(var1, var0, MaterialColor.getColorFromPackedId(this.data.colors[var2]));
                }
            }

            this.texture.upload();
        }

        void draw(PoseStack param0, MultiBufferSource param1, boolean param2, int param3) {
            if (this.requiresUpload) {
                this.updateTexture();
                this.requiresUpload = false;
            }

            int var0 = 0;
            int var1 = 0;
            float var2 = 0.0F;
            Matrix4f var3 = param0.last().pose();
            VertexConsumer var4 = param1.getBuffer(this.renderType);
            var4.vertex(var3, 0.0F, 128.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(param3).endVertex();
            var4.vertex(var3, 128.0F, 128.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(param3).endVertex();
            var4.vertex(var3, 128.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(param3).endVertex();
            var4.vertex(var3, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(param3).endVertex();
            int var5 = 0;

            for(MapDecoration var6 : this.data.getDecorations()) {
                if (!param2 || var6.renderOnFrame()) {
                    param0.pushPose();
                    param0.translate((double)(0.0F + (float)var6.getX() / 2.0F + 64.0F), (double)(0.0F + (float)var6.getY() / 2.0F + 64.0F), -0.02F);
                    param0.mulPose(Vector3f.ZP.rotationDegrees((float)(var6.getRot() * 360) / 16.0F));
                    param0.scale(4.0F, 4.0F, 3.0F);
                    param0.translate(-0.125, 0.125, 0.0);
                    byte var7 = var6.getImage();
                    float var8 = (float)(var7 % 16 + 0) / 16.0F;
                    float var9 = (float)(var7 / 16 + 0) / 16.0F;
                    float var10 = (float)(var7 % 16 + 1) / 16.0F;
                    float var11 = (float)(var7 / 16 + 1) / 16.0F;
                    Matrix4f var12 = param0.last().pose();
                    float var13 = -0.001F;
                    VertexConsumer var14 = param1.getBuffer(MapRenderer.MAP_ICONS);
                    var14.vertex(var12, -1.0F, 1.0F, (float)var5 * -0.001F).color(255, 255, 255, 255).uv(var8, var9).uv2(param3).endVertex();
                    var14.vertex(var12, 1.0F, 1.0F, (float)var5 * -0.001F).color(255, 255, 255, 255).uv(var10, var9).uv2(param3).endVertex();
                    var14.vertex(var12, 1.0F, -1.0F, (float)var5 * -0.001F).color(255, 255, 255, 255).uv(var10, var11).uv2(param3).endVertex();
                    var14.vertex(var12, -1.0F, -1.0F, (float)var5 * -0.001F).color(255, 255, 255, 255).uv(var8, var11).uv2(param3).endVertex();
                    param0.popPose();
                    if (var6.getName() != null) {
                        Font var15 = Minecraft.getInstance().font;
                        Component var16 = var6.getName();
                        float var17 = (float)var15.width(var16);
                        float var18 = Mth.clamp(25.0F / var17, 0.0F, 6.0F / 9.0F);
                        param0.pushPose();
                        param0.translate(
                            (double)(0.0F + (float)var6.getX() / 2.0F + 64.0F - var17 * var18 / 2.0F),
                            (double)(0.0F + (float)var6.getY() / 2.0F + 64.0F + 4.0F),
                            -0.025F
                        );
                        param0.scale(var18, var18, 1.0F);
                        param0.translate(0.0, 0.0, -0.1F);
                        var15.drawInBatch(var16, 0.0F, 0.0F, -1, false, param0.last().pose(), param1, false, Integer.MIN_VALUE, param3);
                        param0.popPose();
                    }

                    ++var5;
                }
            }

        }

        @Override
        public void close() {
            this.texture.close();
        }
    }
}
