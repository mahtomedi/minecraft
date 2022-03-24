package net.minecraft.client.model;

import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.animation.definitions.WardenAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WardenModel<T extends Warden> extends HierarchicalModel<T> {
    private static final float DEFAULT_ARM_X_Y = 13.0F;
    private static final float DEFAULT_ARM_Z = 1.0F;
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();
    private final ModelPart root;
    protected final ModelPart bone;
    protected final ModelPart body;
    protected final ModelPart head;
    protected final ModelPart rightTendril;
    protected final ModelPart leftTendril;
    protected final ModelPart leftLeg;
    protected final ModelPart leftArm;
    protected final ModelPart rightArm;
    protected final ModelPart rightLeg;

    public WardenModel(ModelPart param0) {
        super(RenderType::entityCutoutNoCull);
        this.root = param0;
        this.bone = param0.getChild("bone");
        this.body = this.bone.getChild("body");
        this.head = this.body.getChild("head");
        this.rightLeg = this.bone.getChild("right_leg");
        this.leftLeg = this.bone.getChild("left_leg");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightTendril = this.head.getChild("right_tendril");
        this.leftTendril = this.head.getChild("left_tendril");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        PartDefinition var2 = var1.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition var3 = var2.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0F, -13.0F, -4.0F, 18.0F, 21.0F, 11.0F), PartPose.offset(0.0F, -21.0F, 0.0F)
        );
        PartDefinition var4 = var3.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -16.0F, -5.0F, 16.0F, 16.0F, 10.0F), PartPose.offset(0.0F, -13.0F, 0.0F)
        );
        var4.addOrReplaceChild(
            "right_tendril", CubeListBuilder.create().texOffs(52, 32).addBox(-16.0F, -13.0F, 0.0F, 16.0F, 16.0F, 0.0F), PartPose.offset(-8.0F, -12.0F, 0.0F)
        );
        var4.addOrReplaceChild(
            "left_tendril", CubeListBuilder.create().texOffs(58, 0).addBox(0.0F, -13.0F, 0.0F, 16.0F, 16.0F, 0.0F), PartPose.offset(8.0F, -12.0F, 0.0F)
        );
        var3.addOrReplaceChild(
            "right_arm", CubeListBuilder.create().texOffs(44, 50).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 28.0F, 8.0F), PartPose.offset(-13.0F, -13.0F, 1.0F)
        );
        var3.addOrReplaceChild(
            "left_arm", CubeListBuilder.create().texOffs(0, 58).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 28.0F, 8.0F), PartPose.offset(13.0F, -13.0F, 1.0F)
        );
        var2.addOrReplaceChild(
            "right_leg", CubeListBuilder.create().texOffs(76, 48).addBox(-3.1F, 0.0F, -3.0F, 6.0F, 13.0F, 6.0F), PartPose.offset(-5.9F, -13.0F, 0.0F)
        );
        var2.addOrReplaceChild(
            "left_leg", CubeListBuilder.create().texOffs(76, 76).addBox(-2.9F, 0.0F, -3.0F, 6.0F, 13.0F, 6.0F), PartPose.offset(5.9F, -13.0F, 0.0F)
        );
        return LayerDefinition.create(var0, 128, 128);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float var0 = param3 - (float)param0.tickCount;
        float var1 = Math.min(0.5F, 3.0F * param2);
        float var2 = param3 * 0.1F;
        float var3 = param1 * 0.8662F;
        float var4 = Mth.cos(var3);
        float var5 = Mth.sin(var3);
        float var6 = Mth.cos(var2);
        float var7 = Mth.sin(var2);
        float var8 = Math.min(0.35F, var1);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.head.zRot += 0.3F * var5 * var1;
        this.head.zRot += 0.06F * var6;
        this.head.xRot += 1.2F * Mth.cos(var3 + (float) (Math.PI / 2)) * var8;
        this.head.xRot += 0.06F * var7;
        this.body.zRot = 0.1F * var5 * var1;
        this.body.zRot += 0.025F * var7;
        this.body.xRot = 1.0F * var4 * var8;
        this.body.xRot += 0.025F * var6;
        this.leftLeg.xRot = 1.0F * var4 * var1;
        this.rightLeg.xRot = 1.0F * Mth.cos(var3 + (float) Math.PI) * var1;
        this.leftArm.xRot = -(0.8F * var4 * var1);
        this.leftArm.zRot = 0.0F;
        this.rightArm.xRot = -(0.8F * var5 * var1);
        this.rightArm.zRot = 0.0F;
        this.leftArm.yRot = 0.0F;
        this.leftArm.z = 1.0F;
        this.leftArm.x = 13.0F;
        this.leftArm.y = -13.0F;
        this.rightArm.yRot = 0.0F;
        this.rightArm.z = 1.0F;
        this.rightArm.x = -13.0F;
        this.rightArm.y = -13.0F;
        float var9 = param0.getEarAnimation(var0) * (float)(Math.cos((double)param3 * 2.25) * Math.PI * 0.1F);
        this.leftTendril.xRot = var9;
        this.rightTendril.xRot = -var9;
        long var10 = Util.getMillis();
        this.animate(param0.attackAnimationState, WardenAnimation.WARDEN_ATTACK, var10);
        this.animate(param0.diggingAnimationState, WardenAnimation.WARDEN_DIG, var10);
        this.animate(param0.emergeAnimationState, WardenAnimation.WARDEN_EMERGE, var10);
        this.animate(param0.roarAnimationState, WardenAnimation.WARDEN_ROAR, var10);
        this.animate(param0.sniffAnimationState, WardenAnimation.WARDEN_SNIFF, var10);
    }

    public void animate(AnimationState param0, AnimationDefinition param1, long param2) {
        param0.ifStarted(param2x -> KeyframeAnimations.animate(this, param1, param2 - param2x.startTime(), 1.0F, ANIMATION_VECTOR_CACHE));
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}