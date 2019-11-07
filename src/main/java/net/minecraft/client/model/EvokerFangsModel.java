package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EvokerFangsModel<T extends Entity> extends ListModel<T> {
    private final ModelPart base = new ModelPart(this, 0, 0);
    private final ModelPart upperJaw;
    private final ModelPart lowerJaw;

    public EvokerFangsModel() {
        this.base.setPos(-5.0F, 22.0F, -5.0F);
        this.base.addBox(0.0F, 0.0F, 0.0F, 10.0F, 12.0F, 10.0F);
        this.upperJaw = new ModelPart(this, 40, 0);
        this.upperJaw.setPos(1.5F, 22.0F, -4.0F);
        this.upperJaw.addBox(0.0F, 0.0F, 0.0F, 4.0F, 14.0F, 8.0F);
        this.lowerJaw = new ModelPart(this, 40, 0);
        this.lowerJaw.setPos(-1.5F, 22.0F, 4.0F);
        this.lowerJaw.addBox(0.0F, 0.0F, 0.0F, 4.0F, 14.0F, 8.0F);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = param1 * 2.0F;
        if (var0 > 1.0F) {
            var0 = 1.0F;
        }

        var0 = 1.0F - var0 * var0 * var0;
        this.upperJaw.zRot = (float) Math.PI - var0 * 0.35F * (float) Math.PI;
        this.lowerJaw.zRot = (float) Math.PI + var0 * 0.35F * (float) Math.PI;
        this.lowerJaw.yRot = (float) Math.PI;
        float var1 = (param1 + Mth.sin(param1 * 2.7F)) * 0.6F * 12.0F;
        this.upperJaw.y = 24.0F - var1;
        this.lowerJaw.y = this.upperJaw.y;
        this.base.y = this.upperJaw.y;
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.base, this.upperJaw, this.lowerJaw);
    }
}
