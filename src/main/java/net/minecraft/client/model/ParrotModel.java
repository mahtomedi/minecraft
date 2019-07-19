package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParrotModel extends EntityModel<Parrot> {
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;
    private final ModelPart head;
    private final ModelPart head2;
    private final ModelPart beak1;
    private final ModelPart beak2;
    private final ModelPart feather;
    private final ModelPart legLeft;
    private final ModelPart legRight;

    public ParrotModel() {
        this.texWidth = 32;
        this.texHeight = 32;
        this.body = new ModelPart(this, 2, 8);
        this.body.addBox(-1.5F, 0.0F, -1.5F, 3, 6, 3);
        this.body.setPos(0.0F, 16.5F, -3.0F);
        this.tail = new ModelPart(this, 22, 1);
        this.tail.addBox(-1.5F, -1.0F, -1.0F, 3, 4, 1);
        this.tail.setPos(0.0F, 21.07F, 1.16F);
        this.wingLeft = new ModelPart(this, 19, 8);
        this.wingLeft.addBox(-0.5F, 0.0F, -1.5F, 1, 5, 3);
        this.wingLeft.setPos(1.5F, 16.94F, -2.76F);
        this.wingRight = new ModelPart(this, 19, 8);
        this.wingRight.addBox(-0.5F, 0.0F, -1.5F, 1, 5, 3);
        this.wingRight.setPos(-1.5F, 16.94F, -2.76F);
        this.head = new ModelPart(this, 2, 2);
        this.head.addBox(-1.0F, -1.5F, -1.0F, 2, 3, 2);
        this.head.setPos(0.0F, 15.69F, -2.76F);
        this.head2 = new ModelPart(this, 10, 0);
        this.head2.addBox(-1.0F, -0.5F, -2.0F, 2, 1, 4);
        this.head2.setPos(0.0F, -2.0F, -1.0F);
        this.head.addChild(this.head2);
        this.beak1 = new ModelPart(this, 11, 7);
        this.beak1.addBox(-0.5F, -1.0F, -0.5F, 1, 2, 1);
        this.beak1.setPos(0.0F, -0.5F, -1.5F);
        this.head.addChild(this.beak1);
        this.beak2 = new ModelPart(this, 16, 7);
        this.beak2.addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1);
        this.beak2.setPos(0.0F, -1.75F, -2.45F);
        this.head.addChild(this.beak2);
        this.feather = new ModelPart(this, 2, 18);
        this.feather.addBox(0.0F, -4.0F, -2.0F, 0, 5, 4);
        this.feather.setPos(0.0F, -2.15F, 0.15F);
        this.head.addChild(this.feather);
        this.legLeft = new ModelPart(this, 14, 18);
        this.legLeft.addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1);
        this.legLeft.setPos(1.0F, 22.0F, -1.05F);
        this.legRight = new ModelPart(this, 14, 18);
        this.legRight.addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1);
        this.legRight.setPos(-1.0F, 22.0F, -1.05F);
    }

    public void render(Parrot param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.render(param6);
    }

    public void setupAnim(Parrot param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(getState(param0), param0.tickCount, param1, param2, param3, param4, param5);
    }

    public void prepareMobModel(Parrot param0, float param1, float param2, float param3) {
        this.prepare(getState(param0));
    }

    public void renderOnShoulder(float param0, float param1, float param2, float param3, float param4, int param5) {
        this.prepare(ParrotModel.State.ON_SHOULDER);
        this.setupAnim(ParrotModel.State.ON_SHOULDER, param5, param0, param1, 0.0F, param2, param3);
        this.render(param4);
    }

    private void render(float param0) {
        this.body.render(param0);
        this.wingLeft.render(param0);
        this.wingRight.render(param0);
        this.tail.render(param0);
        this.head.render(param0);
        this.legLeft.render(param0);
        this.legRight.render(param0);
    }

    private void setupAnim(ParrotModel.State param0, int param1, float param2, float param3, float param4, float param5, float param6) {
        this.head.xRot = param6 * (float) (Math.PI / 180.0);
        this.head.yRot = param5 * (float) (Math.PI / 180.0);
        this.head.zRot = 0.0F;
        this.head.x = 0.0F;
        this.body.x = 0.0F;
        this.tail.x = 0.0F;
        this.wingRight.x = -1.5F;
        this.wingLeft.x = 1.5F;
        switch(param0) {
            case SITTING:
                break;
            case PARTY:
                float var0 = Mth.cos((float)param1);
                float var1 = Mth.sin((float)param1);
                this.head.x = var0;
                this.head.y = 15.69F + var1;
                this.head.xRot = 0.0F;
                this.head.yRot = 0.0F;
                this.head.zRot = Mth.sin((float)param1) * 0.4F;
                this.body.x = var0;
                this.body.y = 16.5F + var1;
                this.wingLeft.zRot = -0.0873F - param4;
                this.wingLeft.x = 1.5F + var0;
                this.wingLeft.y = 16.94F + var1;
                this.wingRight.zRot = 0.0873F + param4;
                this.wingRight.x = -1.5F + var0;
                this.wingRight.y = 16.94F + var1;
                this.tail.x = var0;
                this.tail.y = 21.07F + var1;
                break;
            case STANDING:
                this.legLeft.xRot += Mth.cos(param2 * 0.6662F) * 1.4F * param3;
                this.legRight.xRot += Mth.cos(param2 * 0.6662F + (float) Math.PI) * 1.4F * param3;
            case FLYING:
            case ON_SHOULDER:
            default:
                float var2 = param4 * 0.3F;
                this.head.y = 15.69F + var2;
                this.tail.xRot = 1.015F + Mth.cos(param2 * 0.6662F) * 0.3F * param3;
                this.tail.y = 21.07F + var2;
                this.body.y = 16.5F + var2;
                this.wingLeft.zRot = -0.0873F - param4;
                this.wingLeft.y = 16.94F + var2;
                this.wingRight.zRot = 0.0873F + param4;
                this.wingRight.y = 16.94F + var2;
                this.legLeft.y = 22.0F + var2;
                this.legRight.y = 22.0F + var2;
        }

    }

    private void prepare(ParrotModel.State param0) {
        this.feather.xRot = -0.2214F;
        this.body.xRot = 0.4937F;
        this.wingLeft.xRot = -0.6981F;
        this.wingLeft.yRot = (float) -Math.PI;
        this.wingRight.xRot = -0.6981F;
        this.wingRight.yRot = (float) -Math.PI;
        this.legLeft.xRot = -0.0299F;
        this.legRight.xRot = -0.0299F;
        this.legLeft.y = 22.0F;
        this.legRight.y = 22.0F;
        this.legLeft.zRot = 0.0F;
        this.legRight.zRot = 0.0F;
        switch(param0) {
            case SITTING:
                float var0 = 1.9F;
                this.head.y = 17.59F;
                this.tail.xRot = 1.5388988F;
                this.tail.y = 22.97F;
                this.body.y = 18.4F;
                this.wingLeft.zRot = -0.0873F;
                this.wingLeft.y = 18.84F;
                this.wingRight.zRot = 0.0873F;
                this.wingRight.y = 18.84F;
                ++this.legLeft.y;
                ++this.legRight.y;
                ++this.legLeft.xRot;
                ++this.legRight.xRot;
                break;
            case PARTY:
                this.legLeft.zRot = (float) (-Math.PI / 9);
                this.legRight.zRot = (float) (Math.PI / 9);
            case STANDING:
            case ON_SHOULDER:
            default:
                break;
            case FLYING:
                this.legLeft.xRot += (float) (Math.PI * 2.0 / 9.0);
                this.legRight.xRot += (float) (Math.PI * 2.0 / 9.0);
        }

    }

    private static ParrotModel.State getState(Parrot param0) {
        if (param0.isPartyParrot()) {
            return ParrotModel.State.PARTY;
        } else if (param0.isSitting()) {
            return ParrotModel.State.SITTING;
        } else {
            return param0.isFlying() ? ParrotModel.State.FLYING : ParrotModel.State.STANDING;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum State {
        FLYING,
        STANDING,
        SITTING,
        PARTY,
        ON_SHOULDER;
    }
}
