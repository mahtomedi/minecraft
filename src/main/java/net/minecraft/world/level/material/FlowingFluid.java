package net.minecraft.world.level.material;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class FlowingFluid extends Fluid {
    public static final BooleanProperty FALLING = BlockStateProperties.FALLING;
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_FLOWING;
    private static final int CACHE_SIZE = 200;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> var0 = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(200) {
            @Override
            protected void rehash(int param0) {
            }
        };
        var0.defaultReturnValue((byte)127);
        return var0;
    });
    private final Map<FluidState, VoxelShape> shapes = Maps.newIdentityHashMap();

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> param0) {
        param0.add(FALLING);
    }

    @Override
    public Vec3 getFlow(BlockGetter param0, BlockPos param1, FluidState param2) {
        double var0 = 0.0;
        double var1 = 0.0;
        BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();

        for(Direction var3 : Direction.Plane.HORIZONTAL) {
            var2.setWithOffset(param1, var3);
            FluidState var4 = param0.getFluidState(var2);
            if (this.affectsFlow(var4)) {
                float var5 = var4.getOwnHeight();
                float var6 = 0.0F;
                if (var5 == 0.0F) {
                    if (!param0.getBlockState(var2).blocksMotion()) {
                        BlockPos var7 = var2.below();
                        FluidState var8 = param0.getFluidState(var7);
                        if (this.affectsFlow(var8)) {
                            var5 = var8.getOwnHeight();
                            if (var5 > 0.0F) {
                                var6 = param2.getOwnHeight() - (var5 - 0.8888889F);
                            }
                        }
                    }
                } else if (var5 > 0.0F) {
                    var6 = param2.getOwnHeight() - var5;
                }

                if (var6 != 0.0F) {
                    var0 += (double)((float)var3.getStepX() * var6);
                    var1 += (double)((float)var3.getStepZ() * var6);
                }
            }
        }

        Vec3 var9 = new Vec3(var0, 0.0, var1);
        if (param2.getValue(FALLING)) {
            for(Direction var10 : Direction.Plane.HORIZONTAL) {
                var2.setWithOffset(param1, var10);
                if (this.isSolidFace(param0, var2, var10) || this.isSolidFace(param0, var2.above(), var10)) {
                    var9 = var9.normalize().add(0.0, -6.0, 0.0);
                    break;
                }
            }
        }

        return var9.normalize();
    }

    private boolean affectsFlow(FluidState param0) {
        return param0.isEmpty() || param0.getType().isSame(this);
    }

    protected boolean isSolidFace(BlockGetter param0, BlockPos param1, Direction param2) {
        BlockState var0 = param0.getBlockState(param1);
        FluidState var1 = param0.getFluidState(param1);
        if (var1.getType().isSame(this)) {
            return false;
        } else if (param2 == Direction.UP) {
            return true;
        } else {
            return var0.getBlock() instanceof IceBlock ? false : var0.isFaceSturdy(param0, param1, param2);
        }
    }

    protected void spread(Level param0, BlockPos param1, FluidState param2) {
        if (!param2.isEmpty()) {
            BlockState var0 = param0.getBlockState(param1);
            BlockPos var1 = param1.below();
            BlockState var2 = param0.getBlockState(var1);
            FluidState var3 = this.getNewLiquid(param0, var1, var2);
            if (this.canSpreadTo(param0, param1, var0, Direction.DOWN, var1, var2, param0.getFluidState(var1), var3.getType())) {
                this.spreadTo(param0, var1, var2, Direction.DOWN, var3);
                if (this.sourceNeighborCount(param0, param1) >= 3) {
                    this.spreadToSides(param0, param1, param2, var0);
                }
            } else if (param2.isSource() || !this.isWaterHole(param0, var3.getType(), param1, var0, var1, var2)) {
                this.spreadToSides(param0, param1, param2, var0);
            }

        }
    }

    private void spreadToSides(Level param0, BlockPos param1, FluidState param2, BlockState param3) {
        int var0 = param2.getAmount() - this.getDropOff(param0);
        if (param2.getValue(FALLING)) {
            var0 = 7;
        }

        if (var0 > 0) {
            Map<Direction, FluidState> var1 = this.getSpread(param0, param1, param3);

            for(Entry<Direction, FluidState> var2 : var1.entrySet()) {
                Direction var3 = var2.getKey();
                FluidState var4 = var2.getValue();
                BlockPos var5 = param1.relative(var3);
                BlockState var6 = param0.getBlockState(var5);
                if (this.canSpreadTo(param0, param1, param3, var3, var5, var6, param0.getFluidState(var5), var4.getType())) {
                    this.spreadTo(param0, var5, var6, var3, var4);
                }
            }

        }
    }

    protected FluidState getNewLiquid(Level param0, BlockPos param1, BlockState param2) {
        int var0 = 0;
        int var1 = 0;

        for(Direction var2 : Direction.Plane.HORIZONTAL) {
            BlockPos var3 = param1.relative(var2);
            BlockState var4 = param0.getBlockState(var3);
            FluidState var5 = var4.getFluidState();
            if (var5.getType().isSame(this) && this.canPassThroughWall(var2, param0, param1, param2, var3, var4)) {
                if (var5.isSource()) {
                    ++var1;
                }

                var0 = Math.max(var0, var5.getAmount());
            }
        }

        if (this.canConvertToSource(param0) && var1 >= 2) {
            BlockState var6 = param0.getBlockState(param1.below());
            FluidState var7 = var6.getFluidState();
            if (var6.isSolid() || this.isSourceBlockOfThisType(var7)) {
                return this.getSource(false);
            }
        }

        BlockPos var8 = param1.above();
        BlockState var9 = param0.getBlockState(var8);
        FluidState var10 = var9.getFluidState();
        if (!var10.isEmpty() && var10.getType().isSame(this) && this.canPassThroughWall(Direction.UP, param0, param1, param2, var8, var9)) {
            return this.getFlowing(8, true);
        } else {
            int var11 = var0 - this.getDropOff(param0);
            return var11 <= 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing(var11, false);
        }
    }

    private boolean canPassThroughWall(Direction param0, BlockGetter param1, BlockPos param2, BlockState param3, BlockPos param4, BlockState param5) {
        Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> var1;
        if (!param3.getBlock().hasDynamicShape() && !param5.getBlock().hasDynamicShape()) {
            var1 = OCCLUSION_CACHE.get();
        } else {
            var1 = null;
        }

        Block.BlockStatePairKey var2;
        if (var1 != null) {
            var2 = new Block.BlockStatePairKey(param3, param5, param0);
            byte var3 = var1.getAndMoveToFirst(var2);
            if (var3 != 127) {
                return var3 != 0;
            }
        } else {
            var2 = null;
        }

        VoxelShape var5 = param3.getCollisionShape(param1, param2);
        VoxelShape var6 = param5.getCollisionShape(param1, param4);
        boolean var7 = !Shapes.mergedFaceOccludes(var5, var6, param0);
        if (var1 != null) {
            if (var1.size() == 200) {
                var1.removeLastByte();
            }

            var1.putAndMoveToFirst(var2, (byte)(var7 ? 1 : 0));
        }

        return var7;
    }

    public abstract Fluid getFlowing();

    public FluidState getFlowing(int param0, boolean param1) {
        return this.getFlowing().defaultFluidState().setValue(LEVEL, Integer.valueOf(param0)).setValue(FALLING, Boolean.valueOf(param1));
    }

    public abstract Fluid getSource();

    public FluidState getSource(boolean param0) {
        return this.getSource().defaultFluidState().setValue(FALLING, Boolean.valueOf(param0));
    }

    protected abstract boolean canConvertToSource(Level var1);

    protected void spreadTo(LevelAccessor param0, BlockPos param1, BlockState param2, Direction param3, FluidState param4) {
        if (param2.getBlock() instanceof LiquidBlockContainer) {
            ((LiquidBlockContainer)param2.getBlock()).placeLiquid(param0, param1, param2, param4);
        } else {
            if (!param2.isAir()) {
                this.beforeDestroyingBlock(param0, param1, param2);
            }

            param0.setBlock(param1, param4.createLegacyBlock(), 3);
        }

    }

    protected abstract void beforeDestroyingBlock(LevelAccessor var1, BlockPos var2, BlockState var3);

    private static short getCacheKey(BlockPos param0, BlockPos param1) {
        int var0 = param1.getX() - param0.getX();
        int var1 = param1.getZ() - param0.getZ();
        return (short)((var0 + 128 & 0xFF) << 8 | var1 + 128 & 0xFF);
    }

    protected int getSlopeDistance(
        LevelReader param0,
        BlockPos param1,
        int param2,
        Direction param3,
        BlockState param4,
        BlockPos param5,
        Short2ObjectMap<Pair<BlockState, FluidState>> param6,
        Short2BooleanMap param7
    ) {
        int var0 = 1000;

        for(Direction var1 : Direction.Plane.HORIZONTAL) {
            if (var1 != param3) {
                BlockPos var2 = param1.relative(var1);
                short var3 = getCacheKey(param5, var2);
                Pair<BlockState, FluidState> var4 = param6.computeIfAbsent(var3, param2x -> {
                    BlockState var0x = param0.getBlockState(var2);
                    return Pair.of(var0x, var0x.getFluidState());
                });
                BlockState var5 = var4.getFirst();
                FluidState var6 = var4.getSecond();
                if (this.canPassThrough(param0, this.getFlowing(), param1, param4, var1, var2, var5, var6)) {
                    boolean var7 = param7.computeIfAbsent(var3, param3x -> {
                        BlockPos var0x = var2.below();
                        BlockState var1x = param0.getBlockState(var0x);
                        return this.isWaterHole(param0, this.getFlowing(), var2, var5, var0x, var1x);
                    });
                    if (var7) {
                        return param2;
                    }

                    if (param2 < this.getSlopeFindDistance(param0)) {
                        int var8 = this.getSlopeDistance(param0, var2, param2 + 1, var1.getOpposite(), var5, param5, param6, param7);
                        if (var8 < var0) {
                            var0 = var8;
                        }
                    }
                }
            }
        }

        return var0;
    }

    private boolean isWaterHole(BlockGetter param0, Fluid param1, BlockPos param2, BlockState param3, BlockPos param4, BlockState param5) {
        if (!this.canPassThroughWall(Direction.DOWN, param0, param2, param3, param4, param5)) {
            return false;
        } else {
            return param5.getFluidState().getType().isSame(this) ? true : this.canHoldFluid(param0, param4, param5, param1);
        }
    }

    private boolean canPassThrough(
        BlockGetter param0, Fluid param1, BlockPos param2, BlockState param3, Direction param4, BlockPos param5, BlockState param6, FluidState param7
    ) {
        return !this.isSourceBlockOfThisType(param7)
            && this.canPassThroughWall(param4, param0, param2, param3, param5, param6)
            && this.canHoldFluid(param0, param5, param6, param1);
    }

    private boolean isSourceBlockOfThisType(FluidState param0) {
        return param0.getType().isSame(this) && param0.isSource();
    }

    protected abstract int getSlopeFindDistance(LevelReader var1);

    private int sourceNeighborCount(LevelReader param0, BlockPos param1) {
        int var0 = 0;

        for(Direction var1 : Direction.Plane.HORIZONTAL) {
            BlockPos var2 = param1.relative(var1);
            FluidState var3 = param0.getFluidState(var2);
            if (this.isSourceBlockOfThisType(var3)) {
                ++var0;
            }
        }

        return var0;
    }

    protected Map<Direction, FluidState> getSpread(Level param0, BlockPos param1, BlockState param2) {
        int var0 = 1000;
        Map<Direction, FluidState> var1 = Maps.newEnumMap(Direction.class);
        Short2ObjectMap<Pair<BlockState, FluidState>> var2 = new Short2ObjectOpenHashMap<>();
        Short2BooleanMap var3 = new Short2BooleanOpenHashMap();

        for(Direction var4 : Direction.Plane.HORIZONTAL) {
            BlockPos var5 = param1.relative(var4);
            short var6 = getCacheKey(param1, var5);
            Pair<BlockState, FluidState> var7 = var2.computeIfAbsent(var6, param2x -> {
                BlockState var0x = param0.getBlockState(var5);
                return Pair.of(var0x, var0x.getFluidState());
            });
            BlockState var8 = var7.getFirst();
            FluidState var9 = var7.getSecond();
            FluidState var10 = this.getNewLiquid(param0, var5, var8);
            if (this.canPassThrough(param0, var10.getType(), param1, param2, var4, var5, var8, var9)) {
                BlockPos var11 = var5.below();
                boolean var12 = var3.computeIfAbsent(var6, param4 -> {
                    BlockState var0x = param0.getBlockState(var11);
                    return this.isWaterHole(param0, this.getFlowing(), var5, var8, var11, var0x);
                });
                int var13;
                if (var12) {
                    var13 = 0;
                } else {
                    var13 = this.getSlopeDistance(param0, var5, 1, var4.getOpposite(), var8, param1, var2, var3);
                }

                if (var13 < var0) {
                    var1.clear();
                }

                if (var13 <= var0) {
                    var1.put(var4, var10);
                    var0 = var13;
                }
            }
        }

        return var1;
    }

    private boolean canHoldFluid(BlockGetter param0, BlockPos param1, BlockState param2, Fluid param3) {
        Block var0 = param2.getBlock();
        if (var0 instanceof LiquidBlockContainer) {
            return ((LiquidBlockContainer)var0).canPlaceLiquid(param0, param1, param2, param3);
        } else if (var0 instanceof DoorBlock
            || param2.is(BlockTags.SIGNS)
            || param2.is(Blocks.LADDER)
            || param2.is(Blocks.SUGAR_CANE)
            || param2.is(Blocks.BUBBLE_COLUMN)) {
            return false;
        } else if (!param2.is(Blocks.NETHER_PORTAL) && !param2.is(Blocks.END_PORTAL) && !param2.is(Blocks.END_GATEWAY) && !param2.is(Blocks.STRUCTURE_VOID)) {
            return !param2.blocksMotion();
        } else {
            return false;
        }
    }

    protected boolean canSpreadTo(
        BlockGetter param0, BlockPos param1, BlockState param2, Direction param3, BlockPos param4, BlockState param5, FluidState param6, Fluid param7
    ) {
        return param6.canBeReplacedWith(param0, param4, param7, param3)
            && this.canPassThroughWall(param3, param0, param1, param2, param4, param5)
            && this.canHoldFluid(param0, param4, param5, param7);
    }

    protected abstract int getDropOff(LevelReader var1);

    protected int getSpreadDelay(Level param0, BlockPos param1, FluidState param2, FluidState param3) {
        return this.getTickDelay(param0);
    }

    @Override
    public void tick(Level param0, BlockPos param1, FluidState param2) {
        if (!param2.isSource()) {
            FluidState var0 = this.getNewLiquid(param0, param1, param0.getBlockState(param1));
            int var1 = this.getSpreadDelay(param0, param1, param2, var0);
            if (var0.isEmpty()) {
                param2 = var0;
                param0.setBlock(param1, Blocks.AIR.defaultBlockState(), 3);
            } else if (!var0.equals(param2)) {
                param2 = var0;
                BlockState var2 = var0.createLegacyBlock();
                param0.setBlock(param1, var2, 2);
                param0.scheduleTick(param1, var0.getType(), var1);
                param0.updateNeighborsAt(param1, var2.getBlock());
            }
        }

        this.spread(param0, param1, param2);
    }

    protected static int getLegacyLevel(FluidState param0) {
        return param0.isSource() ? 0 : 8 - Math.min(param0.getAmount(), 8) + (param0.getValue(FALLING) ? 8 : 0);
    }

    private static boolean hasSameAbove(FluidState param0, BlockGetter param1, BlockPos param2) {
        return param0.getType().isSame(param1.getFluidState(param2.above()).getType());
    }

    @Override
    public float getHeight(FluidState param0, BlockGetter param1, BlockPos param2) {
        return hasSameAbove(param0, param1, param2) ? 1.0F : param0.getOwnHeight();
    }

    @Override
    public float getOwnHeight(FluidState param0) {
        return (float)param0.getAmount() / 9.0F;
    }

    @Override
    public abstract int getAmount(FluidState var1);

    @Override
    public VoxelShape getShape(FluidState param0, BlockGetter param1, BlockPos param2) {
        return param0.getAmount() == 9 && hasSameAbove(param0, param1, param2)
            ? Shapes.block()
            : this.shapes.computeIfAbsent(param0, param2x -> Shapes.box(0.0, 0.0, 0.0, 1.0, (double)param2x.getHeight(param1, param2), 1.0));
    }
}
