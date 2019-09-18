package net.minecraft.world.level.lighting;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class SkyLightEngine extends LayerLightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, SkyLightSectionStorage> {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public SkyLightEngine(LightChunkGetter param0) {
        super(param0, LightLayer.SKY, new SkyLightSectionStorage(param0));
    }

    @Override
    protected int computeLevelFromNeighbor(long param0, long param1, int param2) {
        if (param1 == Long.MAX_VALUE) {
            return 15;
        } else {
            if (param0 == Long.MAX_VALUE) {
                if (!this.storage.hasLightSource(param1)) {
                    return 15;
                }

                param2 = 0;
            }

            if (param2 >= 15) {
                return param2;
            } else {
                AtomicInteger var0 = new AtomicInteger();
                BlockState var1 = this.getStateAndOpacity(param1, var0);
                if (var0.get() >= 15) {
                    return 15;
                } else {
                    int var2 = BlockPos.getX(param0);
                    int var3 = BlockPos.getY(param0);
                    int var4 = BlockPos.getZ(param0);
                    int var5 = BlockPos.getX(param1);
                    int var6 = BlockPos.getY(param1);
                    int var7 = BlockPos.getZ(param1);
                    boolean var8 = var2 == var5 && var4 == var7;
                    int var9 = Integer.signum(var5 - var2);
                    int var10 = Integer.signum(var6 - var3);
                    int var11 = Integer.signum(var7 - var4);
                    Direction var12;
                    if (param0 == Long.MAX_VALUE) {
                        var12 = Direction.DOWN;
                    } else {
                        var12 = Direction.fromNormal(var9, var10, var11);
                    }

                    BlockState var14 = this.getStateAndOpacity(param0, null);
                    if (var12 != null) {
                        VoxelShape var15 = this.getShape(var14, param0, var12);
                        VoxelShape var16 = this.getShape(var1, param1, var12.getOpposite());
                        if (Shapes.faceShapeOccludes(var15, var16)) {
                            return 15;
                        }
                    } else {
                        VoxelShape var17 = this.getShape(var14, param0, Direction.DOWN);
                        if (Shapes.faceShapeOccludes(var17, Shapes.empty())) {
                            return 15;
                        }

                        int var18 = var8 ? -1 : 0;
                        Direction var19 = Direction.fromNormal(var9, var18, var11);
                        if (var19 == null) {
                            return 15;
                        }

                        VoxelShape var20 = this.getShape(var1, param1, var19.getOpposite());
                        if (Shapes.faceShapeOccludes(Shapes.empty(), var20)) {
                            return 15;
                        }
                    }

                    boolean var21 = param0 == Long.MAX_VALUE || var8 && var3 > var6;
                    return var21 && param2 == 0 && var0.get() == 0 ? 0 : param2 + Math.max(1, var0.get());
                }
            }
        }
    }

    @Override
    protected void checkNeighborsAfterUpdate(long param0, int param1, boolean param2) {
        long var0 = SectionPos.blockToSection(param0);
        int var1 = BlockPos.getY(param0);
        int var2 = SectionPos.sectionRelative(var1);
        int var3 = SectionPos.blockToSectionCoord(var1);
        int var4;
        if (var2 != 0) {
            var4 = 0;
        } else {
            int var5 = 0;

            while(!this.storage.storingLightForSection(SectionPos.offset(var0, 0, -var5 - 1, 0)) && this.storage.hasSectionsBelow(var3 - var5 - 1)) {
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
                    this.checkNeighbor(param0, var13, param1, param2);
                }
            } while(++var12 > var4 * 16);
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
            long var6 = SectionPos.blockToSection(var5);
            DataLayer var7;
            if (var2 == var6) {
                var7 = var3;
            } else {
                var7 = this.storage.getDataLayer(var6, true);
            }

            if (var7 != null) {
                if (var5 != param1) {
                    int var9 = this.computeLevelFromNeighbor(var5, param0, this.getLevel(var7, var5));
                    if (var0 > var9) {
                        var0 = var9;
                    }

                    if (var0 == 0) {
                        return var0;
                    }
                }
            } else if (var4 != Direction.DOWN) {
                for(var5 = BlockPos.getFlatIndex(var5);
                    !this.storage.storingLightForSection(var6) && !this.storage.isAboveData(var6);
                    var5 = BlockPos.offset(var5, 0, 16, 0)
                ) {
                    var6 = SectionPos.offset(var6, Direction.UP);
                }

                DataLayer var10 = this.storage.getDataLayer(var6, true);
                if (var5 != param1) {
                    int var11;
                    if (var10 != null) {
                        var11 = this.computeLevelFromNeighbor(var5, param0, this.getLevel(var10, var5));
                    } else {
                        var11 = this.storage.lightOnInSection(var6) ? 0 : 15;
                    }

                    if (var0 > var11) {
                        var0 = var11;
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
}
