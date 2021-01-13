package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DolphinModel<T extends Entity> extends ListModel<T> {
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart tailFin;

    public DolphinModel() {
        this.texWidth = 64;
        this.texHeight = 64;
        float var0 = 18.0F;
        float var1 = -8.0F;
        this.body = new ModelPart(this, 22, 0);
        this.body.addBox(-4.0F, -7.0F, 0.0F, 8.0F, 7.0F, 13.0F);
        this.body.setPos(0.0F, 22.0F, -5.0F);
        ModelPart var2 = new ModelPart(this, 51, 0);
        var2.addBox(-0.5F, 0.0F, 8.0F, 1.0F, 4.0F, 5.0F);
        var2.xRot = (float) (Math.PI / 3);
        this.body.addChild(var2);
        ModelPart var3 = new ModelPart(this, 48, 20);
        var3.mirror = true;
        var3.addBox(-0.5F, -4.0F, 0.0F, 1.0F, 4.0F, 7.0F);
        var3.setPos(2.0F, -2.0F, 4.0F);
        var3.xRot = (float) (Math.PI / 3);
        var3.zRot = (float) (Math.PI * 2.0 / 3.0);
        this.body.addChild(var3);
        ModelPart var4 = new ModelPart(this, 48, 20);
        var4.addBox(-0.5F, -4.0F, 0.0F, 1.0F, 4.0F, 7.0F);
        var4.setPos(-2.0F, -2.0F, 4.0F);
        var4.xRot = (float) (Math.PI / 3);
        var4.zRot = (float) (-Math.PI * 2.0 / 3.0);
        this.body.addChild(var4);
        this.tail = new ModelPart(this, 0, 19);
        this.tail.addBox(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 11.0F);
        this.tail.setPos(0.0F, -2.5F, 11.0F);
        this.tail.xRot = -0.10471976F;
        this.body.addChild(this.tail);
        this.tailFin = new ModelPart(this, 19, 20);
        this.tailFin.addBox(-5.0F, -0.5F, 0.0F, 10.0F, 1.0F, 6.0F);
        this.tailFin.setPos(0.0F, 0.0F, 9.0F);
        this.tailFin.xRot = 0.0F;
        this.tail.addChild(this.tailFin);
        ModelPart var5 = new ModelPart(this, 0, 0);
        var5.addBox(-4.0F, -3.0F, -3.0F, 8.0F, 7.0F, 6.0F);
        var5.setPos(0.0F, -4.0F, -3.0F);
        ModelPart var6 = new ModelPart(this, 0, 13);
        var6.addBox(-1.0F, 2.0F, -7.0F, 2.0F, 2.0F, 4.0F);
        var5.addChild(var6);
        this.body.addChild(var5);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.body);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.body.xRot = param5 * (float) (Math.PI / 180.0);
        this.body.yRot = param4 * (float) (Math.PI / 180.0);
        if (Entity.getHorizontalDistanceSqr(param0.getDeltaMovement()) > 1.0E-7) {
            this.body.xRot += -0.05F + -0.05F * Mth.cos(param3 * 0.3F);
            this.tail.xRot = -0.1F * Mth.cos(param3 * 0.3F);
            this.tailFin.xRot = -0.2F * Mth.cos(param3 * 0.3F);
        }

    }
}
