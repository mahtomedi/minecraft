package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ForestFlowerProvider extends BlockStateProvider {
    private static final BlockState[] FLOWERS = new BlockState[]{
        Blocks.DANDELION.defaultBlockState(),
        Blocks.POPPY.defaultBlockState(),
        Blocks.ALLIUM.defaultBlockState(),
        Blocks.AZURE_BLUET.defaultBlockState(),
        Blocks.RED_TULIP.defaultBlockState(),
        Blocks.ORANGE_TULIP.defaultBlockState(),
        Blocks.WHITE_TULIP.defaultBlockState(),
        Blocks.PINK_TULIP.defaultBlockState(),
        Blocks.OXEYE_DAISY.defaultBlockState(),
        Blocks.CORNFLOWER.defaultBlockState(),
        Blocks.LILY_OF_THE_VALLEY.defaultBlockState()
    };

    public ForestFlowerProvider() {
        super(BlockStateProviderType.FOREST_FLOWER_PROVIDER);
    }

    public <T> ForestFlowerProvider(Dynamic<T> param0) {
        this();
    }

    @Override
    public BlockState getState(Random param0, BlockPos param1) {
        double var0 = Mth.clamp((1.0 + Biome.BIOME_INFO_NOISE.getValue((double)param1.getX() / 48.0, (double)param1.getZ() / 48.0, false)) / 2.0, 0.0, 0.9999);
        return FLOWERS[(int)(var0 * (double)FLOWERS.length)];
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("type"), param0.createString(Registry.BLOCKSTATE_PROVIDER_TYPES.getKey(this.type).toString()));
        return new Dynamic<>(param0, param0.createMap(var0.build())).getValue();
    }
}
