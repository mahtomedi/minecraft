package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class QuadrupedModel<T extends Entity> extends AgeableListModel<T> {
    protected ModelPart head = new ModelPart(this, 0, 0);
    protected ModelPart body;
    protected ModelPart leg0;
    protected ModelPart leg1;
    protected ModelPart leg2;
    protected ModelPart leg3;

    public QuadrupedModel(int param0, float param1, boolean param2, float param3, float param4, float param5, float param6, int param7) {
        super(param2, param3, param4, param5, param6, (float)param7);
        this.head.addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, param1);
        this.head.setPos(0.0F, (float)(18 - param0), -6.0F);
        this.body = new ModelPart(this, 28, 8);
        this.body.addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F, param1);
        this.body.setPos(0.0F, (float)(17 - param0), 2.0F);
        this.leg0 = new ModelPart(this, 0, 16);
        this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4.0F, (float)param0, 4.0F, param1);
        this.leg0.setPos(-3.0F, (float)(24 - param0), 7.0F);
        this.leg1 = new ModelPart(this, 0, 16);
        this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4.0F, (float)param0, 4.0F, param1);
        this.leg1.setPos(3.0F, (float)(24 - param0), 7.0F);
        this.leg2 = new ModelPart(this, 0, 16);
        this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4.0F, (float)param0, 4.0F, param1);
        this.leg2.setPos(-3.0F, (float)(24 - param0), -5.0F);
        this.leg3 = new ModelPart(this, 0, 16);
        this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4.0F, (float)param0, 4.0F, param1);
        this.leg3.setPos(3.0F, (float)(24 - param0), -5.0F);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.leg0, this.leg1, this.leg2, this.leg3);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.body.xRot = (float) (Math.PI / 2);
        this.leg0.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
        this.leg1.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.leg2.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.leg3.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
    }
}
