package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;

public class OreConfiguration implements FeatureConfiguration {
    public final OreConfiguration.Predicates target;
    public final int size;
    public final BlockState state;

    public OreConfiguration(OreConfiguration.Predicates param0, BlockState param1, int param2) {
        this.size = param2;
        this.state = param1;
        this.target = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("size"),
                    param0.createInt(this.size),
                    param0.createString("target"),
                    param0.createString(this.target.getName()),
                    param0.createString("state"),
                    BlockState.serialize(param0, this.state).getValue()
                )
            )
        );
    }

    public static OreConfiguration deserialize(Dynamic<?> param0) {
        int var0 = param0.get("size").asInt(0);
        OreConfiguration.Predicates var1 = OreConfiguration.Predicates.byName(param0.get("target").asString(""));
        BlockState var2 = param0.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        return new OreConfiguration(var1, var2, var0);
    }

    public static OreConfiguration random(Random param0) {
        return new OreConfiguration(
            Util.randomEnum(OreConfiguration.Predicates.class, param0), Registry.BLOCK.getRandom(param0).defaultBlockState(), param0.nextInt(15)
        );
    }

    public static enum Predicates {
        NATURAL_STONE("natural_stone", param0 -> {
            if (param0 == null) {
                return false;
            } else {
                Block var0 = param0.getBlock();
                return var0 == Blocks.STONE || var0 == Blocks.GRANITE || var0 == Blocks.DIORITE || var0 == Blocks.ANDESITE;
            }
        }),
        NETHERRACK("netherrack", new BlockPredicate(Blocks.NETHERRACK)),
        ANY("any", param0 -> true);

        private static final Map<String, OreConfiguration.Predicates> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(OreConfiguration.Predicates::getName, param0 -> param0));
        private final String name;
        private final Predicate<BlockState> predicate;

        private Predicates(String param0, Predicate<BlockState> param1) {
            this.name = param0;
            this.predicate = param1;
        }

        public String getName() {
            return this.name;
        }

        public static OreConfiguration.Predicates byName(String param0) {
            return BY_NAME.get(param0);
        }

        public Predicate<BlockState> getPredicate() {
            return this.predicate;
        }
    }
}
