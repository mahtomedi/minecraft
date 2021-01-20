package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.UniformFloat;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.phys.Vec3;

public class LargeDripstoneFeature extends Feature<LargeDripstoneConfiguration> {
    public LargeDripstoneFeature(Codec<LargeDripstoneConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, LargeDripstoneConfiguration param4) {
        if (!DripstoneUtils.isEmptyOrWater(param0, param3)) {
            return false;
        } else {
            Optional<Column> var0 = Column.scan(
                param0, param3, param4.floorToCeilingSearchRange, DripstoneUtils::isEmptyOrWater, DripstoneUtils::isDripstoneBaseOrLava
            );
            if (var0.isPresent() && var0.get() instanceof Column.Range) {
                Column.Range var1 = (Column.Range)var0.get();
                if (var1.height() < 4) {
                    return false;
                } else {
                    int var2 = (int)((float)var1.height() * param4.maxColumnRadiusToCaveHeightRatio);
                    int var3 = Mth.clamp(var2, param4.columnRadius.getBaseValue(), param4.columnRadius.getMaxValue());
                    int var4 = Mth.randomBetweenInclusive(param2, param4.columnRadius.getBaseValue(), var3);
                    LargeDripstoneFeature.LargeDripstone var5 = makeDripstone(
                        param3.atY(var1.ceiling() - 1), false, param2, var4, param4.stalactiteBluntness, param4.heightScale
                    );
                    LargeDripstoneFeature.LargeDripstone var6 = makeDripstone(
                        param3.atY(var1.floor() + 1), true, param2, var4, param4.stalagmiteBluntness, param4.heightScale
                    );
                    LargeDripstoneFeature.WindOffsetter var7;
                    if (var5.isSuitableForWind(param4) && var6.isSuitableForWind(param4)) {
                        var7 = new LargeDripstoneFeature.WindOffsetter(param3.getY(), param2, param4.windSpeed);
                    } else {
                        var7 = LargeDripstoneFeature.WindOffsetter.noWind();
                    }

                    boolean var9 = var5.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(param0, var7);
                    boolean var10 = var6.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(param0, var7);
                    if (var9 && var5.getMinY() > 0) {
                        var5.placeBlocks(param0, param2, var7);
                    }

                    if (var10 && var6.getMaxY() < 55) {
                        var6.placeBlocks(param0, param2, var7);
                    }

                    return true;
                }
            } else {
                return false;
            }
        }
    }

    private static LargeDripstoneFeature.LargeDripstone makeDripstone(
        BlockPos param0, boolean param1, Random param2, int param3, UniformFloat param4, UniformFloat param5
    ) {
        return new LargeDripstoneFeature.LargeDripstone(param0, param1, param3, (double)param4.sample(param2), (double)param5.sample(param2));
    }

    static final class LargeDripstone {
        private BlockPos root;
        private final boolean pointingUp;
        private int radius;
        private final double bluntness;
        private final double scale;

        private LargeDripstone(BlockPos param0, boolean param1, int param2, double param3, double param4) {
            this.root = param0;
            this.pointingUp = param1;
            this.radius = param2;
            this.bluntness = param3;
            this.scale = param4;
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

        private boolean moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(WorldGenLevel param0, LargeDripstoneFeature.WindOffsetter param1) {
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

        private void placeBlocks(WorldGenLevel param0, Random param1, LargeDripstoneFeature.WindOffsetter param2) {
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
                                } else if (var5 && param0.getBlockState(var7).is(BlockTags.BASE_STONE_OVERWORLD)) {
                                    break;
                                }

                                var4.move(this.pointingUp ? Direction.UP : Direction.DOWN);
                            }
                        }
                    }
                }
            }

        }

        private boolean isSuitableForWind(LargeDripstoneConfiguration param0) {
            return this.radius >= param0.minRadiusForWind && this.bluntness >= (double)param0.minBluntnessForWind;
        }
    }

    static final class WindOffsetter {
        private final int originY;
        @Nullable
        private final Vec3 windSpeed;

        private WindOffsetter(int param0, Random param1, UniformFloat param2) {
            this.originY = param0;
            float var0 = param2.sample(param1);
            float var1 = Mth.randomBetween(param1, 0.0F, (float) Math.PI);
            this.windSpeed = new Vec3((double)(Mth.cos(var1) * var0), 0.0, (double)(Mth.sin(var1) * var0));
        }

        private WindOffsetter() {
            this.originY = 0;
            this.windSpeed = null;
        }

        private static LargeDripstoneFeature.WindOffsetter noWind() {
            return new LargeDripstoneFeature.WindOffsetter();
        }

        private BlockPos offset(BlockPos param0) {
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
