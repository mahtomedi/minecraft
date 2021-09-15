package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.MineShaftPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class MineshaftFeature extends StructureFeature<MineshaftConfiguration> {
    public MineshaftFeature(Codec<MineshaftConfiguration> param0) {
        super(param0);
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0,
        BiomeSource param1,
        long param2,
        WorldgenRandom param3,
        ChunkPos param4,
        ChunkPos param5,
        MineshaftConfiguration param6,
        LevelHeightAccessor param7
    ) {
        param3.setLargeFeatureSeed(param2, param4.x, param4.z);
        double var0 = (double)param6.probability;
        return param3.nextDouble() < var0;
    }

    @Override
    public StructureFeature.StructureStartFactory<MineshaftConfiguration> getStartFactory() {
        return MineshaftFeature.MineShaftStart::new;
    }

    public static class MineShaftStart extends StructureStart<MineshaftConfiguration> {
        public MineShaftStart(StructureFeature<MineshaftConfiguration> param0, ChunkPos param1, int param2, long param3) {
            super(param0, param1, param2, param3);
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            ChunkPos param3,
            MineshaftConfiguration param4,
            LevelHeightAccessor param5,
            Predicate<Biome> param6
        ) {
            if (param6.test(
                param1.getNoiseBiome(QuartPos.fromBlock(param3.getMiddleBlockX()), QuartPos.fromBlock(50), QuartPos.fromBlock(param3.getMiddleBlockZ()))
            )) {
                MineShaftPieces.MineShaftRoom var0 = new MineShaftPieces.MineShaftRoom(0, this.random, param3.getBlockX(2), param3.getBlockZ(2), param4.type);
                this.addPiece(var0);
                var0.addChildren(var0, this, this.random);
                if (param4.type == MineshaftFeature.Type.MESA) {
                    int var1 = -5;
                    BoundingBox var2 = this.getBoundingBox();
                    int var3 = param1.getSeaLevel() - var2.maxY() + var2.getYSpan() / 2 - -5;
                    this.offsetPiecesVertically(var3);
                } else {
                    this.moveBelowSeaLevel(param1.getSeaLevel(), param1.getMinY(), this.random, 10);
                }

            }
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
