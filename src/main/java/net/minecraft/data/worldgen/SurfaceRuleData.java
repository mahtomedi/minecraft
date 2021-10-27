package net.minecraft.data.worldgen;

import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public class SurfaceRuleData {
    private static final SurfaceRules.RuleSource AIR = makeStateRule(Blocks.AIR);
    private static final SurfaceRules.RuleSource WHITE_TERRACOTTA = makeStateRule(Blocks.WHITE_TERRACOTTA);
    private static final SurfaceRules.RuleSource ORANGE_TERRACOTTA = makeStateRule(Blocks.ORANGE_TERRACOTTA);
    private static final SurfaceRules.RuleSource TERRACOTTA = makeStateRule(Blocks.TERRACOTTA);
    private static final SurfaceRules.RuleSource RED_SAND = makeStateRule(Blocks.RED_SAND);
    private static final SurfaceRules.RuleSource RED_SANDSTONE = makeStateRule(Blocks.RED_SANDSTONE);
    private static final SurfaceRules.RuleSource STONE = makeStateRule(Blocks.STONE);
    private static final SurfaceRules.RuleSource DIRT = makeStateRule(Blocks.DIRT);
    private static final SurfaceRules.RuleSource PODZOL = makeStateRule(Blocks.PODZOL);
    private static final SurfaceRules.RuleSource COARSE_DIRT = makeStateRule(Blocks.COARSE_DIRT);
    private static final SurfaceRules.RuleSource MYCELIUM = makeStateRule(Blocks.MYCELIUM);
    private static final SurfaceRules.RuleSource GRASS_BLOCK = makeStateRule(Blocks.GRASS_BLOCK);
    private static final SurfaceRules.RuleSource CALCITE = makeStateRule(Blocks.CALCITE);
    private static final SurfaceRules.RuleSource GRAVEL = makeStateRule(Blocks.GRAVEL);
    private static final SurfaceRules.RuleSource SAND = makeStateRule(Blocks.SAND);
    private static final SurfaceRules.RuleSource SANDSTONE = makeStateRule(Blocks.SANDSTONE);
    private static final SurfaceRules.RuleSource PACKED_ICE = makeStateRule(Blocks.PACKED_ICE);
    private static final SurfaceRules.RuleSource SNOW_BLOCK = makeStateRule(Blocks.SNOW_BLOCK);
    private static final SurfaceRules.RuleSource POWDER_SNOW = makeStateRule(Blocks.POWDER_SNOW);
    private static final SurfaceRules.RuleSource ICE = makeStateRule(Blocks.ICE);
    private static final SurfaceRules.RuleSource WATER = makeStateRule(Blocks.WATER);
    private static final SurfaceRules.RuleSource LAVA = makeStateRule(Blocks.LAVA);
    private static final SurfaceRules.RuleSource NETHERRACK = makeStateRule(Blocks.NETHERRACK);
    private static final SurfaceRules.RuleSource SOUL_SAND = makeStateRule(Blocks.SOUL_SAND);
    private static final SurfaceRules.RuleSource SOUL_SOIL = makeStateRule(Blocks.SOUL_SOIL);
    private static final SurfaceRules.RuleSource BASALT = makeStateRule(Blocks.BASALT);
    private static final SurfaceRules.RuleSource BLACKSTONE = makeStateRule(Blocks.BLACKSTONE);
    private static final SurfaceRules.RuleSource WARPED_WART_BLOCK = makeStateRule(Blocks.WARPED_WART_BLOCK);
    private static final SurfaceRules.RuleSource WARPED_NYLIUM = makeStateRule(Blocks.WARPED_NYLIUM);
    private static final SurfaceRules.RuleSource NETHER_WART_BLOCK = makeStateRule(Blocks.NETHER_WART_BLOCK);
    private static final SurfaceRules.RuleSource CRIMSON_NYLIUM = makeStateRule(Blocks.CRIMSON_NYLIUM);
    private static final SurfaceRules.RuleSource ENDSTONE = makeStateRule(Blocks.END_STONE);

    private static SurfaceRules.RuleSource makeStateRule(Block param0) {
        return SurfaceRules.state(param0.defaultBlockState());
    }

    public static SurfaceRules.RuleSource overworld() {
        SurfaceRules.ConditionSource var0 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(97), 2);
        SurfaceRules.ConditionSource var1 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(256), 0);
        SurfaceRules.ConditionSource var2 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(63), -1);
        SurfaceRules.ConditionSource var3 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(74), 1);
        SurfaceRules.ConditionSource var4 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(62), 0);
        SurfaceRules.ConditionSource var5 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(63), 0);
        SurfaceRules.ConditionSource var6 = SurfaceRules.waterBlockCheck(-1, 0);
        SurfaceRules.ConditionSource var7 = SurfaceRules.waterBlockCheck(0, 0);
        SurfaceRules.ConditionSource var8 = SurfaceRules.waterStartCheck(-6, -1);
        SurfaceRules.ConditionSource var9 = SurfaceRules.hole();
        SurfaceRules.ConditionSource var10 = SurfaceRules.isBiome(Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN);
        SurfaceRules.ConditionSource var11 = SurfaceRules.steep();
        SurfaceRules.RuleSource var12 = SurfaceRules.sequence(SurfaceRules.ifTrue(var6, GRASS_BLOCK), DIRT);
        SurfaceRules.RuleSource var13 = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, SANDSTONE), SAND);
        SurfaceRules.RuleSource var14 = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, STONE), GRAVEL);
        SurfaceRules.RuleSource var15 = SurfaceRules.sequence(
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.STONY_PEAKS),
                SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.CALCITE, -0.0125, 0.0125), CALCITE), STONE)
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.STONY_SHORE),
                SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.GRAVEL, -0.05, 0.05), var14), STONE)
            ),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.WINDSWEPT_HILLS), SurfaceRules.ifTrue(surfaceNoiseAbove(1.0), STONE)),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.WARM_OCEAN, Biomes.DESERT, Biomes.BEACH, Biomes.SNOWY_BEACH), var13),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.DRIPSTONE_CAVES), STONE)
        );
        SurfaceRules.RuleSource var16 = SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.POWDER_SNOW_UNDER, 0.45, 0.58), POWDER_SNOW);
        SurfaceRules.RuleSource var17 = SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.POWDER_SNOW_SURFACE, 0.35, 0.6), POWDER_SNOW);
        SurfaceRules.RuleSource var18 = SurfaceRules.sequence(
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.FROZEN_PEAKS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(var11, PACKED_ICE),
                    SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.PACKED_ICE, -0.5, 0.2), PACKED_ICE),
                    SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.ICE, -0.0625, 0.025), ICE),
                    SNOW_BLOCK
                )
            ),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.SNOWY_SLOPES), SurfaceRules.sequence(SurfaceRules.ifTrue(var11, STONE), var16, SNOW_BLOCK)),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.JAGGED_PEAKS), STONE),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.GROVE), SurfaceRules.sequence(var16, DIRT)),
            var15,
            SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.WINDSWEPT_SAVANNA), SurfaceRules.ifTrue(surfaceNoiseAbove(1.75), STONE)),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.WINDSWEPT_GRAVELLY_HILLS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(surfaceNoiseAbove(2.0), var14),
                    SurfaceRules.ifTrue(surfaceNoiseAbove(1.0), STONE),
                    SurfaceRules.ifTrue(surfaceNoiseAbove(-1.0), DIRT),
                    var14
                )
            ),
            DIRT
        );
        SurfaceRules.RuleSource var19 = SurfaceRules.sequence(
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.FROZEN_PEAKS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(var11, PACKED_ICE),
                    SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.PACKED_ICE, 0.0, 0.2), PACKED_ICE),
                    SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.ICE, 0.0, 0.025), ICE),
                    SNOW_BLOCK
                )
            ),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.SNOWY_SLOPES), SurfaceRules.sequence(SurfaceRules.ifTrue(var11, STONE), var17, SNOW_BLOCK)),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.JAGGED_PEAKS), SurfaceRules.sequence(SurfaceRules.ifTrue(var11, STONE), SNOW_BLOCK)),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.GROVE), SurfaceRules.sequence(var17, SNOW_BLOCK)),
            var15,
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.WINDSWEPT_SAVANNA),
                SurfaceRules.sequence(SurfaceRules.ifTrue(surfaceNoiseAbove(1.75), STONE), SurfaceRules.ifTrue(surfaceNoiseAbove(-0.5), COARSE_DIRT))
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.WINDSWEPT_GRAVELLY_HILLS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(surfaceNoiseAbove(2.0), var14),
                    SurfaceRules.ifTrue(surfaceNoiseAbove(1.0), STONE),
                    SurfaceRules.ifTrue(surfaceNoiseAbove(-1.0), var12),
                    var14
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA),
                SurfaceRules.sequence(SurfaceRules.ifTrue(surfaceNoiseAbove(1.75), COARSE_DIRT), SurfaceRules.ifTrue(surfaceNoiseAbove(-0.95), PODZOL))
            ),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.ICE_SPIKES), SNOW_BLOCK),
            SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.MUSHROOM_FIELDS), MYCELIUM),
            var12
        );
        SurfaceRules.ConditionSource var20 = SurfaceRules.noiseCondition(Noises.SURFACE, -0.909, -0.5454);
        SurfaceRules.ConditionSource var21 = SurfaceRules.noiseCondition(Noises.SURFACE, -0.1818, 0.1818);
        SurfaceRules.ConditionSource var22 = SurfaceRules.noiseCondition(Noises.SURFACE, 0.5454, 0.909);
        return SurfaceRules.sequence(
            SurfaceRules.ifTrue(
                SurfaceRules.ON_FLOOR,
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(Biomes.WOODED_BADLANDS),
                        SurfaceRules.ifTrue(
                            var0,
                            SurfaceRules.sequence(
                                SurfaceRules.ifTrue(var20, COARSE_DIRT),
                                SurfaceRules.ifTrue(var21, COARSE_DIRT),
                                SurfaceRules.ifTrue(var22, COARSE_DIRT),
                                var12
                            )
                        )
                    ),
                    SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(Biomes.SWAMP),
                        SurfaceRules.ifTrue(
                            var4, SurfaceRules.ifTrue(SurfaceRules.not(var5), SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SWAMP, 0.0), WATER))
                        )
                    )
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.sequence(
                            SurfaceRules.ifTrue(var1, ORANGE_TERRACOTTA),
                            SurfaceRules.ifTrue(
                                var3,
                                SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(var20, TERRACOTTA),
                                    SurfaceRules.ifTrue(var21, TERRACOTTA),
                                    SurfaceRules.ifTrue(var22, TERRACOTTA),
                                    SurfaceRules.bandlands()
                                )
                            ),
                            SurfaceRules.ifTrue(var6, SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, RED_SANDSTONE), RED_SAND)),
                            SurfaceRules.ifTrue(SurfaceRules.not(var9), ORANGE_TERRACOTTA),
                            SurfaceRules.ifTrue(var8, WHITE_TERRACOTTA),
                            var14
                        )
                    ),
                    SurfaceRules.ifTrue(
                        var2,
                        SurfaceRules.sequence(
                            SurfaceRules.ifTrue(var5, SurfaceRules.ifTrue(SurfaceRules.not(var3), ORANGE_TERRACOTTA)), SurfaceRules.bandlands()
                        )
                    ),
                    SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.ifTrue(var8, WHITE_TERRACOTTA))
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.ON_FLOOR,
                SurfaceRules.ifTrue(
                    var6,
                    SurfaceRules.sequence(
                        SurfaceRules.ifTrue(
                            var10,
                            SurfaceRules.ifTrue(
                                var9, SurfaceRules.sequence(SurfaceRules.ifTrue(var7, AIR), SurfaceRules.ifTrue(SurfaceRules.temperature(), ICE), WATER)
                            )
                        ),
                        var19
                    )
                )
            ),
            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.ifTrue(var8, SurfaceRules.ifTrue(var10, SurfaceRules.ifTrue(var9, WATER)))),
            SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.ifTrue(var8, var18)),
            SurfaceRules.ifTrue(
                SurfaceRules.ON_FLOOR,
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.FROZEN_PEAKS, Biomes.JAGGED_PEAKS), STONE),
                    SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN), var13),
                    var14
                )
            )
        );
    }

    public static SurfaceRules.RuleSource nether() {
        SurfaceRules.ConditionSource var0 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(31), 0);
        SurfaceRules.ConditionSource var1 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(32), 0);
        SurfaceRules.ConditionSource var2 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(30), 0);
        SurfaceRules.ConditionSource var3 = SurfaceRules.not(SurfaceRules.yStartCheck(VerticalAnchor.absolute(35), 0));
        SurfaceRules.ConditionSource var4 = SurfaceRules.hole();
        SurfaceRules.ConditionSource var5 = SurfaceRules.noiseCondition(Noises.SOUL_SAND_LAYER, -0.012);
        SurfaceRules.ConditionSource var6 = SurfaceRules.noiseCondition(Noises.GRAVEL_LAYER, -0.012);
        SurfaceRules.ConditionSource var7 = SurfaceRules.noiseCondition(Noises.PATCH, -0.012);
        SurfaceRules.ConditionSource var8 = SurfaceRules.noiseCondition(Noises.NETHERRACK, 0.54);
        SurfaceRules.ConditionSource var9 = SurfaceRules.noiseCondition(Noises.NETHER_WART, 1.17);
        SurfaceRules.ConditionSource var10 = SurfaceRules.noiseCondition(Noises.NETHER_STATE_SELECTOR, 0.0);
        SurfaceRules.RuleSource var11 = SurfaceRules.ifTrue(var7, SurfaceRules.ifTrue(var2, SurfaceRules.ifTrue(var3, GRAVEL)));
        return SurfaceRules.sequence(
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.BASALT_DELTAS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.UNDER_CEILING, BASALT),
                    SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.sequence(var11, SurfaceRules.ifTrue(var10, BASALT), BLACKSTONE))
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.SOUL_SAND_VALLEY),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.UNDER_CEILING, SurfaceRules.sequence(SurfaceRules.ifTrue(var10, SOUL_SAND), SOUL_SOIL)),
                    SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.sequence(var11, SurfaceRules.ifTrue(var10, SOUL_SAND), SOUL_SOIL))
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.ON_FLOOR,
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.not(var1), SurfaceRules.ifTrue(var4, LAVA)),
                    SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(Biomes.WARPED_FOREST),
                        SurfaceRules.ifTrue(
                            SurfaceRules.not(var8),
                            SurfaceRules.ifTrue(var0, SurfaceRules.sequence(SurfaceRules.ifTrue(var9, WARPED_WART_BLOCK), WARPED_NYLIUM))
                        )
                    ),
                    SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(Biomes.CRIMSON_FOREST),
                        SurfaceRules.ifTrue(
                            SurfaceRules.not(var8),
                            SurfaceRules.ifTrue(var0, SurfaceRules.sequence(SurfaceRules.ifTrue(var9, NETHER_WART_BLOCK), CRIMSON_NYLIUM))
                        )
                    )
                )
            ),
            SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.NETHER_WASTES),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(
                        SurfaceRules.UNDER_FLOOR,
                        SurfaceRules.ifTrue(
                            var5,
                            SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.not(var4), SurfaceRules.ifTrue(var2, SurfaceRules.ifTrue(var3, SOUL_SAND))), NETHERRACK
                            )
                        )
                    ),
                    SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.ifTrue(
                            var0,
                            SurfaceRules.ifTrue(
                                var3,
                                SurfaceRules.ifTrue(
                                    var6, SurfaceRules.sequence(SurfaceRules.ifTrue(var1, GRAVEL), SurfaceRules.ifTrue(SurfaceRules.not(var4), GRAVEL))
                                )
                            )
                        )
                    )
                )
            ),
            NETHERRACK
        );
    }

    public static SurfaceRules.RuleSource end() {
        return ENDSTONE;
    }

    private static SurfaceRules.ConditionSource surfaceNoiseAbove(double param0) {
        return SurfaceRules.noiseCondition(Noises.SURFACE, param0 / 8.25, Double.MAX_VALUE);
    }
}
