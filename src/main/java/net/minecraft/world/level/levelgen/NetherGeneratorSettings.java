package net.minecraft.world.level.levelgen;

import java.util.Random;

public class NetherGeneratorSettings extends ChunkGeneratorSettings {
    @Override
    public int getBedrockFloorPosition() {
        return 0;
    }

    @Override
    public int getBedrockRoofPosition() {
        return 127;
    }

    public NetherGeneratorSettings() {
    }

    public NetherGeneratorSettings(Random param0) {
        this.defaultBlock = this.randomGroundBlock(param0);
        this.defaultFluid = this.randomLiquidBlock(param0);
    }
}
