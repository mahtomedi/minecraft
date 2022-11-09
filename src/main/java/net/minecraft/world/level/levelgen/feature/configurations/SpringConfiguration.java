package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FluidState;

public class SpringConfiguration implements FeatureConfiguration {
    public static final Codec<SpringConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    FluidState.CODEC.fieldOf("state").forGetter(param0x -> param0x.state),
                    Codec.BOOL.fieldOf("requires_block_below").orElse(true).forGetter(param0x -> param0x.requiresBlockBelow),
                    Codec.INT.fieldOf("rock_count").orElse(4).forGetter(param0x -> param0x.rockCount),
                    Codec.INT.fieldOf("hole_count").orElse(1).forGetter(param0x -> param0x.holeCount),
                    RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("valid_blocks").forGetter(param0x -> param0x.validBlocks)
                )
                .apply(param0, SpringConfiguration::new)
    );
    public final FluidState state;
    public final boolean requiresBlockBelow;
    public final int rockCount;
    public final int holeCount;
    public final HolderSet<Block> validBlocks;

    public SpringConfiguration(FluidState param0, boolean param1, int param2, int param3, HolderSet<Block> param4) {
        this.state = param0;
        this.requiresBlockBelow = param1;
        this.rockCount = param2;
        this.holeCount = param3;
        this.validBlocks = param4;
    }
}
