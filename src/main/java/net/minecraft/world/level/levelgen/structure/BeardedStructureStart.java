package net.minecraft.world.level.levelgen.structure;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public abstract class BeardedStructureStart extends StructureStart {
    public BeardedStructureStart(StructureFeature<?> param0, int param1, int param2, Biome param3, BoundingBox param4, int param5, long param6) {
        super(param0, param1, param2, param3, param4, param5, param6);
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
