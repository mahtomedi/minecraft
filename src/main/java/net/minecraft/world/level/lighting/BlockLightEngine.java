package net.minecraft.world.level.lighting;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockLightEngine extends LayerLightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {
    private static final Direction[] DIRECTIONS = Direction.values();
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public BlockLightEngine(LightChunkGetter param0) {
        super(param0, LightLayer.BLOCK, new BlockLightSectionStorage(param0));
    }

    private int getLightEmission(long param0) {
        int var0 = BlockPos.getX(param0);
        int var1 = BlockPos.getY(param0);
        int var2 = BlockPos.getZ(param0);
        BlockGetter var3 = this.chunkSource.getChunkForLighting(var0 >> 4, var2 >> 4);
        return var3 != null ? var3.getLightEmission(this.pos.set(var0, var1, var2)) : 0;
    }

    @Override
    protected int computeLevelFromNeighbor(long param0, long param1, int param2) {
        if (param1 == Long.MAX_VALUE) {
            return 15;
        } else if (param0 == Long.MAX_VALUE) {
            return param2 + 15 - this.getLightEmission(param1);
        } else if (param2 >= 15) {
            return param2;
        } else {
            int var0 = Integer.signum(BlockPos.getX(param1) - BlockPos.getX(param0));
            int var1 = Integer.signum(BlockPos.getY(param1) - BlockPos.getY(param0));
            int var2 = Integer.signum(BlockPos.getZ(param1) - BlockPos.getZ(param0));
            Direction var3 = Direction.fromNormal(var0, var1, var2);
            if (var3 == null) {
                return 15;
            } else {
                AtomicInteger var4 = new AtomicInteger();
                BlockState var5 = this.getStateAndOpacity(param1, var4);
                if (var4.get() >= 15) {
                    return 15;
                } else {
                    BlockState var6 = this.getStateAndOpacity(param0, null);
                    VoxelShape var7 = this.getShape(var6, param0, var3);
                    VoxelShape var8 = this.getShape(var5, param1, var3.getOpposite());
                    return Shapes.faceShapeOccludes(var7, var8) ? 15 : param2 + Math.max(1, var4.get());
                }
            }
        }
    }

    @Override
    protected void checkNeighborsAfterUpdate(long param0, int param1, boolean param2) {
        long var0 = SectionPos.blockToSection(param0);

        for(Direction var1 : DIRECTIONS) {
            long var2 = BlockPos.offset(param0, var1);
            long var3 = SectionPos.blockToSection(var2);
            if (var0 == var3 || this.storage.storingLightForSection(var3)) {
                this.checkNeighbor(param0, var2, param1, param2);
            }
        }

    }

    @Override
    protected int getComputedLevel(long param0, long param1, int param2) {
        int var0 = param2;
        if (Long.MAX_VALUE != param1) {
            int var1 = this.computeLevelFromNeighbor(Long.MAX_VALUE, param0, 0);
            if (param2 > var1) {
                var0 = var1;
            }

            if (var0 == 0) {
                return var0;
            }
        }

        long var2 = SectionPos.blockToSection(param0);
        DataLayer var3 = this.storage.getDataLayer(var2, true);

        for(Direction var4 : DIRECTIONS) {
            long var5 = BlockPos.offset(param0, var4);
            if (var5 != param1) {
                long var6 = SectionPos.blockToSection(var5);
                DataLayer var7;
                if (var2 == var6) {
                    var7 = var3;
                } else {
                    var7 = this.storage.getDataLayer(var6, true);
                }

                if (var7 != null) {
                    int var9 = this.computeLevelFromNeighbor(var5, param0, this.getLevel(var7, var5));
                    if (var0 > var9) {
                        var0 = var9;
                    }

                    if (var0 == 0) {
                        return var0;
                    }
                }
            }
        }

        return var0;
    }

    @Override
    public void onBlockEmissionIncrease(BlockPos param0, int param1) {
        this.storage.runAllUpdates();
        this.checkEdge(Long.MAX_VALUE, param0.asLong(), 15 - param1, true);
    }
}
