package net.minecraft.world.level.levelgen;

public class OverworldGeneratorSettings extends NoiseGeneratorSettings {
    private final boolean isAmplified;

    public OverworldGeneratorSettings() {
        this(new ChunkGeneratorSettings(), false);
    }

    public OverworldGeneratorSettings(ChunkGeneratorSettings param0, boolean param1) {
        super(param0);
        this.isAmplified = param1;
    }

    @Override
    public int getBedrockFloorPosition() {
        return 0;
    }

    public boolean isAmplified() {
        return this.isAmplified;
    }
}
