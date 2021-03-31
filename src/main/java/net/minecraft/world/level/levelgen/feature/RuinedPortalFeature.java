package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class RuinedPortalFeature extends StructureFeature<RuinedPortalConfiguration> {
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
    private static final float PROBABILITY_OF_AIR_POCKET = 0.5F;
    private static final float PROBABILITY_OF_UNDERGROUND = 0.5F;
    private static final float UNDERWATER_MOSSINESS = 0.8F;
    private static final float JUNGLE_MOSSINESS = 0.8F;
    private static final float SWAMP_MOSSINESS = 0.5F;
    private static final int MIN_Y = 15;

    public RuinedPortalFeature(Codec<RuinedPortalConfiguration> param0) {
        super(param0);
    }

    @Override
    public StructureFeature.StructureStartFactory<RuinedPortalConfiguration> getStartFactory() {
        return RuinedPortalFeature.FeatureStart::new;
    }

    private static boolean isCold(BlockPos param0, Biome param1) {
        return param1.getTemperature(param0) < 0.15F;
    }

    private static int findSuitableY(
        Random param0,
        ChunkGenerator param1,
        RuinedPortalPiece.VerticalPlacement param2,
        boolean param3,
        int param4,
        int param5,
        BoundingBox param6,
        LevelHeightAccessor param7
    ) {
        int var0;
        if (param2 == RuinedPortalPiece.VerticalPlacement.IN_NETHER) {
            if (param3) {
                var0 = Mth.randomBetweenInclusive(param0, 32, 100);
            } else if (param0.nextFloat() < 0.5F) {
                var0 = Mth.randomBetweenInclusive(param0, 27, 29);
            } else {
                var0 = Mth.randomBetweenInclusive(param0, 29, 100);
            }
        } else if (param2 == RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN) {
            int var3 = param4 - param5;
            var0 = getRandomWithinInterval(param0, 70, var3);
        } else if (param2 == RuinedPortalPiece.VerticalPlacement.UNDERGROUND) {
            int var5 = param4 - param5;
            var0 = getRandomWithinInterval(param0, 15, var5);
        } else if (param2 == RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED) {
            var0 = param4 - param5 + Mth.randomBetweenInclusive(param0, 2, 8);
        } else {
            var0 = param4;
        }

        List<BlockPos> var9 = ImmutableList.of(
            new BlockPos(param6.minX(), 0, param6.minZ()),
            new BlockPos(param6.maxX(), 0, param6.minZ()),
            new BlockPos(param6.minX(), 0, param6.maxZ()),
            new BlockPos(param6.maxX(), 0, param6.maxZ())
        );
        List<NoiseColumn> var10 = var9.stream().map(param2x -> param1.getBaseColumn(param2x.getX(), param2x.getZ(), param7)).collect(Collectors.toList());
        Heightmap.Types var11 = param2 == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR
            ? Heightmap.Types.OCEAN_FLOOR_WG
            : Heightmap.Types.WORLD_SURFACE_WG;
        BlockPos.MutableBlockPos var12 = new BlockPos.MutableBlockPos();

        int var13;
        for(var13 = var0; var13 > 15; --var13) {
            int var14 = 0;
            var12.set(0, var13, 0);

            for(NoiseColumn var15 : var10) {
                BlockState var16 = var15.getBlockState(var12);
                if (var11.isOpaque().test(var16)) {
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

    public static class FeatureStart extends StructureStart<RuinedPortalConfiguration> {
        protected FeatureStart(StructureFeature<RuinedPortalConfiguration> param0, ChunkPos param1, int param2, long param3) {
            super(param0, param1, param2, param3);
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            ChunkPos param3,
            Biome param4,
            RuinedPortalConfiguration param5,
            LevelHeightAccessor param6
        ) {
            RuinedPortalPiece.Properties var0 = new RuinedPortalPiece.Properties();
            RuinedPortalPiece.VerticalPlacement var1;
            if (param5.portalType == RuinedPortalFeature.Type.DESERT) {
                var1 = RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED;
                var0.airPocket = false;
                var0.mossiness = 0.0F;
            } else if (param5.portalType == RuinedPortalFeature.Type.JUNGLE) {
                var1 = RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
                var0.airPocket = this.random.nextFloat() < 0.5F;
                var0.mossiness = 0.8F;
                var0.overgrown = true;
                var0.vines = true;
            } else if (param5.portalType == RuinedPortalFeature.Type.SWAMP) {
                var1 = RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
                var0.airPocket = false;
                var0.mossiness = 0.5F;
                var0.vines = true;
            } else if (param5.portalType == RuinedPortalFeature.Type.MOUNTAIN) {
                boolean var4 = this.random.nextFloat() < 0.5F;
                var1 = var4 ? RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN : RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
                var0.airPocket = var4 || this.random.nextFloat() < 0.5F;
            } else if (param5.portalType == RuinedPortalFeature.Type.OCEAN) {
                var1 = RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
                var0.airPocket = false;
                var0.mossiness = 0.8F;
            } else if (param5.portalType == RuinedPortalFeature.Type.NETHER) {
                var1 = RuinedPortalPiece.VerticalPlacement.IN_NETHER;
                var0.airPocket = this.random.nextFloat() < 0.5F;
                var0.mossiness = 0.0F;
                var0.replaceWithBlackstone = true;
            } else {
                boolean var8 = this.random.nextFloat() < 0.5F;
                var1 = var8 ? RuinedPortalPiece.VerticalPlacement.UNDERGROUND : RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
                var0.airPocket = var8 || this.random.nextFloat() < 0.5F;
            }

            ResourceLocation var10;
            if (this.random.nextFloat() < 0.05F) {
                var10 = new ResourceLocation(
                    RuinedPortalFeature.STRUCTURE_LOCATION_GIANT_PORTALS[this.random.nextInt(RuinedPortalFeature.STRUCTURE_LOCATION_GIANT_PORTALS.length)]
                );
            } else {
                var10 = new ResourceLocation(
                    RuinedPortalFeature.STRUCTURE_LOCATION_PORTALS[this.random.nextInt(RuinedPortalFeature.STRUCTURE_LOCATION_PORTALS.length)]
                );
            }

            StructureTemplate var12 = param2.getOrCreate(var10);
            Rotation var13 = Util.getRandom(Rotation.values(), this.random);
            Mirror var14 = this.random.nextFloat() < 0.5F ? Mirror.NONE : Mirror.FRONT_BACK;
            BlockPos var15 = new BlockPos(var12.getSize().getX() / 2, 0, var12.getSize().getZ() / 2);
            BlockPos var16 = param3.getWorldPosition();
            BoundingBox var17 = var12.getBoundingBox(var16, var13, var15, var14);
            BlockPos var18 = var17.getCenter();
            int var19 = var18.getX();
            int var20 = var18.getZ();
            int var21 = param1.getBaseHeight(var19, var20, RuinedPortalPiece.getHeightMapType(var1), param6) - 1;
            int var22 = RuinedPortalFeature.findSuitableY(this.random, param1, var1, var0.airPocket, var21, var17.getYSpan(), var17, param6);
            BlockPos var23 = new BlockPos(var16.getX(), var22, var16.getZ());
            if (param5.portalType == RuinedPortalFeature.Type.MOUNTAIN
                || param5.portalType == RuinedPortalFeature.Type.OCEAN
                || param5.portalType == RuinedPortalFeature.Type.STANDARD) {
                var0.cold = RuinedPortalFeature.isCold(var23, param4);
            }

            this.addPiece(new RuinedPortalPiece(param2, var23, var1, var0, var10, var12, var13, var14, var15));
        }
    }

    public static enum Type implements StringRepresentable {
        STANDARD("standard"),
        DESERT("desert"),
        JUNGLE("jungle"),
        SWAMP("swamp"),
        MOUNTAIN("mountain"),
        OCEAN("ocean"),
        NETHER("nether");

        public static final Codec<RuinedPortalFeature.Type> CODEC = StringRepresentable.fromEnum(
            RuinedPortalFeature.Type::values, RuinedPortalFeature.Type::byName
        );
        private static final Map<String, RuinedPortalFeature.Type> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(RuinedPortalFeature.Type::getName, param0 -> param0));
        private final String name;

        private Type(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }

        public static RuinedPortalFeature.Type byName(String param0) {
            return BY_NAME.get(param0);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
