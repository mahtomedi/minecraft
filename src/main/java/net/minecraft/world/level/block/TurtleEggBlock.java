package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TurtleEggBlock extends Block {
    private static final VoxelShape ONE_EGG_AABB = Block.box(3.0, 0.0, 3.0, 12.0, 7.0, 12.0);
    private static final VoxelShape MULTIPLE_EGGS_AABB = Block.box(1.0, 0.0, 1.0, 15.0, 7.0, 15.0);
    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
    public static final IntegerProperty EGGS = BlockStateProperties.EGGS;

    public TurtleEggBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, Integer.valueOf(0)).setValue(EGGS, Integer.valueOf(1)));
    }

    @Override
    public void stepOn(Level param0, BlockPos param1, Entity param2) {
        this.destroyEgg(param0, param1, param2, 100);
        super.stepOn(param0, param1, param2);
    }

    @Override
    public void fallOn(Level param0, BlockPos param1, Entity param2, float param3) {
        if (!(param2 instanceof Zombie)) {
            this.destroyEgg(param0, param1, param2, 3);
        }

        super.fallOn(param0, param1, param2, param3);
    }

    private void destroyEgg(Level param0, BlockPos param1, Entity param2, int param3) {
        if (!this.canDestroyEgg(param0, param2)) {
            super.stepOn(param0, param1, param2);
        } else {
            if (!param0.isClientSide && param0.random.nextInt(param3) == 0) {
                this.decreaseEggs(param0, param1, param0.getBlockState(param1));
            }

        }
    }

    private void decreaseEggs(Level param0, BlockPos param1, BlockState param2) {
        param0.playSound(null, param1, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + param0.random.nextFloat() * 0.2F);
        int var0 = param2.getValue(EGGS);
        if (var0 <= 1) {
            param0.destroyBlock(param1, false);
        } else {
            param0.setBlock(param1, param2.setValue(EGGS, Integer.valueOf(var0 - 1)), 2);
            param0.levelEvent(2001, param1, Block.getId(param2));
        }

    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (this.shouldUpdateHatchLevel(param1) && this.onSand(param1, param2)) {
            int var0 = param0.getValue(HATCH);
            if (var0 < 2) {
                param1.playSound(null, param2, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + param3.nextFloat() * 0.2F);
                param1.setBlock(param2, param0.setValue(HATCH, Integer.valueOf(var0 + 1)), 2);
            } else {
                param1.playSound(null, param2, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F + param3.nextFloat() * 0.2F);
                param1.removeBlock(param2, false);

                for(int var1 = 0; var1 < param0.getValue(EGGS); ++var1) {
                    param1.levelEvent(2001, param2, Block.getId(param0));
                    Turtle var2 = EntityType.TURTLE.create(param1);
                    var2.setAge(-24000);
                    var2.setHomePos(param2);
                    var2.moveTo((double)param2.getX() + 0.3 + (double)var1 * 0.2, (double)param2.getY(), (double)param2.getZ() + 0.3, 0.0F, 0.0F);
                    param1.addFreshEntity(var2);
                }
            }
        }

    }

    private boolean onSand(BlockGetter param0, BlockPos param1) {
        return param0.getBlockState(param1.below()).getBlock() == Blocks.SAND;
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (this.onSand(param1, param2) && !param1.isClientSide) {
            param1.levelEvent(2005, param2, 0);
        }

    }

    private boolean shouldUpdateHatchLevel(Level param0) {
        float var0 = param0.getTimeOfDay(1.0F);
        if ((double)var0 < 0.69 && (double)var0 > 0.65) {
            return true;
        } else {
            return param0.random.nextInt(500) == 0;
        }
    }

    @Override
    public void playerDestroy(Level param0, Player param1, BlockPos param2, BlockState param3, @Nullable BlockEntity param4, ItemStack param5) {
        super.playerDestroy(param0, param1, param2, param3, param4, param5);
        this.decreaseEggs(param0, param2, param3);
    }

    @Override
    public boolean canBeReplaced(BlockState param0, BlockPlaceContext param1) {
        return param1.getItemInHand().getItem() == this.asItem() && param0.getValue(EGGS) < 4 ? true : super.canBeReplaced(param0, param1);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockState var0 = param0.getLevel().getBlockState(param0.getClickedPos());
        return var0.getBlock() == this ? var0.setValue(EGGS, Integer.valueOf(Math.min(4, var0.getValue(EGGS) + 1))) : super.getStateForPlacement(param0);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return param0.getValue(EGGS) > 1 ? MULTIPLE_EGGS_AABB : ONE_EGG_AABB;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(HATCH, EGGS);
    }

    private boolean canDestroyEgg(Level param0, Entity param1) {
        if (param1 instanceof Turtle) {
            return false;
        } else if (!(param1 instanceof LivingEntity)) {
            return false;
        } else {
            return param1 instanceof Player || param0.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        }
    }
}
