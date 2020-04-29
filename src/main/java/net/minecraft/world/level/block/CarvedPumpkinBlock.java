package net.minecraft.world.level.block;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockMaterialPredicate;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;

public class CarvedPumpkinBlock extends HorizontalDirectionalBlock implements Wearable {
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
            for(int var1 = 0; var1 < this.getOrCreateSnowGolemFull().getHeight(); ++var1) {
                BlockInWorld var2 = var0.getBlock(0, var1, 0);
                param0.setBlock(var2.getPos(), Blocks.AIR.defaultBlockState(), 2);
                param0.levelEvent(2001, var2.getPos(), Block.getId(var2.getState()));
            }

            SnowGolem var3 = EntityType.SNOW_GOLEM.create(param0);
            BlockPos var4 = var0.getBlock(0, 2, 0).getPos();
            var3.moveTo((double)var4.getX() + 0.5, (double)var4.getY() + 0.05, (double)var4.getZ() + 0.5, 0.0F, 0.0F);
            param0.addFreshEntity(var3);

            for(ServerPlayer var5 : param0.getEntitiesOfClass(ServerPlayer.class, var3.getBoundingBox().inflate(5.0))) {
                CriteriaTriggers.SUMMONED_ENTITY.trigger(var5, var3);
            }

            for(int var6 = 0; var6 < this.getOrCreateSnowGolemFull().getHeight(); ++var6) {
                BlockInWorld var7 = var0.getBlock(0, var6, 0);
                param0.blockUpdated(var7.getPos(), Blocks.AIR);
            }
        } else {
            var0 = this.getOrCreateIronGolemFull().find(param0, param1);
            if (var0 != null) {
                for(int var8 = 0; var8 < this.getOrCreateIronGolemFull().getWidth(); ++var8) {
                    for(int var9 = 0; var9 < this.getOrCreateIronGolemFull().getHeight(); ++var9) {
                        BlockInWorld var10 = var0.getBlock(var8, var9, 0);
                        param0.setBlock(var10.getPos(), Blocks.AIR.defaultBlockState(), 2);
                        param0.levelEvent(2001, var10.getPos(), Block.getId(var10.getState()));
                    }
                }

                BlockPos var11 = var0.getBlock(1, 2, 0).getPos();
                IronGolem var12 = EntityType.IRON_GOLEM.create(param0);
                var12.setPlayerCreated(true);
                var12.moveTo((double)var11.getX() + 0.5, (double)var11.getY() + 0.05, (double)var11.getZ() + 0.5, 0.0F, 0.0F);
                param0.addFreshEntity(var12);

                for(ServerPlayer var13 : param0.getEntitiesOfClass(ServerPlayer.class, var12.getBoundingBox().inflate(5.0))) {
                    CriteriaTriggers.SUMMONED_ENTITY.trigger(var13, var12);
                }

                for(int var14 = 0; var14 < this.getOrCreateIronGolemFull().getWidth(); ++var14) {
                    for(int var15 = 0; var15 < this.getOrCreateIronGolemFull().getHeight(); ++var15) {
                        BlockInWorld var16 = var0.getBlock(var14, var15, 0);
                        param0.blockUpdated(var16.getPos(), Blocks.AIR);
                    }
                }
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
                .where('~', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR)))
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
                .where('~', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR)))
                .build();
        }

        return this.ironGolemFull;
    }
}
