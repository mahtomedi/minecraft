package net.minecraft.world.level.block;

import com.google.common.cache.LoadingCache;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NetherPortalBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    public NetherPortalBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        switch((Direction.Axis)param0.getValue(AXIS)) {
            case Z:
                return Z_AXIS_AABB;
            case X:
            default:
                return X_AXIS_AABB;
        }
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (param1.dimensionType().natural()
            && param1.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
            && param3.nextInt(2000) < param1.getDifficulty().getId()) {
            while(param1.getBlockState(param2).is(this)) {
                param2 = param2.below();
            }

            if (param1.getBlockState(param2).isValidSpawn(param1, param2, EntityType.ZOMBIFIED_PIGLIN)) {
                Entity var0 = EntityType.ZOMBIFIED_PIGLIN.spawn(param1, null, null, null, param2.above(), MobSpawnType.STRUCTURE, false, false);
                if (var0 != null) {
                    var0.setPortalCooldown();
                }
            }
        }

    }

    public static boolean trySpawnPortal(LevelAccessor param0, BlockPos param1) {
        NetherPortalBlock.PortalShape var0 = isPortal(param0, param1);
        if (var0 != null) {
            var0.createPortalBlocks();
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    public static NetherPortalBlock.PortalShape isPortal(LevelAccessor param0, BlockPos param1) {
        NetherPortalBlock.PortalShape var0 = new NetherPortalBlock.PortalShape(param0, param1, Direction.Axis.X);
        if (var0.isValid() && var0.numPortalBlocks == 0) {
            return var0;
        } else {
            NetherPortalBlock.PortalShape var1 = new NetherPortalBlock.PortalShape(param0, param1, Direction.Axis.Z);
            return var1.isValid() && var1.numPortalBlocks == 0 ? var1 : null;
        }
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        Direction.Axis var0 = param1.getAxis();
        Direction.Axis var1 = param0.getValue(AXIS);
        boolean var2 = var1 != var0 && var0.isHorizontal();
        return !var2 && !param2.is(this) && !new NetherPortalBlock.PortalShape(param3, param4, var1).isComplete()
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (!param3.isPassenger() && !param3.isVehicle() && param3.canChangeDimensions()) {
            param3.handleInsidePortal(param2);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (param3.nextInt(100) == 0) {
            param1.playLocalSound(
                (double)param2.getX() + 0.5,
                (double)param2.getY() + 0.5,
                (double)param2.getZ() + 0.5,
                SoundEvents.PORTAL_AMBIENT,
                SoundSource.BLOCKS,
                0.5F,
                param3.nextFloat() * 0.4F + 0.8F,
                false
            );
        }

        for(int var0 = 0; var0 < 4; ++var0) {
            double var1 = (double)param2.getX() + param3.nextDouble();
            double var2 = (double)param2.getY() + param3.nextDouble();
            double var3 = (double)param2.getZ() + param3.nextDouble();
            double var4 = ((double)param3.nextFloat() - 0.5) * 0.5;
            double var5 = ((double)param3.nextFloat() - 0.5) * 0.5;
            double var6 = ((double)param3.nextFloat() - 0.5) * 0.5;
            int var7 = param3.nextInt(2) * 2 - 1;
            if (!param1.getBlockState(param2.west()).is(this) && !param1.getBlockState(param2.east()).is(this)) {
                var1 = (double)param2.getX() + 0.5 + 0.25 * (double)var7;
                var4 = (double)(param3.nextFloat() * 2.0F * (float)var7);
            } else {
                var3 = (double)param2.getZ() + 0.5 + 0.25 * (double)var7;
                var6 = (double)(param3.nextFloat() * 2.0F * (float)var7);
            }

            param1.addParticle(ParticleTypes.PORTAL, var1, var2, var3, var4, var5, var6);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ItemStack getCloneItemStack(BlockGetter param0, BlockPos param1, BlockState param2) {
        return ItemStack.EMPTY;
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        switch(param1) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                switch((Direction.Axis)param0.getValue(AXIS)) {
                    case Z:
                        return param0.setValue(AXIS, Direction.Axis.X);
                    case X:
                        return param0.setValue(AXIS, Direction.Axis.Z);
                    default:
                        return param0;
                }
            default:
                return param0;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(AXIS);
    }

    public static BlockPattern.BlockPatternMatch getPortalShape(LevelAccessor param0, BlockPos param1) {
        Direction.Axis var0 = Direction.Axis.Z;
        NetherPortalBlock.PortalShape var1 = new NetherPortalBlock.PortalShape(param0, param1, Direction.Axis.X);
        LoadingCache<BlockPos, BlockInWorld> var2 = BlockPattern.createLevelCache(param0, true);
        if (!var1.isValid()) {
            var0 = Direction.Axis.X;
            var1 = new NetherPortalBlock.PortalShape(param0, param1, Direction.Axis.Z);
        }

        if (!var1.isValid()) {
            return new BlockPattern.BlockPatternMatch(param1, Direction.NORTH, Direction.UP, var2, 1, 1, 1);
        } else {
            int[] var3 = new int[Direction.AxisDirection.values().length];
            Direction var4 = var1.rightDir.getCounterClockWise();
            BlockPos var5 = var1.bottomLeft.above(var1.getHeight() - 1);

            for(Direction.AxisDirection var6 : Direction.AxisDirection.values()) {
                BlockPattern.BlockPatternMatch var7 = new BlockPattern.BlockPatternMatch(
                    var4.getAxisDirection() == var6 ? var5 : var5.relative(var1.rightDir, var1.getWidth() - 1),
                    Direction.get(var6, var0),
                    Direction.UP,
                    var2,
                    var1.getWidth(),
                    var1.getHeight(),
                    1
                );

                for(int var8 = 0; var8 < var1.getWidth(); ++var8) {
                    for(int var9 = 0; var9 < var1.getHeight(); ++var9) {
                        BlockInWorld var10 = var7.getBlock(var8, var9, 1);
                        if (!var10.getState().isAir()) {
                            var3[var6.ordinal()]++;
                        }
                    }
                }
            }

            Direction.AxisDirection var11 = Direction.AxisDirection.POSITIVE;

            for(Direction.AxisDirection var12 : Direction.AxisDirection.values()) {
                if (var3[var12.ordinal()] < var3[var11.ordinal()]) {
                    var11 = var12;
                }
            }

            return new BlockPattern.BlockPatternMatch(
                var4.getAxisDirection() == var11 ? var5 : var5.relative(var1.rightDir, var1.getWidth() - 1),
                Direction.get(var11, var0),
                Direction.UP,
                var2,
                var1.getWidth(),
                var1.getHeight(),
                1
            );
        }
    }

    public static class PortalShape {
        private final LevelAccessor level;
        private final Direction.Axis axis;
        private final Direction rightDir;
        private final Direction leftDir;
        private int numPortalBlocks;
        @Nullable
        private BlockPos bottomLeft;
        private int height;
        private int width;

        public PortalShape(LevelAccessor param0, BlockPos param1, Direction.Axis param2) {
            this.level = param0;
            this.axis = param2;
            if (param2 == Direction.Axis.X) {
                this.leftDir = Direction.EAST;
                this.rightDir = Direction.WEST;
            } else {
                this.leftDir = Direction.NORTH;
                this.rightDir = Direction.SOUTH;
            }

            BlockPos var0 = param1;

            while(param1.getY() > var0.getY() - 21 && param1.getY() > 0 && this.isEmpty(param0.getBlockState(param1.below()))) {
                param1 = param1.below();
            }

            int var1 = this.getDistanceUntilEdge(param1, this.leftDir) - 1;
            if (var1 >= 0) {
                this.bottomLeft = param1.relative(this.leftDir, var1);
                this.width = this.getDistanceUntilEdge(this.bottomLeft, this.rightDir);
                if (this.width < 2 || this.width > 21) {
                    this.bottomLeft = null;
                    this.width = 0;
                }
            }

            if (this.bottomLeft != null) {
                this.height = this.calculatePortalHeight();
            }

        }

        protected int getDistanceUntilEdge(BlockPos param0, Direction param1) {
            int var0;
            for(var0 = 0; var0 < 22; ++var0) {
                BlockPos var1 = param0.relative(param1, var0);
                if (!this.isEmpty(this.level.getBlockState(var1)) || !this.level.getBlockState(var1.below()).is(Blocks.OBSIDIAN)) {
                    break;
                }
            }

            return this.level.getBlockState(param0.relative(param1, var0)).is(Blocks.OBSIDIAN) ? var0 : 0;
        }

        public int getHeight() {
            return this.height;
        }

        public int getWidth() {
            return this.width;
        }

        protected int calculatePortalHeight() {
            label56:
            for(this.height = 0; this.height < 21; ++this.height) {
                for(int var0 = 0; var0 < this.width; ++var0) {
                    BlockPos var1 = this.bottomLeft.relative(this.rightDir, var0).above(this.height);
                    BlockState var2 = this.level.getBlockState(var1);
                    if (!this.isEmpty(var2)) {
                        break label56;
                    }

                    if (var2.is(Blocks.NETHER_PORTAL)) {
                        ++this.numPortalBlocks;
                    }

                    if (var0 == 0) {
                        if (!this.level.getBlockState(var1.relative(this.leftDir)).is(Blocks.OBSIDIAN)) {
                            break label56;
                        }
                    } else if (var0 == this.width - 1 && !this.level.getBlockState(var1.relative(this.rightDir)).is(Blocks.OBSIDIAN)) {
                        break label56;
                    }
                }
            }

            for(int var3 = 0; var3 < this.width; ++var3) {
                if (!this.level.getBlockState(this.bottomLeft.relative(this.rightDir, var3).above(this.height)).is(Blocks.OBSIDIAN)) {
                    this.height = 0;
                    break;
                }
            }

            if (this.height <= 21 && this.height >= 3) {
                return this.height;
            } else {
                this.bottomLeft = null;
                this.width = 0;
                this.height = 0;
                return 0;
            }
        }

        protected boolean isEmpty(BlockState param0) {
            return param0.isAir() || param0.is(BlockTags.FIRE) || param0.is(Blocks.NETHER_PORTAL);
        }

        public boolean isValid() {
            return this.bottomLeft != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
        }

        public void createPortalBlocks() {
            for(int var0 = 0; var0 < this.width; ++var0) {
                BlockPos var1 = this.bottomLeft.relative(this.rightDir, var0);

                for(int var2 = 0; var2 < this.height; ++var2) {
                    this.level.setBlock(var1.above(var2), Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, this.axis), 18);
                }
            }

        }

        private boolean hasAllPortalBlocks() {
            return this.numPortalBlocks >= this.width * this.height;
        }

        public boolean isComplete() {
            return this.isValid() && this.hasAllPortalBlocks();
        }
    }
}
