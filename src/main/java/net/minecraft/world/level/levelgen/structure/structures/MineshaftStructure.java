package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class MineshaftStructure extends Structure {
    public static final Codec<MineshaftStructure> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(settingsCodec(param0), MineshaftStructure.Type.CODEC.fieldOf("mineshaft_type").forGetter(param0x -> param0x.type))
                .apply(param0, MineshaftStructure::new)
    );
    private final MineshaftStructure.Type type;

    public MineshaftStructure(Structure.StructureSettings param0, MineshaftStructure.Type param1) {
        super(param0);
        this.type = param1;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        param0.random().nextDouble();
        ChunkPos var0 = param0.chunkPos();
        BlockPos var1 = new BlockPos(var0.getMiddleBlockX(), 50, var0.getMinBlockZ());
        StructurePiecesBuilder var2 = new StructurePiecesBuilder();
        int var3 = this.generatePiecesAndAdjust(var2, param0);
        return Optional.of(new Structure.GenerationStub(var1.offset(0, var3, 0), Either.right(var2)));
    }

    private int generatePiecesAndAdjust(StructurePiecesBuilder param0, Structure.GenerationContext param1) {
        ChunkPos var0 = param1.chunkPos();
        WorldgenRandom var1 = param1.random();
        ChunkGenerator var2 = param1.chunkGenerator();
        MineshaftPieces.MineShaftRoom var3 = new MineshaftPieces.MineShaftRoom(0, var1, var0.getBlockX(2), var0.getBlockZ(2), this.type);
        param0.addPiece(var3);
        var3.addChildren(var3, param0, var1);
        int var4 = var2.getSeaLevel();
        if (this.type == MineshaftStructure.Type.MESA) {
            BlockPos var5 = param0.getBoundingBox().getCenter();
            int var6 = var2.getBaseHeight(var5.getX(), var5.getZ(), Heightmap.Types.WORLD_SURFACE_WG, param1.heightAccessor(), param1.randomState());
            int var7 = var6 <= var4 ? var4 : Mth.randomBetweenInclusive(var1, var4, var6);
            int var8 = var7 - var5.getY();
            param0.offsetPiecesVertically(var8);
            return var8;
        } else {
            return param0.moveBelowSeaLevel(var4, var2.getMinY(), var1, 10);
        }
    }

    @Override
    public StructureType<?> type() {
        return StructureType.MINESHAFT;
    }

    public static enum Type implements StringRepresentable {
        NORMAL("normal", Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.OAK_FENCE),
        MESA("mesa", Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_FENCE);

        public static final Codec<MineshaftStructure.Type> CODEC = StringRepresentable.fromEnum(MineshaftStructure.Type::values);
        private static final IntFunction<MineshaftStructure.Type> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
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

        public static MineshaftStructure.Type byId(int param0) {
            return BY_ID.apply(param0);
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
