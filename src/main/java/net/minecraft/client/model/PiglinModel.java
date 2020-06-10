package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PiglinModel<T extends Mob> extends PlayerModel<T> {
    public final ModelPart earRight;
    public final ModelPart earLeft;
    private final ModelPart bodyDefault;
    private final ModelPart headDefault;
    private final ModelPart leftArmDefault;
    private final ModelPart rightArmDefault;

    public PiglinModel(float param0, int param1, int param2) {
        super(param0, false);
        this.texWidth = param1;
        this.texHeight = param2;
        this.body = new ModelPart(this, 16, 16);
        this.body.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, param0);
        this.head = new ModelPart(this);
        this.head.texOffs(0, 0).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, param0);
        this.head.texOffs(31, 1).addBox(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, param0);
        this.head.texOffs(2, 4).addBox(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, param0);
        this.head.texOffs(2, 0).addBox(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, param0);
        this.earRight = new ModelPart(this);
        this.earRight.setPos(4.5F, -6.0F, 0.0F);
        this.earRight.texOffs(51, 6).addBox(0.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, param0);
        this.head.addChild(this.earRight);
        this.earLeft = new ModelPart(this);
        this.earLeft.setPos(-4.5F, -6.0F, 0.0F);
        this.earLeft.texOffs(39, 6).addBox(-1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, param0);
        this.head.addChild(this.earLeft);
        this.hat = new ModelPart(this);
        this.bodyDefault = this.body.createShallowCopy();
        this.headDefault = this.head.createShallowCopy();
        this.leftArmDefault = this.leftArm.createShallowCopy();
        this.rightArmDefault = this.leftArm.createShallowCopy();
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.body.copyFrom(this.bodyDefault);
        this.head.copyFrom(this.headDefault);
        this.leftArm.copyFrom(this.leftArmDefault);
        this.rightArm.copyFrom(this.rightArmDefault);
        super.setupAnim(param0, param1, param2, param3, param4, param5);
        float var0 = (float) (Math.PI / 6);
        float var1 = param3 * 0.1F + param1 * 0.5F;
        float var2 = 0.08F + param2 * 0.4F;
        this.earRight.zRot = (float) (-Math.PI / 6) - Mth.cos(var1 * 1.2F) * var2;
        this.earLeft.zRot = (float) (Math.PI / 6) + Mth.cos(var1) * var2;
        if (param0.getType() == EntityType.PIGLIN) {
            Piglin var3 = (Piglin)param0;
            Piglin.PiglinArmPose var4 = var3.getArmPose();
            if (var4 == Piglin.PiglinArmPose.DANCING) {
                float var5 = param3 / 60.0F;
                this.earLeft.zRot = (float) (Math.PI / 6) + (float) (Math.PI / 180.0) * Mth.sin(var5 * 30.0F) * 10.0F;
                this.earRight.zRot = (float) (-Math.PI / 6) - (float) (Math.PI / 180.0) * Mth.cos(var5 * 30.0F) * 10.0F;
                this.head.x = Mth.sin(var5 * 10.0F);
                this.head.y = Mth.sin(var5 * 40.0F) + 0.4F;
                this.rightArm.zRot = (float) (Math.PI / 180.0) * (70.0F + Mth.cos(var5 * 40.0F) * 10.0F);
                this.leftArm.zRot = this.rightArm.zRot * -1.0F;
                this.rightArm.y = Mth.sin(var5 * 40.0F) * 0.5F + 1.5F;
                this.leftArm.y = Mth.sin(var5 * 40.0F) * 0.5F + 1.5F;
                this.body.y = Mth.sin(var5 * 40.0F) * 0.35F;
            } else if (var4 == Piglin.PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON && this.attackTime == 0.0F) {
                this.holdWeaponHigh(param0);
            } else if (var4 == Piglin.PiglinArmPose.CROSSBOW_HOLD) {
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, !param0.isLeftHanded());
            } else if (var4 == Piglin.PiglinArmPose.CROSSBOW_CHARGE) {
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, param0, !param0.isLeftHanded());
            } else if (var4 == Piglin.PiglinArmPose.ADMIRING_ITEM) {
                this.head.xRot = 0.5F;
                this.head.yRot = 0.0F;
                if (param0.isLeftHanded()) {
                    this.rightArm.yRot = -0.5F;
                    this.rightArm.xRot = -0.9F;
                } else {
                    this.leftArm.yRot = 0.5F;
                    this.leftArm.xRot = -0.9F;
                }
            }
        } else if (param0.getType() == EntityType.ZOMBIFIED_PIGLIN) {
            AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, param0.isAggressive(), this.attackTime, param3);
        }

    }

    protected void setupAttackAnimation(T param0, float param1) {
        if (this.attackTime > 0.0F && param0 instanceof Piglin && ((Piglin)param0).getArmPose() == Piglin.PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON) {
            AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, param0, this.attackTime, param1);
        } else {
            super.setupAttackAnimation(param0, param1);
        }
    }

    private void holdWeaponHigh(T param0) {
        if (param0.isLeftHanded()) {
            this.leftArm.xRot = -1.8F;
        } else {
            this.rightArm.xRot = -1.8F;
        }

    }
}
