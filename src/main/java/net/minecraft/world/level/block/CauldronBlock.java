package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CauldronBlock extends Block {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;
    private static final VoxelShape INSIDE = box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);
    protected static final VoxelShape SHAPE = Shapes.join(
        Shapes.block(),
        Shapes.or(box(0.0, 0.0, 4.0, 16.0, 3.0, 12.0), box(4.0, 0.0, 0.0, 12.0, 3.0, 16.0), box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0), INSIDE),
        BooleanOp.ONLY_FIRST
    );

    public CauldronBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return INSIDE;
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        int var0 = param0.getValue(LEVEL);
        float var1 = (float)param2.getY() + (6.0F + (float)(3 * var0)) / 16.0F;
        if (!param1.isClientSide && param3.isOnFire() && var0 > 0 && param3.getY() <= (double)var1) {
            param3.clearFire();
            this.setWaterLevel(param1, param2, param0, var0 - 1);
        }

    }

    @Override
    public boolean use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        ItemStack var0 = param3.getItemInHand(param4);
        if (var0.isEmpty()) {
            return true;
        } else {
            int var1 = param0.getValue(LEVEL);
            Item var2 = var0.getItem();
            if (var2 == Items.WATER_BUCKET) {
                if (var1 < 3 && !param1.isClientSide) {
                    if (!param3.abilities.instabuild) {
                        param3.setItemInHand(param4, new ItemStack(Items.BUCKET));
                    }

                    param3.awardStat(Stats.FILL_CAULDRON);
                    this.setWaterLevel(param1, param2, param0, 3);
                    param1.playSound(null, param2, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                return true;
            } else if (var2 == Items.BUCKET) {
                if (var1 == 3 && !param1.isClientSide) {
                    if (!param3.abilities.instabuild) {
                        var0.shrink(1);
                        if (var0.isEmpty()) {
                            param3.setItemInHand(param4, new ItemStack(Items.WATER_BUCKET));
                        } else if (!param3.inventory.add(new ItemStack(Items.WATER_BUCKET))) {
                            param3.drop(new ItemStack(Items.WATER_BUCKET), false);
                        }
                    }

                    param3.awardStat(Stats.USE_CAULDRON);
                    this.setWaterLevel(param1, param2, param0, 0);
                    param1.playSound(null, param2, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                return true;
            } else if (var2 == Items.GLASS_BOTTLE) {
                if (var1 > 0 && !param1.isClientSide) {
                    if (!param3.abilities.instabuild) {
                        ItemStack var3 = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
                        param3.awardStat(Stats.USE_CAULDRON);
                        var0.shrink(1);
                        if (var0.isEmpty()) {
                            param3.setItemInHand(param4, var3);
                        } else if (!param3.inventory.add(var3)) {
                            param3.drop(var3, false);
                        } else if (param3 instanceof ServerPlayer) {
                            ((ServerPlayer)param3).refreshContainer(param3.inventoryMenu);
                        }
                    }

                    param1.playSound(null, param2, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    this.setWaterLevel(param1, param2, param0, var1 - 1);
                }

                return true;
            } else if (var2 == Items.POTION && PotionUtils.getPotion(var0) == Potions.WATER) {
                if (var1 < 3 && !param1.isClientSide) {
                    if (!param3.abilities.instabuild) {
                        ItemStack var4 = new ItemStack(Items.GLASS_BOTTLE);
                        param3.awardStat(Stats.USE_CAULDRON);
                        param3.setItemInHand(param4, var4);
                        if (param3 instanceof ServerPlayer) {
                            ((ServerPlayer)param3).refreshContainer(param3.inventoryMenu);
                        }
                    }

                    param1.playSound(null, param2, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    this.setWaterLevel(param1, param2, param0, var1 + 1);
                }

                return true;
            } else {
                if (var1 > 0 && var2 instanceof DyeableLeatherItem) {
                    DyeableLeatherItem var5 = (DyeableLeatherItem)var2;
                    if (var5.hasCustomColor(var0) && !param1.isClientSide) {
                        var5.clearColor(var0);
                        this.setWaterLevel(param1, param2, param0, var1 - 1);
                        param3.awardStat(Stats.CLEAN_ARMOR);
                        return true;
                    }
                }

                if (var1 > 0 && var2 instanceof BannerItem) {
                    if (BannerBlockEntity.getPatternCount(var0) > 0 && !param1.isClientSide) {
                        ItemStack var6 = var0.copy();
                        var6.setCount(1);
                        BannerBlockEntity.removeLastPattern(var6);
                        param3.awardStat(Stats.CLEAN_BANNER);
                        if (!param3.abilities.instabuild) {
                            var0.shrink(1);
                            this.setWaterLevel(param1, param2, param0, var1 - 1);
                        }

                        if (var0.isEmpty()) {
                            param3.setItemInHand(param4, var6);
                        } else if (!param3.inventory.add(var6)) {
                            param3.drop(var6, false);
                        } else if (param3 instanceof ServerPlayer) {
                            ((ServerPlayer)param3).refreshContainer(param3.inventoryMenu);
                        }
                    }

                    return true;
                } else if (var1 > 0 && var2 instanceof BlockItem) {
                    Block var7 = ((BlockItem)var2).getBlock();
                    if (var7 instanceof ShulkerBoxBlock && !param1.isClientSide()) {
                        ItemStack var8 = new ItemStack(Blocks.SHULKER_BOX, 1);
                        if (var0.hasTag()) {
                            var8.setTag(var0.getTag().copy());
                        }

                        param3.setItemInHand(param4, var8);
                        this.setWaterLevel(param1, param2, param0, var1 - 1);
                        param3.awardStat(Stats.CLEAN_SHULKER_BOX);
                    }

                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public void setWaterLevel(Level param0, BlockPos param1, BlockState param2, int param3) {
        param0.setBlock(param1, param2.setValue(LEVEL, Integer.valueOf(Mth.clamp(param3, 0, 3))), 2);
        param0.updateNeighbourForOutputSignal(param1, this);
    }

    @Override
    public void handleRain(Level param0, BlockPos param1) {
        if (param0.random.nextInt(20) == 1) {
            float var0 = param0.getBiome(param1).getTemperature(param1);
            if (!(var0 < 0.15F)) {
                BlockState var1 = param0.getBlockState(param1);
                if (var1.getValue(LEVEL) < 3) {
                    param0.setBlock(param1, var1.cycle(LEVEL), 2);
                }

            }
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState param0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        return param0.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(LEVEL);
    }

    @Override
    public boolean isPathfindable(BlockState param0, BlockGetter param1, BlockPos param2, PathComputationType param3) {
        return false;
    }
}
