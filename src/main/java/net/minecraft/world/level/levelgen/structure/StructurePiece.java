package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableSet;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.NoiseEffect;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.material.FluidState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StructurePiece {
    private static final Logger LOGGER = LogManager.getLogger();
    protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
    protected BoundingBox boundingBox;
    @Nullable
    private Direction orientation;
    private Mirror mirror;
    private Rotation rotation;
    protected int genDepth;
    private final StructurePieceType type;
    private static final Set<Block> SHAPE_CHECK_BLOCKS = ImmutableSet.<Block>builder()
        .add(Blocks.NETHER_BRICK_FENCE)
        .add(Blocks.TORCH)
        .add(Blocks.WALL_TORCH)
        .add(Blocks.OAK_FENCE)
        .add(Blocks.SPRUCE_FENCE)
        .add(Blocks.DARK_OAK_FENCE)
        .add(Blocks.ACACIA_FENCE)
        .add(Blocks.BIRCH_FENCE)
        .add(Blocks.JUNGLE_FENCE)
        .add(Blocks.LADDER)
        .add(Blocks.IRON_BARS)
        .build();

    protected StructurePiece(StructurePieceType param0, int param1, BoundingBox param2) {
        this.type = param0;
        this.genDepth = param1;
        this.boundingBox = param2;
    }

    public StructurePiece(StructurePieceType param0, CompoundTag param1) {
        this(
            param0,
            param1.getInt("GD"),
            BoundingBox.CODEC
                .parse(NbtOps.INSTANCE, param1.get("BB"))
                .resultOrPartial(LOGGER::error)
                .orElseThrow(() -> new IllegalArgumentException("Invalid boundingbox"))
        );
        int var0 = param1.getInt("O");
        this.setOrientation(var0 == -1 ? null : Direction.from2DDataValue(var0));
    }

    protected static BoundingBox makeBoundingBox(int param0, int param1, int param2, Direction param3, int param4, int param5, int param6) {
        return param3.getAxis() == Direction.Axis.Z
            ? new BoundingBox(param0, param1, param2, param0 + param4 - 1, param1 + param5 - 1, param2 + param6 - 1)
            : new BoundingBox(param0, param1, param2, param0 + param6 - 1, param1 + param5 - 1, param2 + param4 - 1);
    }

    protected static Direction getRandomHorizontalDirection(Random param0) {
        return Direction.Plane.HORIZONTAL.getRandomDirection(param0);
    }

    public final CompoundTag createTag(ServerLevel param0) {
        CompoundTag var0 = new CompoundTag();
        var0.putString("id", Registry.STRUCTURE_PIECE.getKey(this.getType()).toString());
        BoundingBox.CODEC.encodeStart(NbtOps.INSTANCE, this.boundingBox).resultOrPartial(LOGGER::error).ifPresent(param1 -> var0.put("BB", param1));
        Direction var1 = this.getOrientation();
        var0.putInt("O", var1 == null ? -1 : var1.get2DDataValue());
        var0.putInt("GD", this.genDepth);
        this.addAdditionalSaveData(param0, var0);
        return var0;
    }

    protected abstract void addAdditionalSaveData(ServerLevel var1, CompoundTag var2);

    public NoiseEffect getNoiseEffect() {
        return NoiseEffect.BEARD;
    }

    public void addChildren(StructurePiece param0, StructurePieceAccessor param1, Random param2) {
    }

    public abstract boolean postProcess(
        WorldGenLevel var1, StructureFeatureManager var2, ChunkGenerator var3, Random var4, BoundingBox var5, ChunkPos var6, BlockPos var7
    );

    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public int getGenDepth() {
        return this.genDepth;
    }

    public boolean isCloseToChunk(ChunkPos param0, int param1) {
        int var0 = param0.getMinBlockX();
        int var1 = param0.getMinBlockZ();
        return this.boundingBox.intersects(var0 - param1, var1 - param1, var0 + 15 + param1, var1 + 15 + param1);
    }

    public BlockPos getLocatorPosition() {
        return new BlockPos(this.boundingBox.getCenter());
    }

    protected BlockPos.MutableBlockPos getWorldPos(int param0, int param1, int param2) {
        return new BlockPos.MutableBlockPos(this.getWorldX(param0, param2), this.getWorldY(param1), this.getWorldZ(param0, param2));
    }

    protected int getWorldX(int param0, int param1) {
        Direction var0 = this.getOrientation();
        if (var0 == null) {
            return param0;
        } else {
            switch(var0) {
                case NORTH:
                case SOUTH:
                    return this.boundingBox.minX() + param0;
                case WEST:
                    return this.boundingBox.maxX() - param1;
                case EAST:
                    return this.boundingBox.minX() + param1;
                default:
                    return param0;
            }
        }
    }

    protected int getWorldY(int param0) {
        return this.getOrientation() == null ? param0 : param0 + this.boundingBox.minY();
    }

    protected int getWorldZ(int param0, int param1) {
        Direction var0 = this.getOrientation();
        if (var0 == null) {
            return param1;
        } else {
            switch(var0) {
                case NORTH:
                    return this.boundingBox.maxZ() - param1;
                case SOUTH:
                    return this.boundingBox.minZ() + param1;
                case WEST:
                case EAST:
                    return this.boundingBox.minZ() + param0;
                default:
                    return param1;
            }
        }
    }

    protected void placeBlock(WorldGenLevel param0, BlockState param1, int param2, int param3, int param4, BoundingBox param5) {
        BlockPos var0 = this.getWorldPos(param2, param3, param4);
        if (param5.isInside(var0)) {
            if (this.canBeReplaced(param0, param2, param3, param4, param5)) {
                if (this.mirror != Mirror.NONE) {
                    param1 = param1.mirror(this.mirror);
                }

                if (this.rotation != Rotation.NONE) {
                    param1 = param1.rotate(this.rotation);
                }

                param0.setBlock(var0, param1, 2);
                FluidState var1 = param0.getFluidState(var0);
                if (!var1.isEmpty()) {
                    param0.getLiquidTicks().scheduleTick(var0, var1.getType(), 0);
                }

                if (SHAPE_CHECK_BLOCKS.contains(param1.getBlock())) {
                    param0.getChunk(var0).markPosForPostprocessing(var0);
                }

            }
        }
    }

    protected boolean canBeReplaced(LevelReader param0, int param1, int param2, int param3, BoundingBox param4) {
        return true;
    }

    protected BlockState getBlock(BlockGetter param0, int param1, int param2, int param3, BoundingBox param4) {
        BlockPos var0 = this.getWorldPos(param1, param2, param3);
        return !param4.isInside(var0) ? Blocks.AIR.defaultBlockState() : param0.getBlockState(var0);
    }

    protected boolean isInterior(LevelReader param0, int param1, int param2, int param3, BoundingBox param4) {
        BlockPos var0 = this.getWorldPos(param1, param2 + 1, param3);
        if (!param4.isInside(var0)) {
            return false;
        } else {
            return var0.getY() < param0.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, var0.getX(), var0.getZ());
        }
    }

    protected void generateAirBox(WorldGenLevel param0, BoundingBox param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        for(int var0 = param3; var0 <= param6; ++var0) {
            for(int var1 = param2; var1 <= param5; ++var1) {
                for(int var2 = param4; var2 <= param7; ++var2) {
                    this.placeBlock(param0, Blocks.AIR.defaultBlockState(), var1, var0, var2, param1);
                }
            }
        }

    }

    protected void generateBox(
        WorldGenLevel param0,
        BoundingBox param1,
        int param2,
        int param3,
        int param4,
        int param5,
        int param6,
        int param7,
        BlockState param8,
        BlockState param9,
        boolean param10
    ) {
        for(int var0 = param3; var0 <= param6; ++var0) {
            for(int var1 = param2; var1 <= param5; ++var1) {
                for(int var2 = param4; var2 <= param7; ++var2) {
                    if (!param10 || !this.getBlock(param0, var1, var0, var2, param1).isAir()) {
                        if (var0 != param3 && var0 != param6 && var1 != param2 && var1 != param5 && var2 != param4 && var2 != param7) {
                            this.placeBlock(param0, param9, var1, var0, var2, param1);
                        } else {
                            this.placeBlock(param0, param8, var1, var0, var2, param1);
                        }
                    }
                }
            }
        }

    }

    protected void generateBox(WorldGenLevel param0, BoundingBox param1, BoundingBox param2, BlockState param3, BlockState param4, boolean param5) {
        this.generateBox(param0, param1, param2.minX(), param2.minY(), param2.minZ(), param2.maxX(), param2.maxY(), param2.maxZ(), param3, param4, param5);
    }

    protected void generateBox(
        WorldGenLevel param0,
        BoundingBox param1,
        int param2,
        int param3,
        int param4,
        int param5,
        int param6,
        int param7,
        boolean param8,
        Random param9,
        StructurePiece.BlockSelector param10
    ) {
        for(int var0 = param3; var0 <= param6; ++var0) {
            for(int var1 = param2; var1 <= param5; ++var1) {
                for(int var2 = param4; var2 <= param7; ++var2) {
                    if (!param8 || !this.getBlock(param0, var1, var0, var2, param1).isAir()) {
                        param10.next(
                            param9, var1, var0, var2, var0 == param3 || var0 == param6 || var1 == param2 || var1 == param5 || var2 == param4 || var2 == param7
                        );
                        this.placeBlock(param0, param10.getNext(), var1, var0, var2, param1);
                    }
                }
            }
        }

    }

    protected void generateBox(WorldGenLevel param0, BoundingBox param1, BoundingBox param2, boolean param3, Random param4, StructurePiece.BlockSelector param5) {
        this.generateBox(param0, param1, param2.minX(), param2.minY(), param2.minZ(), param2.maxX(), param2.maxY(), param2.maxZ(), param3, param4, param5);
    }

    protected void generateMaybeBox(
        WorldGenLevel param0,
        BoundingBox param1,
        Random param2,
        float param3,
        int param4,
        int param5,
        int param6,
        int param7,
        int param8,
        int param9,
        BlockState param10,
        BlockState param11,
        boolean param12,
        boolean param13
    ) {
        for(int var0 = param5; var0 <= param8; ++var0) {
            for(int var1 = param4; var1 <= param7; ++var1) {
                for(int var2 = param6; var2 <= param9; ++var2) {
                    if (!(param2.nextFloat() > param3)
                        && (!param12 || !this.getBlock(param0, var1, var0, var2, param1).isAir())
                        && (!param13 || this.isInterior(param0, var1, var0, var2, param1))) {
                        if (var0 != param5 && var0 != param8 && var1 != param4 && var1 != param7 && var2 != param6 && var2 != param9) {
                            this.placeBlock(param0, param11, var1, var0, var2, param1);
                        } else {
                            this.placeBlock(param0, param10, var1, var0, var2, param1);
                        }
                    }
                }
            }
        }

    }

    protected void maybeGenerateBlock(
        WorldGenLevel param0, BoundingBox param1, Random param2, float param3, int param4, int param5, int param6, BlockState param7, boolean param8
    ) {
        if (param2.nextFloat() < param3) {
            if (!param8) {
                this.placeBlock(param0, param7, param4, param5, param6, param1);
                return;
            }

            Direction[] var0 = Direction.values();
            BlockPos.MutableBlockPos var1 = this.getWorldPos(param4, param5, param6).mutable();

            for(Direction var2 : var0) {
                var1.move(var2);
                if (param1.isInside(var1) && !param0.isEmptyBlock(var1)) {
                    this.placeBlock(param0, param7, param4, param5, param6, param1);
                    return;
                }

                var1.move(var2.getOpposite());
            }
        }

    }

    protected void generateUpperHalfSphere(
        WorldGenLevel param0, BoundingBox param1, int param2, int param3, int param4, int param5, int param6, int param7, BlockState param8, boolean param9
    ) {
        float var0 = (float)(param5 - param2 + 1);
        float var1 = (float)(param6 - param3 + 1);
        float var2 = (float)(param7 - param4 + 1);
        float var3 = (float)param2 + var0 / 2.0F;
        float var4 = (float)param4 + var2 / 2.0F;

        for(int var5 = param3; var5 <= param6; ++var5) {
            float var6 = (float)(var5 - param3) / var1;

            for(int var7 = param2; var7 <= param5; ++var7) {
                float var8 = ((float)var7 - var3) / (var0 * 0.5F);

                for(int var9 = param4; var9 <= param7; ++var9) {
                    float var10 = ((float)var9 - var4) / (var2 * 0.5F);
                    if (!param9 || !this.getBlock(param0, var7, var5, var9, param1).isAir()) {
                        float var11 = var8 * var8 + var6 * var6 + var10 * var10;
                        if (var11 <= 1.05F) {
                            this.placeBlock(param0, param8, var7, var5, var9, param1);
                        }
                    }
                }
            }
        }

    }

    protected void fillColumnDown(WorldGenLevel param0, BlockState param1, int param2, int param3, int param4, BoundingBox param5) {
        BlockPos.MutableBlockPos var0 = this.getWorldPos(param2, param3, param4);
        if (param5.isInside(var0)) {
            while(this.isReplaceableByStructures(param0.getBlockState(var0)) && var0.getY() > param0.getMinBuildHeight() + 1) {
                param0.setBlock(var0, param1, 2);
                var0.move(Direction.DOWN);
            }

        }
    }

    protected boolean isReplaceableByStructures(BlockState param0) {
        return param0.isAir()
            || param0.getMaterial().isLiquid()
            || param0.is(Blocks.GLOW_LICHEN)
            || param0.is(Blocks.SEAGRASS)
            || param0.is(Blocks.TALL_SEAGRASS);
    }

    protected boolean createChest(WorldGenLevel param0, BoundingBox param1, Random param2, int param3, int param4, int param5, ResourceLocation param6) {
        return this.createChest(param0, param1, param2, this.getWorldPos(param3, param4, param5), param6, null);
    }

    public static BlockState reorient(BlockGetter param0, BlockPos param1, BlockState param2) {
        Direction var0 = null;

        for(Direction var1 : Direction.Plane.HORIZONTAL) {
            BlockPos var2 = param1.relative(var1);
            BlockState var3 = param0.getBlockState(var2);
            if (var3.is(Blocks.CHEST)) {
                return param2;
            }

            if (var3.isSolidRender(param0, var2)) {
                if (var0 != null) {
                    var0 = null;
                    break;
                }

                var0 = var1;
            }
        }

        if (var0 != null) {
            return param2.setValue(HorizontalDirectionalBlock.FACING, var0.getOpposite());
        } else {
            Direction var4 = param2.getValue(HorizontalDirectionalBlock.FACING);
            BlockPos var5 = param1.relative(var4);
            if (param0.getBlockState(var5).isSolidRender(param0, var5)) {
                var4 = var4.getOpposite();
                var5 = param1.relative(var4);
            }

            if (param0.getBlockState(var5).isSolidRender(param0, var5)) {
                var4 = var4.getClockWise();
                var5 = param1.relative(var4);
            }

            if (param0.getBlockState(var5).isSolidRender(param0, var5)) {
                var4 = var4.getOpposite();
                var5 = param1.relative(var4);
            }

            return param2.setValue(HorizontalDirectionalBlock.FACING, var4);
        }
    }

    protected boolean createChest(
        ServerLevelAccessor param0, BoundingBox param1, Random param2, BlockPos param3, ResourceLocation param4, @Nullable BlockState param5
    ) {
        if (param1.isInside(param3) && !param0.getBlockState(param3).is(Blocks.CHEST)) {
            if (param5 == null) {
                param5 = reorient(param0, param3, Blocks.CHEST.defaultBlockState());
            }

            param0.setBlock(param3, param5, 2);
            BlockEntity var0 = param0.getBlockEntity(param3);
            if (var0 instanceof ChestBlockEntity) {
                ((ChestBlockEntity)var0).setLootTable(param4, param2.nextLong());
            }

            return true;
        } else {
            return false;
        }
    }

    protected boolean createDispenser(
        WorldGenLevel param0, BoundingBox param1, Random param2, int param3, int param4, int param5, Direction param6, ResourceLocation param7
    ) {
        BlockPos var0 = this.getWorldPos(param3, param4, param5);
        if (param1.isInside(var0) && !param0.getBlockState(var0).is(Blocks.DISPENSER)) {
            this.placeBlock(param0, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, param6), param3, param4, param5, param1);
            BlockEntity var1 = param0.getBlockEntity(var0);
            if (var1 instanceof DispenserBlockEntity) {
                ((DispenserBlockEntity)var1).setLootTable(param7, param2.nextLong());
            }

            return true;
        } else {
            return false;
        }
    }

    public void move(int param0, int param1, int param2) {
        this.boundingBox.move(param0, param1, param2);
    }

    @Nullable
    public Direction getOrientation() {
        return this.orientation;
    }

    public void setOrientation(@Nullable Direction param0) {
        this.orientation = param0;
        if (param0 == null) {
            this.rotation = Rotation.NONE;
            this.mirror = Mirror.NONE;
        } else {
            switch(param0) {
                case SOUTH:
                    this.mirror = Mirror.LEFT_RIGHT;
                    this.rotation = Rotation.NONE;
                    break;
                case WEST:
                    this.mirror = Mirror.LEFT_RIGHT;
                    this.rotation = Rotation.CLOCKWISE_90;
                    break;
                case EAST:
                    this.mirror = Mirror.NONE;
                    this.rotation = Rotation.CLOCKWISE_90;
                    break;
                default:
                    this.mirror = Mirror.NONE;
                    this.rotation = Rotation.NONE;
            }
        }

    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public Mirror getMirror() {
        return this.mirror;
    }

    public StructurePieceType getType() {
        return this.type;
    }

    public abstract static class BlockSelector {
        protected BlockState next = Blocks.AIR.defaultBlockState();

        protected BlockSelector() {
        }

        public abstract void next(Random var1, int var2, int var3, int var4, boolean var5);

        public BlockState getNext() {
            return this.next;
        }
    }
}
