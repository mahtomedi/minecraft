package net.minecraft.world.level.block.state.pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelReader;

public class BlockPattern {
    private final Predicate<BlockInWorld>[][][] pattern;
    private final int depth;
    private final int height;
    private final int width;

    public BlockPattern(Predicate<BlockInWorld>[][][] param0) {
        this.pattern = param0;
        this.depth = param0.length;
        if (this.depth > 0) {
            this.height = param0[0].length;
            if (this.height > 0) {
                this.width = param0[0][0].length;
            } else {
                this.width = 0;
            }
        } else {
            this.height = 0;
            this.width = 0;
        }

    }

    public int getDepth() {
        return this.depth;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    @VisibleForTesting
    public Predicate<BlockInWorld>[][][] getPattern() {
        return this.pattern;
    }

    @Nullable
    @VisibleForTesting
    public BlockPattern.BlockPatternMatch matches(LevelReader param0, BlockPos param1, Direction param2, Direction param3) {
        LoadingCache<BlockPos, BlockInWorld> var0 = createLevelCache(param0, false);
        return this.matches(param1, param2, param3, var0);
    }

    @Nullable
    private BlockPattern.BlockPatternMatch matches(BlockPos param0, Direction param1, Direction param2, LoadingCache<BlockPos, BlockInWorld> param3) {
        for(int var0 = 0; var0 < this.width; ++var0) {
            for(int var1 = 0; var1 < this.height; ++var1) {
                for(int var2 = 0; var2 < this.depth; ++var2) {
                    if (!this.pattern[var2][var1][var0].test(param3.getUnchecked(translateAndRotate(param0, param1, param2, var0, var1, var2)))) {
                        return null;
                    }
                }
            }
        }

        return new BlockPattern.BlockPatternMatch(param0, param1, param2, param3, this.width, this.height, this.depth);
    }

    @Nullable
    public BlockPattern.BlockPatternMatch find(LevelReader param0, BlockPos param1) {
        LoadingCache<BlockPos, BlockInWorld> var0 = createLevelCache(param0, false);
        int var1 = Math.max(Math.max(this.width, this.height), this.depth);

        for(BlockPos var2 : BlockPos.betweenClosed(param1, param1.offset(var1 - 1, var1 - 1, var1 - 1))) {
            for(Direction var3 : Direction.values()) {
                for(Direction var4 : Direction.values()) {
                    if (var4 != var3 && var4 != var3.getOpposite()) {
                        BlockPattern.BlockPatternMatch var5 = this.matches(var2, var3, var4, var0);
                        if (var5 != null) {
                            return var5;
                        }
                    }
                }
            }
        }

        return null;
    }

    public static LoadingCache<BlockPos, BlockInWorld> createLevelCache(LevelReader param0, boolean param1) {
        return CacheBuilder.newBuilder().build(new BlockPattern.BlockCacheLoader(param0, param1));
    }

    protected static BlockPos translateAndRotate(BlockPos param0, Direction param1, Direction param2, int param3, int param4, int param5) {
        if (param1 != param2 && param1 != param2.getOpposite()) {
            Vec3i var0 = new Vec3i(param1.getStepX(), param1.getStepY(), param1.getStepZ());
            Vec3i var1 = new Vec3i(param2.getStepX(), param2.getStepY(), param2.getStepZ());
            Vec3i var2 = var0.cross(var1);
            return param0.offset(
                var1.getX() * -param4 + var2.getX() * param3 + var0.getX() * param5,
                var1.getY() * -param4 + var2.getY() * param3 + var0.getY() * param5,
                var1.getZ() * -param4 + var2.getZ() * param3 + var0.getZ() * param5
            );
        } else {
            throw new IllegalArgumentException("Invalid forwards & up combination");
        }
    }

    static class BlockCacheLoader extends CacheLoader<BlockPos, BlockInWorld> {
        private final LevelReader level;
        private final boolean loadChunks;

        public BlockCacheLoader(LevelReader param0, boolean param1) {
            this.level = param0;
            this.loadChunks = param1;
        }

        public BlockInWorld load(BlockPos param0) {
            return new BlockInWorld(this.level, param0, this.loadChunks);
        }
    }

    public static class BlockPatternMatch {
        private final BlockPos frontTopLeft;
        private final Direction forwards;
        private final Direction up;
        private final LoadingCache<BlockPos, BlockInWorld> cache;
        private final int width;
        private final int height;
        private final int depth;

        public BlockPatternMatch(
            BlockPos param0, Direction param1, Direction param2, LoadingCache<BlockPos, BlockInWorld> param3, int param4, int param5, int param6
        ) {
            this.frontTopLeft = param0;
            this.forwards = param1;
            this.up = param2;
            this.cache = param3;
            this.width = param4;
            this.height = param5;
            this.depth = param6;
        }

        public BlockPos getFrontTopLeft() {
            return this.frontTopLeft;
        }

        public Direction getForwards() {
            return this.forwards;
        }

        public Direction getUp() {
            return this.up;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

        public int getDepth() {
            return this.depth;
        }

        public BlockInWorld getBlock(int param0, int param1, int param2) {
            return this.cache.getUnchecked(BlockPattern.translateAndRotate(this.frontTopLeft, this.getForwards(), this.getUp(), param0, param1, param2));
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("up", this.up).add("forwards", this.forwards).add("frontTopLeft", this.frontTopLeft).toString();
        }
    }
}
