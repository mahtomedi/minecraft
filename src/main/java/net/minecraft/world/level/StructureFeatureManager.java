package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureFeatureManager {
    private final LevelAccessor level;
    private final WorldGenSettings worldGenSettings;
    private final StructureCheck structureCheck;

    public StructureFeatureManager(LevelAccessor param0, WorldGenSettings param1, StructureCheck param2) {
        this.level = param0;
        this.worldGenSettings = param1;
        this.structureCheck = param2;
    }

    public StructureFeatureManager forWorldGenRegion(WorldGenRegion param0) {
        if (param0.getLevel() != this.level) {
            throw new IllegalStateException("Using invalid feature manager (source level: " + param0.getLevel() + ", region: " + param0);
        } else {
            return new StructureFeatureManager(param0, this.worldGenSettings, this.structureCheck);
        }
    }

    public List<? extends StructureStart<?>> startsForFeature(SectionPos param0, StructureFeature<?> param1) {
        LongSet var0 = this.level.getChunk(param0.x(), param0.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForFeature(param1);
        Builder<StructureStart<?>> var1 = ImmutableList.builder();

        for(long var2 : var0) {
            SectionPos var3 = SectionPos.of(new ChunkPos(var2), this.level.getMinSection());
            StructureStart<?> var4 = this.getStartForFeature(var3, param1, this.level.getChunk(var3.x(), var3.z(), ChunkStatus.STRUCTURE_STARTS));
            if (var4 != null && var4.isValid()) {
                var1.add(var4);
            }
        }

        return var1.build();
    }

    @Nullable
    public StructureStart<?> getStartForFeature(SectionPos param0, StructureFeature<?> param1, FeatureAccess param2) {
        return param2.getStartForFeature(param1);
    }

    public void setStartForFeature(SectionPos param0, StructureFeature<?> param1, StructureStart<?> param2, FeatureAccess param3) {
        param3.setStartForFeature(param1, param2);
    }

    public void addReferenceForFeature(SectionPos param0, StructureFeature<?> param1, long param2, FeatureAccess param3) {
        param3.addReferenceForFeature(param1, param2);
    }

    public boolean shouldGenerateFeatures() {
        return this.worldGenSettings.generateFeatures();
    }

    public StructureStart<?> getStructureAt(BlockPos param0, StructureFeature<?> param1) {
        for(StructureStart<?> var0 : this.startsForFeature(SectionPos.of(param0), param1)) {
            if (var0.getBoundingBox().isInside(param0)) {
                return var0;
            }
        }

        return StructureStart.INVALID_START;
    }

    public StructureStart<?> getStructureWithPieceAt(BlockPos param0, StructureFeature<?> param1) {
        for(StructureStart<?> var0 : this.startsForFeature(SectionPos.of(param0), param1)) {
            for(StructurePiece var1 : var0.getPieces()) {
                if (var1.getBoundingBox().isInside(param0)) {
                    return var0;
                }
            }
        }

        return StructureStart.INVALID_START;
    }

    public boolean hasAnyStructureAt(BlockPos param0) {
        SectionPos var0 = SectionPos.of(param0);
        return this.level.getChunk(var0.x(), var0.z(), ChunkStatus.STRUCTURE_REFERENCES).hasAnyStructureReferences();
    }

    public StructureCheckResult checkStructurePresence(ChunkPos param0, StructureFeature<?> param1, boolean param2) {
        return this.structureCheck.checkStart(param0, param1, param2);
    }

    public void addReference(StructureStart<?> param0) {
        param0.addReference();
        this.structureCheck.incrementReference(param0.getChunkPos(), param0.getFeature());
    }
}
