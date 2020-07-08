package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class HugeFungusConfiguration implements FeatureConfiguration {
    public static final Codec<HugeFungusConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockState.CODEC.fieldOf("valid_base_block").forGetter(param0x -> param0x.validBaseState),
                    BlockState.CODEC.fieldOf("stem_state").forGetter(param0x -> param0x.stemState),
                    BlockState.CODEC.fieldOf("hat_state").forGetter(param0x -> param0x.hatState),
                    BlockState.CODEC.fieldOf("decor_state").forGetter(param0x -> param0x.decorState),
                    Codec.BOOL.fieldOf("planted").orElse(false).forGetter(param0x -> param0x.planted)
                )
                .apply(param0, HugeFungusConfiguration::new)
    );
    public static final HugeFungusConfiguration HUGE_CRIMSON_FUNGI_PLANTED_CONFIG = new HugeFungusConfiguration(
        Blocks.CRIMSON_NYLIUM.defaultBlockState(),
        Blocks.CRIMSON_STEM.defaultBlockState(),
        Blocks.NETHER_WART_BLOCK.defaultBlockState(),
        Blocks.SHROOMLIGHT.defaultBlockState(),
        true
    );
    public static final HugeFungusConfiguration HUGE_CRIMSON_FUNGI_NOT_PLANTED_CONFIG;
    public static final HugeFungusConfiguration HUGE_WARPED_FUNGI_PLANTED_CONFIG = new HugeFungusConfiguration(
        Blocks.WARPED_NYLIUM.defaultBlockState(),
        Blocks.WARPED_STEM.defaultBlockState(),
        Blocks.WARPED_WART_BLOCK.defaultBlockState(),
        Blocks.SHROOMLIGHT.defaultBlockState(),
        true
    );
    public static final HugeFungusConfiguration HUGE_WARPED_FUNGI_NOT_PLANTED_CONFIG;
    public final BlockState validBaseState;
    public final BlockState stemState;
    public final BlockState hatState;
    public final BlockState decorState;
    public final boolean planted;

    public HugeFungusConfiguration(BlockState param0, BlockState param1, BlockState param2, BlockState param3, boolean param4) {
        this.validBaseState = param0;
        this.stemState = param1;
        this.hatState = param2;
        this.decorState = param3;
        this.planted = param4;
    }

    static {
        HUGE_CRIMSON_FUNGI_NOT_PLANTED_CONFIG = new HugeFungusConfiguration(
            HUGE_CRIMSON_FUNGI_PLANTED_CONFIG.validBaseState,
            HUGE_CRIMSON_FUNGI_PLANTED_CONFIG.stemState,
            HUGE_CRIMSON_FUNGI_PLANTED_CONFIG.hatState,
            HUGE_CRIMSON_FUNGI_PLANTED_CONFIG.decorState,
            false
        );
        HUGE_WARPED_FUNGI_NOT_PLANTED_CONFIG = new HugeFungusConfiguration(
            HUGE_WARPED_FUNGI_PLANTED_CONFIG.validBaseState,
            HUGE_WARPED_FUNGI_PLANTED_CONFIG.stemState,
            HUGE_WARPED_FUNGI_PLANTED_CONFIG.hatState,
            HUGE_WARPED_FUNGI_PLANTED_CONFIG.decorState,
            false
        );
    }
}
