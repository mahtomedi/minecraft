package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChestedHorseModel<T extends AbstractChestedHorse> extends HorseModel<T> {
    private final ModelPart boxL = new ModelPart(this, 26, 21);
    private final ModelPart boxR;

    public ChestedHorseModel(float param0) {
        super(param0);
        this.boxL.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 3.0F);
        this.boxR = new ModelPart(this, 26, 21);
        this.boxR.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 3.0F);
        this.boxL.yRot = (float) (-Math.PI / 2);
        this.boxR.yRot = (float) (Math.PI / 2);
        this.boxL.setPos(6.0F, -8.0F, 0.0F);
        this.boxR.setPos(-6.0F, -8.0F, 0.0F);
        this.body.addChild(this.boxL);
        this.body.addChild(this.boxR);
    }

    @Override
    protected void addEarModels(ModelPart param0) {
        ModelPart var0 = new ModelPart(this, 0, 12);
        var0.addBox(-1.0F, -7.0F, 0.0F, 2.0F, 7.0F, 1.0F);
        var0.setPos(1.25F, -10.0F, 4.0F);
        ModelPart var1 = new ModelPart(this, 0, 12);
        var1.addBox(-1.0F, -7.0F, 0.0F, 2.0F, 7.0F, 1.0F);
        var1.setPos(-1.25F, -10.0F, 4.0F);
        var0.xRot = (float) (Math.PI / 12);
        var0.zRot = (float) (Math.PI / 12);
        var1.xRot = (float) (Math.PI / 12);
        var1.zRot = (float) (-Math.PI / 12);
        param0.addChild(var0);
        param0.addChild(var1);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        super.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        if (param0.hasChest()) {
            this.boxL.visible = true;
            this.boxR.visible = true;
        } else {
            this.boxL.visible = false;
            this.boxR.visible = false;
        }

    }
}
