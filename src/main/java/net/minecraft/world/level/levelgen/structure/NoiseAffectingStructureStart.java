package net.minecraft.world.level.levelgen.structure;

import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public abstract class NoiseAffectingStructureStart<C extends FeatureConfiguration> extends StructureStart<C> {
    public NoiseAffectingStructureStart(StructureFeature<C> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
        super(param0, param1, param2, param3, param4, param5);
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
