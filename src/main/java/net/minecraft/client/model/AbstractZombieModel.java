package net.minecraft.client.model;

import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractZombieModel<T extends Monster> extends HumanoidModel<T> {
    protected AbstractZombieModel(Function<ResourceLocation, RenderType> param0, float param1, float param2, int param3, int param4) {
        super(param0, param1, param2, param3, param4);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        super.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        boolean var0 = this.isAggressive(param0);
        float var1 = Mth.sin(this.attackTime * (float) Math.PI);
        float var2 = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * (float) Math.PI);
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightArm.yRot = -(0.1F - var1 * 0.6F);
        this.leftArm.yRot = 0.1F - var1 * 0.6F;
        float var3 = (float) -Math.PI / (var0 ? 1.5F : 2.25F);
        this.rightArm.xRot = var3;
        this.leftArm.xRot = var3;
        this.rightArm.xRot += var1 * 1.2F - var2 * 0.4F;
        this.leftArm.xRot += var1 * 1.2F - var2 * 0.4F;
        this.rightArm.zRot += Mth.cos(param3 * 0.09F) * 0.05F + 0.05F;
        this.leftArm.zRot -= Mth.cos(param3 * 0.09F) * 0.05F + 0.05F;
        this.rightArm.xRot += Mth.sin(param3 * 0.067F) * 0.05F;
        this.leftArm.xRot -= Mth.sin(param3 * 0.067F) * 0.05F;
    }

    public abstract boolean isAggressive(T var1);
}
