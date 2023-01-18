package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NetherPortalBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    protected static final int AABB_OFFSET = 2;
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
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (param1.dimensionType().natural()
            && param1.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
            && param3.nextInt(2000) < param1.getDifficulty().getId()) {
            while(param1.getBlockState(param2).is(this)) {
                param2 = param2.below();
            }

            if (param1.getBlockState(param2).isValidSpawn(param1, param2, EntityType.ZOMBIFIED_PIGLIN)) {
                Entity var0 = EntityType.ZOMBIFIED_PIGLIN.spawn(param1, param2.above(), MobSpawnType.STRUCTURE);
                if (var0 != null) {
                    var0.setPortalCooldown();
                }
            }
        }

    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        Direction.Axis var0 = param1.getAxis();
        Direction.Axis var1 = param0.getValue(AXIS);
        boolean var2 = var1 != var0 && var0.isHorizontal();
        return !var2 && !param2.is(this) && !new PortalShape(param3, param4, var1).isComplete()
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (param3.canChangeDimensions()) {
            param3.handleInsidePortal(param2);
        }

    }

    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, RandomSource param3) {
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
}
