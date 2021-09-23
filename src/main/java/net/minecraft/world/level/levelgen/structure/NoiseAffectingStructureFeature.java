package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;

public abstract class NoiseAffectingStructureFeature<C extends FeatureConfiguration> extends StructureFeature<C> {
    public NoiseAffectingStructureFeature(Codec<C> param0, PieceGenerator<C> param1) {
        super(param0, param1);
    }

    public NoiseAffectingStructureFeature(Codec<C> param0, PieceGenerator<C> param1, PostPlacementProcessor param2) {
        super(param0, param1, param2);
    }

    @Override
    public BoundingBox adjustBoundingBox(BoundingBox param0) {
        return super.adjustBoundingBox(param0).inflatedBy(12);
    }
}
