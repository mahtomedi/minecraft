package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerModel<T extends Entity> extends ListModel<T> implements HeadedModel, VillagerHeadModel {
    protected ModelPart head;
    protected ModelPart hat;
    protected final ModelPart hatRim;
    protected final ModelPart body;
    protected final ModelPart jacket;
    protected final ModelPart arms;
    protected final ModelPart leg0;
    protected final ModelPart leg1;
    protected final ModelPart nose;

    public VillagerModel(float param0) {
        this(param0, 64, 64);
    }

    public VillagerModel(float param0, int param1, int param2) {
        float var0 = 0.5F;
        this.head = new ModelPart(this).setTexSize(param1, param2);
        this.head.setPos(0.0F, 0.0F, 0.0F);
        this.head.texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, param0);
        this.hat = new ModelPart(this).setTexSize(param1, param2);
        this.hat.setPos(0.0F, 0.0F, 0.0F);
        this.hat.texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, param0 + 0.5F);
        this.head.addChild(this.hat);
        this.hatRim = new ModelPart(this).setTexSize(param1, param2);
        this.hatRim.setPos(0.0F, 0.0F, 0.0F);
        this.hatRim.texOffs(30, 47).addBox(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F, param0);
        this.hatRim.xRot = (float) (-Math.PI / 2);
        this.hat.addChild(this.hatRim);
        this.nose = new ModelPart(this).setTexSize(param1, param2);
        this.nose.setPos(0.0F, -2.0F, 0.0F);
        this.nose.texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, param0);
        this.head.addChild(this.nose);
        this.body = new ModelPart(this).setTexSize(param1, param2);
        this.body.setPos(0.0F, 0.0F, 0.0F);
        this.body.texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, param0);
        this.jacket = new ModelPart(this).setTexSize(param1, param2);
        this.jacket.setPos(0.0F, 0.0F, 0.0F);
        this.jacket.texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, param0 + 0.5F);
        this.body.addChild(this.jacket);
        this.arms = new ModelPart(this).setTexSize(param1, param2);
        this.arms.setPos(0.0F, 2.0F, 0.0F);
        this.arms.texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, param0);
        this.arms.texOffs(44, 22).addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, param0, true);
        this.arms.texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F, param0);
        this.leg0 = new ModelPart(this, 0, 22).setTexSize(param1, param2);
        this.leg0.setPos(-2.0F, 12.0F, 0.0F);
        this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0);
        this.leg1 = new ModelPart(this, 0, 22).setTexSize(param1, param2);
        this.leg1.mirror = true;
        this.leg1.setPos(2.0F, 12.0F, 0.0F);
        this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, param0);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.head, this.body, this.leg0, this.leg1, this.arms);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        boolean var0 = false;
        if (param0 instanceof AbstractVillager) {
            var0 = ((AbstractVillager)param0).getUnhappyCounter() > 0;
        }

        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        if (var0) {
            this.head.zRot = 0.3F * Mth.sin(0.45F * param3);
            this.head.xRot = 0.4F;
        } else {
            this.head.zRot = 0.0F;
        }

        this.arms.y = 3.0F;
        this.arms.z = -1.0F;
        this.arms.xRot = -0.75F;
        this.leg0.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2 * 0.5F;
        this.leg1.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2 * 0.5F;
        this.leg0.yRot = 0.0F;
        this.leg1.yRot = 0.0F;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public void hatVisible(boolean param0) {
        this.head.visible = param0;
        this.hat.visible = param0;
        this.hatRim.visible = param0;
    }
}
