package net.minecraft.world.level;

import com.google.common.collect.Streams;
import java.util.Collections;
import java.util.Set;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface LevelReader extends BlockAndBiomeGetter {
    default boolean isEmptyBlock(BlockPos param0) {
        return this.getBlockState(param0).isAir();
    }

    default boolean canSeeSkyFromBelowWater(BlockPos param0) {
        if (param0.getY() >= this.getSeaLevel()) {
            return this.canSeeSky(param0);
        } else {
            BlockPos var0 = new BlockPos(param0.getX(), this.getSeaLevel(), param0.getZ());
            if (!this.canSeeSky(var0)) {
                return false;
            } else {
                for(BlockPos var4 = var0.below(); var4.getY() > param0.getY(); var4 = var4.below()) {
                    BlockState var1 = this.getBlockState(var4);
                    if (var1.getLightBlock(this, var4) > 0 && !var1.getMaterial().isLiquid()) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    int getRawBrightness(BlockPos var1, int var2);

    @Nullable
    ChunkAccess getChunk(int var1, int var2, ChunkStatus var3, boolean var4);

    @Deprecated
    boolean hasChunk(int var1, int var2);

    BlockPos getHeightmapPos(Heightmap.Types var1, BlockPos var2);

    int getHeight(Heightmap.Types var1, int var2, int var3);

    default float getBrightness(BlockPos param0) {
        return this.getDimension().getBrightnessRamp()[this.getMaxLocalRawBrightness(param0)];
    }

    int getSkyDarken();

    WorldBorder getWorldBorder();

    boolean isUnobstructed(@Nullable Entity var1, VoxelShape var2);

    default int getDirectSignal(BlockPos param0, Direction param1) {
        return this.getBlockState(param0).getDirectSignal(this, param0, param1);
    }

    boolean isClientSide();

    int getSeaLevel();

    default ChunkAccess getChunk(BlockPos param0) {
        return this.getChunk(param0.getX() >> 4, param0.getZ() >> 4);
    }

    default ChunkAccess getChunk(int param0, int param1) {
        return this.getChunk(param0, param1, ChunkStatus.FULL, true);
    }

    default ChunkAccess getChunk(int param0, int param1, ChunkStatus param2) {
        return this.getChunk(param0, param1, param2, true);
    }

    default ChunkStatus statusForCollisions() {
        return ChunkStatus.EMPTY;
    }

    default boolean isUnobstructed(BlockState param0, BlockPos param1, CollisionContext param2) {
        VoxelShape var0 = param0.getCollisionShape(this, param1, param2);
        return var0.isEmpty() || this.isUnobstructed(null, var0.move((double)param1.getX(), (double)param1.getY(), (double)param1.getZ()));
    }

    default boolean isUnobstructed(Entity param0) {
        return this.isUnobstructed(param0, Shapes.create(param0.getBoundingBox()));
    }

    default boolean noCollision(AABB param0) {
        return this.noCollision(null, param0, Collections.emptySet());
    }

    default boolean noCollision(Entity param0) {
        return this.noCollision(param0, param0.getBoundingBox(), Collections.emptySet());
    }

    default boolean noCollision(Entity param0, AABB param1) {
        return this.noCollision(param0, param1, Collections.emptySet());
    }

    default boolean noCollision(@Nullable Entity param0, AABB param1, Set<Entity> param2) {
        return this.getCollisions(param0, param1, param2).allMatch(VoxelShape::isEmpty);
    }

    default Stream<VoxelShape> getEntityCollisions(@Nullable Entity param0, AABB param1, Set<Entity> param2) {
        return Stream.empty();
    }

    default Stream<VoxelShape> getCollisions(@Nullable Entity param0, AABB param1, Set<Entity> param2) {
        return Streams.concat(this.getBlockCollisions(param0, param1), this.getEntityCollisions(param0, param1, param2));
    }

    default Stream<VoxelShape> getBlockCollisions(@Nullable final Entity param0, AABB param1) {
        int var0 = Mth.floor(param1.minX - 1.0E-7) - 1;
        int var1 = Mth.floor(param1.maxX + 1.0E-7) + 1;
        int var2 = Mth.floor(param1.minY - 1.0E-7) - 1;
        int var3 = Mth.floor(param1.maxY + 1.0E-7) + 1;
        int var4 = Mth.floor(param1.minZ - 1.0E-7) - 1;
        int var5 = Mth.floor(param1.maxZ + 1.0E-7) + 1;
        final CollisionContext var6 = param0 == null ? CollisionContext.empty() : CollisionContext.of(param0);
        final Cursor3D var7 = new Cursor3D(var0, var2, var4, var1, var3, var5);
        final BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();
        final VoxelShape var9 = Shapes.create(param1);
        return StreamSupport.stream(new AbstractSpliterator<VoxelShape>(Long.MAX_VALUE, 1280) {
            boolean checkedBorder = param0 == null;

            @Override
            public boolean tryAdvance(Consumer<? super VoxelShape> param0x) {
                if (!this.checkedBorder) {
                    this.checkedBorder = true;
                    VoxelShape var0 = LevelReader.this.getWorldBorder().getCollisionShape();
                    boolean var1 = Shapes.joinIsNotEmpty(var0, Shapes.create(param0.getBoundingBox().deflate(1.0E-7)), BooleanOp.AND);
                    boolean var2 = Shapes.joinIsNotEmpty(var0, Shapes.create(param0.getBoundingBox().inflate(1.0E-7)), BooleanOp.AND);
                    if (!var1 && var2) {
                        param0.accept(var0);
                        return true;
                    }
                }

                while(var7.advance()) {
                    int var3 = var7.nextX();
                    int var4 = var7.nextY();
                    int var5 = var7.nextZ();
                    int var6 = var7.getNextType();
                    if (var6 != 3) {
                        int var7 = var3 >> 4;
                        int var8 = var5 >> 4;
                        ChunkAccess var9 = LevelReader.this.getChunk(var7, var8, LevelReader.this.statusForCollisions(), false);
                        if (var9 != null) {
                            var8.set(var3, var4, var5);
                            BlockState var10 = var9.getBlockState(var8);
                            if ((var6 != 1 || var10.hasLargeCollisionShape()) && (var6 != 2 || var10.getBlock() == Blocks.MOVING_PISTON)) {
                                VoxelShape var11 = var10.getCollisionShape(LevelReader.this, var8, var6);
                                VoxelShape var12 = var11.move((double)var3, (double)var4, (double)var5);
                                if (Shapes.joinIsNotEmpty(var9, var12, BooleanOp.AND)) {
                                    param0.accept(var12);
                                    return true;
                                }
                            }
                        }
                    }
                }

                return false;
            }
        }, false);
    }

    default boolean isWaterAt(BlockPos param0) {
        return this.getFluidState(param0).is(FluidTags.WATER);
    }

    default boolean containsAnyLiquid(AABB param0) {
        int var0 = Mth.floor(param0.minX);
        int var1 = Mth.ceil(param0.maxX);
        int var2 = Mth.floor(param0.minY);
        int var3 = Mth.ceil(param0.maxY);
        int var4 = Mth.floor(param0.minZ);
        int var5 = Mth.ceil(param0.maxZ);

        try (BlockPos.PooledMutableBlockPos var6 = BlockPos.PooledMutableBlockPos.acquire()) {
            for(int var7 = var0; var7 < var1; ++var7) {
                for(int var8 = var2; var8 < var3; ++var8) {
                    for(int var9 = var4; var9 < var5; ++var9) {
                        BlockState var10 = this.getBlockState(var6.set(var7, var8, var9));
                        if (!var10.getFluidState().isEmpty()) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }

    default int getMaxLocalRawBrightness(BlockPos param0) {
        return this.getMaxLocalRawBrightness(param0, this.getSkyDarken());
    }

    default int getMaxLocalRawBrightness(BlockPos param0, int param1) {
        return param0.getX() >= -30000000 && param0.getZ() >= -30000000 && param0.getX() < 30000000 && param0.getZ() < 30000000
            ? this.getRawBrightness(param0, param1)
            : 15;
    }

    @Deprecated
    default boolean hasChunkAt(BlockPos param0) {
        return this.hasChunk(param0.getX() >> 4, param0.getZ() >> 4);
    }

    @Deprecated
    default boolean hasChunksAt(BlockPos param0, BlockPos param1) {
        return this.hasChunksAt(param0.getX(), param0.getY(), param0.getZ(), param1.getX(), param1.getY(), param1.getZ());
    }

    @Deprecated
    default boolean hasChunksAt(int param0, int param1, int param2, int param3, int param4, int param5) {
        if (param4 >= 0 && param1 < 256) {
            param0 >>= 4;
            param2 >>= 4;
            param3 >>= 4;
            param5 >>= 4;

            for(int var0 = param0; var0 <= param3; ++var0) {
                for(int var1 = param2; var1 <= param5; ++var1) {
                    if (!this.hasChunk(var0, var1)) {
                        return false;
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    Dimension getDimension();
}
