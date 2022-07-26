package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NetherWorldCarver extends CaveWorldCarver {
    public NetherWorldCarver(Codec<CaveCarverConfiguration> param0) {
        super(param0);
        this.liquids = ImmutableSet.of(Fluids.LAVA, Fluids.WATER);
    }

    @Override
    protected int getCaveBound() {
        return 10;
    }

    @Override
    protected float getThickness(RandomSource param0) {
        return (param0.nextFloat() * 2.0F + param0.nextFloat()) * 2.0F;
    }

    @Override
    protected double getYScale() {
        return 5.0;
    }

    protected boolean carveBlock(
        CarvingContext param0,
        CaveCarverConfiguration param1,
        ChunkAccess param2,
        Function<BlockPos, Holder<Biome>> param3,
        CarvingMask param4,
        BlockPos.MutableBlockPos param5,
        BlockPos.MutableBlockPos param6,
        Aquifer param7,
        MutableBoolean param8
    ) {
        if (this.canReplaceBlock(param1, param2.getBlockState(param5))) {
            BlockState var0;
            if (param5.getY() <= param0.getMinGenY() + 31) {
                var0 = LAVA.createLegacyBlock();
            } else {
                var0 = CAVE_AIR;
            }

            param2.setBlockState(param5, var0, false);
            return true;
        } else {
            return false;
        }
    }
}
