package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JigsawPlacement {
    static final Logger LOGGER = LogManager.getLogger();

    public static void addPieces(
        RegistryAccess param0,
        JigsawConfiguration param1,
        JigsawPlacement.PieceFactory param2,
        ChunkGenerator param3,
        StructureManager param4,
        BlockPos param5,
        StructurePieceAccessor param6,
        Random param7,
        boolean param8,
        boolean param9,
        LevelHeightAccessor param10,
        Predicate<Biome> param11
    ) {
        StructureFeature.bootstrap();
        List<PoolElementStructurePiece> var0 = Lists.newArrayList();
        Registry<StructureTemplatePool> var1 = param0.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        Rotation var2 = Rotation.getRandom(param7);
        StructureTemplatePool var3 = param1.startPool().get();
        StructurePoolElement var4 = var3.getRandomTemplate(param7);
        if (var4 != EmptyPoolElement.INSTANCE) {
            PoolElementStructurePiece var5 = param2.create(param4, var4, param5, var4.getGroundLevelDelta(), var2, var4.getBoundingBox(param4, param5, var2));
            BoundingBox var6 = var5.getBoundingBox();
            int var7 = (var6.maxX() + var6.minX()) / 2;
            int var8 = (var6.maxZ() + var6.minZ()) / 2;
            int var9;
            if (param9) {
                var9 = param5.getY() + param3.getFirstFreeHeight(var7, var8, Heightmap.Types.WORLD_SURFACE_WG, param10);
            } else {
                var9 = param5.getY();
            }

            if (param11.test(param3.getNoiseBiome(QuartPos.fromBlock(var7), QuartPos.fromBlock(var9), QuartPos.fromBlock(var8)))) {
                int var11 = var6.minY() + var5.getGroundLevelDelta();
                var5.move(0, var9 - var11, 0);
                var0.add(var5);
                if (param1.maxDepth() > 0) {
                    int var12 = 80;
                    AABB var13 = new AABB(
                        (double)(var7 - 80),
                        (double)(var9 - 80),
                        (double)(var8 - 80),
                        (double)(var7 + 80 + 1),
                        (double)(var9 + 80 + 1),
                        (double)(var8 + 80 + 1)
                    );
                    JigsawPlacement.Placer var14 = new JigsawPlacement.Placer(var1, param1.maxDepth(), param2, param3, param4, var0, param7);
                    var14.placing
                        .addLast(
                            new JigsawPlacement.PieceState(
                                var5, new MutableObject<>(Shapes.join(Shapes.create(var13), Shapes.create(AABB.of(var6)), BooleanOp.ONLY_FIRST)), 0
                            )
                        );

                    while(!var14.placing.isEmpty()) {
                        JigsawPlacement.PieceState var15 = var14.placing.removeFirst();
                        var14.tryPlacingChildren(var15.piece, var15.free, var15.depth, param8, param10);
                    }

                    var0.forEach(param6::addPiece);
                }
            }
        }
    }

    public static void addPieces(
        RegistryAccess param0,
        PoolElementStructurePiece param1,
        int param2,
        JigsawPlacement.PieceFactory param3,
        ChunkGenerator param4,
        StructureManager param5,
        List<? super PoolElementStructurePiece> param6,
        Random param7,
        LevelHeightAccessor param8
    ) {
        Registry<StructureTemplatePool> var0 = param0.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        JigsawPlacement.Placer var1 = new JigsawPlacement.Placer(var0, param2, param3, param4, param5, param6, param7);
        var1.placing.addLast(new JigsawPlacement.PieceState(param1, new MutableObject<>(Shapes.INFINITY), 0));

        while(!var1.placing.isEmpty()) {
            JigsawPlacement.PieceState var2 = var1.placing.removeFirst();
            var1.tryPlacingChildren(var2.piece, var2.free, var2.depth, false, param8);
        }

    }

    public interface PieceFactory {
        PoolElementStructurePiece create(StructureManager var1, StructurePoolElement var2, BlockPos var3, int var4, Rotation var5, BoundingBox var6);
    }

    static final class PieceState {
        final PoolElementStructurePiece piece;
        final MutableObject<VoxelShape> free;
        final int depth;

        PieceState(PoolElementStructurePiece param0, MutableObject<VoxelShape> param1, int param2) {
            this.piece = param0;
            this.free = param1;
            this.depth = param2;
        }
    }

    static final class Placer {
        private final Registry<StructureTemplatePool> pools;
        private final int maxDepth;
        private final JigsawPlacement.PieceFactory factory;
        private final ChunkGenerator chunkGenerator;
        private final StructureManager structureManager;
        private final List<? super PoolElementStructurePiece> pieces;
        private final Random random;
        final Deque<JigsawPlacement.PieceState> placing = Queues.newArrayDeque();

        Placer(
            Registry<StructureTemplatePool> param0,
            int param1,
            JigsawPlacement.PieceFactory param2,
            ChunkGenerator param3,
            StructureManager param4,
            List<? super PoolElementStructurePiece> param5,
            Random param6
        ) {
            this.pools = param0;
            this.maxDepth = param1;
            this.factory = param2;
            this.chunkGenerator = param3;
            this.structureManager = param4;
            this.pieces = param5;
            this.random = param6;
        }

        void tryPlacingChildren(PoolElementStructurePiece param0, MutableObject<VoxelShape> param1, int param2, boolean param3, LevelHeightAccessor param4) {
            StructurePoolElement var0 = param0.getElement();
            BlockPos var1 = param0.getPosition();
            Rotation var2 = param0.getRotation();
            StructureTemplatePool.Projection var3 = var0.getProjection();
            boolean var4 = var3 == StructureTemplatePool.Projection.RIGID;
            MutableObject<VoxelShape> var5 = new MutableObject<>();
            BoundingBox var6 = param0.getBoundingBox();
            int var7 = var6.minY();

            label139:
            for(StructureTemplate.StructureBlockInfo var8 : var0.getShuffledJigsawBlocks(this.structureManager, var1, var2, this.random)) {
                Direction var9 = JigsawBlock.getFrontFacing(var8.state);
                BlockPos var10 = var8.pos;
                BlockPos var11 = var10.relative(var9);
                int var12 = var10.getY() - var7;
                int var13 = -1;
                ResourceLocation var14 = new ResourceLocation(var8.nbt.getString("pool"));
                Optional<StructureTemplatePool> var15 = this.pools.getOptional(var14);
                if (var15.isPresent() && (var15.get().size() != 0 || Objects.equals(var14, Pools.EMPTY.location()))) {
                    ResourceLocation var16 = var15.get().getFallback();
                    Optional<StructureTemplatePool> var17 = this.pools.getOptional(var16);
                    if (var17.isPresent() && (var17.get().size() != 0 || Objects.equals(var16, Pools.EMPTY.location()))) {
                        boolean var18 = var6.isInside(var11);
                        MutableObject<VoxelShape> var19;
                        if (var18) {
                            var19 = var5;
                            if (var5.getValue() == null) {
                                var5.setValue(Shapes.create(AABB.of(var6)));
                            }
                        } else {
                            var19 = param1;
                        }

                        List<StructurePoolElement> var21 = Lists.newArrayList();
                        if (param2 != this.maxDepth) {
                            var21.addAll(var15.get().getShuffledTemplates(this.random));
                        }

                        var21.addAll(var17.get().getShuffledTemplates(this.random));

                        for(StructurePoolElement var22 : var21) {
                            if (var22 == EmptyPoolElement.INSTANCE) {
                                break;
                            }

                            for(Rotation var23 : Rotation.getShuffled(this.random)) {
                                List<StructureTemplate.StructureBlockInfo> var24 = var22.getShuffledJigsawBlocks(
                                    this.structureManager, BlockPos.ZERO, var23, this.random
                                );
                                BoundingBox var25 = var22.getBoundingBox(this.structureManager, BlockPos.ZERO, var23);
                                int var27;
                                if (param3 && var25.getYSpan() <= 16) {
                                    var27 = var24.stream().mapToInt(param1x -> {
                                        if (!var25.isInside(param1x.pos.relative(JigsawBlock.getFrontFacing(param1x.state)))) {
                                            return 0;
                                        } else {
                                            ResourceLocation var0x = new ResourceLocation(param1x.nbt.getString("pool"));
                                            Optional<StructureTemplatePool> var1x = this.pools.getOptional(var0x);
                                            Optional<StructureTemplatePool> var2x = var1x.flatMap(param0x -> this.pools.getOptional(param0x.getFallback()));
                                            int var3x = var1x.<Integer>map(param0x -> param0x.getMaxSize(this.structureManager)).orElse(0);
                                            int var4x = var2x.<Integer>map(param0x -> param0x.getMaxSize(this.structureManager)).orElse(0);
                                            return Math.max(var3x, var4x);
                                        }
                                    }).max().orElse(0);
                                } else {
                                    var27 = 0;
                                }

                                for(StructureTemplate.StructureBlockInfo var28 : var24) {
                                    if (JigsawBlock.canAttach(var8, var28)) {
                                        BlockPos var29 = var28.pos;
                                        BlockPos var30 = var11.subtract(var29);
                                        BoundingBox var31 = var22.getBoundingBox(this.structureManager, var30, var23);
                                        int var32 = var31.minY();
                                        StructureTemplatePool.Projection var33 = var22.getProjection();
                                        boolean var34 = var33 == StructureTemplatePool.Projection.RIGID;
                                        int var35 = var29.getY();
                                        int var36 = var12 - var35 + JigsawBlock.getFrontFacing(var8.state).getStepY();
                                        int var37;
                                        if (var4 && var34) {
                                            var37 = var7 + var36;
                                        } else {
                                            if (var13 == -1) {
                                                var13 = this.chunkGenerator
                                                    .getFirstFreeHeight(var10.getX(), var10.getZ(), Heightmap.Types.WORLD_SURFACE_WG, param4);
                                            }

                                            var37 = var13 - var35;
                                        }

                                        int var39 = var37 - var32;
                                        BoundingBox var40 = var31.moved(0, var39, 0);
                                        BlockPos var41 = var30.offset(0, var39, 0);
                                        if (var27 > 0) {
                                            int var42 = Math.max(var27 + 1, var40.maxY() - var40.minY());
                                            var40.encapsulate(new BlockPos(var40.minX(), var40.minY() + var42, var40.minZ()));
                                        }

                                        if (!Shapes.joinIsNotEmpty(var19.getValue(), Shapes.create(AABB.of(var40).deflate(0.25)), BooleanOp.ONLY_SECOND)) {
                                            var19.setValue(Shapes.joinUnoptimized(var19.getValue(), Shapes.create(AABB.of(var40)), BooleanOp.ONLY_FIRST));
                                            int var43 = param0.getGroundLevelDelta();
                                            int var44;
                                            if (var34) {
                                                var44 = var43 - var36;
                                            } else {
                                                var44 = var22.getGroundLevelDelta();
                                            }

                                            PoolElementStructurePiece var46 = this.factory.create(this.structureManager, var22, var41, var44, var23, var40);
                                            int var47;
                                            if (var4) {
                                                var47 = var7 + var12;
                                            } else if (var34) {
                                                var47 = var37 + var35;
                                            } else {
                                                if (var13 == -1) {
                                                    var13 = this.chunkGenerator
                                                        .getFirstFreeHeight(var10.getX(), var10.getZ(), Heightmap.Types.WORLD_SURFACE_WG, param4);
                                                }

                                                var47 = var13 + var36 / 2;
                                            }

                                            param0.addJunction(new JigsawJunction(var11.getX(), var47 - var12 + var43, var11.getZ(), var36, var33));
                                            var46.addJunction(new JigsawJunction(var10.getX(), var47 - var35 + var44, var10.getZ(), -var36, var3));
                                            this.pieces.add(var46);
                                            if (param2 + 1 <= this.maxDepth) {
                                                this.placing.addLast(new JigsawPlacement.PieceState(var46, var19, param2 + 1));
                                            }
                                            continue label139;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        JigsawPlacement.LOGGER.warn("Empty or non-existent fallback pool: {}", var16);
                    }
                } else {
                    JigsawPlacement.LOGGER.warn("Empty or non-existent pool: {}", var14);
                }
            }

        }
    }
}
