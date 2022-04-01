package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.structure.MineShaftPieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class MineshaftFeature extends StructureFeature<MineshaftConfiguration> {
    public MineshaftFeature(Codec<MineshaftConfiguration> param0) {
        super(param0, PieceGeneratorSupplier.simple(MineshaftFeature::checkLocation, MineshaftFeature::generatePieces));
    }

    private static boolean checkLocation(PieceGeneratorSupplier.Context<MineshaftConfiguration> param0x) {
        WorldgenRandom var0 = new WorldgenRandom(new LegacyRandomSource(0L));
        var0.setLargeFeatureSeed(param0x.seed(), param0x.chunkPos().x, param0x.chunkPos().z);
        double var1 = (double)param0x.config().probability;
        return var0.nextDouble() >= var1
            ? false
            : param0x.validBiome()
                .test(
                    param0x.chunkGenerator()
                        .getNoiseBiome(
                            QuartPos.fromBlock(param0x.chunkPos().getMiddleBlockX()),
                            QuartPos.fromBlock(50),
                            QuartPos.fromBlock(param0x.chunkPos().getMiddleBlockZ())
                        )
                );
    }

    private static void generatePieces(StructurePiecesBuilder param0x, PieceGenerator.Context<MineshaftConfiguration> param1) {
        MineShaftPieces.MineShaftRoom var0 = new MineShaftPieces.MineShaftRoom(
            0, param1.random(), param1.chunkPos().getBlockX(2), param1.chunkPos().getBlockZ(2), param1.config().type
        );
        param0x.addPiece(var0);
        var0.addChildren(var0, param0x, param1.random());
        int var1 = param1.chunkGenerator().getSeaLevel();
        if (param1.config().type == MineshaftFeature.Type.MESA) {
            BlockPos var2 = param0x.getBoundingBox().getCenter();
            int var3 = param1.chunkGenerator().getBaseHeight(var2.getX(), var2.getZ(), Heightmap.Types.WORLD_SURFACE_WG, param1.heightAccessor());
            int var4 = var3 <= var1 ? var1 : Mth.randomBetweenInclusive(param1.random(), var1, var3);
            int var5 = var4 - var2.getY();
            param0x.offsetPiecesVertically(var5);
        } else {
            param0x.moveBelowSeaLevel(var1, param1.chunkGenerator().getMinY(), param1.random(), 10);
        }

    }

    public static enum Type implements StringRepresentable {
        NORMAL("normal", Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.OAK_FENCE),
        MESA("mesa", Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_FENCE);

        public static final Codec<MineshaftFeature.Type> CODEC = StringRepresentable.fromEnum(MineshaftFeature.Type::values, MineshaftFeature.Type::byName);
        private static final Map<String, MineshaftFeature.Type> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(MineshaftFeature.Type::getName, param0 -> param0));
        private final String name;
        private final BlockState woodState;
        private final BlockState planksState;
        private final BlockState fenceState;

        private Type(String param0, Block param1, Block param2, Block param3) {
            this.name = param0;
            this.woodState = param1.defaultBlockState();
            this.planksState = param2.defaultBlockState();
            this.fenceState = param3.defaultBlockState();
        }

        public String getName() {
            return this.name;
        }

        private static MineshaftFeature.Type byName(String param0) {
            return BY_NAME.get(param0);
        }

        public static MineshaftFeature.Type byId(int param0) {
            return param0 >= 0 && param0 < values().length ? values()[param0] : NORMAL;
        }

        public BlockState getWoodState() {
            return this.woodState;
        }

        public BlockState getPlanksState() {
            return this.planksState;
        }

        public BlockState getFenceState() {
            return this.fenceState;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
