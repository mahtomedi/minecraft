package net.minecraft.world.level.lighting;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;

public final class BlockLightEngine extends LightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public BlockLightEngine(LightChunkGetter param0) {
        this(param0, new BlockLightSectionStorage(param0));
    }

    @VisibleForTesting
    public BlockLightEngine(LightChunkGetter param0, BlockLightSectionStorage param1) {
        super(param0, param1);
    }

    @Override
    protected void checkNode(long param0) {
        long var0 = SectionPos.blockToSection(param0);
        if (this.storage.storingLightForSection(var0)) {
            BlockState var1 = this.getState(this.mutablePos.set(param0));
            int var2 = this.getEmission(param0, var1);
            int var3 = this.storage.getStoredLevel(param0);
            if (var2 < var3) {
                this.storage.setStoredLevel(param0, 0);
                this.enqueueDecrease(param0, LightEngine.QueueEntry.decreaseAllDirections(var3));
            } else {
                this.enqueueDecrease(param0, PULL_LIGHT_IN_ENTRY);
            }

            if (var2 > 0) {
                this.enqueueIncrease(param0, LightEngine.QueueEntry.increaseLightFromEmission(var2, isEmptyShape(var1)));
            }

        }
    }

    @Override
    protected void propagateIncrease(long param0, long param1, int param2) {
        BlockState var0 = null;

        for(Direction var1 : PROPAGATION_DIRECTIONS) {
            if (LightEngine.QueueEntry.shouldPropagateInDirection(param1, var1)) {
                long var2 = BlockPos.offset(param0, var1);
                if (this.storage.storingLightForSection(SectionPos.blockToSection(var2))) {
                    int var3 = this.storage.getStoredLevel(var2);
                    int var4 = param2 - 1;
                    if (var4 > var3) {
                        this.mutablePos.set(var2);
                        BlockState var5 = this.getState(this.mutablePos);
                        int var6 = param2 - this.getOpacity(var5, this.mutablePos);
                        if (var6 > var3) {
                            if (var0 == null) {
                                var0 = LightEngine.QueueEntry.isFromEmptyShape(param1)
                                    ? Blocks.AIR.defaultBlockState()
                                    : this.getState(this.mutablePos.set(param0));
                            }

                            if (!this.shapeOccludes(param0, var0, var2, var5, var1)) {
                                this.storage.setStoredLevel(var2, var6);
                                if (var6 > 1) {
                                    this.enqueueIncrease(var2, LightEngine.QueueEntry.increaseSkipOneDirection(var6, isEmptyShape(var5), var1.getOpposite()));
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    protected void propagateDecrease(long param0, long param1) {
        int var0 = LightEngine.QueueEntry.getFromLevel(param1);

        for(Direction var1 : PROPAGATION_DIRECTIONS) {
            if (LightEngine.QueueEntry.shouldPropagateInDirection(param1, var1)) {
                long var2 = BlockPos.offset(param0, var1);
                if (this.storage.storingLightForSection(SectionPos.blockToSection(var2))) {
                    int var3 = this.storage.getStoredLevel(var2);
                    if (var3 != 0) {
                        if (var3 <= var0 - 1) {
                            BlockState var4 = this.getState(this.mutablePos.set(var2));
                            int var5 = this.getEmission(var2, var4);
                            this.storage.setStoredLevel(var2, 0);
                            if (var5 < var3) {
                                this.enqueueDecrease(var2, LightEngine.QueueEntry.decreaseSkipOneDirection(var3, var1.getOpposite()));
                            }

                            if (var5 > 0) {
                                this.enqueueIncrease(var2, LightEngine.QueueEntry.increaseLightFromEmission(var5, isEmptyShape(var4)));
                            }
                        } else {
                            this.enqueueIncrease(var2, LightEngine.QueueEntry.increaseOnlyOneDirection(var3, false, var1.getOpposite()));
                        }
                    }
                }
            }
        }

    }

    private int getEmission(long param0, BlockState param1) {
        int var0 = param1.getLightEmission();
        return var0 > 0 && this.storage.lightOnInSection(SectionPos.blockToSection(param0)) ? var0 : 0;
    }

    @Override
    public void propagateLightSources(ChunkPos param0) {
        this.setLightEnabled(param0, true);
        LightChunk var0 = this.chunkSource.getChunkForLighting(param0.x, param0.z);
        if (var0 != null) {
            var0.findBlockLightSources((param0x, param1) -> {
                int var0x = param1.getLightEmission();
                this.enqueueIncrease(param0x.asLong(), LightEngine.QueueEntry.increaseLightFromEmission(var0x, isEmptyShape(param1)));
            });
        }

    }
}
