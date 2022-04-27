package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public final class JigsawStructure extends Structure {
    public static final int MAX_TOTAL_STRUCTURE_RANGE = 128;
    public static final Codec<JigsawStructure> CODEC = RecordCodecBuilder.<JigsawStructure>mapCodec(
            param0 -> param0.group(
                        settingsCodec(param0),
                        StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(param0x -> param0x.startPool),
                        ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(param0x -> param0x.startJigsawName),
                        Codec.intRange(0, 7).fieldOf("size").forGetter(param0x -> param0x.maxDepth),
                        HeightProvider.CODEC.fieldOf("start_height").forGetter(param0x -> param0x.startHeight),
                        Codec.BOOL.fieldOf("use_expansion_hack").forGetter(param0x -> param0x.useExpansionHack),
                        Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(param0x -> param0x.projectStartToHeightmap),
                        Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(param0x -> param0x.maxDistanceFromCenter)
                    )
                    .apply(param0, JigsawStructure::new)
        )
        .flatXmap(verifyRange(), verifyRange())
        .codec();
    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    private static Function<JigsawStructure, DataResult<JigsawStructure>> verifyRange() {
        return param0 -> {
            int var0 = switch(param0.terrainAdaptation()) {
                case NONE -> 0;
                case BURY, BEARD_THIN, BEARD_BOX -> 12;
            };
            return param0.maxDistanceFromCenter + var0 > 128
                ? DataResult.error("Structure size including terrain adaptation must not exceed 128")
                : DataResult.success(param0);
        };
    }

    public JigsawStructure(
        Structure.StructureSettings param0,
        Holder<StructureTemplatePool> param1,
        Optional<ResourceLocation> param2,
        int param3,
        HeightProvider param4,
        boolean param5,
        Optional<Heightmap.Types> param6,
        int param7
    ) {
        super(param0);
        this.startPool = param1;
        this.startJigsawName = param2;
        this.maxDepth = param3;
        this.startHeight = param4;
        this.useExpansionHack = param5;
        this.projectStartToHeightmap = param6;
        this.maxDistanceFromCenter = param7;
    }

    public JigsawStructure(
        Structure.StructureSettings param0, Holder<StructureTemplatePool> param1, int param2, HeightProvider param3, boolean param4, Heightmap.Types param5
    ) {
        this(param0, param1, Optional.empty(), param2, param3, param4, Optional.of(param5), 80);
    }

    public JigsawStructure(Structure.StructureSettings param0, Holder<StructureTemplatePool> param1, int param2, HeightProvider param3, boolean param4) {
        this(param0, param1, Optional.empty(), param2, param3, param4, Optional.empty(), 80);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        ChunkPos var0 = param0.chunkPos();
        int var1 = this.startHeight.sample(param0.random(), new WorldGenerationContext(param0.chunkGenerator(), param0.heightAccessor()));
        BlockPos var2 = new BlockPos(var0.getMinBlockX(), var1, var0.getMinBlockZ());
        Pools.bootstrap();
        return JigsawPlacement.addPieces(
            param0, this.startPool, this.startJigsawName, this.maxDepth, var2, this.useExpansionHack, this.projectStartToHeightmap, this.maxDistanceFromCenter
        );
    }

    @Override
    public StructureType<?> type() {
        return StructureType.JIGSAW;
    }
}
