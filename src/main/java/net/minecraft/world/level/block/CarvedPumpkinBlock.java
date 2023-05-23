package net.minecraft.world.level.block;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class CarvedPumpkinBlock extends HorizontalDirectionalBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    @Nullable
    private BlockPattern snowGolemBase;
    @Nullable
    private BlockPattern snowGolemFull;
    @Nullable
    private BlockPattern ironGolemBase;
    @Nullable
    private BlockPattern ironGolemFull;
    private static final Predicate<BlockState> PUMPKINS_PREDICATE = param0 -> param0 != null
            && (param0.is(Blocks.CARVED_PUMPKIN) || param0.is(Blocks.JACK_O_LANTERN));

    protected CarvedPumpkinBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param3.is(param0.getBlock())) {
            this.trySpawnGolem(param1, param2);
        }
    }

    public boolean canSpawnGolem(LevelReader param0, BlockPos param1) {
        return this.getOrCreateSnowGolemBase().find(param0, param1) != null || this.getOrCreateIronGolemBase().find(param0, param1) != null;
    }

    private void trySpawnGolem(Level param0, BlockPos param1) {
        BlockPattern.BlockPatternMatch var0 = this.getOrCreateSnowGolemFull().find(param0, param1);
        if (var0 != null) {
            SnowGolem var1 = EntityType.SNOW_GOLEM.create(param0);
            if (var1 != null) {
                spawnGolemInWorld(param0, var0, var1, var0.getBlock(0, 2, 0).getPos());
            }
        } else {
            BlockPattern.BlockPatternMatch var2 = this.getOrCreateIronGolemFull().find(param0, param1);
            if (var2 != null) {
                IronGolem var3 = EntityType.IRON_GOLEM.create(param0);
                if (var3 != null) {
                    var3.setPlayerCreated(true);
                    spawnGolemInWorld(param0, var2, var3, var2.getBlock(1, 2, 0).getPos());
                }
            }
        }

    }

    private static void spawnGolemInWorld(Level param0, BlockPattern.BlockPatternMatch param1, Entity param2, BlockPos param3) {
        clearPatternBlocks(param0, param1);
        param2.moveTo((double)param3.getX() + 0.5, (double)param3.getY() + 0.05, (double)param3.getZ() + 0.5, 0.0F, 0.0F);
        param0.addFreshEntity(param2);

        for(ServerPlayer var0 : param0.getEntitiesOfClass(ServerPlayer.class, param2.getBoundingBox().inflate(5.0))) {
            CriteriaTriggers.SUMMONED_ENTITY.trigger(var0, param2);
        }

        updatePatternBlocks(param0, param1);
    }

    public static void clearPatternBlocks(Level param0, BlockPattern.BlockPatternMatch param1) {
        for(int var0 = 0; var0 < param1.getWidth(); ++var0) {
            for(int var1 = 0; var1 < param1.getHeight(); ++var1) {
                BlockInWorld var2 = param1.getBlock(var0, var1, 0);
                param0.setBlock(var2.getPos(), Blocks.AIR.defaultBlockState(), 2);
                param0.levelEvent(2001, var2.getPos(), Block.getId(var2.getState()));
            }
        }

    }

    public static void updatePatternBlocks(Level param0, BlockPattern.BlockPatternMatch param1) {
        for(int var0 = 0; var0 < param1.getWidth(); ++var0) {
            for(int var1 = 0; var1 < param1.getHeight(); ++var1) {
                BlockInWorld var2 = param1.getBlock(var0, var1, 0);
                param0.blockUpdated(var2.getPos(), Blocks.AIR);
            }
        }

    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(FACING, param0.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING);
    }

    private BlockPattern getOrCreateSnowGolemBase() {
        if (this.snowGolemBase == null) {
            this.snowGolemBase = BlockPatternBuilder.start()
                .aisle(" ", "#", "#")
                .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.SNOW_BLOCK)))
                .build();
        }

        return this.snowGolemBase;
    }

    private BlockPattern getOrCreateSnowGolemFull() {
        if (this.snowGolemFull == null) {
            this.snowGolemFull = BlockPatternBuilder.start()
                .aisle("^", "#", "#")
                .where('^', BlockInWorld.hasState(PUMPKINS_PREDICATE))
                .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.SNOW_BLOCK)))
                .build();
        }

        return this.snowGolemFull;
    }

    private BlockPattern getOrCreateIronGolemBase() {
        if (this.ironGolemBase == null) {
            this.ironGolemBase = BlockPatternBuilder.start()
                .aisle("~ ~", "###", "~#~")
                .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK)))
                .where('~', param0 -> param0.getState().isAir())
                .build();
        }

        return this.ironGolemBase;
    }

    private BlockPattern getOrCreateIronGolemFull() {
        if (this.ironGolemFull == null) {
            this.ironGolemFull = BlockPatternBuilder.start()
                .aisle("~^~", "###", "~#~")
                .where('^', BlockInWorld.hasState(PUMPKINS_PREDICATE))
                .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK)))
                .where('~', param0 -> param0.getState().isAir())
                .build();
        }

        return this.ironGolemFull;
    }
}
