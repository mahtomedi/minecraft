package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class HugeFungusConfiguration implements FeatureConfiguration {
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

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("valid_base_block"),
                    BlockState.serialize(param0, this.validBaseState).getValue(),
                    param0.createString("stem_state"),
                    BlockState.serialize(param0, this.stemState).getValue(),
                    param0.createString("hat_state"),
                    BlockState.serialize(param0, this.hatState).getValue(),
                    param0.createString("decor_state"),
                    BlockState.serialize(param0, this.decorState).getValue(),
                    param0.createString("planted"),
                    param0.createBoolean(this.planted)
                )
            )
        );
    }

    public static <T> HugeFungusConfiguration deserialize(Dynamic<T> param0) {
        BlockState var0 = param0.get("valid_base_state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        BlockState var1 = param0.get("stem_state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        BlockState var2 = param0.get("hat_state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        BlockState var3 = param0.get("decor_state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        boolean var4 = param0.get("planted").asBoolean(false);
        return new HugeFungusConfiguration(var0, var1, var2, var3, var4);
    }

    public static <T> HugeFungusConfiguration random(Random param0) {
        return new HugeFungusConfiguration(
            Registry.BLOCK.getRandom(param0).defaultBlockState(),
            Registry.BLOCK.getRandom(param0).defaultBlockState(),
            Registry.BLOCK.getRandom(param0).defaultBlockState(),
            Registry.BLOCK.getRandom(param0).defaultBlockState(),
            false
        );
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
