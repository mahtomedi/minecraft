package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RangeConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class NetherFossilFeature extends StructureFeature<RangeConfiguration> {
    public NetherFossilFeature(Codec<RangeConfiguration> param0) {
        super(param0, NetherFossilFeature::pieceGeneratorSupplier);
    }

    private static Optional<PieceGenerator<RangeConfiguration>> pieceGeneratorSupplier(PieceGeneratorSupplier.Context<RangeConfiguration> param0x) {
        WorldgenRandom var0 = new WorldgenRandom(new LegacyRandomSource(0L));
        var0.setLargeFeatureSeed(param0x.seed(), param0x.chunkPos().x, param0x.chunkPos().z);
        int var1 = param0x.chunkPos().getMinBlockX() + var0.nextInt(16);
        int var2 = param0x.chunkPos().getMinBlockZ() + var0.nextInt(16);
        int var3 = param0x.chunkGenerator().getSeaLevel();
        WorldGenerationContext var4 = new WorldGenerationContext(param0x.chunkGenerator(), param0x.heightAccessor());
        int var5 = param0x.config().height.sample(var0, var4);
        NoiseColumn var6 = param0x.chunkGenerator().getBaseColumn(var1, var2, param0x.heightAccessor());
        BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos(var1, var5, var2);

        while(var5 > var3) {
            BlockState var8 = var6.getBlock(var5);
            BlockState var9 = var6.getBlock(--var5);
            if (var8.isAir() && (var9.is(Blocks.SOUL_SAND) || var9.isFaceSturdy(EmptyBlockGetter.INSTANCE, var7.setY(var5), Direction.UP))) {
                break;
            }
        }

        if (var5 <= var3) {
            return Optional.empty();
        } else if (!param0x.validBiome()
            .test(param0x.chunkGenerator().getNoiseBiome(QuartPos.fromBlock(var1), QuartPos.fromBlock(var5), QuartPos.fromBlock(var2)))) {
            return Optional.empty();
        } else {
            BlockPos var10 = new BlockPos(var1, var5, var2);
            return Optional.of((param3, param4) -> NetherFossilPieces.addPieces(param0x.structureManager(), param3, var0, var10));
        }
    }
}
