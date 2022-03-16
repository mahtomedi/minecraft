package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class MineshaftStructure extends Structure {
    public static final Codec<MineshaftStructure> CODEC = RecordCodecBuilder.create(
        param0 -> codec(param0)
                .and(MineshaftStructure.Type.CODEC.fieldOf("mineshaft_type").forGetter(param0x -> param0x.type))
                .apply(param0, MineshaftStructure::new)
    );
    private final MineshaftStructure.Type type;

    public MineshaftStructure(
        HolderSet<Biome> param0,
        Map<MobCategory, StructureSpawnOverride> param1,
        GenerationStep.Decoration param2,
        boolean param3,
        MineshaftStructure.Type param4
    ) {
        super(param0, param1, param2, param3);
        this.type = param4;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        param0.random().nextDouble();
        ChunkPos var0 = param0.chunkPos();
        BlockPos var1 = new BlockPos(var0.getMiddleBlockX(), 50, var0.getMinBlockZ());
        return Optional.of(new Structure.GenerationStub(var1, param2 -> this.generatePieces(param2, var1, param0)));
    }

    private void generatePieces(StructurePiecesBuilder param0, BlockPos param1, Structure.GenerationContext param2) {
        ChunkPos var0 = param2.chunkPos();
        WorldgenRandom var1 = param2.random();
        ChunkGenerator var2 = param2.chunkGenerator();
        MineshaftPieces.MineShaftRoom var3 = new MineshaftPieces.MineShaftRoom(0, var1, var0.getBlockX(2), var0.getBlockZ(2), this.type);
        param0.addPiece(var3);
        var3.addChildren(var3, param0, var1);
        int var4 = var2.getSeaLevel();
        if (this.type == MineshaftStructure.Type.MESA) {
            BlockPos var5 = param0.getBoundingBox().getCenter();
            int var6 = var2.getBaseHeight(var5.getX(), var5.getZ(), Heightmap.Types.WORLD_SURFACE_WG, param2.heightAccessor(), param2.randomState());
            int var7 = var6 <= var4 ? var4 : Mth.randomBetweenInclusive(var1, var4, var6);
            int var8 = var7 - var5.getY();
            param0.offsetPiecesVertically(var8);
        } else {
            param0.moveBelowSeaLevel(var4, var2.getMinY(), var1, 10);
        }

    }

    @Override
    public StructureType<?> type() {
        return StructureType.MINESHAFT;
    }

    public static enum Type implements StringRepresentable {
        NORMAL("normal", Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.OAK_FENCE),
        MESA("mesa", Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_FENCE);

        public static final Codec<MineshaftStructure.Type> CODEC = StringRepresentable.fromEnum(
            MineshaftStructure.Type::values, MineshaftStructure.Type::byName
        );
        private static final Map<String, MineshaftStructure.Type> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(MineshaftStructure.Type::getName, param0 -> param0));
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

        private static MineshaftStructure.Type byName(String param0) {
            return BY_NAME.get(param0);
        }

        public static MineshaftStructure.Type byId(int param0) {
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
