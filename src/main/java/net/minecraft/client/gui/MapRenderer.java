package net.minecraft.client.gui;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
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

    public void render(MapItemSavedData param0, boolean param1) {
        this.getMapInstance(param0).draw(param1);
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

        private void draw(boolean param0) {
            int var0 = 0;
            int var1 = 0;
            Tesselator var2 = Tesselator.getInstance();
            BufferBuilder var3 = var2.getBuilder();
            float var4 = 0.0F;
            MapRenderer.this.textureManager.bind(this.location);
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
            );
            GlStateManager.disableAlphaTest();
            var3.begin(7, DefaultVertexFormat.POSITION_TEX);
            var3.vertex(0.0, 128.0, -0.01F).uv(0.0, 1.0).endVertex();
            var3.vertex(128.0, 128.0, -0.01F).uv(1.0, 1.0).endVertex();
            var3.vertex(128.0, 0.0, -0.01F).uv(1.0, 0.0).endVertex();
            var3.vertex(0.0, 0.0, -0.01F).uv(0.0, 0.0).endVertex();
            var2.end();
            GlStateManager.enableAlphaTest();
            GlStateManager.disableBlend();
            int var5 = 0;

            for(MapDecoration var6 : this.data.decorations.values()) {
                if (!param0 || var6.renderOnFrame()) {
                    MapRenderer.this.textureManager.bind(MapRenderer.MAP_ICONS_LOCATION);
                    GlStateManager.pushMatrix();
                    GlStateManager.translatef(0.0F + (float)var6.getX() / 2.0F + 64.0F, 0.0F + (float)var6.getY() / 2.0F + 64.0F, -0.02F);
                    GlStateManager.rotatef((float)(var6.getRot() * 360) / 16.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.scalef(4.0F, 4.0F, 3.0F);
                    GlStateManager.translatef(-0.125F, 0.125F, 0.0F);
                    byte var7 = var6.getImage();
                    float var8 = (float)(var7 % 16 + 0) / 16.0F;
                    float var9 = (float)(var7 / 16 + 0) / 16.0F;
                    float var10 = (float)(var7 % 16 + 1) / 16.0F;
                    float var11 = (float)(var7 / 16 + 1) / 16.0F;
                    var3.begin(7, DefaultVertexFormat.POSITION_TEX);
                    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    float var12 = -0.001F;
                    var3.vertex(-1.0, 1.0, (double)((float)var5 * -0.001F)).uv((double)var8, (double)var9).endVertex();
                    var3.vertex(1.0, 1.0, (double)((float)var5 * -0.001F)).uv((double)var10, (double)var9).endVertex();
                    var3.vertex(1.0, -1.0, (double)((float)var5 * -0.001F)).uv((double)var10, (double)var11).endVertex();
                    var3.vertex(-1.0, -1.0, (double)((float)var5 * -0.001F)).uv((double)var8, (double)var11).endVertex();
                    var2.end();
                    GlStateManager.popMatrix();
                    if (var6.getName() != null) {
                        Font var13 = Minecraft.getInstance().font;
                        String var14 = var6.getName().getColoredString();
                        float var15 = (float)var13.width(var14);
                        float var16 = Mth.clamp(25.0F / var15, 0.0F, 6.0F / 9.0F);
                        GlStateManager.pushMatrix();
                        GlStateManager.translatef(
                            0.0F + (float)var6.getX() / 2.0F + 64.0F - var15 * var16 / 2.0F, 0.0F + (float)var6.getY() / 2.0F + 64.0F + 4.0F, -0.025F
                        );
                        GlStateManager.scalef(var16, var16, 1.0F);
                        GuiComponent.fill(-1, -1, (int)var15, 9 - 1, Integer.MIN_VALUE);
                        GlStateManager.translatef(0.0F, 0.0F, -0.1F);
                        var13.draw(var14, 0.0F, 0.0F, -1);
                        GlStateManager.popMatrix();
                    }

                    ++var5;
                }
            }

            GlStateManager.pushMatrix();
            GlStateManager.translatef(0.0F, 0.0F, -0.04F);
            GlStateManager.scalef(1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }

        @Override
        public void close() {
            this.texture.close();
        }
    }
}
