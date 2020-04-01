package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class SpringConfiguration implements FeatureConfiguration {
    public final FluidState state;
    public final boolean requiresBlockBelow;
    public final int rockCount;
    public final int holeCount;
    public final Set<Block> validBlocks;

    public SpringConfiguration(FluidState param0, boolean param1, int param2, int param3, Set<Block> param4) {
        this.state = param0;
        this.requiresBlockBelow = param1;
        this.rockCount = param2;
        this.holeCount = param3;
        this.validBlocks = param4;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("state"),
                    FluidState.serialize(param0, this.state).getValue(),
                    param0.createString("requires_block_below"),
                    param0.createBoolean(this.requiresBlockBelow),
                    param0.createString("rock_count"),
                    param0.createInt(this.rockCount),
                    param0.createString("hole_count"),
                    param0.createInt(this.holeCount),
                    param0.createString("valid_blocks"),
                    param0.createList(this.validBlocks.stream().map(Registry.BLOCK::getKey).map(ResourceLocation::toString).map(param0::createString))
                )
            )
        );
    }

    public static <T> SpringConfiguration deserialize(Dynamic<T> param0) {
        return new SpringConfiguration(
            param0.get("state").map(FluidState::deserialize).orElse(Fluids.EMPTY.defaultFluidState()),
            param0.get("requires_block_below").asBoolean(true),
            param0.get("rock_count").asInt(4),
            param0.get("hole_count").asInt(1),
            ImmutableSet.copyOf(param0.get("valid_blocks").asList(param0x -> Registry.BLOCK.get(new ResourceLocation(param0x.asString("minecraft:air")))))
        );
    }

    public static SpringConfiguration random(Random param0) {
        return new SpringConfiguration(
            Registry.FLUID.getRandom(param0).defaultFluidState(),
            param0.nextInt(5) == 0,
            param0.nextInt(5),
            param0.nextInt(5),
            Util.randomObjectStream(param0, 10, Registry.BLOCK).collect(Collectors.toSet())
        );
    }
}
