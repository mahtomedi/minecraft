package net.minecraft.world.level.lighting;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public final class SkyLightEngine extends LayerLightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, SkyLightSectionStorage> {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public SkyLightEngine(LightChunkGetter param0) {
        super(param0, new SkyLightSectionStorage(param0));
    }

    @Override
    protected int computeLevelFromNeighbor(long param0, long param1, int param2) {
        if (this.isSource(param1) || this.isSource(param0)) {
            return 15;
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
                    throw new IllegalStateException(
                        String.format(
                            Locale.ROOT,
                            "Light was spread in illegal direction. From %d, %d, %d to %d, %d, %d",
                            BlockPos.getX(param0),
                            BlockPos.getY(param0),
                            BlockPos.getZ(param0),
                            BlockPos.getX(param1),
                            BlockPos.getY(param1),
                            BlockPos.getZ(param1)
                        )
                    );
                } else {
                    this.pos.set(param0);
                    BlockState var3 = this.getState(this.pos);
                    if (this.shapeOccludes(param0, var3, param1, var0, var2)) {
                        return 15;
                    } else {
                        boolean var4 = var2 == Direction.DOWN;
                        return var4 && param2 == 0 && var1 == 0 ? 0 : param2 + Math.max(1, var1);
                    }
                }
            }
        }
    }

    @Override
    protected void checkNeighborsAfterUpdate(long param0, int param1, boolean param2) {
        if (!param2 || param1 < this.levelCount - 2) {
            long var0 = SectionPos.blockToSection(param0);
            int var1 = BlockPos.getY(param0);
            int var2 = SectionPos.sectionRelative(var1);
            int var3 = SectionPos.blockToSectionCoord(var1);
            int var4;
            if (var2 != 0) {
                var4 = 0;
            } else {
                int var5 = 0;

                while(!this.storage.storingLightForSection(SectionPos.offset(var0, 0, -var5 - 1, 0)) && this.storage.hasLightDataAtOrBelow(var3 - var5 - 1)) {
                    ++var5;
                }

                var4 = var5;
            }

            long var7 = BlockPos.offset(param0, 0, -1 - var4 * 16, 0);
            long var8 = SectionPos.blockToSection(var7);
            if (var0 == var8 || this.storage.storingLightForSection(var8)) {
                this.checkNeighbor(param0, var7, param1, param2);
            }

            long var9 = BlockPos.offset(param0, Direction.UP);
            long var10 = SectionPos.blockToSection(var9);
            if (var0 == var10 || this.storage.storingLightForSection(var10)) {
                this.checkNeighbor(param0, var9, param1, param2);
            }

            for(Direction var11 : HORIZONTALS) {
                int var12 = 0;

                do {
                    long var13 = BlockPos.offset(param0, var11.getStepX(), -var12, var11.getStepZ());
                    long var14 = SectionPos.blockToSection(var13);
                    if (var0 == var14) {
                        this.checkNeighbor(param0, var13, param1, param2);
                        break;
                    }

                    if (this.storage.storingLightForSection(var14)) {
                        long var15 = BlockPos.offset(param0, 0, -var12, 0);
                        this.checkNeighbor(var15, var13, param1, param2);
                    }
                } while(++var12 > var4 * 16);
            }

        }
    }

    @Override
    protected int getComputedLevel(long param0, long param1, int param2) {
        int var0 = param2;
        long var1 = SectionPos.blockToSection(param0);
        DataLayer var2 = this.storage.getDataLayer(var1, true);

        for(Direction var3 : DIRECTIONS) {
            long var4 = BlockPos.offset(param0, var3);
            if (var4 != param1) {
                long var5 = SectionPos.blockToSection(var4);
                DataLayer var6;
                if (var1 == var5) {
                    var6 = var2;
                } else {
                    var6 = this.storage.getDataLayer(var5, true);
                }

                int var8;
                if (var6 != null) {
                    var8 = this.getLevel(var6, var4);
                } else {
                    if (var3 == Direction.DOWN) {
                        continue;
                    }

                    var8 = 15 - this.storage.getLightValue(var4, true);
                }

                if (var8 + 1 < var0 || var8 == 0 && var3 == Direction.UP) {
                    int var10 = this.computeLevelFromNeighbor(var4, param0, var8);
                    if (var0 > var10) {
                        var0 = var10;
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
    protected void checkNode(long param0) {
        this.storage.runAllUpdates();
        long var0 = SectionPos.blockToSection(param0);
        if (this.storage.storingLightForSection(var0)) {
            super.checkNode(param0);
        } else {
            for(param0 = BlockPos.getFlatIndex(param0);
                !this.storage.storingLightForSection(var0) && !this.storage.isAboveData(var0);
                param0 = BlockPos.offset(param0, 0, 16, 0)
            ) {
                var0 = SectionPos.offset(var0, Direction.UP);
            }

            if (this.storage.storingLightForSection(var0)) {
                super.checkNode(param0);
            }
        }

    }

    @Override
    public String getDebugData(long param0) {
        return super.getDebugData(param0) + (this.storage.isAboveData(param0) ? "*" : "");
    }
}
