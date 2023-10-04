package net.minecraft.world.level.block.grower;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class TreeGrower {
    private static final Map<String, TreeGrower> GROWERS = new Object2ObjectArrayMap<>();
    public static final Codec<TreeGrower> CODEC = ExtraCodecs.stringResolverCodec(param0 -> param0.name, GROWERS::get);
    public static final TreeGrower OAK = new TreeGrower(
        "oak",
        0.1F,
        Optional.empty(),
        Optional.empty(),
        Optional.of(TreeFeatures.OAK),
        Optional.of(TreeFeatures.FANCY_OAK),
        Optional.of(TreeFeatures.OAK_BEES_005),
        Optional.of(TreeFeatures.FANCY_OAK_BEES_005)
    );
    public static final TreeGrower SPRUCE = new TreeGrower(
        "spruce",
        0.5F,
        Optional.of(TreeFeatures.MEGA_SPRUCE),
        Optional.of(TreeFeatures.MEGA_PINE),
        Optional.of(TreeFeatures.SPRUCE),
        Optional.empty(),
        Optional.empty(),
        Optional.empty()
    );
    public static final TreeGrower MANGROVE = new TreeGrower(
        "mangrove",
        0.85F,
        Optional.empty(),
        Optional.empty(),
        Optional.of(TreeFeatures.MANGROVE),
        Optional.of(TreeFeatures.TALL_MANGROVE),
        Optional.empty(),
        Optional.empty()
    );
    public static final TreeGrower AZALEA = new TreeGrower("azalea", Optional.empty(), Optional.of(TreeFeatures.AZALEA_TREE), Optional.empty());
    public static final TreeGrower BIRCH = new TreeGrower("birch", Optional.empty(), Optional.of(TreeFeatures.BIRCH), Optional.of(TreeFeatures.BIRCH_BEES_005));
    public static final TreeGrower JUNGLE = new TreeGrower(
        "jungle", Optional.of(TreeFeatures.MEGA_JUNGLE_TREE), Optional.of(TreeFeatures.JUNGLE_TREE_NO_VINE), Optional.empty()
    );
    public static final TreeGrower ACACIA = new TreeGrower("acacia", Optional.empty(), Optional.of(TreeFeatures.ACACIA), Optional.empty());
    public static final TreeGrower CHERRY = new TreeGrower(
        "cherry", Optional.empty(), Optional.of(TreeFeatures.CHERRY), Optional.of(TreeFeatures.CHERRY_BEES_005)
    );
    public static final TreeGrower DARK_OAK = new TreeGrower("dark_oak", Optional.of(TreeFeatures.DARK_OAK), Optional.empty(), Optional.empty());
    private final String name;
    private final float secondaryChance;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> megaTree;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryMegaTree;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> tree;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryTree;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> flowers;
    private final Optional<ResourceKey<ConfiguredFeature<?, ?>>> secondaryFlowers;

    public TreeGrower(
        String param0,
        Optional<ResourceKey<ConfiguredFeature<?, ?>>> param1,
        Optional<ResourceKey<ConfiguredFeature<?, ?>>> param2,
        Optional<ResourceKey<ConfiguredFeature<?, ?>>> param3
    ) {
        this(param0, 0.0F, param1, Optional.empty(), param2, Optional.empty(), param3, Optional.empty());
    }

    public TreeGrower(
        String param0,
        float param1,
        Optional<ResourceKey<ConfiguredFeature<?, ?>>> param2,
        Optional<ResourceKey<ConfiguredFeature<?, ?>>> param3,
        Optional<ResourceKey<ConfiguredFeature<?, ?>>> param4,
        Optional<ResourceKey<ConfiguredFeature<?, ?>>> param5,
        Optional<ResourceKey<ConfiguredFeature<?, ?>>> param6,
        Optional<ResourceKey<ConfiguredFeature<?, ?>>> param7
    ) {
        this.name = param0;
        this.secondaryChance = param1;
        this.megaTree = param2;
        this.secondaryMegaTree = param3;
        this.tree = param4;
        this.secondaryTree = param5;
        this.flowers = param6;
        this.secondaryFlowers = param7;
        GROWERS.put(param0, this);
    }

    @Nullable
    private ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource param0, boolean param1) {
        if (param0.nextFloat() < this.secondaryChance) {
            if (param1 && this.secondaryFlowers.isPresent()) {
                return this.secondaryFlowers.get();
            }

            if (this.secondaryTree.isPresent()) {
                return this.secondaryTree.get();
            }
        }

        return param1 && this.flowers.isPresent() ? this.flowers.get() : this.tree.orElse(null);
    }

    @Nullable
    private ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource param0) {
        return this.secondaryMegaTree.isPresent() && param0.nextFloat() < this.secondaryChance ? this.secondaryMegaTree.get() : this.megaTree.orElse(null);
    }

    public boolean growTree(ServerLevel param0, ChunkGenerator param1, BlockPos param2, BlockState param3, RandomSource param4) {
        ResourceKey<ConfiguredFeature<?, ?>> var0 = this.getConfiguredMegaFeature(param4);
        if (var0 != null) {
            Holder<ConfiguredFeature<?, ?>> var1 = param0.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolder(var0).orElse(null);
            if (var1 != null) {
                for(int var2 = 0; var2 >= -1; --var2) {
                    for(int var3 = 0; var3 >= -1; --var3) {
                        if (isTwoByTwoSapling(param3, param0, param2, var2, var3)) {
                            ConfiguredFeature<?, ?> var4 = var1.value();
                            BlockState var5 = Blocks.AIR.defaultBlockState();
                            param0.setBlock(param2.offset(var2, 0, var3), var5, 4);
                            param0.setBlock(param2.offset(var2 + 1, 0, var3), var5, 4);
                            param0.setBlock(param2.offset(var2, 0, var3 + 1), var5, 4);
                            param0.setBlock(param2.offset(var2 + 1, 0, var3 + 1), var5, 4);
                            if (var4.place(param0, param1, param4, param2.offset(var2, 0, var3))) {
                                return true;
                            }

                            param0.setBlock(param2.offset(var2, 0, var3), param3, 4);
                            param0.setBlock(param2.offset(var2 + 1, 0, var3), param3, 4);
                            param0.setBlock(param2.offset(var2, 0, var3 + 1), param3, 4);
                            param0.setBlock(param2.offset(var2 + 1, 0, var3 + 1), param3, 4);
                            return false;
                        }
                    }
                }
            }
        }

        ResourceKey<ConfiguredFeature<?, ?>> var6 = this.getConfiguredFeature(param4, this.hasFlowers(param0, param2));
        if (var6 == null) {
            return false;
        } else {
            Holder<ConfiguredFeature<?, ?>> var7 = param0.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolder(var6).orElse(null);
            if (var7 == null) {
                return false;
            } else {
                ConfiguredFeature<?, ?> var8 = var7.value();
                BlockState var9 = param0.getFluidState(param2).createLegacyBlock();
                param0.setBlock(param2, var9, 4);
                if (var8.place(param0, param1, param4, param2)) {
                    if (param0.getBlockState(param2) == var9) {
                        param0.sendBlockUpdated(param2, param3, var9, 2);
                    }

                    return true;
                } else {
                    param0.setBlock(param2, param3, 4);
                    return false;
                }
            }
        }
    }

    private static boolean isTwoByTwoSapling(BlockState param0, BlockGetter param1, BlockPos param2, int param3, int param4) {
        Block var0 = param0.getBlock();
        return param1.getBlockState(param2.offset(param3, 0, param4)).is(var0)
            && param1.getBlockState(param2.offset(param3 + 1, 0, param4)).is(var0)
            && param1.getBlockState(param2.offset(param3, 0, param4 + 1)).is(var0)
            && param1.getBlockState(param2.offset(param3 + 1, 0, param4 + 1)).is(var0);
    }

    private boolean hasFlowers(LevelAccessor param0, BlockPos param1) {
        for(BlockPos var0 : BlockPos.MutableBlockPos.betweenClosed(param1.below().north(2).west(2), param1.above().south(2).east(2))) {
            if (param0.getBlockState(var0).is(BlockTags.FLOWERS)) {
                return true;
            }
        }

        return false;
    }
}
