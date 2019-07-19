package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DolphinModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart tailFin;

    public DolphinModel() {
        this.texWidth = 64;
        this.texHeight = 64;
        float var0 = 18.0F;
        float var1 = -8.0F;
        this.body = new ModelPart(this, 22, 0);
        this.body.addBox(-4.0F, -7.0F, 0.0F, 8, 7, 13);
        this.body.setPos(0.0F, 22.0F, -5.0F);
        ModelPart var2 = new ModelPart(this, 51, 0);
        var2.addBox(-0.5F, 0.0F, 8.0F, 1, 4, 5);
        var2.xRot = (float) (Math.PI / 3);
        this.body.addChild(var2);
        ModelPart var3 = new ModelPart(this, 48, 20);
        var3.mirror = true;
        var3.addBox(-0.5F, -4.0F, 0.0F, 1, 4, 7);
        var3.setPos(2.0F, -2.0F, 4.0F);
        var3.xRot = (float) (Math.PI / 3);
        var3.zRot = (float) (Math.PI * 2.0 / 3.0);
        this.body.addChild(var3);
        ModelPart var4 = new ModelPart(this, 48, 20);
        var4.addBox(-0.5F, -4.0F, 0.0F, 1, 4, 7);
        var4.setPos(-2.0F, -2.0F, 4.0F);
        var4.xRot = (float) (Math.PI / 3);
        var4.zRot = (float) (-Math.PI * 2.0 / 3.0);
        this.body.addChild(var4);
        this.tail = new ModelPart(this, 0, 19);
        this.tail.addBox(-2.0F, -2.5F, 0.0F, 4, 5, 11);
        this.tail.setPos(0.0F, -2.5F, 11.0F);
        this.tail.xRot = -0.10471976F;
        this.body.addChild(this.tail);
        this.tailFin = new ModelPart(this, 19, 20);
        this.tailFin.addBox(-5.0F, -0.5F, 0.0F, 10, 1, 6);
        this.tailFin.setPos(0.0F, 0.0F, 9.0F);
        this.tailFin.xRot = 0.0F;
        this.tail.addChild(this.tailFin);
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-4.0F, -3.0F, -3.0F, 8, 7, 6);
        this.head.setPos(0.0F, -4.0F, -3.0F);
        ModelPart var5 = new ModelPart(this, 0, 13);
        var5.addBox(-1.0F, 2.0F, -7.0F, 2, 2, 4);
        this.head.addChild(var5);
        this.body.addChild(this.head);
    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.body.render(param6);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.body.xRot = param5 * (float) (Math.PI / 180.0);
        this.body.yRot = param4 * (float) (Math.PI / 180.0);
        if (Entity.getHorizontalDistanceSqr(param0.getDeltaMovement()) > 1.0E-7) {
            this.body.xRot += -0.05F + -0.05F * Mth.cos(param3 * 0.3F);
            this.tail.xRot = -0.1F * Mth.cos(param3 * 0.3F);
            this.tailFin.xRot = -0.2F * Mth.cos(param3 * 0.3F);
        }

    }
}
