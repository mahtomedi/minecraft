package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import net.minecraft.world.level.ChunkPos;

public interface ChunkTrackingView {
    ChunkTrackingView EMPTY = new ChunkTrackingView() {
        @Override
        public boolean contains(int param0, int param1, boolean param2) {
            return false;
        }

        @Override
        public void forEach(Consumer<ChunkPos> param0) {
        }
    };

    static ChunkTrackingView of(ChunkPos param0, int param1) {
        return new ChunkTrackingView.Positioned(param0, param1);
    }

    static void difference(ChunkTrackingView param0, ChunkTrackingView param1, Consumer<ChunkPos> param2, Consumer<ChunkPos> param3) {
        if (!param0.equals(param1)) {
            if (param0 instanceof ChunkTrackingView.Positioned var0 && param1 instanceof ChunkTrackingView.Positioned var1 && var0.squareIntersects(var1)) {
                int var2 = Math.min(var0.minX(), var1.minX());
                int var3 = Math.min(var0.minZ(), var1.minZ());
                int var4 = Math.max(var0.maxX(), var1.maxX());
                int var5 = Math.max(var0.maxZ(), var1.maxZ());

                for(int var6 = var2; var6 <= var4; ++var6) {
                    for(int var7 = var3; var7 <= var5; ++var7) {
                        boolean var8 = var0.contains(var6, var7);
                        boolean var9 = var1.contains(var6, var7);
                        if (var8 != var9) {
                            if (var9) {
                                param2.accept(new ChunkPos(var6, var7));
                            } else {
                                param3.accept(new ChunkPos(var6, var7));
                            }
                        }
                    }
                }

                return;
            }

            param0.forEach(param3);
            param1.forEach(param2);
        }
    }

    default boolean contains(ChunkPos param0) {
        return this.contains(param0.x, param0.z);
    }

    default boolean contains(int param0, int param1) {
        return this.contains(param0, param1, true);
    }

    boolean contains(int var1, int var2, boolean var3);

    void forEach(Consumer<ChunkPos> var1);

    default boolean isInViewDistance(int param0, int param1) {
        return this.contains(param0, param1, false);
    }

    static boolean isInViewDistance(int param0, int param1, int param2, int param3, int param4) {
        return isWithinDistance(param0, param1, param2, param3, param4, false);
    }

    static boolean isWithinDistance(int param0, int param1, int param2, int param3, int param4, boolean param5) {
        int var0 = Math.max(0, Math.abs(param3 - param0) - 1);
        int var1 = Math.max(0, Math.abs(param4 - param1) - 1);
        long var2 = (long)Math.max(0, Math.max(var0, var1) - (param5 ? 1 : 0));
        long var3 = (long)Math.min(var0, var1);
        long var4 = var3 * var3 + var2 * var2;
        int var5 = param2 * param2;
        return var4 < (long)var5;
    }

    public static record Positioned(ChunkPos center, int viewDistance) implements ChunkTrackingView {
        int minX() {
            return this.center.x - this.viewDistance - 1;
        }

        int minZ() {
            return this.center.z - this.viewDistance - 1;
        }

        int maxX() {
            return this.center.x + this.viewDistance + 1;
        }

        int maxZ() {
            return this.center.z + this.viewDistance + 1;
        }

        @VisibleForTesting
        protected boolean squareIntersects(ChunkTrackingView.Positioned param0) {
            return this.minX() <= param0.maxX() && this.maxX() >= param0.minX() && this.minZ() <= param0.maxZ() && this.maxZ() >= param0.minZ();
        }

        @Override
        public boolean contains(int param0, int param1, boolean param2) {
            return ChunkTrackingView.isWithinDistance(this.center.x, this.center.z, this.viewDistance, param0, param1, param2);
        }

        @Override
        public void forEach(Consumer<ChunkPos> param0) {
            for(int var0 = this.minX(); var0 <= this.maxX(); ++var0) {
                for(int var1 = this.minZ(); var1 <= this.maxZ(); ++var1) {
                    if (this.contains(var0, var1)) {
                        param0.accept(new ChunkPos(var0, var1));
                    }
                }
            }

        }
    }
}
