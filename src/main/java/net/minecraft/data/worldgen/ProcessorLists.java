package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.AxisAlignedLinearPosTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.CappedProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosAlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProtectedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.AppendLoot;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class ProcessorLists {
    private static final ResourceKey<StructureProcessorList> EMPTY = createKey("empty");
    public static final ResourceKey<StructureProcessorList> ZOMBIE_PLAINS = createKey("zombie_plains");
    public static final ResourceKey<StructureProcessorList> ZOMBIE_SAVANNA = createKey("zombie_savanna");
    public static final ResourceKey<StructureProcessorList> ZOMBIE_SNOWY = createKey("zombie_snowy");
    public static final ResourceKey<StructureProcessorList> ZOMBIE_TAIGA = createKey("zombie_taiga");
    public static final ResourceKey<StructureProcessorList> ZOMBIE_DESERT = createKey("zombie_desert");
    public static final ResourceKey<StructureProcessorList> MOSSIFY_10_PERCENT = createKey("mossify_10_percent");
    public static final ResourceKey<StructureProcessorList> MOSSIFY_20_PERCENT = createKey("mossify_20_percent");
    public static final ResourceKey<StructureProcessorList> MOSSIFY_70_PERCENT = createKey("mossify_70_percent");
    public static final ResourceKey<StructureProcessorList> STREET_PLAINS = createKey("street_plains");
    public static final ResourceKey<StructureProcessorList> STREET_SAVANNA = createKey("street_savanna");
    public static final ResourceKey<StructureProcessorList> STREET_SNOWY_OR_TAIGA = createKey("street_snowy_or_taiga");
    public static final ResourceKey<StructureProcessorList> FARM_PLAINS = createKey("farm_plains");
    public static final ResourceKey<StructureProcessorList> FARM_SAVANNA = createKey("farm_savanna");
    public static final ResourceKey<StructureProcessorList> FARM_SNOWY = createKey("farm_snowy");
    public static final ResourceKey<StructureProcessorList> FARM_TAIGA = createKey("farm_taiga");
    public static final ResourceKey<StructureProcessorList> FARM_DESERT = createKey("farm_desert");
    public static final ResourceKey<StructureProcessorList> OUTPOST_ROT = createKey("outpost_rot");
    public static final ResourceKey<StructureProcessorList> BOTTOM_RAMPART = createKey("bottom_rampart");
    public static final ResourceKey<StructureProcessorList> TREASURE_ROOMS = createKey("treasure_rooms");
    public static final ResourceKey<StructureProcessorList> HOUSING = createKey("housing");
    public static final ResourceKey<StructureProcessorList> SIDE_WALL_DEGRADATION = createKey("side_wall_degradation");
    public static final ResourceKey<StructureProcessorList> STABLE_DEGRADATION = createKey("stable_degradation");
    public static final ResourceKey<StructureProcessorList> BASTION_GENERIC_DEGRADATION = createKey("bastion_generic_degradation");
    public static final ResourceKey<StructureProcessorList> RAMPART_DEGRADATION = createKey("rampart_degradation");
    public static final ResourceKey<StructureProcessorList> ENTRANCE_REPLACEMENT = createKey("entrance_replacement");
    public static final ResourceKey<StructureProcessorList> BRIDGE = createKey("bridge");
    public static final ResourceKey<StructureProcessorList> ROOF = createKey("roof");
    public static final ResourceKey<StructureProcessorList> HIGH_WALL = createKey("high_wall");
    public static final ResourceKey<StructureProcessorList> HIGH_RAMPART = createKey("high_rampart");
    public static final ResourceKey<StructureProcessorList> FOSSIL_ROT = createKey("fossil_rot");
    public static final ResourceKey<StructureProcessorList> FOSSIL_COAL = createKey("fossil_coal");
    public static final ResourceKey<StructureProcessorList> FOSSIL_DIAMONDS = createKey("fossil_diamonds");
    public static final ResourceKey<StructureProcessorList> ANCIENT_CITY_START_DEGRADATION = createKey("ancient_city_start_degradation");
    public static final ResourceKey<StructureProcessorList> ANCIENT_CITY_GENERIC_DEGRADATION = createKey("ancient_city_generic_degradation");
    public static final ResourceKey<StructureProcessorList> ANCIENT_CITY_WALLS_DEGRADATION = createKey("ancient_city_walls_degradation");
    public static final ResourceKey<StructureProcessorList> TRAIL_RUINS_SUSPICIOUS_SAND = createKey("trail_ruins_suspicious_sand");

    private static ResourceKey<StructureProcessorList> createKey(String param0) {
        return ResourceKey.create(Registries.PROCESSOR_LIST, new ResourceLocation(param0));
    }

    private static void register(BootstapContext<StructureProcessorList> param0, ResourceKey<StructureProcessorList> param1, List<StructureProcessor> param2) {
        param0.register(param1, new StructureProcessorList(param2));
    }

    public static void bootstrap(BootstapContext<StructureProcessorList> param0) {
        HolderGetter<Block> var0 = param0.lookup(Registries.BLOCK);
        ProcessorRule var1 = new ProcessorRule(
            new RandomBlockMatchTest(Blocks.BLACKSTONE, 0.01F), AlwaysTrueTest.INSTANCE, Blocks.GILDED_BLACKSTONE.defaultBlockState()
        );
        ProcessorRule var2 = new ProcessorRule(
            new RandomBlockMatchTest(Blocks.GILDED_BLACKSTONE, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.BLACKSTONE.defaultBlockState()
        );
        register(param0, EMPTY, ImmutableList.of());
        register(
            param0,
            ZOMBIE_PLAINS,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.8F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                        ),
                        new ProcessorRule(new TagMatchTest(BlockTags.DOORS), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        new ProcessorRule(new BlockMatchTest(Blocks.TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        new ProcessorRule(new BlockMatchTest(Blocks.WALL_TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.07F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.MOSSY_COBBLESTONE, 0.07F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHITE_TERRACOTTA, 0.07F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.OAK_LOG, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.OAK_PLANKS, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.OAK_STAIRS, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.STRIPPED_OAK_LOG, 0.02F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
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
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CARROTS.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.BEETROOTS.defaultBlockState())
                    )
                )
            )
        );
        register(
            param0,
            ZOMBIE_SAVANNA,
            ImmutableList.of(
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
            )
        );
        register(
            param0,
            ZOMBIE_SNOWY,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(new TagMatchTest(BlockTags.DOORS), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        new ProcessorRule(new BlockMatchTest(Blocks.TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        new ProcessorRule(new BlockMatchTest(Blocks.WALL_TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        new ProcessorRule(new BlockMatchTest(Blocks.LANTERN), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.SPRUCE_PLANKS, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.SPRUCE_SLAB, 0.4F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.STRIPPED_SPRUCE_LOG, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.STRIPPED_SPRUCE_WOOD, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()
                        ),
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
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.CARROTS.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.8F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState())
                    )
                )
            )
        );
        register(
            param0,
            ZOMBIE_TAIGA,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.8F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                        ),
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
            )
        );
        register(
            param0,
            ZOMBIE_DESERT,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(new TagMatchTest(BlockTags.DOORS), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        new ProcessorRule(new BlockMatchTest(Blocks.TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        new ProcessorRule(new BlockMatchTest(Blocks.WALL_TORCH), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.SMOOTH_SANDSTONE, 0.08F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.CUT_SANDSTONE, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.TERRACOTTA, 0.08F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.SMOOTH_SANDSTONE_STAIRS, 0.08F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.SMOOTH_SANDSTONE_SLAB, 0.08F), AlwaysTrueTest.INSTANCE, Blocks.COBWEB.defaultBlockState()
                        ),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.BEETROOTS.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MELON_STEM.defaultBlockState())
                    )
                )
            )
        );
        register(
            param0,
            MOSSIFY_10_PERCENT,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                        )
                    )
                )
            )
        );
        register(
            param0,
            MOSSIFY_20_PERCENT,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                        )
                    )
                )
            )
        );
        register(
            param0,
            MOSSIFY_70_PERCENT,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.COBBLESTONE, 0.7F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                        )
                    )
                )
            )
        );
        register(
            param0,
            STREET_PLAINS,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(new BlockMatchTest(Blocks.DIRT_PATH), new BlockMatchTest(Blocks.WATER), Blocks.OAK_PLANKS.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.DIRT_PATH, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.GRASS_BLOCK.defaultBlockState()),
                        new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
                        new ProcessorRule(new BlockMatchTest(Blocks.DIRT), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState())
                    )
                )
            )
        );
        register(
            param0,
            STREET_SAVANNA,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(new BlockMatchTest(Blocks.DIRT_PATH), new BlockMatchTest(Blocks.WATER), Blocks.ACACIA_PLANKS.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.DIRT_PATH, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.GRASS_BLOCK.defaultBlockState()),
                        new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
                        new ProcessorRule(new BlockMatchTest(Blocks.DIRT), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState())
                    )
                )
            )
        );
        register(
            param0,
            STREET_SNOWY_OR_TAIGA,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(new BlockMatchTest(Blocks.DIRT_PATH), new BlockMatchTest(Blocks.WATER), Blocks.SPRUCE_PLANKS.defaultBlockState()),
                        new ProcessorRule(new BlockMatchTest(Blocks.DIRT_PATH), new BlockMatchTest(Blocks.ICE), Blocks.SPRUCE_PLANKS.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.DIRT_PATH, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.GRASS_BLOCK.defaultBlockState()),
                        new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
                        new ProcessorRule(new BlockMatchTest(Blocks.DIRT), new BlockMatchTest(Blocks.WATER), Blocks.WATER.defaultBlockState())
                    )
                )
            )
        );
        register(
            param0,
            FARM_PLAINS,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CARROTS.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.BEETROOTS.defaultBlockState())
                    )
                )
            )
        );
        register(
            param0,
            FARM_SAVANNA,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MELON_STEM.defaultBlockState())
                    )
                )
            )
        );
        register(
            param0,
            FARM_SNOWY,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.CARROTS.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.8F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState())
                    )
                )
            )
        );
        register(
            param0,
            FARM_TAIGA,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.PUMPKIN_STEM.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.POTATOES.defaultBlockState())
                    )
                )
            )
        );
        register(
            param0,
            FARM_DESERT,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.BEETROOTS.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MELON_STEM.defaultBlockState())
                    )
                )
            )
        );
        register(param0, OUTPOST_ROT, ImmutableList.of(new BlockRotProcessor(0.05F)));
        register(
            param0,
            BOTTOM_RAMPART,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.MAGMA_BLOCK, 0.75F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, 0.15F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        var2,
                        var1
                    )
                )
            )
        );
        register(
            param0,
            TREASURE_ROOMS,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.35F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.CHISELED_POLISHED_BLACKSTONE, 0.1F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        var2,
                        var1
                    )
                )
            )
        );
        register(
            param0,
            HOUSING,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        var2,
                        var1
                    )
                )
            )
        );
        register(
            param0,
            SIDE_WALL_DEGRADATION,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.CHISELED_POLISHED_BLACKSTONE, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.1F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        var2,
                        var1
                    )
                )
            )
        );
        register(
            param0,
            STABLE_DEGRADATION,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.1F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        var2,
                        var1
                    )
                )
            )
        );
        register(
            param0,
            BASTION_GENERIC_DEGRADATION,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.3F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        var2,
                        var1
                    )
                )
            )
        );
        register(
            param0,
            RAMPART_DEGRADATION,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.4F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.BLACKSTONE, 0.01F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()
                        ),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.3F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        var2,
                        var1
                    )
                )
            )
        );
        register(
            param0,
            ENTRANCE_REPLACEMENT,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.CHISELED_POLISHED_BLACKSTONE, 0.5F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.6F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        var2,
                        var1
                    )
                )
            )
        );
        register(
            param0,
            BRIDGE,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.BLACKSTONE, 1.0E-4F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState())
                    )
                )
            )
        );
        register(
            param0,
            ROOF,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.15F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.BLACKSTONE.defaultBlockState()
                        )
                    )
                )
            )
        );
        register(
            param0,
            HIGH_WALL,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.01F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.5F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.BLACKSTONE.defaultBlockState()
                        ),
                        var2
                    )
                )
            )
        );
        register(
            param0,
            HIGH_RAMPART,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.GOLD_BLOCK, 0.3F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        ),
                        new ProcessorRule(
                            AlwaysTrueTest.INSTANCE,
                            AlwaysTrueTest.INSTANCE,
                            new AxisAlignedLinearPosTest(0.0F, 0.05F, 0, 100, Direction.Axis.Y),
                            Blocks.AIR.defaultBlockState()
                        ),
                        var2
                    )
                )
            )
        );
        register(param0, FOSSIL_ROT, ImmutableList.of(new BlockRotProcessor(0.9F), new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)));
        register(param0, FOSSIL_COAL, ImmutableList.of(new BlockRotProcessor(0.1F), new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)));
        register(
            param0,
            FOSSIL_DIAMONDS,
            ImmutableList.of(
                new BlockRotProcessor(0.1F),
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(new BlockMatchTest(Blocks.COAL_ORE), AlwaysTrueTest.INSTANCE, Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState())
                    )
                ),
                new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)
            )
        );
        register(
            param0,
            ANCIENT_CITY_START_DEGRADATION,
            ImmutableList.of(
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.DEEPSLATE_BRICKS, 0.3F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState()
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.DEEPSLATE_TILES, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState()
                        ),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.SOUL_LANTERN, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState())
                    )
                ),
                new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)
            )
        );
        register(
            param0,
            ANCIENT_CITY_GENERIC_DEGRADATION,
            ImmutableList.of(
                new BlockRotProcessor(var0.getOrThrow(BlockTags.ANCIENT_CITY_REPLACEABLE), 0.95F),
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.DEEPSLATE_BRICKS, 0.3F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState()
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.DEEPSLATE_TILES, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState()
                        ),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.SOUL_LANTERN, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState())
                    )
                ),
                new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)
            )
        );
        register(
            param0,
            ANCIENT_CITY_WALLS_DEGRADATION,
            ImmutableList.of(
                new BlockRotProcessor(var0.getOrThrow(BlockTags.ANCIENT_CITY_REPLACEABLE), 0.95F),
                new RuleProcessor(
                    ImmutableList.of(
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.DEEPSLATE_BRICKS, 0.3F),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState()
                        ),
                        new ProcessorRule(
                            new RandomBlockMatchTest(Blocks.DEEPSLATE_TILES, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState()
                        ),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.DEEPSLATE_TILE_SLAB, 0.3F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.SOUL_LANTERN, 0.05F), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState())
                    )
                ),
                new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)
            )
        );
        register(
            param0,
            TRAIL_RUINS_SUSPICIOUS_SAND,
            List.of(
                new RuleProcessor(
                    List.of(
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.SAND, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.GRAVEL.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.SAND, 0.2F), AlwaysTrueTest.INSTANCE, Blocks.DIRT.defaultBlockState()),
                        new ProcessorRule(new RandomBlockMatchTest(Blocks.SAND, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.COARSE_DIRT.defaultBlockState())
                    )
                ),
                new CappedProcessor(
                    new RuleProcessor(
                        List.of(
                            new ProcessorRule(
                                new TagMatchTest(BlockTags.TRAIL_RUINS_REPLACEABLE),
                                AlwaysTrueTest.INSTANCE,
                                PosAlwaysTrueTest.INSTANCE,
                                Blocks.SUSPICIOUS_SAND.defaultBlockState(),
                                new AppendLoot(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY)
                            )
                        )
                    ),
                    ConstantInt.of(6)
                ),
                new CappedProcessor(
                    new RuleProcessor(
                        List.of(
                            new ProcessorRule(
                                new TagMatchTest(BlockTags.TRAIL_RUINS_REPLACEABLE),
                                AlwaysTrueTest.INSTANCE,
                                PosAlwaysTrueTest.INSTANCE,
                                Blocks.SUSPICIOUS_GRAVEL.defaultBlockState(),
                                new AppendLoot(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY)
                            )
                        )
                    ),
                    ConstantInt.of(2)
                )
            )
        );
    }
}
