package net.minecraft.world.level;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureFeatureManager {
    private final ServerLevel level;
    private final WorldGenSettings worldGenSettings;

    public StructureFeatureManager(ServerLevel param0, WorldGenSettings param1) {
        this.level = param0;
        this.worldGenSettings = param1;
    }

    public Stream<StructureStart> startsForFeature(SectionPos param0, StructureFeature<?> param1) {
        return this.level
            .getChunk(param0.x(), param0.z(), ChunkStatus.STRUCTURE_REFERENCES)
            .getReferencesForFeature(param1.getFeatureName())
            .stream()
            .map(param0x -> SectionPos.of(new ChunkPos(param0x), 0))
            .map(param1x -> this.getStartForFeature(param1x, param1, this.level.getChunk(param1x.x(), param1x.z(), ChunkStatus.STRUCTURE_STARTS)))
            .filter(param0x -> param0x != null && param0x.isValid());
    }

    @Nullable
    public StructureStart getStartForFeature(SectionPos param0, StructureFeature<?> param1, FeatureAccess param2) {
        return param2.getStartForFeature(param1.getFeatureName());
    }

    public void setStartForFeature(SectionPos param0, StructureFeature<?> param1, StructureStart param2, FeatureAccess param3) {
        param3.setStartForFeature(param1.getFeatureName(), param2);
    }

    public void addReferenceForFeature(SectionPos param0, StructureFeature<?> param1, long param2, FeatureAccess param3) {
        param3.addReferenceForFeature(param1.getFeatureName(), param2);
    }

    public boolean shouldGenerateFeatures() {
        return this.worldGenSettings.generateFeatures();
    }
}
