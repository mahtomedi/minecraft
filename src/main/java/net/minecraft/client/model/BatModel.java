package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BatModel extends EntityModel<Bat> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart rightWingTip;
    private final ModelPart leftWingTip;

    public BatModel() {
        this.texWidth = 64;
        this.texHeight = 64;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6);
        ModelPart var0 = new ModelPart(this, 24, 0);
        var0.addBox(-4.0F, -6.0F, -2.0F, 3, 4, 1);
        this.head.addChild(var0);
        ModelPart var1 = new ModelPart(this, 24, 0);
        var1.mirror = true;
        var1.addBox(1.0F, -6.0F, -2.0F, 3, 4, 1);
        this.head.addChild(var1);
        this.body = new ModelPart(this, 0, 16);
        this.body.addBox(-3.0F, 4.0F, -3.0F, 6, 12, 6);
        this.body.texOffs(0, 34).addBox(-5.0F, 16.0F, 0.0F, 10, 6, 1);
        this.rightWing = new ModelPart(this, 42, 0);
        this.rightWing.addBox(-12.0F, 1.0F, 1.5F, 10, 16, 1);
        this.rightWingTip = new ModelPart(this, 24, 16);
        this.rightWingTip.setPos(-12.0F, 1.0F, 1.5F);
        this.rightWingTip.addBox(-8.0F, 1.0F, 0.0F, 8, 12, 1);
        this.leftWing = new ModelPart(this, 42, 0);
        this.leftWing.mirror = true;
        this.leftWing.addBox(2.0F, 1.0F, 1.5F, 10, 16, 1);
        this.leftWingTip = new ModelPart(this, 24, 16);
        this.leftWingTip.mirror = true;
        this.leftWingTip.setPos(12.0F, 1.0F, 1.5F);
        this.leftWingTip.addBox(0.0F, 1.0F, 0.0F, 8, 12, 1);
        this.body.addChild(this.rightWing);
        this.body.addChild(this.leftWing);
        this.rightWing.addChild(this.rightWingTip);
        this.leftWing.addChild(this.leftWingTip);
    }

    public void render(Bat param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.head.render(param6);
        this.body.render(param6);
    }

    public void setupAnim(Bat param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        if (param0.isResting()) {
            this.head.xRot = param5 * (float) (Math.PI / 180.0);
            this.head.yRot = (float) Math.PI - param4 * (float) (Math.PI / 180.0);
            this.head.zRot = (float) Math.PI;
            this.head.setPos(0.0F, -2.0F, 0.0F);
            this.rightWing.setPos(-3.0F, 0.0F, 3.0F);
            this.leftWing.setPos(3.0F, 0.0F, 3.0F);
            this.body.xRot = (float) Math.PI;
            this.rightWing.xRot = (float) (-Math.PI / 20);
            this.rightWing.yRot = (float) (-Math.PI * 2.0 / 5.0);
            this.rightWingTip.yRot = -1.7278761F;
            this.leftWing.xRot = this.rightWing.xRot;
            this.leftWing.yRot = -this.rightWing.yRot;
            this.leftWingTip.yRot = -this.rightWingTip.yRot;
        } else {
            this.head.xRot = param5 * (float) (Math.PI / 180.0);
            this.head.yRot = param4 * (float) (Math.PI / 180.0);
            this.head.zRot = 0.0F;
            this.head.setPos(0.0F, 0.0F, 0.0F);
            this.rightWing.setPos(0.0F, 0.0F, 0.0F);
            this.leftWing.setPos(0.0F, 0.0F, 0.0F);
            this.body.xRot = (float) (Math.PI / 4) + Mth.cos(param3 * 0.1F) * 0.15F;
            this.body.yRot = 0.0F;
            this.rightWing.yRot = Mth.cos(param3 * 1.3F) * (float) Math.PI * 0.25F;
            this.leftWing.yRot = -this.rightWing.yRot;
            this.rightWingTip.yRot = this.rightWing.yRot * 0.5F;
            this.leftWingTip.yRot = -this.rightWing.yRot * 0.5F;
        }

    }
}
