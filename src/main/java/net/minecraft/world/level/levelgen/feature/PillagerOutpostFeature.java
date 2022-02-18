package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class PillagerOutpostFeature extends JigsawFeature {
    public PillagerOutpostFeature(Codec<JigsawConfiguration> param0) {
        super(param0, 0, true, true, PillagerOutpostFeature::checkLocation);
    }

    private static boolean checkLocation(PieceGeneratorSupplier.Context<JigsawConfiguration> param0x) {
        ChunkPos var0 = param0x.chunkPos();
        int var1 = var0.x >> 4;
        int var2 = var0.z >> 4;
        WorldgenRandom var3 = new WorldgenRandom(new LegacyRandomSource(0L));
        var3.setSeed((long)(var1 ^ var2 << 4) ^ param0x.seed());
        var3.nextInt();
        if (var3.nextInt(5) != 0) {
            return false;
        } else {
            return !param0x.chunkGenerator().hasFeatureChunkInRange(BuiltinStructureSets.VILLAGES, var0.x, var0.z, 10);
        }
    }
}
