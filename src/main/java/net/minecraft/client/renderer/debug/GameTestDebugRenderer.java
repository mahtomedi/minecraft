package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GameTestDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private static final float PADDING = 0.02F;
    private final Map<BlockPos, GameTestDebugRenderer.Marker> markers = Maps.newHashMap();

    public void addMarker(BlockPos param0, int param1, String param2, int param3) {
        this.markers.put(param0, new GameTestDebugRenderer.Marker(param1, param2, Util.getMillis() + (long)param3));
    }

    @Override
    public void clear() {
        this.markers.clear();
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        long var0 = Util.getMillis();
        this.markers.entrySet().removeIf(param1x -> var0 > param1x.getValue().removeAtTime);
        this.markers.forEach(this::renderMarker);
    }

    private void renderMarker(BlockPos param0x, GameTestDebugRenderer.Marker param1x) {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        RenderSystem.setShaderColor(0.0F, 1.0F, 0.0F, 0.75F);
        RenderSystem.disableTexture();
        DebugRenderer.renderFilledBox(param0x, 0.02F, param1x.getR(), param1x.getG(), param1x.getB(), param1x.getA());
        if (!param1x.text.isEmpty()) {
            double var0x = (double)param0x.getX() + 0.5;
            double var1 = (double)param0x.getY() + 1.2;
            double var2 = (double)param0x.getZ() + 0.5;
            DebugRenderer.renderFloatingText(param1x.text, var0x, var1, var2, -1, 0.01F, true, 0.0F, true);
        }

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    @OnlyIn(Dist.CLIENT)
    static class Marker {
        public int color;
        public String text;
        public long removeAtTime;

        public Marker(int param0, String param1, long param2) {
            this.color = param0;
            this.text = param1;
            this.removeAtTime = param2;
        }

        public float getR() {
            return (float)(this.color >> 16 & 0xFF) / 255.0F;
        }

        public float getG() {
            return (float)(this.color >> 8 & 0xFF) / 255.0F;
        }

        public float getB() {
            return (float)(this.color & 0xFF) / 255.0F;
        }

        public float getA() {
            return (float)(this.color >> 24 & 0xFF) / 255.0F;
        }
    }
}
