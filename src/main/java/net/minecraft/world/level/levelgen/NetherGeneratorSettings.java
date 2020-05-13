package net.minecraft.world.level.levelgen;

public class NetherGeneratorSettings extends NoiseGeneratorSettings {
    public NetherGeneratorSettings(ChunkGeneratorSettings param0) {
        super(param0);
        param0.ruinedPortalSpacing = 25;
        param0.ruinedPortalSeparation = 10;
    }

    @Override
    public int getBedrockFloorPosition() {
        return 0;
    }

    @Override
    public int getBedrockRoofPosition() {
        return 127;
    }
}
