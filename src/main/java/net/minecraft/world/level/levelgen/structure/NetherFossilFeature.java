package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.feature.configurations.RangeConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class NetherFossilFeature extends NoiseAffectingStructureFeature<RangeConfiguration> {
    public NetherFossilFeature(Codec<RangeConfiguration> param0) {
        super(param0, NetherFossilFeature::generatePieces);
    }

    private static void generatePieces(StructurePiecesBuilder param0x, RangeConfiguration param1, PieceGenerator.Context param2) {
        int var0 = param2.chunkPos().getMinBlockX() + param2.random().nextInt(16);
        int var1 = param2.chunkPos().getMinBlockZ() + param2.random().nextInt(16);
        int var2 = param2.chunkGenerator().getSeaLevel();
        WorldGenerationContext var3 = new WorldGenerationContext(param2.chunkGenerator(), param2.heightAccessor());
        int var4 = param1.height.sample(param2.random(), var3);
        NoiseColumn var5 = param2.chunkGenerator().getBaseColumn(var0, var1, param2.heightAccessor());
        BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos(var0, var4, var1);

        while(var4 > var2) {
            BlockState var7 = var5.getBlock(var4);
            BlockState var8 = var5.getBlock(--var4);
            if (var7.isAir() && (var8.is(Blocks.SOUL_SAND) || var8.isFaceSturdy(EmptyBlockGetter.INSTANCE, var6.setY(var4), Direction.UP))) {
                break;
            }
        }

        if (var4 > var2) {
            if (param2.validBiome().test(param2.chunkGenerator().getNoiseBiome(QuartPos.fromBlock(var0), QuartPos.fromBlock(var4), QuartPos.fromBlock(var1)))) {
                NetherFossilPieces.addPieces(param2.structureManager(), param0x, param2.random(), new BlockPos(var0, var4, var1));
            }
        }
    }
}
