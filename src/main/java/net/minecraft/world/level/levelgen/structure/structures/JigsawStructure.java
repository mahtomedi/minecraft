package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.datafixers.Products.P10;
import com.mojang.datafixers.Products.P4;
import com.mojang.datafixers.Products.P6;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public final class JigsawStructure extends Structure {
    public static final Codec<JigsawStructure> CODEC = RecordCodecBuilder.create(param0 -> jigsawCodec(param0).apply(param0, JigsawStructure::new));
    private final Holder<StructureTemplatePool> startPool;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    public static P10<Mu<JigsawStructure>, HolderSet<Biome>, Map<MobCategory, StructureSpawnOverride>, GenerationStep.Decoration, Boolean, Holder<StructureTemplatePool>, Integer, HeightProvider, Boolean, Optional<Heightmap.Types>, Integer> jigsawCodec(
        Instance<JigsawStructure> param0
    ) {
        P4<Mu<JigsawStructure>, HolderSet<Biome>, Map<MobCategory, StructureSpawnOverride>, GenerationStep.Decoration, Boolean> var0 = codec(param0);
        P6<Mu<JigsawStructure>, Holder<StructureTemplatePool>, Integer, HeightProvider, Boolean, Optional<Heightmap.Types>, Integer> var1 = param0.group(
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(param0x -> param0x.startPool),
            Codec.intRange(0, 7).fieldOf("size").forGetter(param0x -> param0x.maxDepth),
            HeightProvider.CODEC.fieldOf("start_height").forGetter(param0x -> param0x.startHeight),
            Codec.BOOL.fieldOf("use_expansion_hack").forGetter(param0x -> param0x.useExpansionHack),
            Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(param0x -> param0x.projectStartToHeightmap),
            Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(param0x -> param0x.maxDistanceFromCenter)
        );
        return new P10<>(var0.t1(), var0.t2(), var0.t3(), var0.t4(), var1.t1(), var1.t2(), var1.t3(), var1.t4(), var1.t5(), var1.t6());
    }

    public JigsawStructure(
        HolderSet<Biome> param0,
        Map<MobCategory, StructureSpawnOverride> param1,
        GenerationStep.Decoration param2,
        boolean param3,
        Holder<StructureTemplatePool> param4,
        int param5,
        HeightProvider param6,
        boolean param7,
        Optional<Heightmap.Types> param8,
        int param9
    ) {
        super(param0, param1, param2, param3);
        this.startPool = param4;
        this.maxDepth = param5;
        this.startHeight = param6;
        this.useExpansionHack = param7;
        this.projectStartToHeightmap = param8;
        this.maxDistanceFromCenter = param9;
    }

    public JigsawStructure(
        HolderSet<Biome> param0,
        Map<MobCategory, StructureSpawnOverride> param1,
        GenerationStep.Decoration param2,
        boolean param3,
        Holder<StructureTemplatePool> param4,
        int param5,
        HeightProvider param6,
        boolean param7,
        Heightmap.Types param8
    ) {
        this(param0, param1, param2, param3, param4, param5, param6, param7, Optional.of(param8), 80);
    }

    public JigsawStructure(
        HolderSet<Biome> param0,
        Map<MobCategory, StructureSpawnOverride> param1,
        GenerationStep.Decoration param2,
        boolean param3,
        Holder<StructureTemplatePool> param4,
        int param5,
        HeightProvider param6,
        boolean param7
    ) {
        this(param0, param1, param2, param3, param4, param5, param6, param7, Optional.empty(), 80);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        ChunkPos var0 = param0.chunkPos();
        int var1 = this.startHeight.sample(param0.random(), new WorldGenerationContext(param0.chunkGenerator(), param0.heightAccessor()));
        BlockPos var2 = new BlockPos(var0.getMinBlockX(), var1, var0.getMinBlockZ());
        Pools.bootstrap();
        return JigsawPlacement.addPieces(
            param0,
            this.startPool,
            this.maxDepth,
            PoolElementStructurePiece::new,
            var2,
            this.useExpansionHack,
            this.projectStartToHeightmap,
            this.maxDistanceFromCenter
        );
    }

    @Override
    public StructureType<?> type() {
        return StructureType.JIGSAW;
    }
}
