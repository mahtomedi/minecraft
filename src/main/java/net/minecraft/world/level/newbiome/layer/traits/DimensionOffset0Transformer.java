package net.minecraft.world.level.newbiome.layer.traits;

public interface DimensionOffset0Transformer extends DimensionTransformer {
    @Override
    default int getParentX(int param0) {
        return param0;
    }

    @Override
    default int getParentY(int param0) {
        return param0;
    }
}
