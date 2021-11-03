package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.phys.Vec3;

public class LargeDripstoneFeature extends Feature<LargeDripstoneConfiguration> {
    public LargeDripstoneFeature(Codec<LargeDripstoneConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<LargeDripstoneConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        LargeDripstoneConfiguration var2 = param0.config();
        Random var3 = param0.random();
        if (!DripstoneUtils.isEmptyOrWater(var0, var1)) {
            return false;
        } else {
            Optional<Column> var4 = Column.scan(
                var0, var1, var2.floorToCeilingSearchRange, DripstoneUtils::isEmptyOrWater, DripstoneUtils::isDripstoneBaseOrLava
            );
            if (var4.isPresent() && var4.get() instanceof Column.Range var5) {
                if (var5.height() < 4) {
                    return false;
                } else {
                    int var6 = (int)((float)var5.height() * var2.maxColumnRadiusToCaveHeightRatio);
                    int var7 = Mth.clamp(var6, var2.columnRadius.getMinValue(), var2.columnRadius.getMaxValue());
                    int var8 = Mth.randomBetweenInclusive(var3, var2.columnRadius.getMinValue(), var7);
                    LargeDripstoneFeature.LargeDripstone var9 = makeDripstone(
                        var1.atY(var5.ceiling() - 1), false, var3, var8, var2.stalactiteBluntness, var2.heightScale, var5.height() + 1
                    );
                    LargeDripstoneFeature.LargeDripstone var10 = makeDripstone(
                        var1.atY(var5.floor() + 1), true, var3, var8, var2.stalagmiteBluntness, var2.heightScale, var5.height() + 1
                    );
                    LargeDripstoneFeature.WindOffsetter var11;
                    if (var9.isSuitableForWind(var2) && var10.isSuitableForWind(var2)) {
                        var11 = new LargeDripstoneFeature.WindOffsetter(var1.getY(), var3, var2.windSpeed);
                    } else {
                        var11 = LargeDripstoneFeature.WindOffsetter.noWind();
                    }

                    boolean var13 = var9.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(var0, var11);
                    boolean var14 = var10.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(var0, var11);
                    if (var13) {
                        var9.placeBlocks(var0, var3, var11);
                    }

                    if (var14) {
                        var10.placeBlocks(var0, var3, var11);
                    }

                    return true;
                }
            } else {
                return false;
            }
        }
    }

    private static LargeDripstoneFeature.LargeDripstone makeDripstone(
        BlockPos param0, boolean param1, Random param2, int param3, FloatProvider param4, FloatProvider param5, int param6
    ) {
        return new LargeDripstoneFeature.LargeDripstone(param0, param1, param3, (double)param4.sample(param2), (double)param5.sample(param2), param6);
    }

    private void placeDebugMarkers(WorldGenLevel param0, BlockPos param1, Column.Range param2, LargeDripstoneFeature.WindOffsetter param3) {
        param0.setBlock(param3.offset(param1.atY(param2.ceiling() - 1)), Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);
        param0.setBlock(param3.offset(param1.atY(param2.floor() + 1)), Blocks.GOLD_BLOCK.defaultBlockState(), 2);

        for(BlockPos.MutableBlockPos var0 = param1.atY(param2.floor() + 2).mutable(); var0.getY() < param2.ceiling() - 1; var0.move(Direction.UP)) {
            BlockPos var1 = param3.offset(var0);
            if (DripstoneUtils.isEmptyOrWater(param0, var1) || param0.getBlockState(var1).is(Blocks.DRIPSTONE_BLOCK)) {
                param0.setBlock(var1, Blocks.CREEPER_HEAD.defaultBlockState(), 2);
            }
        }

    }

    static final class LargeDripstone {
        private BlockPos root;
        private final boolean pointingUp;
        private int radius;
        private final double bluntness;
        private final double scale;
        private final int columnHeight;

        LargeDripstone(BlockPos param0, boolean param1, int param2, double param3, double param4, int param5) {
            this.root = param0;
            this.pointingUp = param1;
            this.radius = param2;
            this.bluntness = param3;
            this.scale = param4;
            this.columnHeight = param5;
        }

        private int getHeight() {
            return this.getHeightAtRadius(0.0F);
        }

        private int getMinY() {
            return this.pointingUp ? this.root.getY() : this.root.getY() - this.getHeight();
        }

        private int getMaxY() {
            return !this.pointingUp ? this.root.getY() : this.root.getY() + this.getHeight();
        }

        boolean moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(WorldGenLevel param0, LargeDripstoneFeature.WindOffsetter param1) {
            while(this.radius > 1) {
                BlockPos.MutableBlockPos var0 = this.root.mutable();
                int var1 = Math.min(10, this.getHeight());

                for(int var2 = 0; var2 < var1; ++var2) {
                    if (param0.getBlockState(var0).is(Blocks.LAVA)) {
                        return false;
                    }

                    if (DripstoneUtils.isCircleMostlyEmbeddedInStone(param0, param1.offset(var0), this.radius)) {
                        this.root = var0;
                        return true;
                    }

                    var0.move(this.pointingUp ? Direction.DOWN : Direction.UP);
                }

                this.radius /= 2;
            }

            return false;
        }

        private int getHeightAtRadius(float param0) {
            return (int)DripstoneUtils.getDripstoneHeight((double)param0, (double)this.radius, this.scale, this.bluntness);
        }

        void placeBlocks(WorldGenLevel param0, Random param1, LargeDripstoneFeature.WindOffsetter param2) {
            for(int var0 = -this.radius; var0 <= this.radius; ++var0) {
                for(int var1 = -this.radius; var1 <= this.radius; ++var1) {
                    float var2 = Mth.sqrt((float)(var0 * var0 + var1 * var1));
                    if (!(var2 > (float)this.radius)) {
                        int var3 = this.getHeightAtRadius(var2);
                        if (var3 > 0) {
                            if ((double)param1.nextFloat() < 0.2) {
                                var3 = (int)((float)var3 * Mth.randomBetween(param1, 0.8F, 1.0F));
                            }

                            BlockPos.MutableBlockPos var4 = this.root.offset(var0, 0, var1).mutable();
                            boolean var5 = false;

                            for(int var6 = 0; var6 < var3; ++var6) {
                                BlockPos var7 = param2.offset(var4);
                                if (DripstoneUtils.isEmptyOrWaterOrLava(param0, var7)) {
                                    var5 = true;
                                    Block var8 = Blocks.DRIPSTONE_BLOCK;
                                    param0.setBlock(var7, var8.defaultBlockState(), 2);
                                } else if (var5 && param0.getBlockState(var7).is(BlockTags.BASE_STONE_OVERWORLD) || !var5 && var6 > this.columnHeight) {
                                    break;
                                }

                                var4.move(this.pointingUp ? Direction.UP : Direction.DOWN);
                            }
                        }
                    }
                }
            }

        }

        boolean isSuitableForWind(LargeDripstoneConfiguration param0) {
            return this.radius >= param0.minRadiusForWind && this.bluntness >= (double)param0.minBluntnessForWind;
        }
    }

    static final class WindOffsetter {
        private final int originY;
        @Nullable
        private final Vec3 windSpeed;

        WindOffsetter(int param0, Random param1, FloatProvider param2) {
            this.originY = param0;
            float var0 = param2.sample(param1);
            float var1 = Mth.randomBetween(param1, 0.0F, (float) Math.PI);
            this.windSpeed = new Vec3((double)(Mth.cos(var1) * var0), 0.0, (double)(Mth.sin(var1) * var0));
        }

        private WindOffsetter() {
            this.originY = 0;
            this.windSpeed = null;
        }

        static LargeDripstoneFeature.WindOffsetter noWind() {
            return new LargeDripstoneFeature.WindOffsetter();
        }

        BlockPos offset(BlockPos param0) {
            if (this.windSpeed == null) {
                return param0;
            } else {
                int var0 = this.originY - param0.getY();
                Vec3 var1 = this.windSpeed.scale((double)var0);
                return param0.offset(var1.x, 0.0, var1.z);
            }
        }
    }
}
