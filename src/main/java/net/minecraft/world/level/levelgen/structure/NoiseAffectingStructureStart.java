package net.minecraft.world.level.levelgen.structure;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public abstract class NoiseAffectingStructureStart<C extends FeatureConfiguration> extends StructureStart<C> {
    public NoiseAffectingStructureStart(StructureFeature<C> param0, ChunkPos param1, int param2, long param3) {
        super(param0, param1, param2, param3);
    }

    @Override
    protected BoundingBox createBoundingBox() {
        return super.createBoundingBox().inflate(12);
    }
}
