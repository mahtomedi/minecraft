package net.minecraft.world.level.lighting;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public final class BlockLightEngine extends LayerLightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {
    private static final Direction[] DIRECTIONS = Direction.values();
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public BlockLightEngine(LightChunkGetter param0) {
        this(param0, new BlockLightSectionStorage(param0));
    }

    @VisibleForTesting
    public BlockLightEngine(LightChunkGetter param0, BlockLightSectionStorage param1) {
        super(param0, param1);
    }

    private int getLightEmission(long param0) {
        return this.getState(this.pos.set(param0)).getLightEmission();
    }

    @Override
    protected int computeLevelFromNeighbor(long param0, long param1, int param2) {
        if (this.isSource(param1)) {
            return 15;
        } else if (this.isSource(param0)) {
            return param2 + 15 - this.getLightEmission(param1);
        } else if (param2 >= 14) {
            return 15;
        } else {
            this.pos.set(param1);
            BlockState var0 = this.getState(this.pos);
            int var1 = this.getOpacity(var0, this.pos);
            if (var1 >= 15) {
                return 15;
            } else {
                Direction var2 = getDirection(param0, param1);
                if (var2 == null) {
                    return 15;
                } else {
                    this.pos.set(param0);
                    BlockState var3 = this.getState(this.pos);
                    return this.shapeOccludes(param0, var3, param1, var0, var2) ? 15 : param2 + Math.max(1, var1);
                }
            }
        }
    }

    @Override
    protected void checkNeighborsAfterUpdate(long param0, int param1, boolean param2) {
        if (!param2 || param1 < this.levelCount - 2) {
            long var0 = SectionPos.blockToSection(param0);

            for(Direction var1 : DIRECTIONS) {
                long var2 = BlockPos.offset(param0, var1);
                long var3 = SectionPos.blockToSection(var2);
                if (var0 == var3 || this.storage.storingLightForSection(var3)) {
                    this.checkNeighbor(param0, var2, param1, param2);
                }
            }

        }
    }

    @Override
    protected int getComputedLevel(long param0, long param1, int param2) {
        int var0 = param2;
        if (!this.isSource(param1)) {
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
                    int var9 = this.getLevel(var7, var5);
                    if (var9 + 1 < var0) {
                        int var10 = this.computeLevelFromNeighbor(var5, param0, var9);
                        if (var0 > var10) {
                            var0 = var10;
                        }

                        if (var0 == 0) {
                            return var0;
                        }
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
