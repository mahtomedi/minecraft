package net.minecraft.world.item;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BoneMealItem extends Item {
    public static final int GRASS_SPREAD_WIDTH = 3;
    public static final int GRASS_SPREAD_HEIGHT = 1;
    public static final int GRASS_COUNT_MULTIPLIER = 3;

    public BoneMealItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        BlockPos var2 = var1.relative(param0.getClickedFace());
        if (growCrop(param0.getItemInHand(), var0, var1)) {
            if (!var0.isClientSide) {
                var0.levelEvent(1505, var1, 0);
            }

            return InteractionResult.sidedSuccess(var0.isClientSide);
        } else {
            BlockState var3 = var0.getBlockState(var1);
            boolean var4 = var3.isFaceSturdy(var0, var1, param0.getClickedFace());
            if (var4 && growWaterPlant(param0.getItemInHand(), var0, var2, param0.getClickedFace())) {
                if (!var0.isClientSide) {
                    var0.levelEvent(1505, var2, 0);
                }

                return InteractionResult.sidedSuccess(var0.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    public static boolean growCrop(ItemStack param0, Level param1, BlockPos param2) {
        BlockState var0 = param1.getBlockState(param2);
        if (var0.getBlock() instanceof BonemealableBlock var1 && var1.isValidBonemealTarget(param1, param2, var0, param1.isClientSide)) {
            if (param1 instanceof ServerLevel) {
                if (var1.isBonemealSuccess(param1, param1.random, param2, var0)) {
                    var1.performBonemeal((ServerLevel)param1, param1.random, param2, var0);
                }

                param0.shrink(1);
            }

            return true;
        }

        return false;
    }

    public static boolean growWaterPlant(ItemStack param0, Level param1, BlockPos param2, @Nullable Direction param3) {
        if (param1.getBlockState(param2).is(Blocks.WATER) && param1.getFluidState(param2).getAmount() == 8) {
            if (!(param1 instanceof ServerLevel)) {
                return true;
            } else {
                Random var0 = param1.getRandom();

                label76:
                for(int var1 = 0; var1 < 128; ++var1) {
                    BlockPos var2 = param2;
                    BlockState var3 = Blocks.SEAGRASS.defaultBlockState();

                    for(int var4 = 0; var4 < var1 / 16; ++var4) {
                        var2 = var2.offset(var0.nextInt(3) - 1, (var0.nextInt(3) - 1) * var0.nextInt(3) / 2, var0.nextInt(3) - 1);
                        if (param1.getBlockState(var2).isCollisionShapeFullBlock(param1, var2)) {
                            continue label76;
                        }
                    }

                    Optional<ResourceKey<Biome>> var5 = param1.getBiomeName(var2);
                    if (Objects.equals(var5, Optional.of(Biomes.WARM_OCEAN))) {
                        if (var1 == 0 && param3 != null && param3.getAxis().isHorizontal()) {
                            var3 = BlockTags.WALL_CORALS.getRandomElement(param1.random).defaultBlockState().setValue(BaseCoralWallFanBlock.FACING, param3);
                        } else if (var0.nextInt(4) == 0) {
                            var3 = BlockTags.UNDERWATER_BONEMEALS.getRandomElement(var0).defaultBlockState();
                        }
                    }

                    if (var3.is(BlockTags.WALL_CORALS)) {
                        for(int var6 = 0; !var3.canSurvive(param1, var2) && var6 < 4; ++var6) {
                            var3 = var3.setValue(BaseCoralWallFanBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(var0));
                        }
                    }

                    if (var3.canSurvive(param1, var2)) {
                        BlockState var7 = param1.getBlockState(var2);
                        if (var7.is(Blocks.WATER) && param1.getFluidState(var2).getAmount() == 8) {
                            param1.setBlock(var2, var3, 3);
                        } else if (var7.is(Blocks.SEAGRASS) && var0.nextInt(10) == 0) {
                            ((BonemealableBlock)Blocks.SEAGRASS).performBonemeal((ServerLevel)param1, var0, var2, var7);
                        }
                    }
                }

                param0.shrink(1);
                return true;
            }
        } else {
            return false;
        }
    }

    public static void addGrowthParticles(LevelAccessor param0, BlockPos param1, int param2) {
        if (param2 == 0) {
            param2 = 15;
        }

        BlockState var0 = param0.getBlockState(param1);
        if (!var0.isAir()) {
            double var1 = 0.5;
            double var2;
            if (var0.is(Blocks.WATER)) {
                param2 *= 3;
                var2 = 1.0;
                var1 = 3.0;
            } else if (var0.isSolidRender(param0, param1)) {
                param1 = param1.above();
                param2 *= 3;
                var1 = 3.0;
                var2 = 1.0;
            } else {
                var2 = var0.getShape(param0, param1).max(Direction.Axis.Y);
            }

            param0.addParticle(
                ParticleTypes.HAPPY_VILLAGER, (double)param1.getX() + 0.5, (double)param1.getY() + 0.5, (double)param1.getZ() + 0.5, 0.0, 0.0, 0.0
            );
            Random var5 = param0.getRandom();

            for(int var6 = 0; var6 < param2; ++var6) {
                double var7 = var5.nextGaussian() * 0.02;
                double var8 = var5.nextGaussian() * 0.02;
                double var9 = var5.nextGaussian() * 0.02;
                double var10 = 0.5 - var1;
                double var11 = (double)param1.getX() + var10 + var5.nextDouble() * var1 * 2.0;
                double var12 = (double)param1.getY() + var5.nextDouble() * var2;
                double var13 = (double)param1.getZ() + var10 + var5.nextDouble() * var1 * 2.0;
                if (!param0.getBlockState(new BlockPos(var11, var12, var13).below()).isAir()) {
                    param0.addParticle(ParticleTypes.HAPPY_VILLAGER, var11, var12, var13, var7, var8, var9);
                }
            }

        }
    }
}
