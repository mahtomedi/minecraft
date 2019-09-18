package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GameTestDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Map<BlockPos, GameTestDebugRenderer.Marker> markers = Maps.newHashMap();

    public void addMarker(BlockPos param0, int param1, String param2, int param3) {
        this.markers.put(param0, new GameTestDebugRenderer.Marker(param1, param2, Util.getMillis() + (long)param3));
    }

    @Override
    public void clear() {
        this.markers.clear();
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
    }
}
