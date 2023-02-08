package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
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
        this.markers.forEach((param2x, param3x) -> this.renderMarker(param0, param1, param2x, param3x));
    }

    private void renderMarker(PoseStack param0, MultiBufferSource param1, BlockPos param2, GameTestDebugRenderer.Marker param3) {
        DebugRenderer.renderFilledBox(param0, param1, param2, 0.02F, param3.getR(), param3.getG(), param3.getB(), param3.getA() * 0.75F);
        if (!param3.text.isEmpty()) {
            double var0 = (double)param2.getX() + 0.5;
            double var1 = (double)param2.getY() + 1.2;
            double var2 = (double)param2.getZ() + 0.5;
            DebugRenderer.renderFloatingText(param0, param1, param3.text, var0, var1, var2, -1, 0.01F, true, 0.0F, true);
        }

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
