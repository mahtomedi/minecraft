package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class RuinedPortalFeature extends RandomScatteredFeature<RuinedPortalConfiguration> {
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

    public RuinedPortalFeature(Function<Dynamic<?>, ? extends RuinedPortalConfiguration> param0) {
        super(param0);
    }

    @Override
    public String getFeatureName() {
        return "Ruined_Portal";
    }

    @Override
    public int getLookupRange() {
        return 3;
    }

    @Override
    protected int getSpacing(ChunkGeneratorSettings param0) {
        return param0.getRuinedPortalSpacing();
    }

    @Override
    protected int getSeparation(ChunkGeneratorSettings param0) {
        return param0.getRuinedPortalSeparation();
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return RuinedPortalFeature.FeatureStart::new;
    }

    @Override
    protected int getRandomSalt(ChunkGeneratorSettings param0) {
        return 34222645;
    }

    private static boolean isCold(BlockPos param0, Biome param1) {
        return param1.getTemperature(param0) < 0.15F;
    }

    private static int findSuitableY(
        Random param0, ChunkGenerator param1, RuinedPortalPiece.VerticalPlacement param2, boolean param3, int param4, int param5, BoundingBox param6
    ) {
        int var0;
        if (param2 == RuinedPortalPiece.VerticalPlacement.IN_NETHER) {
            if (param3) {
                var0 = randomIntInclusive(param0, 32, 100);
            } else if (param0.nextFloat() < 0.5F) {
                var0 = randomIntInclusive(param0, 27, 29);
            } else {
                var0 = randomIntInclusive(param0, 29, 100);
            }
        } else if (param2 == RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN) {
            int var3 = param4 - param5;
            var0 = getRandomWithinInterval(param0, 70, var3);
        } else if (param2 == RuinedPortalPiece.VerticalPlacement.UNDERGROUND) {
            int var5 = param4 - param5;
            var0 = getRandomWithinInterval(param0, 15, var5);
        } else if (param2 == RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED) {
            var0 = param4 - param5 + randomIntInclusive(param0, 2, 8);
        } else {
            var0 = param4;
        }

        List<BlockPos> var9 = ImmutableList.of(
            new BlockPos(param6.x0, 0, param6.z0),
            new BlockPos(param6.x1, 0, param6.z0),
            new BlockPos(param6.x0, 0, param6.z1),
            new BlockPos(param6.x1, 0, param6.z1)
        );
        List<BlockGetter> var10 = var9.stream().map(param1x -> param1.getBaseColumn(param1x.getX(), param1x.getZ())).collect(Collectors.toList());
        Heightmap.Types var11 = param2 == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR
            ? Heightmap.Types.OCEAN_FLOOR_WG
            : Heightmap.Types.WORLD_SURFACE_WG;

        int var12;
        for(var12 = var0; var12 > 15; --var12) {
            int var13 = 0;

            for(BlockGetter var14 : var10) {
                if (var11.isOpaque().test(var14.getBlockState(new BlockPos(0, var12, 0)))) {
                    if (++var13 == 3) {
                        return var12;
                    }
                }
            }
        }

        return var12;
    }

    private static int randomIntInclusive(Random param0, int param1, int param2) {
        return param0.nextInt(param2 - param1 + 1) + param1;
    }

    private static int getRandomWithinInterval(Random param0, int param1, int param2) {
        return param1 < param2 ? randomIntInclusive(param0, param1, param2) : param2;
    }

    public static class FeatureStart extends StructureStart {
        protected FeatureStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator param0, StructureManager param1, int param2, int param3, Biome param4) {
            RuinedPortalConfiguration var0 = param0.getStructureConfiguration(param4, Feature.RUINED_PORTAL);
            if (var0 != null) {
                RuinedPortalPiece.Properties var1 = new RuinedPortalPiece.Properties();
                RuinedPortalPiece.VerticalPlacement var2;
                if (var0.portalType == RuinedPortalFeature.Type.DESERT) {
                    var2 = RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED;
                    var1.airPocket = false;
                    var1.mossiness = 0.0F;
                } else if (var0.portalType == RuinedPortalFeature.Type.JUNGLE) {
                    var2 = RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
                    var1.airPocket = this.random.nextFloat() < 0.5F;
                    var1.mossiness = 0.8F;
                    var1.overgrown = true;
                    var1.vines = true;
                } else if (var0.portalType == RuinedPortalFeature.Type.SWAMP) {
                    var2 = RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
                    var1.airPocket = false;
                    var1.mossiness = 0.5F;
                    var1.vines = true;
                } else if (var0.portalType == RuinedPortalFeature.Type.MOUNTAIN) {
                    boolean var5 = this.random.nextFloat() < 0.5F;
                    var2 = var5 ? RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN : RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
                    var1.airPocket = var5 || this.random.nextFloat() < 0.5F;
                } else if (var0.portalType == RuinedPortalFeature.Type.OCEAN) {
                    var2 = RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
                    var1.airPocket = false;
                    var1.mossiness = 0.8F;
                } else if (var0.portalType == RuinedPortalFeature.Type.NETHER) {
                    var2 = RuinedPortalPiece.VerticalPlacement.IN_NETHER;
                    var1.airPocket = this.random.nextFloat() < 0.5F;
                    var1.mossiness = 0.0F;
                    var1.replaceWithBlackstone = true;
                } else {
                    boolean var9 = this.random.nextFloat() < 0.5F;
                    var2 = var9 ? RuinedPortalPiece.VerticalPlacement.UNDERGROUND : RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
                    var1.airPocket = var9 || this.random.nextFloat() < 0.5F;
                }

                ResourceLocation var11;
                if (this.random.nextFloat() < 0.05F) {
                    var11 = new ResourceLocation(
                        RuinedPortalFeature.STRUCTURE_LOCATION_GIANT_PORTALS[this.random.nextInt(RuinedPortalFeature.STRUCTURE_LOCATION_GIANT_PORTALS.length)]
                    );
                } else {
                    var11 = new ResourceLocation(
                        RuinedPortalFeature.STRUCTURE_LOCATION_PORTALS[this.random.nextInt(RuinedPortalFeature.STRUCTURE_LOCATION_PORTALS.length)]
                    );
                }

                StructureTemplate var13 = param1.getOrCreate(var11);
                Rotation var14 = Util.getRandom(Rotation.values(), this.random);
                Mirror var15 = this.random.nextFloat() < 0.5F ? Mirror.NONE : Mirror.FRONT_BACK;
                BlockPos var16 = new BlockPos(var13.getSize().getX() / 2, 0, var13.getSize().getZ() / 2);
                BlockPos var17 = new ChunkPos(param2, param3).getWorldPosition();
                BoundingBox var18 = var13.getBoundingBox(var17, var14, var16, var15);
                Vec3i var19 = var18.getCenter();
                int var20 = var19.getX();
                int var21 = var19.getZ();
                int var22 = param0.getBaseHeight(var20, var21, RuinedPortalPiece.getHeightMapType(var2)) - 1;
                int var23 = RuinedPortalFeature.findSuitableY(this.random, param0, var2, var1.airPocket, var22, var18.getYSpan(), var18);
                BlockPos var24 = new BlockPos(var17.getX(), var23, var17.getZ());
                if (var0.portalType == RuinedPortalFeature.Type.MOUNTAIN
                    || var0.portalType == RuinedPortalFeature.Type.OCEAN
                    || var0.portalType == RuinedPortalFeature.Type.STANDARD) {
                    var1.cold = RuinedPortalFeature.isCold(var24, param4);
                }

                this.pieces.add(new RuinedPortalPiece(var24, var2, var1, var11, var13, var14, var15, var16));
                this.calculateBoundingBox();
            }
        }
    }

    public static enum Type {
        STANDARD("standard"),
        DESERT("desert"),
        JUNGLE("jungle"),
        SWAMP("swamp"),
        MOUNTAIN("mountain"),
        OCEAN("ocean"),
        NETHER("nether");

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
    }
}
