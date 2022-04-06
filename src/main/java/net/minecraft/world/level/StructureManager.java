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
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.StructureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureManager {
    private final LevelAccessor level;
    private final WorldGenSettings worldGenSettings;
    private final StructureCheck structureCheck;

    public StructureManager(LevelAccessor param0, WorldGenSettings param1, StructureCheck param2) {
        this.level = param0;
        this.worldGenSettings = param1;
        this.structureCheck = param2;
    }

    public StructureManager forWorldGenRegion(WorldGenRegion param0) {
        if (param0.getLevel() != this.level) {
            throw new IllegalStateException("Using invalid structure manager (source level: " + param0.getLevel() + ", region: " + param0);
        } else {
            return new StructureManager(param0, this.worldGenSettings, this.structureCheck);
        }
    }

    public List<StructureStart> startsForStructure(ChunkPos param0, Predicate<Structure> param1) {
        Map<Structure, LongSet> var0 = this.level.getChunk(param0.x, param0.z, ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
        Builder<StructureStart> var1 = ImmutableList.builder();

        for(Entry<Structure, LongSet> var2 : var0.entrySet()) {
            Structure var3 = var2.getKey();
            if (param1.test(var3)) {
                this.fillStartsForStructure(var3, var2.getValue(), var1::add);
            }
        }

        return var1.build();
    }

    public List<StructureStart> startsForStructure(SectionPos param0, Structure param1) {
        LongSet var0 = this.level.getChunk(param0.x(), param0.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForStructure(param1);
        Builder<StructureStart> var1 = ImmutableList.builder();
        this.fillStartsForStructure(param1, var0, var1::add);
        return var1.build();
    }

    public void fillStartsForStructure(Structure param0, LongSet param1, Consumer<StructureStart> param2) {
        for(long var0 : param1) {
            SectionPos var1 = SectionPos.of(new ChunkPos(var0), this.level.getMinSection());
            StructureStart var2 = this.getStartForStructure(var1, param0, this.level.getChunk(var1.x(), var1.z(), ChunkStatus.STRUCTURE_STARTS));
            if (var2 != null && var2.isValid()) {
                param2.accept(var2);
            }
        }

    }

    @Nullable
    public StructureStart getStartForStructure(SectionPos param0, Structure param1, StructureAccess param2) {
        return param2.getStartForStructure(param1);
    }

    public void setStartForStructure(SectionPos param0, Structure param1, StructureStart param2, StructureAccess param3) {
        param3.setStartForStructure(param1, param2);
    }

    public void addReferenceForStructure(SectionPos param0, Structure param1, long param2, StructureAccess param3) {
        param3.addReferenceForStructure(param1, param2);
    }

    public boolean shouldGenerateStructures() {
        return this.worldGenSettings.generateStructures();
    }

    public StructureStart getStructureAt(BlockPos param0, Structure param1) {
        for(StructureStart var0 : this.startsForStructure(SectionPos.of(param0), param1)) {
            if (var0.getBoundingBox().isInside(param0)) {
                return var0;
            }
        }

        return StructureStart.INVALID_START;
    }

    public StructureStart getStructureWithPieceAt(BlockPos param0, ResourceKey<Structure> param1) {
        Structure var0 = this.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY).get(param1);
        return var0 == null ? StructureStart.INVALID_START : this.getStructureWithPieceAt(param0, var0);
    }

    public StructureStart getStructureWithPieceAt(BlockPos param0, TagKey<Structure> param1) {
        Registry<Structure> var0 = this.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);

        for(StructureStart var1 : this.startsForStructure(
            new ChunkPos(param0), param2 -> var0.getHolder(var0.getId(param2)).map(param1x -> param1x.is(param1)).orElse(false)
        )) {
            if (this.structureHasPieceAt(param0, var1)) {
                return var1;
            }
        }

        return StructureStart.INVALID_START;
    }

    public StructureStart getStructureWithPieceAt(BlockPos param0, Structure param1) {
        for(StructureStart var0 : this.startsForStructure(SectionPos.of(param0), param1)) {
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

    public Map<Structure, LongSet> getAllStructuresAt(BlockPos param0) {
        SectionPos var0 = SectionPos.of(param0);
        return this.level.getChunk(var0.x(), var0.z(), ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
    }

    public StructureCheckResult checkStructurePresence(ChunkPos param0, Structure param1, boolean param2) {
        return this.structureCheck.checkStart(param0, param1, param2);
    }

    public void addReference(StructureStart param0) {
        param0.addReference();
        this.structureCheck.incrementReference(param0.getChunkPos(), param0.getStructure());
    }

    public RegistryAccess registryAccess() {
        return this.level.registryAccess();
    }
}
