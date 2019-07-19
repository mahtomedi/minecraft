package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitchModel<T extends Entity> extends VillagerModel<T> {
    private boolean holdingItem;
    private final ModelPart mole = new ModelPart(this).setTexSize(64, 128);

    public WitchModel(float param0) {
        super(param0, 64, 128);
        this.mole.setPos(0.0F, -2.0F, 0.0F);
        this.mole.texOffs(0, 0).addBox(0.0F, 3.0F, -6.75F, 1, 1, 1, -0.25F);
        this.nose.addChild(this.mole);
        this.head.removeChild(this.hat);
        this.hat = new ModelPart(this).setTexSize(64, 128);
        this.hat.setPos(-5.0F, -10.03125F, -5.0F);
        this.hat.texOffs(0, 64).addBox(0.0F, 0.0F, 0.0F, 10, 2, 10);
        this.head.addChild(this.hat);
        ModelPart var0 = new ModelPart(this).setTexSize(64, 128);
        var0.setPos(1.75F, -4.0F, 2.0F);
        var0.texOffs(0, 76).addBox(0.0F, 0.0F, 0.0F, 7, 4, 7);
        var0.xRot = -0.05235988F;
        var0.zRot = 0.02617994F;
        this.hat.addChild(var0);
        ModelPart var1 = new ModelPart(this).setTexSize(64, 128);
        var1.setPos(1.75F, -4.0F, 2.0F);
        var1.texOffs(0, 87).addBox(0.0F, 0.0F, 0.0F, 4, 4, 4);
        var1.xRot = -0.10471976F;
        var1.zRot = 0.05235988F;
        var0.addChild(var1);
        ModelPart var2 = new ModelPart(this).setTexSize(64, 128);
        var2.setPos(1.75F, -2.0F, 2.0F);
        var2.texOffs(0, 95).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.25F);
        var2.xRot = (float) (-Math.PI / 15);
        var2.zRot = 0.10471976F;
        var1.addChild(var2);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        super.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.nose.translateX = 0.0F;
        this.nose.translateY = 0.0F;
        this.nose.translateZ = 0.0F;
        float var0 = 0.01F * (float)(param0.getId() % 10);
        this.nose.xRot = Mth.sin((float)param0.tickCount * var0) * 4.5F * (float) (Math.PI / 180.0);
        this.nose.yRot = 0.0F;
        this.nose.zRot = Mth.cos((float)param0.tickCount * var0) * 2.5F * (float) (Math.PI / 180.0);
        if (this.holdingItem) {
            this.nose.xRot = -0.9F;
            this.nose.translateZ = -0.09375F;
            this.nose.translateY = 0.1875F;
        }

    }

    public ModelPart getNose() {
        return this.nose;
    }

    public void setHoldingItem(boolean param0) {
        this.holdingItem = param0;
    }
}
