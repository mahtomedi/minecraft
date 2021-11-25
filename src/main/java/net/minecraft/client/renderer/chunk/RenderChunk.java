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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
class RenderChunk {
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
