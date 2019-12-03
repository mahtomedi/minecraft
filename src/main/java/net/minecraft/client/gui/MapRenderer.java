package net.minecraft.client.gui;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
    private static final RenderType MAP_ICONS = RenderType.text(MAP_ICONS_LOCATION);
    private final TextureManager textureManager;
    private final Map<String, MapRenderer.MapInstance> maps = Maps.newHashMap();

    public MapRenderer(TextureManager param0) {
        this.textureManager = param0;
    }

    public void update(MapItemSavedData param0) {
        this.getMapInstance(param0).updateTexture();
    }

    public void render(PoseStack param0, MultiBufferSource param1, MapItemSavedData param2, boolean param3, int param4) {
        this.getMapInstance(param2).draw(param0, param1, param3, param4);
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
        private final RenderType renderType;

        private MapInstance(MapItemSavedData param0) {
            this.data = param0;
            this.texture = new DynamicTexture(128, 128, true);
            ResourceLocation param1 = MapRenderer.this.textureManager.register("map/" + param0.getId(), this.texture);
            this.renderType = RenderType.text(param1);
        }

        private void updateTexture() {
            for(int var0 = 0; var0 < 128; ++var0) {
                for(int var1 = 0; var1 < 128; ++var1) {
                    int var2 = var1 + var0 * 128;
                    int var3 = this.data.colors[var2] & 255;
                    if (var3 / 4 == 0) {
                        this.texture.getPixels().setPixelRGBA(var1, var0, 0);
                    } else {
                        this.texture.getPixels().setPixelRGBA(var1, var0, MaterialColor.MATERIAL_COLORS[var3 / 4].calculateRGBColor(var3 & 3));
                    }
                }
            }

            this.texture.upload();
        }

        private void draw(PoseStack param0, MultiBufferSource param1, boolean param2, int param3) {
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

            for(MapDecoration var6 : this.data.decorations.values()) {
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
                        String var16 = var6.getName().getColoredString();
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
