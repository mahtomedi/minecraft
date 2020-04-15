package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.BastionPieces;
import net.minecraft.world.level.levelgen.feature.VillagePieces;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PillagerOutpostPieces;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureFeatureIO;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JigsawPlacement {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final StructureTemplatePools POOLS = new StructureTemplatePools();

    public static void bootstrap() {
        BastionPieces.bootstrap();
        VillagePieces.bootstrap();
        PillagerOutpostPieces.bootstrap();
    }

    public static void addPieces(
        ResourceLocation param0,
        int param1,
        JigsawPlacement.PieceFactory param2,
        ChunkGenerator<?> param3,
        StructureManager param4,
        BlockPos param5,
        List<? super PoolElementStructurePiece> param6,
        Random param7,
        boolean param8,
        boolean param9
    ) {
        StructureFeatureIO.bootstrap();
        Rotation var0 = Rotation.getRandom(param7);
        StructureTemplatePool var1 = POOLS.getPool(param0);
        StructurePoolElement var2 = var1.getRandomTemplate(param7);
        PoolElementStructurePiece var3 = param2.create(param4, var2, param5, var2.getGroundLevelDelta(), var0, var2.getBoundingBox(param4, param5, var0));
        BoundingBox var4 = var3.getBoundingBox();
        int var5 = (var4.x1 + var4.x0) / 2;
        int var6 = (var4.z1 + var4.z0) / 2;
        int var7;
        if (param9) {
            var7 = param5.getY() + param3.getFirstFreeHeight(var5, var6, Heightmap.Types.WORLD_SURFACE_WG);
        } else {
            var7 = param5.getY();
        }

        int var9 = var4.y0 + var3.getGroundLevelDelta();
        var3.move(0, var7 - var9, 0);
        param6.add(var3);
        if (param1 > 0) {
            int var10 = 80;
            AABB var11 = new AABB(
                (double)(var5 - 80), (double)(var7 - 80), (double)(var6 - 80), (double)(var5 + 80 + 1), (double)(var7 + 80 + 1), (double)(var6 + 80 + 1)
            );
            JigsawPlacement.Placer var12 = new JigsawPlacement.Placer(param1, param2, param3, param4, param6, param7);
            var12.placing
                .addLast(
                    new JigsawPlacement.PieceState(
                        var3, new AtomicReference<>(Shapes.join(Shapes.create(var11), Shapes.create(AABB.of(var4)), BooleanOp.ONLY_FIRST)), var7 + 80, 0
                    )
                );

            while(!var12.placing.isEmpty()) {
                JigsawPlacement.PieceState var13 = var12.placing.removeFirst();
                var12.tryPlacingChildren(var13.piece, var13.free, var13.boundsTop, var13.depth, param8);
            }

        }
    }

    public static void addPieces(
        PoolElementStructurePiece param0,
        int param1,
        JigsawPlacement.PieceFactory param2,
        ChunkGenerator<?> param3,
        StructureManager param4,
        List<? super PoolElementStructurePiece> param5,
        Random param6
    ) {
        bootstrap();
        JigsawPlacement.Placer var0 = new JigsawPlacement.Placer(param1, param2, param3, param4, param5, param6);
        var0.placing.addLast(new JigsawPlacement.PieceState(param0, new AtomicReference<>(Shapes.INFINITY), 0, 0));

        while(!var0.placing.isEmpty()) {
            JigsawPlacement.PieceState var1 = var0.placing.removeFirst();
            var0.tryPlacingChildren(var1.piece, var1.free, var1.boundsTop, var1.depth, false);
        }

    }

    static {
        POOLS.register(StructureTemplatePool.EMPTY);
    }

    public interface PieceFactory {
        PoolElementStructurePiece create(StructureManager var1, StructurePoolElement var2, BlockPos var3, int var4, Rotation var5, BoundingBox var6);
    }

    static final class PieceState {
        private final PoolElementStructurePiece piece;
        private final AtomicReference<VoxelShape> free;
        private final int boundsTop;
        private final int depth;

        private PieceState(PoolElementStructurePiece param0, AtomicReference<VoxelShape> param1, int param2, int param3) {
            this.piece = param0;
            this.free = param1;
            this.boundsTop = param2;
            this.depth = param3;
        }
    }

    static final class Placer {
        private final int maxDepth;
        private final JigsawPlacement.PieceFactory factory;
        private final ChunkGenerator<?> chunkGenerator;
        private final StructureManager structureManager;
        private final List<? super PoolElementStructurePiece> pieces;
        private final Random random;
        private final Deque<JigsawPlacement.PieceState> placing = Queues.newArrayDeque();

        private Placer(
            int param0,
            JigsawPlacement.PieceFactory param1,
            ChunkGenerator<?> param2,
            StructureManager param3,
            List<? super PoolElementStructurePiece> param4,
            Random param5
        ) {
            this.maxDepth = param0;
            this.factory = param1;
            this.chunkGenerator = param2;
            this.structureManager = param3;
            this.pieces = param4;
            this.random = param5;
        }

        private void tryPlacingChildren(PoolElementStructurePiece param0, AtomicReference<VoxelShape> param1, int param2, int param3, boolean param4) {
            StructurePoolElement var0 = param0.getElement();
            BlockPos var1 = param0.getPosition();
            Rotation var2 = param0.getRotation();
            StructureTemplatePool.Projection var3 = var0.getProjection();
            boolean var4 = var3 == StructureTemplatePool.Projection.RIGID;
            AtomicReference<VoxelShape> var5 = new AtomicReference<>();
            BoundingBox var6 = param0.getBoundingBox();
            int var7 = var6.y0;

            label127:
            for(StructureTemplate.StructureBlockInfo var8 : var0.getShuffledJigsawBlocks(this.structureManager, var1, var2, this.random)) {
                Direction var9 = JigsawBlock.getFrontFacing(var8.state);
                BlockPos var10 = var8.pos;
                BlockPos var11 = var10.relative(var9);
                int var12 = var10.getY() - var7;
                int var13 = -1;
                StructureTemplatePool var14 = JigsawPlacement.POOLS.getPool(new ResourceLocation(var8.nbt.getString("pool")));
                StructureTemplatePool var15 = JigsawPlacement.POOLS.getPool(var14.getFallback());
                if (var14 != StructureTemplatePool.INVALID && (var14.size() != 0 || var14 == StructureTemplatePool.EMPTY)) {
                    boolean var16 = var6.isInside(var11);
                    AtomicReference<VoxelShape> var17;
                    int var18;
                    if (var16) {
                        var17 = var5;
                        var18 = var7;
                        if (var5.get() == null) {
                            var5.set(Shapes.create(AABB.of(var6)));
                        }
                    } else {
                        var17 = param1;
                        var18 = param2;
                    }

                    List<StructurePoolElement> var21 = Lists.newArrayList();
                    if (param3 != this.maxDepth) {
                        var21.addAll(var14.getShuffledTemplates(this.random));
                    }

                    var21.addAll(var15.getShuffledTemplates(this.random));

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
                            if (param4 && var25.getYSpan() <= 16) {
                                var27 = var24.stream().mapToInt(param1x -> {
                                    if (!var25.isInside(param1x.pos.relative(JigsawBlock.getFrontFacing(param1x.state)))) {
                                        return 0;
                                    } else {
                                        ResourceLocation var0x = new ResourceLocation(param1x.nbt.getString("pool"));
                                        StructureTemplatePool var1x = JigsawPlacement.POOLS.getPool(var0x);
                                        StructureTemplatePool var2x = JigsawPlacement.POOLS.getPool(var1x.getFallback());
                                        return Math.max(var1x.getMaxSize(this.structureManager), var2x.getMaxSize(this.structureManager));
                                    }
                                }).max().orElse(0);
                            } else {
                                var27 = 0;
                            }

                            for(StructureTemplate.StructureBlockInfo var28 : var24) {
                                if (JigsawBlock.canAttach(var8, var28)) {
                                    BlockPos var29 = var28.pos;
                                    BlockPos var30 = new BlockPos(var11.getX() - var29.getX(), var11.getY() - var29.getY(), var11.getZ() - var29.getZ());
                                    BoundingBox var31 = var22.getBoundingBox(this.structureManager, var30, var23);
                                    int var32 = var31.y0;
                                    StructureTemplatePool.Projection var33 = var22.getProjection();
                                    boolean var34 = var33 == StructureTemplatePool.Projection.RIGID;
                                    int var35 = var29.getY();
                                    int var36 = var12 - var35 + JigsawBlock.getFrontFacing(var8.state).getStepY();
                                    int var37;
                                    if (var4 && var34) {
                                        var37 = var7 + var36;
                                    } else {
                                        if (var13 == -1) {
                                            var13 = this.chunkGenerator.getFirstFreeHeight(var10.getX(), var10.getZ(), Heightmap.Types.WORLD_SURFACE_WG);
                                        }

                                        var37 = var13 - var35;
                                    }

                                    int var39 = var37 - var32;
                                    BoundingBox var40 = var31.moved(0, var39, 0);
                                    BlockPos var41 = var30.offset(0, var39, 0);
                                    if (var27 > 0) {
                                        int var42 = Math.max(var27 + 1, var40.y1 - var40.y0);
                                        var40.y1 = var40.y0 + var42;
                                    }

                                    if (!Shapes.joinIsNotEmpty(var17.get(), Shapes.create(AABB.of(var40).deflate(0.25)), BooleanOp.ONLY_SECOND)) {
                                        var17.set(Shapes.joinUnoptimized(var17.get(), Shapes.create(AABB.of(var40)), BooleanOp.ONLY_FIRST));
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
                                                var13 = this.chunkGenerator.getFirstFreeHeight(var10.getX(), var10.getZ(), Heightmap.Types.WORLD_SURFACE_WG);
                                            }

                                            var47 = var13 + var36 / 2;
                                        }

                                        param0.addJunction(new JigsawJunction(var11.getX(), var47 - var12 + var43, var11.getZ(), var36, var33));
                                        var46.addJunction(new JigsawJunction(var10.getX(), var47 - var35 + var44, var10.getZ(), -var36, var3));
                                        this.pieces.add(var46);
                                        if (param3 + 1 <= this.maxDepth) {
                                            this.placing.addLast(new JigsawPlacement.PieceState(var46, var17, var18, param3 + 1));
                                        }
                                        continue label127;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    JigsawPlacement.LOGGER.warn("Empty or none existent pool: {}", var8.nbt.getString("pool"));
                }
            }

        }
    }
}
