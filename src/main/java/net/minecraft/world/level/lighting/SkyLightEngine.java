package net.minecraft.world.level.lighting;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.jetbrains.annotations.VisibleForTesting;

public final class SkyLightEngine extends LightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, SkyLightSectionStorage> {
    private static final long REMOVE_TOP_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseAllDirections(15);
    private static final long REMOVE_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseSkipOneDirection(15, Direction.UP);
    private static final long ADD_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.increaseSkipOneDirection(15, false, Direction.UP);
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    private final ChunkSkyLightSources emptyChunkSources;

    public SkyLightEngine(LightChunkGetter param0) {
        this(param0, new SkyLightSectionStorage(param0));
    }

    @VisibleForTesting
    protected SkyLightEngine(LightChunkGetter param0, SkyLightSectionStorage param1) {
        super(param0, param1);
        this.emptyChunkSources = new ChunkSkyLightSources(param0.getLevel());
    }

    private static boolean isSourceLevel(int param0) {
        return param0 == 15;
    }

    private int getLowestSourceY(int param0, int param1, int param2) {
        ChunkSkyLightSources var0 = this.getChunkSources(SectionPos.blockToSectionCoord(param0), SectionPos.blockToSectionCoord(param1));
        return var0 == null ? param2 : var0.getLowestSourceY(SectionPos.sectionRelative(param0), SectionPos.sectionRelative(param1));
    }

    @Nullable
    private ChunkSkyLightSources getChunkSources(int param0, int param1) {
        LightChunk var0 = this.chunkSource.getChunkForLighting(param0, param1);
        return var0 != null ? var0.getSkyLightSources() : null;
    }

    @Override
    protected void checkNode(long param0) {
        int var0 = BlockPos.getX(param0);
        int var1 = BlockPos.getY(param0);
        int var2 = BlockPos.getZ(param0);
        long var3 = SectionPos.blockToSection(param0);
        int var4 = this.storage.lightOnInSection(var3) ? this.getLowestSourceY(var0, var2, Integer.MAX_VALUE) : Integer.MAX_VALUE;
        if (var4 != Integer.MAX_VALUE) {
            this.updateSourcesInColumn(var0, var2, var4);
        }

        if (this.storage.storingLightForSection(var3)) {
            boolean var5 = var1 >= var4;
            if (var5) {
                this.enqueueDecrease(param0, REMOVE_SKY_SOURCE_ENTRY);
                this.enqueueIncrease(param0, ADD_SKY_SOURCE_ENTRY);
            } else {
                int var6 = this.storage.getStoredLevel(param0);
                if (var6 > 0) {
                    this.storage.setStoredLevel(param0, 0);
                    this.enqueueDecrease(param0, LightEngine.QueueEntry.decreaseAllDirections(var6));
                } else {
                    this.enqueueDecrease(param0, PULL_LIGHT_IN_ENTRY);
                }
            }

        }
    }

    private void updateSourcesInColumn(int param0, int param1, int param2) {
        int var0 = SectionPos.sectionToBlockCoord(this.storage.getBottomSectionY());
        this.removeSourcesBelow(param0, param1, param2, var0);
        this.addSourcesAbove(param0, param1, param2, var0);
    }

    private void removeSourcesBelow(int param0, int param1, int param2, int param3) {
        if (param2 > param3) {
            int var0 = SectionPos.blockToSectionCoord(param0);
            int var1 = SectionPos.blockToSectionCoord(param1);
            int var2 = param2 - 1;

            for(int var3 = SectionPos.blockToSectionCoord(var2); this.storage.hasLightDataAtOrBelow(var3); --var3) {
                if (this.storage.storingLightForSection(SectionPos.asLong(var0, var3, var1))) {
                    int var4 = SectionPos.sectionToBlockCoord(var3);
                    int var5 = var4 + 15;

                    for(int var6 = Math.min(var5, var2); var6 >= var4; --var6) {
                        long var7 = BlockPos.asLong(param0, var6, param1);
                        if (!isSourceLevel(this.storage.getStoredLevel(var7))) {
                            return;
                        }

                        this.storage.setStoredLevel(var7, 0);
                        this.enqueueDecrease(var7, var6 == param2 - 1 ? REMOVE_TOP_SKY_SOURCE_ENTRY : REMOVE_SKY_SOURCE_ENTRY);
                    }
                }
            }

        }
    }

    private void addSourcesAbove(int param0, int param1, int param2, int param3) {
        int var0 = SectionPos.blockToSectionCoord(param0);
        int var1 = SectionPos.blockToSectionCoord(param1);
        int var2 = Math.max(
            Math.max(this.getLowestSourceY(param0 - 1, param1, Integer.MIN_VALUE), this.getLowestSourceY(param0 + 1, param1, Integer.MIN_VALUE)),
            Math.max(this.getLowestSourceY(param0, param1 - 1, Integer.MIN_VALUE), this.getLowestSourceY(param0, param1 + 1, Integer.MIN_VALUE))
        );
        int var3 = Math.max(param2, param3);

        for(long var4 = SectionPos.asLong(var0, SectionPos.blockToSectionCoord(var3), var1);
            !this.storage.isAboveData(var4);
            var4 = SectionPos.offset(var4, Direction.UP)
        ) {
            if (this.storage.storingLightForSection(var4)) {
                int var5 = SectionPos.sectionToBlockCoord(SectionPos.y(var4));
                int var6 = var5 + 15;

                for(int var7 = Math.max(var5, var3); var7 <= var6; ++var7) {
                    long var8 = BlockPos.asLong(param0, var7, param1);
                    if (isSourceLevel(this.storage.getStoredLevel(var8))) {
                        return;
                    }

                    this.storage.setStoredLevel(var8, 15);
                    if (var7 < var2 || var7 == param2) {
                        this.enqueueIncrease(var8, ADD_SKY_SOURCE_ENTRY);
                    }
                }
            }
        }

    }

    @Override
    protected void propagateIncrease(long param0, long param1, int param2) {
        BlockState var0 = null;
        int var1 = this.countEmptySectionsBelowIfAtBorder(param0);

        for(Direction var2 : PROPAGATION_DIRECTIONS) {
            if (LightEngine.QueueEntry.shouldPropagateInDirection(param1, var2)) {
                long var3 = BlockPos.offset(param0, var2);
                if (this.storage.storingLightForSection(SectionPos.blockToSection(var3))) {
                    int var4 = this.storage.getStoredLevel(var3);
                    int var5 = param2 - 1;
                    if (var5 > var4) {
                        this.mutablePos.set(var3);
                        BlockState var6 = this.getState(this.mutablePos);
                        int var7 = param2 - this.getOpacity(var6, this.mutablePos);
                        if (var7 > var4) {
                            if (var0 == null) {
                                var0 = LightEngine.QueueEntry.isFromEmptyShape(param1)
                                    ? Blocks.AIR.defaultBlockState()
                                    : this.getState(this.mutablePos.set(param0));
                            }

                            if (!this.shapeOccludes(param0, var0, var3, var6, var2)) {
                                this.storage.setStoredLevel(var3, var7);
                                if (var7 > 1) {
                                    this.enqueueIncrease(var3, LightEngine.QueueEntry.increaseSkipOneDirection(var7, isEmptyShape(var6), var2.getOpposite()));
                                }

                                this.propagateFromEmptySections(var3, var2, var7, true, var1);
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    protected void propagateDecrease(long param0, long param1) {
        int var0 = this.countEmptySectionsBelowIfAtBorder(param0);
        int var1 = LightEngine.QueueEntry.getFromLevel(param1);

        for(Direction var2 : PROPAGATION_DIRECTIONS) {
            if (LightEngine.QueueEntry.shouldPropagateInDirection(param1, var2)) {
                long var3 = BlockPos.offset(param0, var2);
                if (this.storage.storingLightForSection(SectionPos.blockToSection(var3))) {
                    int var4 = this.storage.getStoredLevel(var3);
                    if (var4 != 0) {
                        if (var4 <= var1 - 1) {
                            this.storage.setStoredLevel(var3, 0);
                            this.enqueueDecrease(var3, LightEngine.QueueEntry.decreaseSkipOneDirection(var4, var2.getOpposite()));
                            this.propagateFromEmptySections(var3, var2, var4, false, var0);
                        } else {
                            this.enqueueIncrease(var3, LightEngine.QueueEntry.increaseOnlyOneDirection(var4, false, var2.getOpposite()));
                        }
                    }
                }
            }
        }

    }

    private int countEmptySectionsBelowIfAtBorder(long param0) {
        int var0 = BlockPos.getY(param0);
        int var1 = SectionPos.sectionRelative(var0);
        if (var1 != 0) {
            return 0;
        } else {
            int var2 = BlockPos.getX(param0);
            int var3 = BlockPos.getZ(param0);
            int var4 = SectionPos.sectionRelative(var2);
            int var5 = SectionPos.sectionRelative(var3);
            if (var4 != 0 && var4 != 15 && var5 != 0 && var5 != 15) {
                return 0;
            } else {
                int var6 = SectionPos.blockToSectionCoord(var2);
                int var7 = SectionPos.blockToSectionCoord(var0);
                int var8 = SectionPos.blockToSectionCoord(var3);
                int var9 = 0;

                while(
                    !this.storage.storingLightForSection(SectionPos.asLong(var6, var7 - var9 - 1, var8)) && this.storage.hasLightDataAtOrBelow(var7 - var9 - 1)
                ) {
                    ++var9;
                }

                return var9;
            }
        }
    }

    private void propagateFromEmptySections(long param0, Direction param1, int param2, boolean param3, int param4) {
        if (param4 != 0) {
            int var0 = BlockPos.getX(param0);
            int var1 = BlockPos.getZ(param0);
            if (crossedSectionEdge(param1, SectionPos.sectionRelative(var0), SectionPos.sectionRelative(var1))) {
                int var2 = BlockPos.getY(param0);
                int var3 = SectionPos.blockToSectionCoord(var0);
                int var4 = SectionPos.blockToSectionCoord(var1);
                int var5 = SectionPos.blockToSectionCoord(var2) - 1;
                int var6 = var5 - param4 + 1;

                while(var5 >= var6) {
                    if (!this.storage.storingLightForSection(SectionPos.asLong(var3, var5, var4))) {
                        --var5;
                    } else {
                        int var7 = SectionPos.sectionToBlockCoord(var5);

                        for(int var8 = 15; var8 >= 0; --var8) {
                            long var9 = BlockPos.asLong(var0, var7 + var8, var1);
                            if (param3) {
                                this.storage.setStoredLevel(var9, param2);
                                if (param2 > 1) {
                                    this.enqueueIncrease(var9, LightEngine.QueueEntry.increaseSkipOneDirection(param2, true, param1.getOpposite()));
                                }
                            } else {
                                this.storage.setStoredLevel(var9, 0);
                                this.enqueueDecrease(var9, LightEngine.QueueEntry.decreaseSkipOneDirection(param2, param1.getOpposite()));
                            }
                        }

                        --var5;
                    }
                }

            }
        }
    }

    private static boolean crossedSectionEdge(Direction param0, int param1, int param2) {
        return switch(param0) {
            case NORTH -> param2 == 15;
            case SOUTH -> param2 == 0;
            case WEST -> param1 == 15;
            case EAST -> param1 == 0;
            default -> false;
        };
    }

    @Override
    public void setLightEnabled(ChunkPos param0, boolean param1) {
        super.setLightEnabled(param0, param1);
        if (param1) {
            ChunkSkyLightSources var0 = Objects.requireNonNullElse(this.getChunkSources(param0.x, param0.z), this.emptyChunkSources);
            int var1 = SectionPos.blockToSectionCoord(var0.getHighestLowestSourceY());
            long var2 = SectionPos.getZeroNode(param0.x, param0.z);
            int var3 = this.storage.getTopSectionY(var2);
            int var4 = Math.max(this.storage.getBottomSectionY(), var1);

            for(int var5 = var3 - 1; var5 >= var4; --var5) {
                DataLayer var6 = this.storage.getDataLayerToWrite(SectionPos.asLong(param0.x, var5, param0.z));
                if (var6 != null && var6.isEmpty()) {
                    var6.fill(15);
                }
            }
        }

    }

    @Override
    public void propagateLightSources(ChunkPos param0) {
        long var0 = SectionPos.getZeroNode(param0.x, param0.z);
        this.storage.setLightEnabled(var0, true);
        ChunkSkyLightSources var1 = Objects.requireNonNullElse(this.getChunkSources(param0.x, param0.z), this.emptyChunkSources);
        ChunkSkyLightSources var2 = Objects.requireNonNullElse(this.getChunkSources(param0.x, param0.z - 1), this.emptyChunkSources);
        ChunkSkyLightSources var3 = Objects.requireNonNullElse(this.getChunkSources(param0.x, param0.z + 1), this.emptyChunkSources);
        ChunkSkyLightSources var4 = Objects.requireNonNullElse(this.getChunkSources(param0.x - 1, param0.z), this.emptyChunkSources);
        ChunkSkyLightSources var5 = Objects.requireNonNullElse(this.getChunkSources(param0.x + 1, param0.z), this.emptyChunkSources);
        int var6 = this.storage.getTopSectionY(var0);
        int var7 = this.storage.getBottomSectionY();
        int var8 = SectionPos.sectionToBlockCoord(param0.x);
        int var9 = SectionPos.sectionToBlockCoord(param0.z);

        for(int var10 = var6 - 1; var10 >= var7; --var10) {
            long var11 = SectionPos.asLong(param0.x, var10, param0.z);
            DataLayer var12 = this.storage.getDataLayerToWrite(var11);
            if (var12 != null) {
                int var13 = SectionPos.sectionToBlockCoord(var10);
                int var14 = var13 + 15;
                boolean var15 = false;

                for(int var16 = 0; var16 < 16; ++var16) {
                    for(int var17 = 0; var17 < 16; ++var17) {
                        int var18 = var1.getLowestSourceY(var17, var16);
                        if (var18 <= var14) {
                            int var19 = var16 == 0 ? var2.getLowestSourceY(var17, 15) : var1.getLowestSourceY(var17, var16 - 1);
                            int var20 = var16 == 15 ? var3.getLowestSourceY(var17, 0) : var1.getLowestSourceY(var17, var16 + 1);
                            int var21 = var17 == 0 ? var4.getLowestSourceY(15, var16) : var1.getLowestSourceY(var17 - 1, var16);
                            int var22 = var17 == 15 ? var5.getLowestSourceY(0, var16) : var1.getLowestSourceY(var17 + 1, var16);
                            int var23 = Math.max(Math.max(var19, var20), Math.max(var21, var22));

                            for(int var24 = var14; var24 >= Math.max(var13, var18); --var24) {
                                var12.set(var17, SectionPos.sectionRelative(var24), var16, 15);
                                if (var24 == var18 || var24 < var23) {
                                    long var25 = BlockPos.asLong(var8 + var17, var24, var9 + var16);
                                    this.enqueueIncrease(
                                        var25,
                                        LightEngine.QueueEntry.increaseSkySourceInDirections(
                                            var24 == var18, var24 < var19, var24 < var20, var24 < var21, var24 < var22
                                        )
                                    );
                                }
                            }

                            if (var18 < var13) {
                                var15 = true;
                            }
                        }
                    }
                }

                if (!var15) {
                    break;
                }
            }
        }

    }
}
