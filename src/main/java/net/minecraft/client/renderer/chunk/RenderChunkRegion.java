package net.minecraft.client.renderer.chunk;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderChunkRegion implements BlockAndTintGetter {
    private final int centerX;
    private final int centerZ;
    protected final RenderChunkRegion.RenderChunk[][] chunks;
    protected final Level level;

    @Nullable
    public static RenderChunkRegion createIfNotEmpty(Level param0, BlockPos param1, BlockPos param2, int param3) {
        int var0 = SectionPos.blockToSectionCoord(param1.getX() - param3);
        int var1 = SectionPos.blockToSectionCoord(param1.getZ() - param3);
        int var2 = SectionPos.blockToSectionCoord(param2.getX() + param3);
        int var3 = SectionPos.blockToSectionCoord(param2.getZ() + param3);
        LevelChunk[][] var4 = new LevelChunk[var2 - var0 + 1][var3 - var1 + 1];

        for(int var5 = var0; var5 <= var2; ++var5) {
            for(int var6 = var1; var6 <= var3; ++var6) {
                var4[var5 - var0][var6 - var1] = param0.getChunk(var5, var6);
            }
        }

        if (isAllEmpty(param1, param2, var0, var1, var4)) {
            return null;
        } else {
            RenderChunkRegion.RenderChunk[][] var7 = new RenderChunkRegion.RenderChunk[var2 - var0 + 1][var3 - var1 + 1];

            for(int var8 = var0; var8 <= var2; ++var8) {
                for(int var9 = var1; var9 <= var3; ++var9) {
                    LevelChunk var10 = var4[var8 - var0][var9 - var1];
                    var7[var8 - var0][var9 - var1] = new RenderChunkRegion.RenderChunk(var10);
                }
            }

            return new RenderChunkRegion(param0, var0, var1, var7);
        }
    }

    private static boolean isAllEmpty(BlockPos param0, BlockPos param1, int param2, int param3, LevelChunk[][] param4) {
        for(int var0 = SectionPos.blockToSectionCoord(param0.getX()); var0 <= SectionPos.blockToSectionCoord(param1.getX()); ++var0) {
            for(int var1 = SectionPos.blockToSectionCoord(param0.getZ()); var1 <= SectionPos.blockToSectionCoord(param1.getZ()); ++var1) {
                LevelChunk var2 = param4[var0 - param2][var1 - param3];
                if (!var2.isYSpaceEmpty(param0.getY(), param1.getY())) {
                    return false;
                }
            }
        }

        return true;
    }

    private RenderChunkRegion(Level param0, int param1, int param2, RenderChunkRegion.RenderChunk[][] param3) {
        this.level = param0;
        this.centerX = param1;
        this.centerZ = param2;
        this.chunks = param3;
    }

    @Override
    public BlockState getBlockState(BlockPos param0) {
        int var0 = SectionPos.blockToSectionCoord(param0.getX()) - this.centerX;
        int var1 = SectionPos.blockToSectionCoord(param0.getZ()) - this.centerZ;
        return this.chunks[var0][var1].getBlockState(param0);
    }

    @Override
    public FluidState getFluidState(BlockPos param0) {
        int var0 = SectionPos.blockToSectionCoord(param0.getX()) - this.centerX;
        int var1 = SectionPos.blockToSectionCoord(param0.getZ()) - this.centerZ;
        return this.chunks[var0][var1].getBlockState(param0).getFluidState();
    }

    @Override
    public float getShade(Direction param0, boolean param1) {
        return this.level.getShade(param0, param1);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0) {
        int var0 = SectionPos.blockToSectionCoord(param0.getX()) - this.centerX;
        int var1 = SectionPos.blockToSectionCoord(param0.getZ()) - this.centerZ;
        return this.chunks[var0][var1].getBlockEntity(param0);
    }

    @Override
    public int getBlockTint(BlockPos param0, ColorResolver param1) {
        return this.level.getBlockTint(param0, param1);
    }

    @Override
    public int getMinBuildHeight() {
        return this.level.getMinBuildHeight();
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }

    @OnlyIn(Dist.CLIENT)
    static final class RenderChunk {
        private final Map<BlockPos, BlockEntity> blockEntities;
        @Nullable
        private final List<PalettedContainer<BlockState>> sections;
        private final boolean debug;
        private final LevelChunk wrapped;

        RenderChunk(LevelChunk param0) {
            this.wrapped = param0;
            this.debug = param0.getLevel().isDebug();
            this.blockEntities = ImmutableMap.copyOf(param0.getBlockEntities());
            if (param0 instanceof EmptyLevelChunk) {
                this.sections = null;
            } else {
                LevelChunkSection[] var0 = param0.getSections();
                this.sections = new ArrayList<>(var0.length);

                for(LevelChunkSection var1 : var0) {
                    this.sections.add(var1.hasOnlyAir() ? null : var1.getStates().copy());
                }
            }

        }

        @Nullable
        public BlockEntity getBlockEntity(BlockPos param0) {
            return this.blockEntities.get(param0);
        }

        public BlockState getBlockState(BlockPos param0) {
            int var0 = param0.getX();
            int var1 = param0.getY();
            int var2 = param0.getZ();
            if (this.debug) {
                BlockState var3 = null;
                if (var1 == 60) {
                    var3 = Blocks.BARRIER.defaultBlockState();
                }

                if (var1 == 70) {
                    var3 = DebugLevelSource.getBlockStateFor(var0, var2);
                }

                return var3 == null ? Blocks.AIR.defaultBlockState() : var3;
            } else if (this.sections == null) {
                return Blocks.AIR.defaultBlockState();
            } else {
                try {
                    int var4 = this.wrapped.getSectionIndex(var1);
                    if (var4 >= 0 && var4 < this.sections.size()) {
                        PalettedContainer<BlockState> var5 = this.sections.get(var4);
                        if (var5 != null) {
                            return var5.get(var0 & 15, var1 & 15, var2 & 15);
                        }
                    }

                    return Blocks.AIR.defaultBlockState();
                } catch (Throwable var81) {
                    CrashReport var7 = CrashReport.forThrowable(var81, "Getting block state");
                    CrashReportCategory var8 = var7.addCategory("Block being got");
                    var8.setDetail("Location", () -> CrashReportCategory.formatLocation(this.wrapped, var0, var1, var2));
                    throw new ReportedException(var7);
                }
            }
        }
    }
}
