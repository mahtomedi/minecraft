package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.levelgen.feature.structures.EmptyPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.FeaturePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class SavannaVillagePools {
    public static void bootstrap() {
    }

    static {
        ImmutableList<StructureProcessor> var0 = ImmutableList.of(
            new RuleProcessor(
                ImmutableList.of(
                    new ProcessorRule(new TagMatchTest(BlockTags.DOORS), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                    new ProcessorRule(new BlockMatchTest(Blocks.TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                    new ProcessorRule(new BlockMatchTest(Blocks.WALL_TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.ACACIA_PLANKS, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.ACACIA_STAIRS, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.ACACIA_LOG, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.ACACIA_WOOD, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.ORANGE_TERRACOTTA, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.YELLOW_TERRACOTTA, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.RED_TERRACOTTA, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.GLASS_PANE, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                    new ProcessorRule(
                        new BlockStateMatchTest(
                            Blocks.GLASS_PANE
                                .defaultBlockState()
                                .setValue(IronBarsBlock.NORTH, Boolean.valueOf(true))
                                .setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true))
                        ),
                        AlwaysTrueTest.INSTANCE,
                        Blocks.BROWN_STAINED_GLASS_PANE
                            .defaultBlockState()
                            .setValue(IronBarsBlock.NORTH, Boolean.valueOf(true))
                            .setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true))
                    ),
                    new ProcessorRule(
                        new BlockStateMatchTest(
                            Blocks.GLASS_PANE
                                .defaultBlockState()
                                .setValue(IronBarsBlock.EAST, Boolean.valueOf(true))
                                .setValue(IronBarsBlock.WEST, Boolean.valueOf(true))
                        ),
                        AlwaysTrueTest.INSTANCE,
                        Blocks.BROWN_STAINED_GLASS_PANE
                            .defaultBlockState()
                            .setValue(IronBarsBlock.EAST, Boolean.valueOf(true))
                            .setValue(IronBarsBlock.WEST, Boolean.valueOf(true))
                    ),
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MELON_STEM.defaultBlockState())
                )
            )
        );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/savanna/town_centers"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/savanna/town_centers/savanna_meeting_point_1"), 100),
                        new Pair<>(new SinglePoolElement("village/savanna/town_centers/savanna_meeting_point_2"), 50),
                        new Pair<>(new SinglePoolElement("village/savanna/town_centers/savanna_meeting_point_3"), 150),
                        new Pair<>(new SinglePoolElement("village/savanna/town_centers/savanna_meeting_point_4"), 150),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/town_centers/savanna_meeting_point_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/town_centers/savanna_meeting_point_2", var0), 1),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/town_centers/savanna_meeting_point_3", var0), 3),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/town_centers/savanna_meeting_point_4", var0), 3)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        ImmutableList<StructureProcessor> var1 = ImmutableList.of(
            new RuleProcessor(
                ImmutableList.of(
                    new ProcessorRule(new BlockMatchTest(Blocks.GRASS_PATH), new BlockMatchTest(Blocks.WATER), Blocks.ACACIA_PLANKS.defaultBlockState()),
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.GRASS_PATH, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.GRASS_BLOCK.defaultBlockState()),
                    new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
                    new ProcessorRule(new BlockMatchTest(Blocks.DIRT), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState())
                )
            )
        );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/savanna/streets"),
                    new ResourceLocation("village/savanna/terminators"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/savanna/streets/corner_01", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/corner_03", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/straight_02", var1), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/straight_04", var1), 7),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/straight_05", var1), 3),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/straight_06", var1), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/straight_08", var1), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/straight_09", var1), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/straight_10", var1), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/straight_11", var1), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/crossroad_02", var1), 1),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/crossroad_03", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/crossroad_04", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/crossroad_05", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/crossroad_06", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/crossroad_07", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/split_01", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/split_02", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/streets/turn_01", var1), 3)
                    ),
                    StructureTemplatePool.Projection.TERRAIN_MATCHING
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/savanna/zombie/streets"),
                    new ResourceLocation("village/savanna/zombie/terminators"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/corner_01", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/corner_03", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/straight_02", var1), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/straight_04", var1), 7),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/straight_05", var1), 3),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/straight_06", var1), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/straight_08", var1), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/straight_09", var1), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/straight_10", var1), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/straight_11", var1), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/crossroad_02", var1), 1),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/crossroad_03", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/crossroad_04", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/crossroad_05", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/crossroad_06", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/crossroad_07", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/split_01", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/split_02", var1), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/streets/turn_01", var1), 3)
                    ),
                    StructureTemplatePool.Projection.TERRAIN_MATCHING
                )
            );
        ImmutableList<StructureProcessor> var2 = ImmutableList.of(
            new RuleProcessor(
                ImmutableList.of(
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MELON_STEM.defaultBlockState())
                )
            )
        );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/savanna/houses"),
                    new ResourceLocation("village/savanna/terminators"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_small_house_1"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_small_house_2"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_small_house_3"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_small_house_4"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_small_house_5"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_small_house_6"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_small_house_7"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_small_house_8"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_medium_house_1"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_medium_house_2"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_butchers_shop_1"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_butchers_shop_2"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_tool_smith_1"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_fletcher_house_1"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_shepherd_1"), 7),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_armorer_1"), 1),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_fisher_cottage_1"), 3),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_tannery_1"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_cartographer_1"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_library_1"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_mason_1"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_weaponsmith_1"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_weaponsmith_2"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_temple_1"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_temple_2"), 3),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_large_farm_1", var2), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_large_farm_2", var2), 6),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_small_farm", var2), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_animal_pen_1"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_animal_pen_2"), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_animal_pen_3"), 2),
                        Pair.of(EmptyPoolElement.INSTANCE, 5)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/savanna/zombie/houses"),
                    new ResourceLocation("village/savanna/zombie/terminators"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/houses/savanna_small_house_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/houses/savanna_small_house_2", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/houses/savanna_small_house_3", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/houses/savanna_small_house_4", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/houses/savanna_small_house_5", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/houses/savanna_small_house_6", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/houses/savanna_small_house_7", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/houses/savanna_small_house_8", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/houses/savanna_medium_house_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/houses/savanna_medium_house_2", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_butchers_shop_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_butchers_shop_2", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_tool_smith_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_fletcher_house_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_shepherd_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_armorer_1", var0), 1),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_fisher_cottage_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_tannery_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_cartographer_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_library_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_mason_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_weaponsmith_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_weaponsmith_2", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_temple_1", var0), 1),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_temple_2", var0), 3),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_large_farm_1", var0), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/houses/savanna_large_farm_2", var0), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_small_farm", var0), 4),
                        new Pair<>(new SinglePoolElement("village/savanna/houses/savanna_animal_pen_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/houses/savanna_animal_pen_2", var0), 2),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/houses/savanna_animal_pen_3", var0), 2),
                        Pair.of(EmptyPoolElement.INSTANCE, 5)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/savanna/terminators"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/plains/terminators/terminator_01", var1), 1),
                        new Pair<>(new SinglePoolElement("village/plains/terminators/terminator_02", var1), 1),
                        new Pair<>(new SinglePoolElement("village/plains/terminators/terminator_03", var1), 1),
                        new Pair<>(new SinglePoolElement("village/plains/terminators/terminator_04", var1), 1),
                        new Pair<>(new SinglePoolElement("village/savanna/terminators/terminator_05", var1), 1)
                    ),
                    StructureTemplatePool.Projection.TERRAIN_MATCHING
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/savanna/zombie/terminators"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/plains/terminators/terminator_01", var1), 1),
                        new Pair<>(new SinglePoolElement("village/plains/terminators/terminator_02", var1), 1),
                        new Pair<>(new SinglePoolElement("village/plains/terminators/terminator_03", var1), 1),
                        new Pair<>(new SinglePoolElement("village/plains/terminators/terminator_04", var1), 1),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/terminators/terminator_05", var1), 1)
                    ),
                    StructureTemplatePool.Projection.TERRAIN_MATCHING
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/savanna/trees"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(new Pair<>(new FeaturePoolElement(Feature.ACACIA_TREE.configured(BiomeDefaultFeatures.ACACIA_TREE_CONFIG)), 1)),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/savanna/decor"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/savanna/savanna_lamp_post_01"), 4),
                        new Pair<>(new FeaturePoolElement(Feature.ACACIA_TREE.configured(BiomeDefaultFeatures.ACACIA_TREE_CONFIG)), 4),
                        new Pair<>(new FeaturePoolElement(Feature.BLOCK_PILE.configured(BiomeDefaultFeatures.HAY_PILE_CONFIG)), 4),
                        new Pair<>(new FeaturePoolElement(Feature.BLOCK_PILE.configured(BiomeDefaultFeatures.MELON_PILE_CONFIG)), 1),
                        Pair.of(EmptyPoolElement.INSTANCE, 4)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/savanna/zombie/decor"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/savanna/savanna_lamp_post_01", var0), 4),
                        new Pair<>(new FeaturePoolElement(Feature.ACACIA_TREE.configured(BiomeDefaultFeatures.ACACIA_TREE_CONFIG)), 4),
                        new Pair<>(new FeaturePoolElement(Feature.BLOCK_PILE.configured(BiomeDefaultFeatures.HAY_PILE_CONFIG)), 4),
                        new Pair<>(new FeaturePoolElement(Feature.BLOCK_PILE.configured(BiomeDefaultFeatures.MELON_PILE_CONFIG)), 1),
                        Pair.of(EmptyPoolElement.INSTANCE, 4)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/savanna/villagers"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/savanna/villagers/nitwit"), 1),
                        new Pair<>(new SinglePoolElement("village/savanna/villagers/baby"), 1),
                        new Pair<>(new SinglePoolElement("village/savanna/villagers/unemployed"), 10)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/savanna/zombie/villagers"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/villagers/nitwit"), 1),
                        new Pair<>(new SinglePoolElement("village/savanna/zombie/villagers/unemployed"), 10)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
    }
}
