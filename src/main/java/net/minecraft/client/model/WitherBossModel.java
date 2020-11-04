package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherBossModel<T extends WitherBoss> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart centerHead;
    private final ModelPart rightHead;
    private final ModelPart leftHead;
    private final ModelPart ribcage;
    private final ModelPart tail;

    public WitherBossModel(ModelPart param0) {
        this.root = param0;
        this.ribcage = param0.getChild("ribcage");
        this.tail = param0.getChild("tail");
        this.centerHead = param0.getChild("center_head");
        this.rightHead = param0.getChild("right_head");
        this.leftHead = param0.getChild("left_head");
    }

    public static LayerDefinition createBodyLayer(CubeDeformation param0) {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild("shoulders", CubeListBuilder.create().texOffs(0, 16).addBox(-10.0F, 3.9F, -0.5F, 20.0F, 3.0F, 3.0F, param0), PartPose.ZERO);
        float var2 = 0.20420352F;
        var1.addOrReplaceChild(
            "ribcage",
            CubeListBuilder.create()
                .texOffs(0, 22)
                .addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F, param0)
                .texOffs(24, 22)
                .addBox(-4.0F, 1.5F, 0.5F, 11.0F, 2.0F, 2.0F, param0)
                .texOffs(24, 22)
                .addBox(-4.0F, 4.0F, 0.5F, 11.0F, 2.0F, 2.0F, param0)
                .texOffs(24, 22)
                .addBox(-4.0F, 6.5F, 0.5F, 11.0F, 2.0F, 2.0F, param0),
            PartPose.offsetAndRotation(-2.0F, 6.9F, -0.5F, 0.20420352F, 0.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "tail",
            CubeListBuilder.create().texOffs(12, 22).addBox(0.0F, 0.0F, 0.0F, 3.0F, 6.0F, 3.0F, param0),
            PartPose.offsetAndRotation(-2.0F, 6.9F + Mth.cos(0.20420352F) * 10.0F, -0.5F + Mth.sin(0.20420352F) * 10.0F, 0.83252203F, 0.0F, 0.0F)
        );
        var1.addOrReplaceChild("center_head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, param0), PartPose.ZERO);
        CubeListBuilder var3 = CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, param0);
        var1.addOrReplaceChild("right_head", var3, PartPose.offset(-8.0F, 4.0F, 0.0F));
        var1.addOrReplaceChild("left_head", var3, PartPose.offset(10.0F, 4.0F, 0.0F));
        return LayerDefinition.create(var0, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = Mth.cos(param3 * 0.1F);
        this.ribcage.xRot = (0.065F + 0.05F * var0) * (float) Math.PI;
        this.tail.setPos(-2.0F, 6.9F + Mth.cos(this.ribcage.xRot) * 10.0F, -0.5F + Mth.sin(this.ribcage.xRot) * 10.0F);
        this.tail.xRot = (0.265F + 0.1F * var0) * (float) Math.PI;
        this.centerHead.yRot = param4 * (float) (Math.PI / 180.0);
        this.centerHead.xRot = param5 * (float) (Math.PI / 180.0);
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        setupHeadRotation(param0, this.rightHead, 0);
        setupHeadRotation(param0, this.leftHead, 1);
    }

    private static <T extends WitherBoss> void setupHeadRotation(T param0, ModelPart param1, int param2) {
        param1.yRot = (param0.getHeadYRot(param2) - param0.yBodyRot) * (float) (Math.PI / 180.0);
        param1.xRot = param0.getHeadXRot(param2) * (float) (Math.PI / 180.0);
    }
}
