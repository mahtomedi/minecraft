package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;

public class OreConfiguration implements FeatureConfiguration {
    public static final Codec<OreConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    OreConfiguration.Predicates.CODEC.fieldOf("target").forGetter(param0x -> param0x.target),
                    BlockState.CODEC.fieldOf("state").forGetter(param0x -> param0x.state),
                    Codec.INT.fieldOf("size").withDefault(0).forGetter(param0x -> param0x.size)
                )
                .apply(param0, OreConfiguration::new)
    );
    public final OreConfiguration.Predicates target;
    public final int size;
    public final BlockState state;

    public OreConfiguration(OreConfiguration.Predicates param0, BlockState param1, int param2) {
        this.size = param2;
        this.state = param1;
        this.target = param0;
    }

    public static enum Predicates implements StringRepresentable {
        NATURAL_STONE("natural_stone", param0 -> {
            if (param0 == null) {
                return false;
            } else {
                return param0.is(Blocks.STONE) || param0.is(Blocks.GRANITE) || param0.is(Blocks.DIORITE) || param0.is(Blocks.ANDESITE);
            }
        }),
        NETHERRACK("netherrack", new BlockPredicate(Blocks.NETHERRACK)),
        NETHER_ORE_REPLACEABLES("nether_ore_replaceables", param0 -> {
            if (param0 == null) {
                return false;
            } else {
                return param0.is(Blocks.NETHERRACK) || param0.is(Blocks.BASALT) || param0.is(Blocks.BLACKSTONE);
            }
        });

        public static final Codec<OreConfiguration.Predicates> CODEC = StringRepresentable.fromEnum(
            OreConfiguration.Predicates::values, OreConfiguration.Predicates::byName
        );
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

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
