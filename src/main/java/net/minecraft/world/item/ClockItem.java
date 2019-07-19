package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClockItem extends Item {
    public ClockItem(Item.Properties param0) {
        super(param0);
        this.addProperty(new ResourceLocation("time"), new ItemPropertyFunction() {
            @OnlyIn(Dist.CLIENT)
            private double rotation;
            @OnlyIn(Dist.CLIENT)
            private double rota;
            @OnlyIn(Dist.CLIENT)
            private long lastUpdateTick;

            @OnlyIn(Dist.CLIENT)
            @Override
            public float call(ItemStack param0, @Nullable Level param1, @Nullable LivingEntity param2) {
                boolean var0 = param2 != null;
                Entity var1 = (Entity)(var0 ? param2 : param0.getFrame());
                if (param1 == null && var1 != null) {
                    param1 = var1.level;
                }

                if (param1 == null) {
                    return 0.0F;
                } else {
                    double var2;
                    if (param1.dimension.isNaturalDimension()) {
                        var2 = (double)param1.getTimeOfDay(1.0F);
                    } else {
                        var2 = Math.random();
                    }

                    var2 = this.wobble(param1, var2);
                    return (float)var2;
                }
            }

            @OnlyIn(Dist.CLIENT)
            private double wobble(Level param0, double param1) {
                if (param0.getGameTime() != this.lastUpdateTick) {
                    this.lastUpdateTick = param0.getGameTime();
                    double var0 = param1 - this.rotation;
                    var0 = Mth.positiveModulo(var0 + 0.5, 1.0) - 0.5;
                    this.rota += var0 * 0.1;
                    this.rota *= 0.9;
                    this.rotation = Mth.positiveModulo(this.rotation + this.rota, 1.0);
                }

                return this.rotation;
            }
        });
    }
}
