package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CompassItem extends Item {
    public CompassItem(Item.Properties param0) {
        super(param0);
        this.addProperty(new ResourceLocation("angle"), new ItemPropertyFunction() {
            @OnlyIn(Dist.CLIENT)
            private double rotation;
            @OnlyIn(Dist.CLIENT)
            private double rota;
            @OnlyIn(Dist.CLIENT)
            private long lastUpdateTick;

            @OnlyIn(Dist.CLIENT)
            @Override
            public float call(ItemStack param0, @Nullable Level param1, @Nullable LivingEntity param2) {
                if (param2 == null && !param0.isFramed()) {
                    return 0.0F;
                } else {
                    boolean var0 = param2 != null;
                    Entity var1 = (Entity)(var0 ? param2 : param0.getFrame());
                    if (param1 == null) {
                        param1 = var1.level;
                    }

                    double var4;
                    if (param1.dimension.isNaturalDimension()) {
                        double var2 = var0 ? (double)var1.yRot : this.getFrameRotation((ItemFrame)var1);
                        var2 = Mth.positiveModulo(var2 / 360.0, 1.0);
                        double var3 = this.getSpawnToAngle(param1, var1) / (float) (Math.PI * 2);
                        var4 = 0.5 - (var2 - 0.25 - var3);
                    } else {
                        var4 = Math.random();
                    }

                    if (var0) {
                        var4 = this.wobble(param1, var4);
                    }

                    return Mth.positiveModulo((float)var4, 1.0F);
                }
            }

            @OnlyIn(Dist.CLIENT)
            private double wobble(Level param0, double param1) {
                if (param0.getGameTime() != this.lastUpdateTick) {
                    this.lastUpdateTick = param0.getGameTime();
                    double var0 = param1 - this.rotation;
                    var0 = Mth.positiveModulo(var0 + 0.5, 1.0) - 0.5;
                    this.rota += var0 * 0.1;
                    this.rota *= 0.8;
                    this.rotation = Mth.positiveModulo(this.rotation + this.rota, 1.0);
                }

                return this.rotation;
            }

            @OnlyIn(Dist.CLIENT)
            private double getFrameRotation(ItemFrame param0) {
                return (double)Mth.wrapDegrees(180 + param0.getDirection().get2DDataValue() * 90);
            }

            @OnlyIn(Dist.CLIENT)
            private double getSpawnToAngle(LevelAccessor param0, Entity param1) {
                BlockPos var0 = param0.getSharedSpawnPos();
                return Math.atan2((double)var0.getZ() - param1.z, (double)var0.getX() - param1.x);
            }
        });
    }
}
