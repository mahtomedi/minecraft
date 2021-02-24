package net.minecraft.world.level.levelgen.structure;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public abstract class NoiseAffectingStructureStart<C extends FeatureConfiguration> extends StructureStart<C> {
    public NoiseAffectingStructureStart(StructureFeature<C> param0, ChunkPos param1, BoundingBox param2, int param3, long param4) {
        super(param0, param1, param2, param3, param4);
    }

    @Override
    protected void calculateBoundingBox() {
        super.calculateBoundingBox();
        int var0 = 12;
        this.boundingBox.x0 -= 12;
        this.boundingBox.y0 -= 12;
        this.boundingBox.z0 -= 12;
        this.boundingBox.x1 += 12;
        this.boundingBox.y1 += 12;
        this.boundingBox.z1 += 12;
    }
}
