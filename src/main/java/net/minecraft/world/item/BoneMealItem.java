package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BoneMealItem extends Item {
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
                var0.levelEvent(2005, var1, 0);
            }

            return InteractionResult.SUCCESS;
        } else {
            BlockState var3 = var0.getBlockState(var1);
            boolean var4 = var3.isFaceSturdy(var0, var1, param0.getClickedFace());
            if (var4 && growWaterPlant(param0.getItemInHand(), var0, var2, param0.getClickedFace())) {
                if (!var0.isClientSide) {
                    var0.levelEvent(2005, var2, 0);
                }

                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    public static boolean growCrop(ItemStack param0, Level param1, BlockPos param2) {
        BlockState var0 = param1.getBlockState(param2);
        if (var0.getBlock() instanceof BonemealableBlock) {
            BonemealableBlock var1 = (BonemealableBlock)var0.getBlock();
            if (var1.isValidBonemealTarget(param1, param2, var0, param1.isClientSide)) {
                if (param1 instanceof ServerLevel) {
                    if (var1.isBonemealSuccess(param1, param1.random, param2, var0)) {
                        var1.performBonemeal((ServerLevel)param1, param1.random, param2, var0);
                    }

                    param0.shrink(1);
                }

                return true;
            }
        }

        return false;
    }

    public static boolean growWaterPlant(ItemStack param0, Level param1, BlockPos param2, @Nullable Direction param3) {
        if (param1.getBlockState(param2).getBlock() == Blocks.WATER && param1.getFluidState(param2).getAmount() == 8) {
            if (!(param1 instanceof ServerLevel)) {
                return true;
            } else {
                label80:
                for(int var0 = 0; var0 < 128; ++var0) {
                    BlockPos var1 = param2;
                    Biome var2 = param1.getBiome(param2);
                    BlockState var3 = Blocks.SEAGRASS.defaultBlockState();

                    for(int var4 = 0; var4 < var0 / 16; ++var4) {
                        var1 = var1.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                        var2 = param1.getBiome(var1);
                        if (param1.getBlockState(var1).isCollisionShapeFullBlock(param1, var1)) {
                            continue label80;
                        }
                    }

                    if (var2 == Biomes.WARM_OCEAN || var2 == Biomes.DEEP_WARM_OCEAN) {
                        if (var0 == 0 && param3 != null && param3.getAxis().isHorizontal()) {
                            var3 = BlockTags.WALL_CORALS.getRandomElement(param1.random).defaultBlockState().setValue(BaseCoralWallFanBlock.FACING, param3);
                        } else if (random.nextInt(4) == 0) {
                            var3 = BlockTags.UNDERWATER_BONEMEALS.getRandomElement(random).defaultBlockState();
                        }
                    }

                    if (var3.getBlock().is(BlockTags.WALL_CORALS)) {
                        for(int var5 = 0; !var3.canSurvive(param1, var1) && var5 < 4; ++var5) {
                            var3 = var3.setValue(BaseCoralWallFanBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
                        }
                    }

                    if (var3.canSurvive(param1, var1)) {
                        BlockState var6 = param1.getBlockState(var1);
                        if (var6.getBlock() == Blocks.WATER && param1.getFluidState(var1).getAmount() == 8) {
                            param1.setBlock(var1, var3, 3);
                        } else if (var6.getBlock() == Blocks.SEAGRASS && random.nextInt(10) == 0) {
                            ((BonemealableBlock)Blocks.SEAGRASS).performBonemeal((ServerLevel)param1, random, var1, var6);
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

    @OnlyIn(Dist.CLIENT)
    public static void addGrowthParticles(LevelAccessor param0, BlockPos param1, int param2) {
        if (param2 == 0) {
            param2 = 15;
        }

        BlockState var0 = param0.getBlockState(param1);
        if (!var0.isAir()) {
            double var1 = 0.5;
            double var2;
            if (!var0.getFluidState().isEmpty()) {
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

            for(int var5 = 0; var5 < param2; ++var5) {
                double var6 = random.nextGaussian() * 0.02;
                double var7 = random.nextGaussian() * 0.02;
                double var8 = random.nextGaussian() * 0.02;
                double var9 = 0.5 - var1;
                double var10 = (double)param1.getX() + var9 + random.nextDouble() * var1 * 2.0;
                double var11 = (double)param1.getY() + random.nextDouble() * var2;
                double var12 = (double)param1.getZ() + var9 + random.nextDouble() * var1 * 2.0;
                if (!param0.getBlockState(new BlockPos(var10, var11, var12).below()).isAir()) {
                    param0.addParticle(ParticleTypes.HAPPY_VILLAGER, var10, var11, var12, var6, var7, var8);
                }
            }

        }
    }
}
