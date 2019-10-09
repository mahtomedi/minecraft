package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
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

public class TaigaVillagePools {
    public static void bootstrap() {
    }

    static {
        ImmutableList<StructureProcessor> var0 = ImmutableList.of(
            new RuleProcessor(
                ImmutableList.of(
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.8F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
                    new ProcessorRule(new TagMatchTest(BlockTags.DOORS), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                    new ProcessorRule(new BlockMatchTest(Blocks.TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                    new ProcessorRule(new BlockMatchTest(Blocks.WALL_TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                    new ProcessorRule(
                        new BlockMatchTest(Blocks.CAMPFIRE),
                        AlwaysTrueTest.INSTANCE,
                        Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, Boolean.valueOf(false))
                    ),
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.08F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.SPRUCE_LOG, 0.08F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
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
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.PUMPKIN_STEM.defaultBlockState()),
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState())
                )
            )
        );
        ImmutableList<StructureProcessor> var1 = ImmutableList.of(
            new RuleProcessor(
                ImmutableList.of(
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState())
                )
            )
        );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/taiga/town_centers"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/taiga/town_centers/taiga_meeting_point_1", var1), 49),
                        new Pair<>(new SinglePoolElement("village/taiga/town_centers/taiga_meeting_point_2", var1), 49),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/town_centers/taiga_meeting_point_1", var0), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/town_centers/taiga_meeting_point_2", var0), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        ImmutableList<StructureProcessor> var2 = ImmutableList.of(
            new RuleProcessor(
                ImmutableList.of(
                    new ProcessorRule(new BlockMatchTest(Blocks.GRASS_PATH), new BlockMatchTest(Blocks.WATER), Blocks.SPRUCE_PLANKS.defaultBlockState()),
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.GRASS_PATH, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.GRASS_BLOCK.defaultBlockState()),
                    new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
                    new ProcessorRule(new BlockMatchTest(Blocks.DIRT), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState())
                )
            )
        );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/taiga/streets"),
                    new ResourceLocation("village/taiga/terminators"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/taiga/streets/corner_01", var2), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/streets/corner_02", var2), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/streets/corner_03", var2), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/streets/straight_01", var2), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/streets/straight_02", var2), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/streets/straight_03", var2), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/streets/straight_04", var2), 7),
                        new Pair<>(new SinglePoolElement("village/taiga/streets/straight_05", var2), 7),
                        new Pair<>(new SinglePoolElement("village/taiga/streets/straight_06", var2), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/streets/crossroad_01", var2), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/streets/crossroad_02", var2), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/streets/crossroad_03", var2), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/streets/crossroad_04", var2), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/streets/crossroad_05", var2), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/streets/crossroad_06", var2), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/streets/turn_01", var2), 3)
                    ),
                    StructureTemplatePool.Projection.TERRAIN_MATCHING
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/taiga/zombie/streets"),
                    new ResourceLocation("village/taiga/terminators"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/streets/corner_01", var2), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/streets/corner_02", var2), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/streets/corner_03", var2), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/streets/straight_01", var2), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/streets/straight_02", var2), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/streets/straight_03", var2), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/streets/straight_04", var2), 7),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/streets/straight_05", var2), 7),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/streets/straight_06", var2), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/streets/crossroad_01", var2), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/streets/crossroad_02", var2), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/streets/crossroad_03", var2), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/streets/crossroad_04", var2), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/streets/crossroad_05", var2), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/streets/crossroad_06", var2), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/streets/turn_01", var2), 3)
                    ),
                    StructureTemplatePool.Projection.TERRAIN_MATCHING
                )
            );
        ImmutableList<StructureProcessor> var3 = ImmutableList.of(
            new RuleProcessor(
                ImmutableList.of(
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.PUMPKIN_STEM.defaultBlockState()),
                    new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState())
                )
            )
        );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/taiga/houses"),
                    new ResourceLocation("village/taiga/terminators"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_small_house_1", var1), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_small_house_2", var1), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_small_house_3", var1), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_small_house_4", var1), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_small_house_5", var1), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_medium_house_1", var1), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_medium_house_2", var1), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_medium_house_3", var1), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_medium_house_4", var1), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_butcher_shop_1", var1), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_tool_smith_1", var1), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_fletcher_house_1", var1), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_shepherds_house_1", var1), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_armorer_house_1", var1), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_armorer_2", var1), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_fisher_cottage_1", var1), 3),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_tannery_1", var1), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_cartographer_house_1", var1), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_library_1", var1), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_masons_house_1", var1), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_weaponsmith_1", var1), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_weaponsmith_2", var1), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_temple_1", var1), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_large_farm_1", var3), 6),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_large_farm_2", var3), 6),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_small_farm_1", var1), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_animal_pen_1", var1), 2),
                        Pair.of(EmptyPoolElement.INSTANCE, 6)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/taiga/zombie/houses"),
                    new ResourceLocation("village/taiga/terminators"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_small_house_1", var0), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_small_house_2", var0), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_small_house_3", var0), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_small_house_4", var0), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_small_house_5", var0), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_medium_house_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_medium_house_2", var0), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_medium_house_3", var0), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_medium_house_4", var0), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_butcher_shop_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_tool_smith_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_fletcher_house_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_shepherds_house_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_armorer_house_1", var0), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_fisher_cottage_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_tannery_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_cartographer_house_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_library_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_masons_house_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_weaponsmith_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_weaponsmith_2", var0), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_temple_1", var0), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_large_farm_1", var0), 6),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/houses/taiga_large_farm_2", var0), 6),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_small_farm_1", var0), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/houses/taiga_animal_pen_1", var0), 2),
                        Pair.of(EmptyPoolElement.INSTANCE, 6)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/taiga/terminators"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/plains/terminators/terminator_01", var2), 1),
                        new Pair<>(new SinglePoolElement("village/plains/terminators/terminator_02", var2), 1),
                        new Pair<>(new SinglePoolElement("village/plains/terminators/terminator_03", var2), 1),
                        new Pair<>(new SinglePoolElement("village/plains/terminators/terminator_04", var2), 1)
                    ),
                    StructureTemplatePool.Projection.TERRAIN_MATCHING
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/taiga/decor"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/taiga/taiga_lamp_post_1"), 10),
                        new Pair<>(new SinglePoolElement("village/taiga/taiga_decoration_1"), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/taiga_decoration_2"), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/taiga_decoration_3"), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/taiga_decoration_4"), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/taiga_decoration_5"), 2),
                        new Pair<>(new SinglePoolElement("village/taiga/taiga_decoration_6"), 1),
                        new Pair<>(new FeaturePoolElement(Feature.NORMAL_TREE.configured(BiomeDefaultFeatures.SPRUCE_TREE_CONFIG)), 4),
                        new Pair<>(new FeaturePoolElement(Feature.NORMAL_TREE.configured(BiomeDefaultFeatures.PINE_TREE_CONFIG)), 4),
                        new Pair<>(new FeaturePoolElement(Feature.BLOCK_PILE.configured(BiomeDefaultFeatures.PUMPKIN_PILE_CONFIG)), 2),
                        new Pair<>(new FeaturePoolElement(Feature.RANDOM_PATCH.configured(BiomeDefaultFeatures.TAIGA_GRASS_CONFIG)), 4),
                        new Pair<>(new FeaturePoolElement(Feature.RANDOM_PATCH.configured(BiomeDefaultFeatures.SWEET_BERRY_BUSH_CONFIG)), 1),
                        Pair.of(EmptyPoolElement.INSTANCE, 4)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/taiga/zombie/decor"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/taiga/taiga_decoration_1"), 4),
                        new Pair<>(new SinglePoolElement("village/taiga/taiga_decoration_2"), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/taiga_decoration_3"), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/taiga_decoration_4"), 1),
                        new Pair<>(new FeaturePoolElement(Feature.NORMAL_TREE.configured(BiomeDefaultFeatures.SPRUCE_TREE_CONFIG)), 4),
                        new Pair<>(new FeaturePoolElement(Feature.NORMAL_TREE.configured(BiomeDefaultFeatures.PINE_TREE_CONFIG)), 4),
                        new Pair<>(new FeaturePoolElement(Feature.BLOCK_PILE.configured(BiomeDefaultFeatures.PUMPKIN_PILE_CONFIG)), 2),
                        new Pair<>(new FeaturePoolElement(Feature.RANDOM_PATCH.configured(BiomeDefaultFeatures.TAIGA_GRASS_CONFIG)), 4),
                        new Pair<>(new FeaturePoolElement(Feature.RANDOM_PATCH.configured(BiomeDefaultFeatures.SWEET_BERRY_BUSH_CONFIG)), 1),
                        Pair.of(EmptyPoolElement.INSTANCE, 4)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/taiga/villagers"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/taiga/villagers/nitwit"), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/villagers/baby"), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/villagers/unemployed"), 10)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("village/taiga/zombie/villagers"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/villagers/nitwit"), 1),
                        new Pair<>(new SinglePoolElement("village/taiga/zombie/villagers/unemployed"), 10)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
    }
}
