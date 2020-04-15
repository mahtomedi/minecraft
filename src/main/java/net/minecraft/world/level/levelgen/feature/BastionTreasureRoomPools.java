package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.AxisAlignedLinearPosTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public class BastionTreasureRoomPools {
    public static void bootstrap() {
    }

    static {
        ImmutableList<StructureProcessor> var0 = ImmutableList.of(
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
        );
        ImmutableList<StructureProcessor> var1 = ImmutableList.of(
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
                    BastionSharedPools.GILDED_BLACKSTONE_REPLACEMENT_RULE
                )
            )
        );
        ImmutableList<StructureProcessor> var2 = ImmutableList.of(
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
                    )
                )
            )
        );
        ImmutableList<StructureProcessor> var3 = ImmutableList.of(
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
                    )
                )
            )
        );
        ImmutableList<StructureProcessor> var4 = ImmutableList.of(
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
                    BastionSharedPools.GILDED_BLACKSTONE_REPLACEMENT_RULE
                )
            )
        );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/starters"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(Pair.of(new SinglePoolElement("bastion/treasure/big_air_full", var1), 1)),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/bases"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(Pair.of(new SinglePoolElement("bastion/treasure/bases/lava_basin", var1), 1)),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/stairs"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(Pair.of(new SinglePoolElement("bastion/treasure/stairs/lower_stairs", var1), 1)),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/bases/centers"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/treasure/bases/centers/center_0", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/bases/centers/center_1", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/bases/centers/center_2", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/bases/centers/center_3", var1), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/brains"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(Pair.of(new SinglePoolElement("bastion/treasure/brains/center_brain", var1), 1)),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/walls"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/lava_wall", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/entrance_wall", var2), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/walls/outer"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/outer/top_corner", var2), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/outer/mid_corner", var2), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/outer/bottom_corner", var2), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/outer/outer_wall", var2), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/outer/medium_outer_wall", var2), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/outer/tall_outer_wall", var2), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/walls/bottom"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/bottom/wall_0", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/bottom/wall_1", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/bottom/wall_2", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/bottom/wall_3", var1), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/walls/mid"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/mid/wall_0", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/mid/wall_1", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/mid/wall_2", var1), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/walls/top"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/top/main_entrance", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/top/wall_0", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/walls/top/wall_1", var1), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/connectors"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/treasure/connectors/center_to_wall_middle", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/connectors/center_to_wall_top", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/connectors/center_to_wall_top_entrance", var1), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/entrances"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(Pair.of(new SinglePoolElement("bastion/treasure/entrances/entrance_0", var1), 1)),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/ramparts"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/treasure/ramparts/mid_wall_main", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/ramparts/mid_wall_side", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/ramparts/bottom_wall_0", var4), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/ramparts/top_wall", var3), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/ramparts/lava_basin_side", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/ramparts/lava_basin_main", var1), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/corners/bottom"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/treasure/corners/bottom/corner_0", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/corners/bottom/corner_1", var1), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/corners/edges"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/treasure/corners/edges/bottom", var2), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/corners/edges/middle", var2), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/corners/edges/top", var2), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/corners/middle"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/treasure/corners/middle/corner_0", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/corners/middle/corner_1", var1), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/corners/top"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/treasure/corners/top/corner_0", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/corners/top/corner_1", var1), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/extensions/large_pool"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/empty", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/empty", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/fire_room", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/large_bridge_0", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/large_bridge_1", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/large_bridge_2", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/large_bridge_3", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/roofed_bridge", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/empty", var1), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/extensions/small_pool"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/empty", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/fire_room", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/empty", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/small_bridge_0", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/small_bridge_1", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/small_bridge_2", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/small_bridge_3", var1), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/extensions/houses"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/house_0", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/extensions/house_1", var1), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/treasure/roofs"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/treasure/roofs/wall_roof", var0), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/roofs/corner_roof", var0), 1),
                        Pair.of(new SinglePoolElement("bastion/treasure/roofs/center_roof", var0), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
    }
}
