package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public class BastionBridgePools {
    public static void bootstrap() {
    }

    static {
        ImmutableList<StructureProcessor> var0 = ImmutableList.of(
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
                    BastionSharedPools.GILDED_BLACKSTONE_REPLACEMENT_RULE
                )
            )
        );
        ImmutableList<StructureProcessor> var1 = ImmutableList.of(
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
                    BastionSharedPools.GILDED_BLACKSTONE_REPLACEMENT_RULE
                )
            )
        );
        ImmutableList<StructureProcessor> var2 = ImmutableList.of(
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
                    BastionSharedPools.GILDED_BLACKSTONE_REPLACEMENT_RULE
                )
            )
        );
        ImmutableList<StructureProcessor> var3 = ImmutableList.of(
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
        );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/bridge/start"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(Pair.of(new SinglePoolElement("bastion/bridge/starting_pieces/entrance_base", var1), 1)),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/bridge/starting_pieces"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/bridge/starting_pieces/entrance", var2), 1),
                        Pair.of(new SinglePoolElement("bastion/bridge/starting_pieces/entrance_face", var1), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/bridge/bridge_pieces"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(Pair.of(new SinglePoolElement("bastion/bridge/bridge_pieces/bridge", var3), 1)),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/bridge/legs"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/bridge/legs/leg_0", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/bridge/legs/leg_1", var1), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/bridge/walls"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/bridge/walls/wall_base_0", var0), 1),
                        Pair.of(new SinglePoolElement("bastion/bridge/walls/wall_base_1", var0), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/bridge/ramparts"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/bridge/ramparts/rampart_0", var0), 1),
                        Pair.of(new SinglePoolElement("bastion/bridge/ramparts/rampart_1", var0), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/bridge/rampart_plates"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(Pair.of(new SinglePoolElement("bastion/bridge/rampart_plates/plate_0", var0), 1)),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("bastion/bridge/connectors"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("bastion/bridge/connectors/back_bridge_top", var1), 1),
                        Pair.of(new SinglePoolElement("bastion/bridge/connectors/back_bridge_bottom", var1), 1)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
    }
}