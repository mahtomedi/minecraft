package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacer;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class RandomPatchConfiguration implements FeatureConfiguration {
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

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("state_provider"), this.stateProvider.serialize(param0))
            .put(param0.createString("block_placer"), this.blockPlacer.serialize(param0))
            .put(
                param0.createString("whitelist"),
                param0.createList(this.whitelist.stream().map(param1 -> BlockState.serialize(param0, param1.defaultBlockState()).getValue()))
            )
            .put(param0.createString("blacklist"), param0.createList(this.blacklist.stream().map(param1 -> BlockState.serialize(param0, param1).getValue())))
            .put(param0.createString("tries"), param0.createInt(this.tries))
            .put(param0.createString("xspread"), param0.createInt(this.xspread))
            .put(param0.createString("yspread"), param0.createInt(this.yspread))
            .put(param0.createString("zspread"), param0.createInt(this.zspread))
            .put(param0.createString("can_replace"), param0.createBoolean(this.canReplace))
            .put(param0.createString("project"), param0.createBoolean(this.project))
            .put(param0.createString("need_water"), param0.createBoolean(this.needWater));
        return new Dynamic<>(param0, param0.createMap(var0.build()));
    }

    public static <T> RandomPatchConfiguration deserialize(Dynamic<T> param0) {
        BlockStateProviderType<?> var0 = Registry.BLOCKSTATE_PROVIDER_TYPES
            .get(new ResourceLocation(param0.get("state_provider").get("type").asString().orElseThrow(RuntimeException::new)));
        BlockPlacerType<?> var1 = Registry.BLOCK_PLACER_TYPES
            .get(new ResourceLocation(param0.get("block_placer").get("type").asString().orElseThrow(RuntimeException::new)));
        return new RandomPatchConfiguration(
            var0.deserialize(param0.get("state_provider").orElseEmptyMap()),
            var1.deserialize(param0.get("block_placer").orElseEmptyMap()),
            param0.get("whitelist").asList(BlockState::deserialize).stream().map(BlockBehaviour.BlockStateBase::getBlock).collect(Collectors.toSet()),
            Sets.newHashSet(param0.get("blacklist").asList(BlockState::deserialize)),
            param0.get("tries").asInt(128),
            param0.get("xspread").asInt(7),
            param0.get("yspread").asInt(3),
            param0.get("zspread").asInt(7),
            param0.get("can_replace").asBoolean(false),
            param0.get("project").asBoolean(true),
            param0.get("need_water").asBoolean(false)
        );
    }

    public static RandomPatchConfiguration random(Random param0) {
        return new RandomPatchConfiguration(
            BlockStateProvider.random(param0),
            BlockPlacer.random(param0),
            ImmutableSet.of(),
            ImmutableSet.of(),
            param0.nextInt(50),
            1 + param0.nextInt(20),
            1 + param0.nextInt(20),
            1 + param0.nextInt(20),
            param0.nextBoolean(),
            param0.nextBoolean(),
            param0.nextInt(7) == 0
        );
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
        private boolean needWater = false;

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
