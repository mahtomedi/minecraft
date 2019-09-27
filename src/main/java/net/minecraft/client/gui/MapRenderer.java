package net.minecraft.client.gui;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
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
    private final TextureManager textureManager;
    private final Map<String, MapRenderer.MapInstance> maps = Maps.newHashMap();

    public MapRenderer(TextureManager param0) {
        this.textureManager = param0;
    }

    public void update(MapItemSavedData param0) {
        this.getMapInstance(param0).updateTexture();
    }

    public void render(PoseStack param0, MultiBufferSource param1, MapItemSavedData param2, boolean param3) {
        this.getMapInstance(param2).draw(param0, param1, param3);
    }

    private MapRenderer.MapInstance getMapInstance(MapItemSavedData param0) {
        MapRenderer.MapInstance var0 = this.maps.get(param0.getId());
        if (var0 == null) {
            var0 = new MapRenderer.MapInstance(param0);
            this.maps.put(param0.getId(), var0);
        }

        return var0;
    }

    @Nullable
    public MapRenderer.MapInstance getMapInstanceIfExists(String param0) {
        return this.maps.get(param0);
    }

    public void resetData() {
        for(MapRenderer.MapInstance var0 : this.maps.values()) {
            var0.close();
        }

        this.maps.clear();
    }

    @Nullable
    public MapItemSavedData getData(@Nullable MapRenderer.MapInstance param0) {
        return param0 != null ? param0.data : null;
    }

    @Override
    public void close() {
        this.resetData();
    }

    @OnlyIn(Dist.CLIENT)
    class MapInstance implements AutoCloseable {
        private final MapItemSavedData data;
        private final DynamicTexture texture;
        private final ResourceLocation location;

        private MapInstance(MapItemSavedData param0) {
            this.data = param0;
            this.texture = new DynamicTexture(128, 128, true);
            this.location = MapRenderer.this.textureManager.register("map/" + param0.getId(), this.texture);
        }

        private void updateTexture() {
            for(int var0 = 0; var0 < 128; ++var0) {
                for(int var1 = 0; var1 < 128; ++var1) {
                    int var2 = var1 + var0 * 128;
                    int var3 = this.data.colors[var2] & 255;
                    if (var3 / 4 == 0) {
                        this.texture.getPixels().setPixelRGBA(var1, var0, (var2 + var2 / 128 & 1) * 8 + 16 << 24);
                    } else {
                        this.texture.getPixels().setPixelRGBA(var1, var0, MaterialColor.MATERIAL_COLORS[var3 / 4].calculateRGBColor(var3 & 3));
                    }
                }
            }

            this.texture.upload();
        }

        private void draw(PoseStack param0, MultiBufferSource param1, boolean param2) {
            int var0 = 0;
            int var1 = 0;
            Tesselator var2 = Tesselator.getInstance();
            BufferBuilder var3 = var2.getBuilder();
            float var4 = 0.0F;
            Matrix4f var5 = param0.getPose();
            MapRenderer.this.textureManager.bind(this.location);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
            );
            RenderSystem.disableAlphaTest();
            var3.begin(7, DefaultVertexFormat.POSITION_TEX);
            var3.vertex(var5, 0.0F, 128.0F, -0.01F).uv(0.0F, 1.0F).endVertex();
            var3.vertex(var5, 128.0F, 128.0F, -0.01F).uv(1.0F, 1.0F).endVertex();
            var3.vertex(var5, 128.0F, 0.0F, -0.01F).uv(1.0F, 0.0F).endVertex();
            var3.vertex(var5, 0.0F, 0.0F, -0.01F).uv(0.0F, 0.0F).endVertex();
            var2.end();
            RenderSystem.enableAlphaTest();
            RenderSystem.disableBlend();
            int var6 = 0;

            for(MapDecoration var7 : this.data.decorations.values()) {
                if (!param2 || var7.renderOnFrame()) {
                    MapRenderer.this.textureManager.bind(MapRenderer.MAP_ICONS_LOCATION);
                    param0.pushPose();
                    param0.translate((double)(0.0F + (float)var7.getX() / 2.0F + 64.0F), (double)(0.0F + (float)var7.getY() / 2.0F + 64.0F), -0.02F);
                    param0.mulPose(Vector3f.ZP.rotation((float)(var7.getRot() * 360) / 16.0F, true));
                    param0.scale(4.0F, 4.0F, 3.0F);
                    param0.translate(-0.125, 0.125, 0.0);
                    byte var8 = var7.getImage();
                    float var9 = (float)(var8 % 16 + 0) / 16.0F;
                    float var10 = (float)(var8 / 16 + 0) / 16.0F;
                    float var11 = (float)(var8 % 16 + 1) / 16.0F;
                    float var12 = (float)(var8 / 16 + 1) / 16.0F;
                    Matrix4f var13 = param0.getPose();
                    var3.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
                    float var14 = -0.001F;
                    var3.vertex(var13, -1.0F, 1.0F, (float)var6 * -0.001F).uv(var9, var10).color(255, 255, 255, 255).endVertex();
                    var3.vertex(var13, 1.0F, 1.0F, (float)var6 * -0.001F).uv(var11, var10).color(255, 255, 255, 255).endVertex();
                    var3.vertex(var13, 1.0F, -1.0F, (float)var6 * -0.001F).uv(var11, var12).color(255, 255, 255, 255).endVertex();
                    var3.vertex(var13, -1.0F, -1.0F, (float)var6 * -0.001F).uv(var9, var12).color(255, 255, 255, 255).endVertex();
                    var3.end();
                    BufferUploader.end(var3);
                    param0.popPose();
                    if (var7.getName() != null) {
                        Font var15 = Minecraft.getInstance().font;
                        String var16 = var7.getName().getColoredString();
                        float var17 = (float)var15.width(var16);
                        float var18 = Mth.clamp(25.0F / var17, 0.0F, 6.0F / 9.0F);
                        param0.pushPose();
                        param0.translate(
                            (double)(0.0F + (float)var7.getX() / 2.0F + 64.0F - var17 * var18 / 2.0F),
                            (double)(0.0F + (float)var7.getY() / 2.0F + 64.0F + 4.0F),
                            -0.025F
                        );
                        param0.scale(var18, var18, 1.0F);
                        GuiComponent.fill(param0.getPose(), -1, -1, (int)var17, 9 - 1, Integer.MIN_VALUE);
                        param0.translate(0.0, 0.0, -0.1F);
                        RenderSystem.enableAlphaTest();
                        var15.drawInBatch(var16, 0.0F, 0.0F, -1, false, param0.getPose(), param1, false, 0, 15728880);
                        param0.popPose();
                    }

                    ++var6;
                }
            }

        }

        @Override
        public void close() {
            this.texture.close();
        }
    }
}
