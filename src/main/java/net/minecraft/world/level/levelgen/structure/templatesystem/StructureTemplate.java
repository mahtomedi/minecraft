package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class StructureTemplate {
    public static final String PALETTE_TAG = "palette";
    public static final String PALETTE_LIST_TAG = "palettes";
    public static final String ENTITIES_TAG = "entities";
    public static final String BLOCKS_TAG = "blocks";
    public static final String BLOCK_TAG_POS = "pos";
    public static final String BLOCK_TAG_STATE = "state";
    public static final String BLOCK_TAG_NBT = "nbt";
    public static final String ENTITY_TAG_POS = "pos";
    public static final String ENTITY_TAG_BLOCKPOS = "blockPos";
    public static final String ENTITY_TAG_NBT = "nbt";
    public static final String SIZE_TAG = "size";
    private final List<StructureTemplate.Palette> palettes = Lists.newArrayList();
    private final List<StructureTemplate.StructureEntityInfo> entityInfoList = Lists.newArrayList();
    private Vec3i size = Vec3i.ZERO;
    private String author = "?";

    public Vec3i getSize() {
        return this.size;
    }

    public void setAuthor(String param0) {
        this.author = param0;
    }

    public String getAuthor() {
        return this.author;
    }

    public void fillFromWorld(Level param0, BlockPos param1, Vec3i param2, boolean param3, @Nullable Block param4) {
        if (param2.getX() >= 1 && param2.getY() >= 1 && param2.getZ() >= 1) {
            BlockPos var0 = param1.offset(param2).offset(-1, -1, -1);
            List<StructureTemplate.StructureBlockInfo> var1 = Lists.newArrayList();
            List<StructureTemplate.StructureBlockInfo> var2 = Lists.newArrayList();
            List<StructureTemplate.StructureBlockInfo> var3 = Lists.newArrayList();
            BlockPos var4 = new BlockPos(Math.min(param1.getX(), var0.getX()), Math.min(param1.getY(), var0.getY()), Math.min(param1.getZ(), var0.getZ()));
            BlockPos var5 = new BlockPos(Math.max(param1.getX(), var0.getX()), Math.max(param1.getY(), var0.getY()), Math.max(param1.getZ(), var0.getZ()));
            this.size = param2;

            for(BlockPos var6 : BlockPos.betweenClosed(var4, var5)) {
                BlockPos var7 = var6.subtract(var4);
                BlockState var8 = param0.getBlockState(var6);
                if (param4 == null || !var8.is(param4)) {
                    BlockEntity var9 = param0.getBlockEntity(var6);
                    StructureTemplate.StructureBlockInfo var10;
                    if (var9 != null) {
                        var10 = new StructureTemplate.StructureBlockInfo(var7, var8, var9.saveWithId());
                    } else {
                        var10 = new StructureTemplate.StructureBlockInfo(var7, var8, null);
                    }

                    addToLists(var10, var1, var2, var3);
                }
            }

            List<StructureTemplate.StructureBlockInfo> var12 = buildInfoList(var1, var2, var3);
            this.palettes.clear();
            this.palettes.add(new StructureTemplate.Palette(var12));
            if (param3) {
                this.fillEntityList(param0, var4, var5);
            } else {
                this.entityInfoList.clear();
            }

        }
    }

    private static void addToLists(
        StructureTemplate.StructureBlockInfo param0,
        List<StructureTemplate.StructureBlockInfo> param1,
        List<StructureTemplate.StructureBlockInfo> param2,
        List<StructureTemplate.StructureBlockInfo> param3
    ) {
        if (param0.nbt != null) {
            param2.add(param0);
        } else if (!param0.state.getBlock().hasDynamicShape() && param0.state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
            param1.add(param0);
        } else {
            param3.add(param0);
        }

    }

    private static List<StructureTemplate.StructureBlockInfo> buildInfoList(
        List<StructureTemplate.StructureBlockInfo> param0, List<StructureTemplate.StructureBlockInfo> param1, List<StructureTemplate.StructureBlockInfo> param2
    ) {
        Comparator<StructureTemplate.StructureBlockInfo> var0 = Comparator.<StructureTemplate.StructureBlockInfo>comparingInt(param0x -> param0x.pos.getY())
            .thenComparingInt(param0x -> param0x.pos.getX())
            .thenComparingInt(param0x -> param0x.pos.getZ());
        param0.sort(var0);
        param2.sort(var0);
        param1.sort(var0);
        List<StructureTemplate.StructureBlockInfo> var1 = Lists.newArrayList();
        var1.addAll(param0);
        var1.addAll(param2);
        var1.addAll(param1);
        return var1;
    }

    private void fillEntityList(Level param0, BlockPos param1, BlockPos param2) {
        List<Entity> var0 = param0.getEntitiesOfClass(Entity.class, AABB.encapsulatingFullBlocks(param1, param2), param0x -> !(param0x instanceof Player));
        this.entityInfoList.clear();

        for(Entity var1 : var0) {
            Vec3 var2 = new Vec3(var1.getX() - (double)param1.getX(), var1.getY() - (double)param1.getY(), var1.getZ() - (double)param1.getZ());
            CompoundTag var3 = new CompoundTag();
            var1.save(var3);
            BlockPos var4;
            if (var1 instanceof Painting) {
                var4 = ((Painting)var1).getPos().subtract(param1);
            } else {
                var4 = BlockPos.containing(var2);
            }

            this.entityInfoList.add(new StructureTemplate.StructureEntityInfo(var2, var4, var3.copy()));
        }

    }

    public List<StructureTemplate.StructureBlockInfo> filterBlocks(BlockPos param0, StructurePlaceSettings param1, Block param2) {
        return this.filterBlocks(param0, param1, param2, true);
    }

    public ObjectArrayList<StructureTemplate.StructureBlockInfo> filterBlocks(BlockPos param0, StructurePlaceSettings param1, Block param2, boolean param3) {
        ObjectArrayList<StructureTemplate.StructureBlockInfo> var0 = new ObjectArrayList<>();
        BoundingBox var1 = param1.getBoundingBox();
        if (this.palettes.isEmpty()) {
            return var0;
        } else {
            for(StructureTemplate.StructureBlockInfo var2 : param1.getRandomPalette(this.palettes, param0).blocks(param2)) {
                BlockPos var3 = param3 ? calculateRelativePosition(param1, var2.pos).offset(param0) : var2.pos;
                if (var1 == null || var1.isInside(var3)) {
                    var0.add(new StructureTemplate.StructureBlockInfo(var3, var2.state.rotate(param1.getRotation()), var2.nbt));
                }
            }

            return var0;
        }
    }

    public BlockPos calculateConnectedPosition(StructurePlaceSettings param0, BlockPos param1, StructurePlaceSettings param2, BlockPos param3) {
        BlockPos var0 = calculateRelativePosition(param0, param1);
        BlockPos var1 = calculateRelativePosition(param2, param3);
        return var0.subtract(var1);
    }

    public static BlockPos calculateRelativePosition(StructurePlaceSettings param0, BlockPos param1) {
        return transform(param1, param0.getMirror(), param0.getRotation(), param0.getRotationPivot());
    }

    public boolean placeInWorld(ServerLevelAccessor param0, BlockPos param1, BlockPos param2, StructurePlaceSettings param3, RandomSource param4, int param5) {
        if (this.palettes.isEmpty()) {
            return false;
        } else {
            List<StructureTemplate.StructureBlockInfo> var0 = param3.getRandomPalette(this.palettes, param1).blocks();
            if ((!var0.isEmpty() || !param3.isIgnoreEntities() && !this.entityInfoList.isEmpty())
                && this.size.getX() >= 1
                && this.size.getY() >= 1
                && this.size.getZ() >= 1) {
                BoundingBox var1 = param3.getBoundingBox();
                List<BlockPos> var2 = Lists.newArrayListWithCapacity(param3.shouldKeepLiquids() ? var0.size() : 0);
                List<BlockPos> var3 = Lists.newArrayListWithCapacity(param3.shouldKeepLiquids() ? var0.size() : 0);
                List<Pair<BlockPos, CompoundTag>> var4 = Lists.newArrayListWithCapacity(var0.size());
                int var5 = Integer.MAX_VALUE;
                int var6 = Integer.MAX_VALUE;
                int var7 = Integer.MAX_VALUE;
                int var8 = Integer.MIN_VALUE;
                int var9 = Integer.MIN_VALUE;
                int var10 = Integer.MIN_VALUE;

                for(StructureTemplate.StructureBlockInfo var12 : processBlockInfos(param0, param1, param2, param3, var0)) {
                    BlockPos var13 = var12.pos;
                    if (var1 == null || var1.isInside(var13)) {
                        FluidState var14 = param3.shouldKeepLiquids() ? param0.getFluidState(var13) : null;
                        BlockState var15 = var12.state.mirror(param3.getMirror()).rotate(param3.getRotation());
                        if (var12.nbt != null) {
                            BlockEntity var16 = param0.getBlockEntity(var13);
                            Clearable.tryClear(var16);
                            param0.setBlock(var13, Blocks.BARRIER.defaultBlockState(), 20);
                        }

                        if (param0.setBlock(var13, var15, param5)) {
                            var5 = Math.min(var5, var13.getX());
                            var6 = Math.min(var6, var13.getY());
                            var7 = Math.min(var7, var13.getZ());
                            var8 = Math.max(var8, var13.getX());
                            var9 = Math.max(var9, var13.getY());
                            var10 = Math.max(var10, var13.getZ());
                            var4.add(Pair.of(var13, var12.nbt));
                            if (var12.nbt != null) {
                                BlockEntity var17 = param0.getBlockEntity(var13);
                                if (var17 != null) {
                                    if (var17 instanceof RandomizableContainer) {
                                        var12.nbt.putLong("LootTableSeed", param4.nextLong());
                                    }

                                    var17.load(var12.nbt);
                                }
                            }

                            if (var14 != null) {
                                if (var15.getFluidState().isSource()) {
                                    var3.add(var13);
                                } else if (var15.getBlock() instanceof LiquidBlockContainer) {
                                    ((LiquidBlockContainer)var15.getBlock()).placeLiquid(param0, var13, var15, var14);
                                    if (!var14.isSource()) {
                                        var2.add(var13);
                                    }
                                }
                            }
                        }
                    }
                }

                boolean var18 = true;
                Direction[] var19 = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

                while(var18 && !var2.isEmpty()) {
                    var18 = false;
                    Iterator<BlockPos> var20 = var2.iterator();

                    while(var20.hasNext()) {
                        BlockPos var21 = var20.next();
                        FluidState var22 = param0.getFluidState(var21);

                        for(int var23 = 0; var23 < var19.length && !var22.isSource(); ++var23) {
                            BlockPos var24 = var21.relative(var19[var23]);
                            FluidState var25 = param0.getFluidState(var24);
                            if (var25.isSource() && !var3.contains(var24)) {
                                var22 = var25;
                            }
                        }

                        if (var22.isSource()) {
                            BlockState var26 = param0.getBlockState(var21);
                            Block var27 = var26.getBlock();
                            if (var27 instanceof LiquidBlockContainer) {
                                ((LiquidBlockContainer)var27).placeLiquid(param0, var21, var26, var22);
                                var18 = true;
                                var20.remove();
                            }
                        }
                    }
                }

                if (var5 <= var8) {
                    if (!param3.getKnownShape()) {
                        DiscreteVoxelShape var28 = new BitSetDiscreteVoxelShape(var8 - var5 + 1, var9 - var6 + 1, var10 - var7 + 1);
                        int var29 = var5;
                        int var30 = var6;
                        int var31 = var7;

                        for(Pair<BlockPos, CompoundTag> var32 : var4) {
                            BlockPos var33 = var32.getFirst();
                            var28.fill(var33.getX() - var29, var33.getY() - var30, var33.getZ() - var31);
                        }

                        updateShapeAtEdge(param0, param5, var28, var29, var30, var31);
                    }

                    for(Pair<BlockPos, CompoundTag> var34 : var4) {
                        BlockPos var35 = var34.getFirst();
                        if (!param3.getKnownShape()) {
                            BlockState var36 = param0.getBlockState(var35);
                            BlockState var37 = Block.updateFromNeighbourShapes(var36, param0, var35);
                            if (var36 != var37) {
                                param0.setBlock(var35, var37, param5 & -2 | 16);
                            }

                            param0.blockUpdated(var35, var37.getBlock());
                        }

                        if (var34.getSecond() != null) {
                            BlockEntity var38 = param0.getBlockEntity(var35);
                            if (var38 != null) {
                                var38.setChanged();
                            }
                        }
                    }
                }

                if (!param3.isIgnoreEntities()) {
                    this.placeEntities(
                        param0, param1, param3.getMirror(), param3.getRotation(), param3.getRotationPivot(), var1, param3.shouldFinalizeEntities()
                    );
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public static void updateShapeAtEdge(LevelAccessor param0, int param1, DiscreteVoxelShape param2, int param3, int param4, int param5) {
        param2.forAllFaces((param5x, param6, param7, param8) -> {
            BlockPos var0x = new BlockPos(param3 + param6, param4 + param7, param5 + param8);
            BlockPos var1x = var0x.relative(param5x);
            BlockState var2x = param0.getBlockState(var0x);
            BlockState var3x = param0.getBlockState(var1x);
            BlockState var4x = var2x.updateShape(param5x, var3x, param0, var0x, var1x);
            if (var2x != var4x) {
                param0.setBlock(var0x, var4x, param1 & -2);
            }

            BlockState var5x = var3x.updateShape(param5x.getOpposite(), var4x, param0, var1x, var0x);
            if (var3x != var5x) {
                param0.setBlock(var1x, var5x, param1 & -2);
            }

        });
    }

    public static List<StructureTemplate.StructureBlockInfo> processBlockInfos(
        ServerLevelAccessor param0, BlockPos param1, BlockPos param2, StructurePlaceSettings param3, List<StructureTemplate.StructureBlockInfo> param4
    ) {
        List<StructureTemplate.StructureBlockInfo> var0 = new ArrayList<>();
        List<StructureTemplate.StructureBlockInfo> var1 = new ArrayList<>();

        for(StructureTemplate.StructureBlockInfo var2 : param4) {
            BlockPos var3 = calculateRelativePosition(param3, var2.pos).offset(param1);
            StructureTemplate.StructureBlockInfo var4 = new StructureTemplate.StructureBlockInfo(var3, var2.state, var2.nbt != null ? var2.nbt.copy() : null);
            Iterator<StructureProcessor> var5 = param3.getProcessors().iterator();

            while(var4 != null && var5.hasNext()) {
                var4 = var5.next().processBlock(param0, param1, param2, var2, var4, param3);
            }

            if (var4 != null) {
                var1.add(var4);
                var0.add(var2);
            }
        }

        for(StructureProcessor var6 : param3.getProcessors()) {
            var1 = var6.finalizeProcessing(param0, param1, param2, var0, var1, param3);
        }

        return var1;
    }

    private void placeEntities(
        ServerLevelAccessor param0, BlockPos param1, Mirror param2, Rotation param3, BlockPos param4, @Nullable BoundingBox param5, boolean param6
    ) {
        for(StructureTemplate.StructureEntityInfo var0 : this.entityInfoList) {
            BlockPos var1 = transform(var0.blockPos, param2, param3, param4).offset(param1);
            if (param5 == null || param5.isInside(var1)) {
                CompoundTag var2 = var0.nbt.copy();
                Vec3 var3 = transform(var0.pos, param2, param3, param4);
                Vec3 var4 = var3.add((double)param1.getX(), (double)param1.getY(), (double)param1.getZ());
                ListTag var5 = new ListTag();
                var5.add(DoubleTag.valueOf(var4.x));
                var5.add(DoubleTag.valueOf(var4.y));
                var5.add(DoubleTag.valueOf(var4.z));
                var2.put("Pos", var5);
                var2.remove("UUID");
                createEntityIgnoreException(param0, var2).ifPresent(param6x -> {
                    float var0x = param6x.rotate(param3);
                    var0x += param6x.mirror(param2) - param6x.getYRot();
                    param6x.moveTo(var4.x, var4.y, var4.z, var0x, param6x.getXRot());
                    if (param6 && param6x instanceof Mob) {
                        ((Mob)param6x).finalizeSpawn(param0, param0.getCurrentDifficultyAt(BlockPos.containing(var4)), MobSpawnType.STRUCTURE, null, var2);
                    }

                    param0.addFreshEntityWithPassengers(param6x);
                });
            }
        }

    }

    private static Optional<Entity> createEntityIgnoreException(ServerLevelAccessor param0, CompoundTag param1) {
        try {
            return EntityType.create(param1, param0.getLevel());
        } catch (Exception var3) {
            return Optional.empty();
        }
    }

    public Vec3i getSize(Rotation param0) {
        switch(param0) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                return new Vec3i(this.size.getZ(), this.size.getY(), this.size.getX());
            default:
                return this.size;
        }
    }

    public static BlockPos transform(BlockPos param0, Mirror param1, Rotation param2, BlockPos param3) {
        int var0 = param0.getX();
        int var1 = param0.getY();
        int var2 = param0.getZ();
        boolean var3 = true;
        switch(param1) {
            case LEFT_RIGHT:
                var2 = -var2;
                break;
            case FRONT_BACK:
                var0 = -var0;
                break;
            default:
                var3 = false;
        }

        int var4 = param3.getX();
        int var5 = param3.getZ();
        switch(param2) {
            case COUNTERCLOCKWISE_90:
                return new BlockPos(var4 - var5 + var2, var1, var4 + var5 - var0);
            case CLOCKWISE_90:
                return new BlockPos(var4 + var5 - var2, var1, var5 - var4 + var0);
            case CLOCKWISE_180:
                return new BlockPos(var4 + var4 - var0, var1, var5 + var5 - var2);
            default:
                return var3 ? new BlockPos(var0, var1, var2) : param0;
        }
    }

    public static Vec3 transform(Vec3 param0, Mirror param1, Rotation param2, BlockPos param3) {
        double var0 = param0.x;
        double var1 = param0.y;
        double var2 = param0.z;
        boolean var3 = true;
        switch(param1) {
            case LEFT_RIGHT:
                var2 = 1.0 - var2;
                break;
            case FRONT_BACK:
                var0 = 1.0 - var0;
                break;
            default:
                var3 = false;
        }

        int var4 = param3.getX();
        int var5 = param3.getZ();
        switch(param2) {
            case COUNTERCLOCKWISE_90:
                return new Vec3((double)(var4 - var5) + var2, var1, (double)(var4 + var5 + 1) - var0);
            case CLOCKWISE_90:
                return new Vec3((double)(var4 + var5 + 1) - var2, var1, (double)(var5 - var4) + var0);
            case CLOCKWISE_180:
                return new Vec3((double)(var4 + var4 + 1) - var0, var1, (double)(var5 + var5 + 1) - var2);
            default:
                return var3 ? new Vec3(var0, var1, var2) : param0;
        }
    }

    public BlockPos getZeroPositionWithTransform(BlockPos param0, Mirror param1, Rotation param2) {
        return getZeroPositionWithTransform(param0, param1, param2, this.getSize().getX(), this.getSize().getZ());
    }

    public static BlockPos getZeroPositionWithTransform(BlockPos param0, Mirror param1, Rotation param2, int param3, int param4) {
        --param3;
        --param4;
        int var0 = param1 == Mirror.FRONT_BACK ? param3 : 0;
        int var1 = param1 == Mirror.LEFT_RIGHT ? param4 : 0;
        BlockPos var2 = param0;
        switch(param2) {
            case COUNTERCLOCKWISE_90:
                var2 = param0.offset(var1, 0, param3 - var0);
                break;
            case CLOCKWISE_90:
                var2 = param0.offset(param4 - var1, 0, var0);
                break;
            case CLOCKWISE_180:
                var2 = param0.offset(param3 - var0, 0, param4 - var1);
                break;
            case NONE:
                var2 = param0.offset(var0, 0, var1);
        }

        return var2;
    }

    public BoundingBox getBoundingBox(StructurePlaceSettings param0, BlockPos param1) {
        return this.getBoundingBox(param1, param0.getRotation(), param0.getRotationPivot(), param0.getMirror());
    }

    public BoundingBox getBoundingBox(BlockPos param0, Rotation param1, BlockPos param2, Mirror param3) {
        return getBoundingBox(param0, param1, param2, param3, this.size);
    }

    @VisibleForTesting
    protected static BoundingBox getBoundingBox(BlockPos param0, Rotation param1, BlockPos param2, Mirror param3, Vec3i param4) {
        Vec3i var0 = param4.offset(-1, -1, -1);
        BlockPos var1 = transform(BlockPos.ZERO, param3, param1, param2);
        BlockPos var2 = transform(BlockPos.ZERO.offset(var0), param3, param1, param2);
        return BoundingBox.fromCorners(var1, var2).move(param0);
    }

    public CompoundTag save(CompoundTag param0) {
        if (this.palettes.isEmpty()) {
            param0.put("blocks", new ListTag());
            param0.put("palette", new ListTag());
        } else {
            List<StructureTemplate.SimplePalette> var0 = Lists.newArrayList();
            StructureTemplate.SimplePalette var1 = new StructureTemplate.SimplePalette();
            var0.add(var1);

            for(int var2 = 1; var2 < this.palettes.size(); ++var2) {
                var0.add(new StructureTemplate.SimplePalette());
            }

            ListTag var3 = new ListTag();
            List<StructureTemplate.StructureBlockInfo> var4 = this.palettes.get(0).blocks();

            for(int var5 = 0; var5 < var4.size(); ++var5) {
                StructureTemplate.StructureBlockInfo var6 = var4.get(var5);
                CompoundTag var7 = new CompoundTag();
                var7.put("pos", this.newIntegerList(var6.pos.getX(), var6.pos.getY(), var6.pos.getZ()));
                int var8 = var1.idFor(var6.state);
                var7.putInt("state", var8);
                if (var6.nbt != null) {
                    var7.put("nbt", var6.nbt);
                }

                var3.add(var7);

                for(int var9 = 1; var9 < this.palettes.size(); ++var9) {
                    StructureTemplate.SimplePalette var10 = var0.get(var9);
                    var10.addMapping(this.palettes.get(var9).blocks().get(var5).state, var8);
                }
            }

            param0.put("blocks", var3);
            if (var0.size() == 1) {
                ListTag var11 = new ListTag();

                for(BlockState var12 : var1) {
                    var11.add(NbtUtils.writeBlockState(var12));
                }

                param0.put("palette", var11);
            } else {
                ListTag var13 = new ListTag();

                for(StructureTemplate.SimplePalette var14 : var0) {
                    ListTag var15 = new ListTag();

                    for(BlockState var16 : var14) {
                        var15.add(NbtUtils.writeBlockState(var16));
                    }

                    var13.add(var15);
                }

                param0.put("palettes", var13);
            }
        }

        ListTag var17 = new ListTag();

        for(StructureTemplate.StructureEntityInfo var18 : this.entityInfoList) {
            CompoundTag var19 = new CompoundTag();
            var19.put("pos", this.newDoubleList(var18.pos.x, var18.pos.y, var18.pos.z));
            var19.put("blockPos", this.newIntegerList(var18.blockPos.getX(), var18.blockPos.getY(), var18.blockPos.getZ()));
            if (var18.nbt != null) {
                var19.put("nbt", var18.nbt);
            }

            var17.add(var19);
        }

        param0.put("entities", var17);
        param0.put("size", this.newIntegerList(this.size.getX(), this.size.getY(), this.size.getZ()));
        return NbtUtils.addCurrentDataVersion(param0);
    }

    public void load(HolderGetter<Block> param0, CompoundTag param1) {
        this.palettes.clear();
        this.entityInfoList.clear();
        ListTag var0 = param1.getList("size", 3);
        this.size = new Vec3i(var0.getInt(0), var0.getInt(1), var0.getInt(2));
        ListTag var1 = param1.getList("blocks", 10);
        if (param1.contains("palettes", 9)) {
            ListTag var2 = param1.getList("palettes", 9);

            for(int var3 = 0; var3 < var2.size(); ++var3) {
                this.loadPalette(param0, var2.getList(var3), var1);
            }
        } else {
            this.loadPalette(param0, param1.getList("palette", 10), var1);
        }

        ListTag var4 = param1.getList("entities", 10);

        for(int var5 = 0; var5 < var4.size(); ++var5) {
            CompoundTag var6 = var4.getCompound(var5);
            ListTag var7 = var6.getList("pos", 6);
            Vec3 var8 = new Vec3(var7.getDouble(0), var7.getDouble(1), var7.getDouble(2));
            ListTag var9 = var6.getList("blockPos", 3);
            BlockPos var10 = new BlockPos(var9.getInt(0), var9.getInt(1), var9.getInt(2));
            if (var6.contains("nbt")) {
                CompoundTag var11 = var6.getCompound("nbt");
                this.entityInfoList.add(new StructureTemplate.StructureEntityInfo(var8, var10, var11));
            }
        }

    }

    private void loadPalette(HolderGetter<Block> param0, ListTag param1, ListTag param2) {
        StructureTemplate.SimplePalette var0 = new StructureTemplate.SimplePalette();

        for(int var1 = 0; var1 < param1.size(); ++var1) {
            var0.addMapping(NbtUtils.readBlockState(param0, param1.getCompound(var1)), var1);
        }

        List<StructureTemplate.StructureBlockInfo> var2 = Lists.newArrayList();
        List<StructureTemplate.StructureBlockInfo> var3 = Lists.newArrayList();
        List<StructureTemplate.StructureBlockInfo> var4 = Lists.newArrayList();

        for(int var5 = 0; var5 < param2.size(); ++var5) {
            CompoundTag var6 = param2.getCompound(var5);
            ListTag var7 = var6.getList("pos", 3);
            BlockPos var8 = new BlockPos(var7.getInt(0), var7.getInt(1), var7.getInt(2));
            BlockState var9 = var0.stateFor(var6.getInt("state"));
            CompoundTag var10;
            if (var6.contains("nbt")) {
                var10 = var6.getCompound("nbt");
            } else {
                var10 = null;
            }

            StructureTemplate.StructureBlockInfo var12 = new StructureTemplate.StructureBlockInfo(var8, var9, var10);
            addToLists(var12, var2, var3, var4);
        }

        List<StructureTemplate.StructureBlockInfo> var13 = buildInfoList(var2, var3, var4);
        this.palettes.add(new StructureTemplate.Palette(var13));
    }

    private ListTag newIntegerList(int... param0) {
        ListTag var0 = new ListTag();

        for(int var1 : param0) {
            var0.add(IntTag.valueOf(var1));
        }

        return var0;
    }

    private ListTag newDoubleList(double... param0) {
        ListTag var0 = new ListTag();

        for(double var1 : param0) {
            var0.add(DoubleTag.valueOf(var1));
        }

        return var0;
    }

    public static final class Palette {
        private final List<StructureTemplate.StructureBlockInfo> blocks;
        private final Map<Block, List<StructureTemplate.StructureBlockInfo>> cache = Maps.newHashMap();

        Palette(List<StructureTemplate.StructureBlockInfo> param0) {
            this.blocks = param0;
        }

        public List<StructureTemplate.StructureBlockInfo> blocks() {
            return this.blocks;
        }

        public List<StructureTemplate.StructureBlockInfo> blocks(Block param0) {
            return this.cache.computeIfAbsent(param0, param0x -> this.blocks.stream().filter(param1 -> param1.state.is(param0x)).collect(Collectors.toList()));
        }
    }

    static class SimplePalette implements Iterable<BlockState> {
        public static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.defaultBlockState();
        private final IdMapper<BlockState> ids = new IdMapper<>(16);
        private int lastId;

        public int idFor(BlockState param0) {
            int var0 = this.ids.getId(param0);
            if (var0 == -1) {
                var0 = this.lastId++;
                this.ids.addMapping(param0, var0);
            }

            return var0;
        }

        @Nullable
        public BlockState stateFor(int param0) {
            BlockState var0 = this.ids.byId(param0);
            return var0 == null ? DEFAULT_BLOCK_STATE : var0;
        }

        @Override
        public Iterator<BlockState> iterator() {
            return this.ids.iterator();
        }

        public void addMapping(BlockState param0, int param1) {
            this.ids.addMapping(param0, param1);
        }
    }

    public static record StructureBlockInfo(BlockPos pos, BlockState state, @Nullable CompoundTag nbt) {
        @Override
        public String toString() {
            return String.format(Locale.ROOT, "<StructureBlockInfo | %s | %s | %s>", this.pos, this.state, this.nbt);
        }
    }

    public static class StructureEntityInfo {
        public final Vec3 pos;
        public final BlockPos blockPos;
        public final CompoundTag nbt;

        public StructureEntityInfo(Vec3 param0, BlockPos param1, CompoundTag param2) {
            this.pos = param0;
            this.blockPos = param1;
            this.nbt = param2;
        }
    }
}
