package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SequencedPriorityIterator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class JigsawPlacement {
    static final Logger LOGGER = LogUtils.getLogger();

    public static Optional<Structure.GenerationStub> addPieces(
        Structure.GenerationContext param0,
        Holder<StructureTemplatePool> param1,
        Optional<ResourceLocation> param2,
        int param3,
        BlockPos param4,
        boolean param5,
        Optional<Heightmap.Types> param6,
        int param7,
        PoolAliasLookup param8
    ) {
        RegistryAccess var0 = param0.registryAccess();
        ChunkGenerator var1 = param0.chunkGenerator();
        StructureTemplateManager var2 = param0.structureTemplateManager();
        LevelHeightAccessor var3 = param0.heightAccessor();
        WorldgenRandom var4 = param0.random();
        Registry<StructureTemplatePool> var5 = var0.registryOrThrow(Registries.TEMPLATE_POOL);
        Rotation var6 = Rotation.getRandom(var4);
        StructureTemplatePool var7 = param1.value();
        StructurePoolElement var8 = var7.getRandomTemplate(var4);
        if (var8 == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        } else {
            BlockPos var11;
            if (param2.isPresent()) {
                ResourceLocation var9 = param2.get();
                Optional<BlockPos> var10 = getRandomNamedJigsaw(var8, var9, param4, var6, var2, var4);
                if (var10.isEmpty()) {
                    LOGGER.error(
                        "No starting jigsaw {} found in start pool {}",
                        var9,
                        param1.unwrapKey().map(param0x -> param0x.location().toString()).orElse("<unregistered>")
                    );
                    return Optional.empty();
                }

                var11 = var10.get();
            } else {
                var11 = param4;
            }

            Vec3i var13 = var11.subtract(param4);
            BlockPos var14 = param4.subtract(var13);
            PoolElementStructurePiece var15 = new PoolElementStructurePiece(
                var2, var8, var14, var8.getGroundLevelDelta(), var6, var8.getBoundingBox(var2, var14, var6)
            );
            BoundingBox var16 = var15.getBoundingBox();
            int var17 = (var16.maxX() + var16.minX()) / 2;
            int var18 = (var16.maxZ() + var16.minZ()) / 2;
            int var19;
            if (param6.isPresent()) {
                var19 = param4.getY() + var1.getFirstFreeHeight(var17, var18, param6.get(), var3, param0.randomState());
            } else {
                var19 = var14.getY();
            }

            int var21 = var16.minY() + var15.getGroundLevelDelta();
            var15.move(0, var19 - var21, 0);
            int var22 = var19 + var13.getY();
            return Optional.of(
                new Structure.GenerationStub(
                    new BlockPos(var17, var22, var18),
                    param15 -> {
                        List<PoolElementStructurePiece> var0x = Lists.newArrayList();
                        var0x.add(var15);
                        if (param3 > 0) {
                            AABB var1x = new AABB(
                                (double)(var17 - param7),
                                (double)(var22 - param7),
                                (double)(var18 - param7),
                                (double)(var17 + param7 + 1),
                                (double)(var22 + param7 + 1),
                                (double)(var18 + param7 + 1)
                            );
                            VoxelShape var2x = Shapes.join(Shapes.create(var1x), Shapes.create(AABB.of(var16)), BooleanOp.ONLY_FIRST);
                            addPieces(param0.randomState(), param3, param5, var1, var2, var3, var4, var5, var15, var0x, var2x, param8);
                            var0x.forEach(param15::addPiece);
                        }
                    }
                )
            );
        }
    }

    private static Optional<BlockPos> getRandomNamedJigsaw(
        StructurePoolElement param0, ResourceLocation param1, BlockPos param2, Rotation param3, StructureTemplateManager param4, WorldgenRandom param5
    ) {
        List<StructureTemplate.StructureBlockInfo> var0 = param0.getShuffledJigsawBlocks(param4, param2, param3, param5);
        Optional<BlockPos> var1 = Optional.empty();

        for(StructureTemplate.StructureBlockInfo var2 : var0) {
            ResourceLocation var3 = ResourceLocation.tryParse(Objects.requireNonNull(var2.nbt(), () -> var2 + " nbt was null").getString("name"));
            if (param1.equals(var3)) {
                var1 = Optional.of(var2.pos());
                break;
            }
        }

        return var1;
    }

    private static void addPieces(
        RandomState param0,
        int param1,
        boolean param2,
        ChunkGenerator param3,
        StructureTemplateManager param4,
        LevelHeightAccessor param5,
        RandomSource param6,
        Registry<StructureTemplatePool> param7,
        PoolElementStructurePiece param8,
        List<PoolElementStructurePiece> param9,
        VoxelShape param10,
        PoolAliasLookup param11
    ) {
        JigsawPlacement.Placer var0 = new JigsawPlacement.Placer(param7, param1, param3, param4, param9, param6);
        var0.tryPlacingChildren(param8, new MutableObject<>(param10), 0, param2, param5, param0, param11);

        while(var0.placing.hasNext()) {
            JigsawPlacement.PieceState var1 = var0.placing.next();
            var0.tryPlacingChildren(var1.piece, var1.free, var1.depth, param2, param5, param0, param11);
        }

    }

    public static boolean generateJigsaw(
        ServerLevel param0, Holder<StructureTemplatePool> param1, ResourceLocation param2, int param3, BlockPos param4, boolean param5
    ) {
        ChunkGenerator var0 = param0.getChunkSource().getGenerator();
        StructureTemplateManager var1 = param0.getStructureManager();
        StructureManager var2 = param0.structureManager();
        RandomSource var3 = param0.getRandom();
        Structure.GenerationContext var4 = new Structure.GenerationContext(
            param0.registryAccess(),
            var0,
            var0.getBiomeSource(),
            param0.getChunkSource().randomState(),
            var1,
            param0.getSeed(),
            new ChunkPos(param4),
            param0,
            param0x -> true
        );
        Optional<Structure.GenerationStub> var5 = addPieces(
            var4, param1, Optional.of(param2), param3, param4, false, Optional.empty(), 128, PoolAliasLookup.EMPTY
        );
        if (var5.isPresent()) {
            StructurePiecesBuilder var6 = var5.get().getPiecesBuilder();

            for(StructurePiece var7 : var6.build().pieces()) {
                if (var7 instanceof PoolElementStructurePiece var8) {
                    var8.place(param0, var2, var0, var3, BoundingBox.infinite(), param4, param5);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    static record PieceState(PoolElementStructurePiece piece, MutableObject<VoxelShape> free, int depth) {
    }

    static final class Placer {
        private final Registry<StructureTemplatePool> pools;
        private final int maxDepth;
        private final ChunkGenerator chunkGenerator;
        private final StructureTemplateManager structureTemplateManager;
        private final List<? super PoolElementStructurePiece> pieces;
        private final RandomSource random;
        final SequencedPriorityIterator<JigsawPlacement.PieceState> placing = new SequencedPriorityIterator<>();

        Placer(
            Registry<StructureTemplatePool> param0,
            int param1,
            ChunkGenerator param2,
            StructureTemplateManager param3,
            List<? super PoolElementStructurePiece> param4,
            RandomSource param5
        ) {
            this.pools = param0;
            this.maxDepth = param1;
            this.chunkGenerator = param2;
            this.structureTemplateManager = param3;
            this.pieces = param4;
            this.random = param5;
        }

        void tryPlacingChildren(
            PoolElementStructurePiece param0,
            MutableObject<VoxelShape> param1,
            int param2,
            boolean param3,
            LevelHeightAccessor param4,
            RandomState param5,
            PoolAliasLookup param6
        ) {
            StructurePoolElement var0 = param0.getElement();
            BlockPos var1 = param0.getPosition();
            Rotation var2 = param0.getRotation();
            StructureTemplatePool.Projection var3 = var0.getProjection();
            boolean var4 = var3 == StructureTemplatePool.Projection.RIGID;
            MutableObject<VoxelShape> var5 = new MutableObject<>();
            BoundingBox var6 = param0.getBoundingBox();
            int var7 = var6.minY();

            label136:
            for(StructureTemplate.StructureBlockInfo var8 : var0.getShuffledJigsawBlocks(this.structureTemplateManager, var1, var2, this.random)) {
                Direction var9 = JigsawBlock.getFrontFacing(var8.state());
                BlockPos var10 = var8.pos();
                BlockPos var11 = var10.relative(var9);
                int var12 = var10.getY() - var7;
                int var13 = -1;
                ResourceKey<StructureTemplatePool> var14 = readPoolKey(var8, param6);
                Optional<? extends Holder<StructureTemplatePool>> var15 = this.pools.getHolder(var14);
                if (var15.isEmpty()) {
                    JigsawPlacement.LOGGER.warn("Empty or non-existent pool: {}", var14.location());
                } else {
                    Holder<StructureTemplatePool> var16 = var15.get();
                    if (var16.value().size() == 0 && !var16.is(Pools.EMPTY)) {
                        JigsawPlacement.LOGGER.warn("Empty or non-existent pool: {}", var14.location());
                    } else {
                        Holder<StructureTemplatePool> var17 = var16.value().getFallback();
                        if (var17.value().size() == 0 && !var17.is(Pools.EMPTY)) {
                            JigsawPlacement.LOGGER
                                .warn(
                                    "Empty or non-existent fallback pool: {}",
                                    var17.unwrapKey().map(param0x -> param0x.location().toString()).orElse("<unregistered>")
                                );
                        } else {
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
                                var21.addAll(var16.value().getShuffledTemplates(this.random));
                            }

                            var21.addAll(var17.value().getShuffledTemplates(this.random));
                            int var22 = var8.nbt() != null ? var8.nbt().getInt("placement_priority") : 0;

                            for(StructurePoolElement var23 : var21) {
                                if (var23 == EmptyPoolElement.INSTANCE) {
                                    break;
                                }

                                for(Rotation var24 : Rotation.getShuffled(this.random)) {
                                    List<StructureTemplate.StructureBlockInfo> var25 = var23.getShuffledJigsawBlocks(
                                        this.structureTemplateManager, BlockPos.ZERO, var24, this.random
                                    );
                                    BoundingBox var26 = var23.getBoundingBox(this.structureTemplateManager, BlockPos.ZERO, var24);
                                    int var28;
                                    if (param3 && var26.getYSpan() <= 16) {
                                        var28 = var25.stream().mapToInt(param2x -> {
                                            if (!var26.isInside(param2x.pos().relative(JigsawBlock.getFrontFacing(param2x.state())))) {
                                                return 0;
                                            } else {
                                                ResourceKey<StructureTemplatePool> var0x = readPoolKey(param2x, param6);
                                                Optional<? extends Holder<StructureTemplatePool>> var1x = this.pools.getHolder(var0x);
                                                Optional<Holder<StructureTemplatePool>> var2x = var1x.map(param0x -> param0x.value().getFallback());
                                                int var3x = var1x.<Integer>map(param0x -> param0x.value().getMaxSize(this.structureTemplateManager)).orElse(0);
                                                int var4x = var2x.<Integer>map(param0x -> param0x.value().getMaxSize(this.structureTemplateManager)).orElse(0);
                                                return Math.max(var3x, var4x);
                                            }
                                        }).max().orElse(0);
                                    } else {
                                        var28 = 0;
                                    }

                                    for(StructureTemplate.StructureBlockInfo var29 : var25) {
                                        if (JigsawBlock.canAttach(var8, var29)) {
                                            BlockPos var30 = var29.pos();
                                            BlockPos var31 = var11.subtract(var30);
                                            BoundingBox var32 = var23.getBoundingBox(this.structureTemplateManager, var31, var24);
                                            int var33 = var32.minY();
                                            StructureTemplatePool.Projection var34 = var23.getProjection();
                                            boolean var35 = var34 == StructureTemplatePool.Projection.RIGID;
                                            int var36 = var30.getY();
                                            int var37 = var12 - var36 + JigsawBlock.getFrontFacing(var8.state()).getStepY();
                                            int var38;
                                            if (var4 && var35) {
                                                var38 = var7 + var37;
                                            } else {
                                                if (var13 == -1) {
                                                    var13 = this.chunkGenerator
                                                        .getFirstFreeHeight(var10.getX(), var10.getZ(), Heightmap.Types.WORLD_SURFACE_WG, param4, param5);
                                                }

                                                var38 = var13 - var36;
                                            }

                                            int var40 = var38 - var33;
                                            BoundingBox var41 = var32.moved(0, var40, 0);
                                            BlockPos var42 = var31.offset(0, var40, 0);
                                            if (var28 > 0) {
                                                int var43 = Math.max(var28 + 1, var41.maxY() - var41.minY());
                                                var41.encapsulate(new BlockPos(var41.minX(), var41.minY() + var43, var41.minZ()));
                                            }

                                            if (!Shapes.joinIsNotEmpty(var19.getValue(), Shapes.create(AABB.of(var41).deflate(0.25)), BooleanOp.ONLY_SECOND)) {
                                                var19.setValue(Shapes.joinUnoptimized(var19.getValue(), Shapes.create(AABB.of(var41)), BooleanOp.ONLY_FIRST));
                                                int var44 = param0.getGroundLevelDelta();
                                                int var45;
                                                if (var35) {
                                                    var45 = var44 - var37;
                                                } else {
                                                    var45 = var23.getGroundLevelDelta();
                                                }

                                                PoolElementStructurePiece var47 = new PoolElementStructurePiece(
                                                    this.structureTemplateManager, var23, var42, var45, var24, var41
                                                );
                                                int var48;
                                                if (var4) {
                                                    var48 = var7 + var12;
                                                } else if (var35) {
                                                    var48 = var38 + var36;
                                                } else {
                                                    if (var13 == -1) {
                                                        var13 = this.chunkGenerator
                                                            .getFirstFreeHeight(var10.getX(), var10.getZ(), Heightmap.Types.WORLD_SURFACE_WG, param4, param5);
                                                    }

                                                    var48 = var13 + var37 / 2;
                                                }

                                                param0.addJunction(new JigsawJunction(var11.getX(), var48 - var12 + var44, var11.getZ(), var37, var34));
                                                var47.addJunction(new JigsawJunction(var10.getX(), var48 - var36 + var45, var10.getZ(), -var37, var3));
                                                this.pieces.add(var47);
                                                if (param2 + 1 <= this.maxDepth) {
                                                    JigsawPlacement.PieceState var51 = new JigsawPlacement.PieceState(var47, var19, param2 + 1);
                                                    this.placing.add(var51, var22);
                                                }
                                                continue label136;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        private static ResourceKey<StructureTemplatePool> readPoolKey(StructureTemplate.StructureBlockInfo param0, PoolAliasLookup param1) {
            CompoundTag var0 = Objects.requireNonNull(param0.nbt(), () -> param0 + " nbt was null");
            ResourceKey<StructureTemplatePool> var1 = Pools.createKey(var0.getString("pool"));
            return param1.lookup(var1);
        }
    }
}
