package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.structures.EmptyPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.ListPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class PillagerOutpostPieces {
    public static void addPieces(ChunkGenerator<?> param0, StructureManager param1, BlockPos param2, List<StructurePiece> param3, WorldgenRandom param4) {
        JigsawPlacement.addPieces(
            new ResourceLocation("pillager_outpost/base_plates"), 7, PillagerOutpostPieces.PillagerOutpostPiece::new, param0, param1, param2, param3, param4
        );
    }

    static {
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("pillager_outpost/base_plates"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(Pair.of(new SinglePoolElement("pillager_outpost/base_plate"), 1)),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("pillager_outpost/towers"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(
                            new ListPoolElement(
                                ImmutableList.of(
                                    new SinglePoolElement("pillager_outpost/watchtower"),
                                    new SinglePoolElement("pillager_outpost/watchtower_overgrown", ImmutableList.of(new BlockRotProcessor(0.05F)))
                                )
                            ),
                            1
                        )
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("pillager_outpost/feature_plates"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(Pair.of(new SinglePoolElement("pillager_outpost/feature_plate"), 1)),
                    StructureTemplatePool.Projection.TERRAIN_MATCHING
                )
            );
        JigsawPlacement.POOLS
            .register(
                new StructureTemplatePool(
                    new ResourceLocation("pillager_outpost/features"),
                    new ResourceLocation("empty"),
                    ImmutableList.of(
                        Pair.of(new SinglePoolElement("pillager_outpost/feature_cage1"), 1),
                        Pair.of(new SinglePoolElement("pillager_outpost/feature_cage2"), 1),
                        Pair.of(new SinglePoolElement("pillager_outpost/feature_logs"), 1),
                        Pair.of(new SinglePoolElement("pillager_outpost/feature_tent1"), 1),
                        Pair.of(new SinglePoolElement("pillager_outpost/feature_tent2"), 1),
                        Pair.of(new SinglePoolElement("pillager_outpost/feature_targets"), 1),
                        Pair.of(EmptyPoolElement.INSTANCE, 6)
                    ),
                    StructureTemplatePool.Projection.RIGID
                )
            );
    }

    public static class PillagerOutpostPiece extends PoolElementStructurePiece {
        public PillagerOutpostPiece(StructureManager param0, StructurePoolElement param1, BlockPos param2, int param3, Rotation param4, BoundingBox param5) {
            super(StructurePieceType.PILLAGER_OUTPOST, param0, param1, param2, param3, param4, param5);
        }

        public PillagerOutpostPiece(StructureManager param0, CompoundTag param1) {
            super(param0, param1, StructurePieceType.PILLAGER_OUTPOST);
        }
    }
}
