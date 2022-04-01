package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
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

    public List<StructureStart> startsForFeature(SectionPos param0, Predicate<ConfiguredStructureFeature<?, ?>> param1) {
        Map<ConfiguredStructureFeature<?, ?>, LongSet> var0 = this.level.getChunk(param0.x(), param0.z(), ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
        Builder<StructureStart> var1 = ImmutableList.builder();

        for(Entry<ConfiguredStructureFeature<?, ?>, LongSet> var2 : var0.entrySet()) {
            ConfiguredStructureFeature<?, ?> var3 = var2.getKey();
            if (param1.test(var3)) {
                this.fillStartsForFeature(var3, var2.getValue(), var1::add);
            }
        }

        return var1.build();
    }

    public List<StructureStart> startsForFeature(SectionPos param0, ConfiguredStructureFeature<?, ?> param1) {
        LongSet var0 = this.level.getChunk(param0.x(), param0.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForFeature(param1);
        Builder<StructureStart> var1 = ImmutableList.builder();
        this.fillStartsForFeature(param1, var0, var1::add);
        return var1.build();
    }

    public void fillStartsForFeature(ConfiguredStructureFeature<?, ?> param0, LongSet param1, Consumer<StructureStart> param2) {
        for(long var0 : param1) {
            SectionPos var1 = SectionPos.of(new ChunkPos(var0), this.level.getMinSection());
            StructureStart var2 = this.getStartForFeature(var1, param0, this.level.getChunk(var1.x(), var1.z(), ChunkStatus.STRUCTURE_STARTS));
            if (var2 != null && var2.isValid()) {
                param2.accept(var2);
            }
        }

    }

    @Nullable
    public StructureStart getStartForFeature(SectionPos param0, ConfiguredStructureFeature<?, ?> param1, FeatureAccess param2) {
        return param2.getStartForFeature(param1);
    }

    public void setStartForFeature(SectionPos param0, ConfiguredStructureFeature<?, ?> param1, StructureStart param2, FeatureAccess param3) {
        param3.setStartForFeature(param1, param2);
    }

    public void addReferenceForFeature(SectionPos param0, ConfiguredStructureFeature<?, ?> param1, long param2, FeatureAccess param3) {
        param3.addReferenceForFeature(param1, param2);
    }

    public boolean shouldGenerateFeatures() {
        return this.worldGenSettings.generateFeatures();
    }

    public StructureStart getStructureAt(BlockPos param0, ConfiguredStructureFeature<?, ?> param1) {
        for(StructureStart var0 : this.startsForFeature(SectionPos.of(param0), param1)) {
            if (var0.getBoundingBox().isInside(param0)) {
                return var0;
            }
        }

        return StructureStart.INVALID_START;
    }

    public StructureStart getStructureWithPieceAt(BlockPos param0, ResourceKey<ConfiguredStructureFeature<?, ?>> param1) {
        ConfiguredStructureFeature<?, ?> var0 = this.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY).get(param1);
        return var0 == null ? StructureStart.INVALID_START : this.getStructureWithPieceAt(param0, var0);
    }

    public StructureStart getStructureWithPieceAt(BlockPos param0, ConfiguredStructureFeature<?, ?> param1) {
        for(StructureStart var0 : this.startsForFeature(SectionPos.of(param0), param1)) {
            if (this.structureHasPieceAt(param0, var0)) {
                return var0;
            }
        }

        return StructureStart.INVALID_START;
    }

    public boolean structureHasPieceAt(BlockPos param0, StructureStart param1) {
        for(StructurePiece var0 : param1.getPieces()) {
            if (var0.getBoundingBox().isInside(param0)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasAnyStructureAt(BlockPos param0) {
        SectionPos var0 = SectionPos.of(param0);
        return this.level.getChunk(var0.x(), var0.z(), ChunkStatus.STRUCTURE_REFERENCES).hasAnyStructureReferences();
    }

    public Map<ConfiguredStructureFeature<?, ?>, LongSet> getAllStructuresAt(BlockPos param0) {
        SectionPos var0 = SectionPos.of(param0);
        return this.level.getChunk(var0.x(), var0.z(), ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
    }

    public StructureCheckResult checkStructurePresence(ChunkPos param0, ConfiguredStructureFeature<?, ?> param1, boolean param2) {
        return this.structureCheck.checkStart(param0, param1, param2);
    }

    public void addReference(StructureStart param0) {
        param0.addReference();
        this.structureCheck.incrementReference(param0.getChunkPos(), param0.getFeature());
    }

    public RegistryAccess registryAccess() {
        return this.level.registryAccess();
    }
}
