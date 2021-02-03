package net.minecraft.world.level;

import com.mojang.datafixers.DataFixUtils;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureFeatureManager {
    private final LevelAccessor level;
    private final WorldGenSettings worldGenSettings;

    public StructureFeatureManager(LevelAccessor param0, WorldGenSettings param1) {
        this.level = param0;
        this.worldGenSettings = param1;
    }

    public StructureFeatureManager forWorldGenRegion(WorldGenRegion param0) {
        if (param0.getLevel() != this.level) {
            throw new IllegalStateException("Using invalid feature manager (source level: " + param0.getLevel() + ", region: " + param0);
        } else {
            return new StructureFeatureManager(param0, this.worldGenSettings);
        }
    }

    public Stream<? extends StructureStart<?>> startsForFeature(SectionPos param0, StructureFeature<?> param1) {
        return this.level
            .getChunk(param0.x(), param0.z(), ChunkStatus.STRUCTURE_REFERENCES)
            .getReferencesForFeature(param1)
            .stream()
            .map(param0x -> SectionPos.of(new ChunkPos(param0x), this.level.getMinSection()))
            .map(param1x -> this.getStartForFeature(param1x, param1, this.level.getChunk(param1x.x(), param1x.z(), ChunkStatus.STRUCTURE_STARTS)))
            .filter(param0x -> param0x != null && param0x.isValid());
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

    public StructureStart<?> getStructureAt(BlockPos param0, boolean param1, StructureFeature<?> param2) {
        return DataFixUtils.orElse(
            this.startsForFeature(SectionPos.of(param0), param2)
                .filter(param1x -> param1x.getBoundingBox().isInside(param0))
                .filter(param2x -> !param1 || param2x.getPieces().stream().anyMatch(param1x -> param1x.getBoundingBox().isInside(param0)))
                .findFirst(),
            StructureStart.INVALID_START
        );
    }
}
