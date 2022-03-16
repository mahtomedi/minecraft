package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class NetherFossilStructure extends Structure {
    public static final Codec<NetherFossilStructure> CODEC = RecordCodecBuilder.create(
        param0 -> codec(param0).and(HeightProvider.CODEC.fieldOf("height").forGetter(param0x -> param0x.height)).apply(param0, NetherFossilStructure::new)
    );
    public final HeightProvider height;

    public NetherFossilStructure(
        HolderSet<Biome> param0, Map<MobCategory, StructureSpawnOverride> param1, GenerationStep.Decoration param2, boolean param3, HeightProvider param4
    ) {
        super(param0, param1, param2, param3);
        this.height = param4;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        WorldgenRandom var0 = param0.random();
        int var1 = param0.chunkPos().getMinBlockX() + var0.nextInt(16);
        int var2 = param0.chunkPos().getMinBlockZ() + var0.nextInt(16);
        int var3 = param0.chunkGenerator().getSeaLevel();
        WorldGenerationContext var4 = new WorldGenerationContext(param0.chunkGenerator(), param0.heightAccessor());
        int var5 = this.height.sample(var0, var4);
        NoiseColumn var6 = param0.chunkGenerator().getBaseColumn(var1, var2, param0.heightAccessor(), param0.randomState());
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
        } else {
            BlockPos var10 = new BlockPos(var1, var5, var2);
            return Optional.of(
                new Structure.GenerationStub(var10, param3 -> NetherFossilPieces.addPieces(param0.structureTemplateManager(), param3, var0, var10))
            );
        }
    }

    @Override
    public StructureType<?> type() {
        return StructureType.NETHER_FOSSIL;
    }
}
