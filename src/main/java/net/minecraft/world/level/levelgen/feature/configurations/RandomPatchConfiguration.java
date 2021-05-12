package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class RandomPatchConfiguration implements FeatureConfiguration {
    public static final Codec<RandomPatchConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockStateProvider.CODEC.fieldOf("state_provider").forGetter(param0x -> param0x.stateProvider),
                    BlockPlacer.CODEC.fieldOf("block_placer").forGetter(param0x -> param0x.blockPlacer),
                    BlockState.CODEC
                        .listOf()
                        .fieldOf("whitelist")
                        .forGetter(param0x -> param0x.whitelist.stream().map(Block::defaultBlockState).collect(Collectors.toList())),
                    BlockState.CODEC.listOf().fieldOf("blacklist").forGetter(param0x -> ImmutableList.copyOf(param0x.blacklist)),
                    ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(128).forGetter(param0x -> param0x.tries),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xspread").orElse(7).forGetter(param0x -> param0x.xspread),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("yspread").orElse(3).forGetter(param0x -> param0x.yspread),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("zspread").orElse(7).forGetter(param0x -> param0x.zspread),
                    Codec.BOOL.fieldOf("can_replace").orElse(false).forGetter(param0x -> param0x.canReplace),
                    Codec.BOOL.fieldOf("project").orElse(true).forGetter(param0x -> param0x.project),
                    Codec.BOOL.fieldOf("need_water").orElse(false).forGetter(param0x -> param0x.needWater)
                )
                .apply(param0, RandomPatchConfiguration::new)
    );
    public final BlockStateProvider stateProvider;
    public final BlockPlacer blockPlacer;
    public final Set<Block> whitelist;
    public final Set<BlockState> blacklist;
    public final int tries;
    public final int xspread;
    public final int yspread;
    public final int zspread;
    public final boolean canReplace;
    public final boolean project;
    public final boolean needWater;

    private RandomPatchConfiguration(
        BlockStateProvider param0,
        BlockPlacer param1,
        List<BlockState> param2,
        List<BlockState> param3,
        int param4,
        int param5,
        int param6,
        int param7,
        boolean param8,
        boolean param9,
        boolean param10
    ) {
        this(
            param0,
            param1,
            param2.stream().map(BlockBehaviour.BlockStateBase::getBlock).collect(Collectors.toSet()),
            ImmutableSet.copyOf(param3),
            param4,
            param5,
            param6,
            param7,
            param8,
            param9,
            param10
        );
    }

    RandomPatchConfiguration(
        BlockStateProvider param0,
        BlockPlacer param1,
        Set<Block> param2,
        Set<BlockState> param3,
        int param4,
        int param5,
        int param6,
        int param7,
        boolean param8,
        boolean param9,
        boolean param10
    ) {
        this.stateProvider = param0;
        this.blockPlacer = param1;
        this.whitelist = param2;
        this.blacklist = param3;
        this.tries = param4;
        this.xspread = param5;
        this.yspread = param6;
        this.zspread = param7;
        this.canReplace = param8;
        this.project = param9;
        this.needWater = param10;
    }

    public static class GrassConfigurationBuilder {
        private final BlockStateProvider stateProvider;
        private final BlockPlacer blockPlacer;
        private Set<Block> whitelist = ImmutableSet.of();
        private Set<BlockState> blacklist = ImmutableSet.of();
        private int tries = 64;
        private int xspread = 7;
        private int yspread = 3;
        private int zspread = 7;
        private boolean canReplace;
        private boolean project = true;
        private boolean needWater;

        public GrassConfigurationBuilder(BlockStateProvider param0, BlockPlacer param1) {
            this.stateProvider = param0;
            this.blockPlacer = param1;
        }

        public RandomPatchConfiguration.GrassConfigurationBuilder whitelist(Set<Block> param0) {
            this.whitelist = param0;
            return this;
        }

        public RandomPatchConfiguration.GrassConfigurationBuilder blacklist(Set<BlockState> param0) {
            this.blacklist = param0;
            return this;
        }

        public RandomPatchConfiguration.GrassConfigurationBuilder tries(int param0) {
            this.tries = param0;
            return this;
        }

        public RandomPatchConfiguration.GrassConfigurationBuilder xspread(int param0) {
            this.xspread = param0;
            return this;
        }

        public RandomPatchConfiguration.GrassConfigurationBuilder yspread(int param0) {
            this.yspread = param0;
            return this;
        }

        public RandomPatchConfiguration.GrassConfigurationBuilder zspread(int param0) {
            this.zspread = param0;
            return this;
        }

        public RandomPatchConfiguration.GrassConfigurationBuilder canReplace() {
            this.canReplace = true;
            return this;
        }

        public RandomPatchConfiguration.GrassConfigurationBuilder noProjection() {
            this.project = false;
            return this;
        }

        public RandomPatchConfiguration.GrassConfigurationBuilder needWater() {
            this.needWater = true;
            return this;
        }

        public RandomPatchConfiguration build() {
            return new RandomPatchConfiguration(
                this.stateProvider,
                this.blockPlacer,
                this.whitelist,
                this.blacklist,
                this.tries,
                this.xspread,
                this.yspread,
                this.zspread,
                this.canReplace,
                this.project,
                this.needWater
            );
        }
    }
}
