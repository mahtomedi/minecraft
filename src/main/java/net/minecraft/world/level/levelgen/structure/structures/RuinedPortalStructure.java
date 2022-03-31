package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class RuinedPortalStructure extends Structure {
    private static final String[] STRUCTURE_LOCATION_PORTALS = new String[]{
        "ruined_portal/portal_1",
        "ruined_portal/portal_2",
        "ruined_portal/portal_3",
        "ruined_portal/portal_4",
        "ruined_portal/portal_5",
        "ruined_portal/portal_6",
        "ruined_portal/portal_7",
        "ruined_portal/portal_8",
        "ruined_portal/portal_9",
        "ruined_portal/portal_10"
    };
    private static final String[] STRUCTURE_LOCATION_GIANT_PORTALS = new String[]{
        "ruined_portal/giant_portal_1", "ruined_portal/giant_portal_2", "ruined_portal/giant_portal_3"
    };
    private static final float PROBABILITY_OF_GIANT_PORTAL = 0.05F;
    private static final int MIN_Y_INDEX = 15;
    private final List<RuinedPortalStructure.Setup> setups;
    public static final Codec<RuinedPortalStructure> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    settingsCodec(param0),
                    ExtraCodecs.nonEmptyList(RuinedPortalStructure.Setup.CODEC.listOf()).fieldOf("setups").forGetter(param0x -> param0x.setups)
                )
                .apply(param0, RuinedPortalStructure::new)
    );

    public RuinedPortalStructure(Structure.StructureSettings param0, List<RuinedPortalStructure.Setup> param1) {
        super(param0);
        this.setups = param1;
    }

    public RuinedPortalStructure(Structure.StructureSettings param0, RuinedPortalStructure.Setup param1) {
        this(param0, List.of(param1));
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        RuinedPortalPiece.Properties var0 = new RuinedPortalPiece.Properties();
        WorldgenRandom var1 = param0.random();
        RuinedPortalStructure.Setup var2 = null;
        if (this.setups.size() > 1) {
            float var3 = 0.0F;

            for(RuinedPortalStructure.Setup var4 : this.setups) {
                var3 += var4.weight();
            }

            float var5 = var1.nextFloat();

            for(RuinedPortalStructure.Setup var6 : this.setups) {
                var5 -= var6.weight() / var3;
                if (var5 < 0.0F) {
                    var2 = var6;
                    break;
                }
            }
        } else {
            var2 = this.setups.get(0);
        }

        if (var2 == null) {
            throw new IllegalStateException();
        } else {
            RuinedPortalStructure.Setup var7 = var2;
            var0.airPocket = sample(var1, var7.airPocketProbability());
            var0.mossiness = var7.mossiness();
            var0.overgrown = var7.overgrown();
            var0.vines = var7.vines();
            var0.replaceWithBlackstone = var7.replaceWithBlackstone();
            ResourceLocation var8;
            if (var1.nextFloat() < 0.05F) {
                var8 = new ResourceLocation(STRUCTURE_LOCATION_GIANT_PORTALS[var1.nextInt(STRUCTURE_LOCATION_GIANT_PORTALS.length)]);
            } else {
                var8 = new ResourceLocation(STRUCTURE_LOCATION_PORTALS[var1.nextInt(STRUCTURE_LOCATION_PORTALS.length)]);
            }

            StructureTemplate var10 = param0.structureTemplateManager().getOrCreate(var8);
            Rotation var11 = Util.getRandom(Rotation.values(), var1);
            Mirror var12 = var1.nextFloat() < 0.5F ? Mirror.NONE : Mirror.FRONT_BACK;
            BlockPos var13 = new BlockPos(var10.getSize().getX() / 2, 0, var10.getSize().getZ() / 2);
            ChunkGenerator var14 = param0.chunkGenerator();
            LevelHeightAccessor var15 = param0.heightAccessor();
            RandomState var16 = param0.randomState();
            BlockPos var17 = param0.chunkPos().getWorldPosition();
            BoundingBox var18 = var10.getBoundingBox(var17, var11, var13, var12);
            BlockPos var19 = var18.getCenter();
            int var20 = var14.getBaseHeight(var19.getX(), var19.getZ(), RuinedPortalPiece.getHeightMapType(var7.placement()), var15, var16) - 1;
            int var21 = findSuitableY(var1, var14, var7.placement(), var0.airPocket, var20, var18.getYSpan(), var18, var15, var16);
            BlockPos var22 = new BlockPos(var17.getX(), var21, var17.getZ());
            return Optional.of(
                new Structure.GenerationStub(
                    var22,
                    param10 -> {
                        if (var7.canBeCold()) {
                            var0.cold = isCold(
                                var22,
                                param0.chunkGenerator()
                                    .getBiomeSource()
                                    .getNoiseBiome(
                                        QuartPos.fromBlock(var22.getX()), QuartPos.fromBlock(var22.getY()), QuartPos.fromBlock(var22.getZ()), var16.sampler()
                                    )
                            );
                        }
        
                        param10.addPiece(
                            new RuinedPortalPiece(param0.structureTemplateManager(), var22, var7.placement(), var0, var8, var10, var11, var12, var13)
                        );
                    }
                )
            );
        }
    }

    private static boolean sample(WorldgenRandom param0, float param1) {
        if (param1 == 0.0F) {
            return false;
        } else if (param1 == 1.0F) {
            return true;
        } else {
            return param0.nextFloat() < param1;
        }
    }

    private static boolean isCold(BlockPos param0, Holder<Biome> param1) {
        return param1.value().coldEnoughToSnow(param0);
    }

    private static int findSuitableY(
        Random param0,
        ChunkGenerator param1,
        RuinedPortalPiece.VerticalPlacement param2,
        boolean param3,
        int param4,
        int param5,
        BoundingBox param6,
        LevelHeightAccessor param7,
        RandomState param8
    ) {
        int var0 = param7.getMinBuildHeight() + 15;
        int var1;
        if (param2 == RuinedPortalPiece.VerticalPlacement.IN_NETHER) {
            if (param3) {
                var1 = Mth.randomBetweenInclusive(param0, 32, 100);
            } else if (param0.nextFloat() < 0.5F) {
                var1 = Mth.randomBetweenInclusive(param0, 27, 29);
            } else {
                var1 = Mth.randomBetweenInclusive(param0, 29, 100);
            }
        } else if (param2 == RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN) {
            int var4 = param4 - param5;
            var1 = getRandomWithinInterval(param0, 70, var4);
        } else if (param2 == RuinedPortalPiece.VerticalPlacement.UNDERGROUND) {
            int var6 = param4 - param5;
            var1 = getRandomWithinInterval(param0, var0, var6);
        } else if (param2 == RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED) {
            var1 = param4 - param5 + Mth.randomBetweenInclusive(param0, 2, 8);
        } else {
            var1 = param4;
        }

        List<BlockPos> var10 = ImmutableList.of(
            new BlockPos(param6.minX(), 0, param6.minZ()),
            new BlockPos(param6.maxX(), 0, param6.minZ()),
            new BlockPos(param6.minX(), 0, param6.maxZ()),
            new BlockPos(param6.maxX(), 0, param6.maxZ())
        );
        List<NoiseColumn> var11 = var10.stream()
            .map(param3x -> param1.getBaseColumn(param3x.getX(), param3x.getZ(), param7, param8))
            .collect(Collectors.toList());
        Heightmap.Types var12 = param2 == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR
            ? Heightmap.Types.OCEAN_FLOOR_WG
            : Heightmap.Types.WORLD_SURFACE_WG;

        int var13;
        for(var13 = var1; var13 > var0; --var13) {
            int var14 = 0;

            for(NoiseColumn var15 : var11) {
                BlockState var16 = var15.getBlock(var13);
                if (var12.isOpaque().test(var16)) {
                    if (++var14 == 3) {
                        return var13;
                    }
                }
            }
        }

        return var13;
    }

    private static int getRandomWithinInterval(Random param0, int param1, int param2) {
        return param1 < param2 ? Mth.randomBetweenInclusive(param0, param1, param2) : param2;
    }

    @Override
    public StructureType<?> type() {
        return StructureType.RUINED_PORTAL;
    }

    public static record Setup(
        RuinedPortalPiece.VerticalPlacement placement,
        float airPocketProbability,
        float mossiness,
        boolean overgrown,
        boolean vines,
        boolean canBeCold,
        boolean replaceWithBlackstone,
        float weight
    ) {
        public static final Codec<RuinedPortalStructure.Setup> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        RuinedPortalPiece.VerticalPlacement.CODEC.fieldOf("placement").forGetter(RuinedPortalStructure.Setup::placement),
                        Codec.floatRange(0.0F, 1.0F).fieldOf("air_pocket_probability").forGetter(RuinedPortalStructure.Setup::airPocketProbability),
                        Codec.floatRange(0.0F, 1.0F).fieldOf("mossiness").forGetter(RuinedPortalStructure.Setup::mossiness),
                        Codec.BOOL.fieldOf("overgrown").forGetter(RuinedPortalStructure.Setup::overgrown),
                        Codec.BOOL.fieldOf("vines").forGetter(RuinedPortalStructure.Setup::vines),
                        Codec.BOOL.fieldOf("can_be_cold").forGetter(RuinedPortalStructure.Setup::canBeCold),
                        Codec.BOOL.fieldOf("replace_with_blackstone").forGetter(RuinedPortalStructure.Setup::replaceWithBlackstone),
                        ExtraCodecs.POSITIVE_FLOAT.fieldOf("weight").forGetter(RuinedPortalStructure.Setup::weight)
                    )
                    .apply(param0, RuinedPortalStructure.Setup::new)
        );
    }
}
