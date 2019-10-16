package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaModel<T extends AbstractChestedHorse> extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart chest1;
    private final ModelPart chest2;

    public LlamaModel(float param0) {
        this.texWidth = 128;
        this.texHeight = 64;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-2.0F, -14.0F, -10.0F, 4.0F, 4.0F, 9.0F, param0);
        this.head.setPos(0.0F, 7.0F, -6.0F);
        this.head.texOffs(0, 14).addBox(-4.0F, -16.0F, -6.0F, 8.0F, 18.0F, 6.0F, param0);
        this.head.texOffs(17, 0).addBox(-4.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, param0);
        this.head.texOffs(17, 0).addBox(1.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, param0);
        this.body = new ModelPart(this, 29, 0);
        this.body.addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F, param0);
        this.body.setPos(0.0F, 5.0F, 2.0F);
        this.chest1 = new ModelPart(this, 45, 28);
        this.chest1.addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, param0);
        this.chest1.setPos(-8.5F, 3.0F, 3.0F);
        this.chest1.yRot = (float) (Math.PI / 2);
        this.chest2 = new ModelPart(this, 45, 41);
        this.chest2.addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, param0);
        this.chest2.setPos(5.5F, 3.0F, 3.0F);
        this.chest2.yRot = (float) (Math.PI / 2);
        int var0 = 4;
        int var1 = 14;
        this.leg0 = new ModelPart(this, 29, 29);
        this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, param0);
        this.leg0.setPos(-2.5F, 10.0F, 6.0F);
        this.leg1 = new ModelPart(this, 29, 29);
        this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, param0);
        this.leg1.setPos(2.5F, 10.0F, 6.0F);
        this.leg2 = new ModelPart(this, 29, 29);
        this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, param0);
        this.leg2.setPos(-2.5F, 10.0F, -4.0F);
        this.leg3 = new ModelPart(this, 29, 29);
        this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, param0);
        this.leg3.setPos(2.5F, 10.0F, -4.0F);
        --this.leg0.x;
        ++this.leg1.x;
        this.leg0.z += 0.0F;
        this.leg1.z += 0.0F;
        --this.leg2.x;
        ++this.leg3.x;
        --this.leg2.z;
        --this.leg3.z;
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.body.xRot = (float) (Math.PI / 2);
        this.leg0.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
        this.leg1.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.leg2.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.leg3.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
        boolean var0 = !param0.isBaby() && param0.hasChest();
        this.chest1.visible = var0;
        this.chest2.visible = var0;
    }

    @Override
    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2, int param3, float param4, float param5, float param6) {
        if (this.young) {
            float var0 = 2.0F;
            param0.pushPose();
            float var1 = 0.7F;
            param0.scale(0.71428573F, 0.64935064F, 0.7936508F);
            param0.translate(0.0, 1.3125, 0.22F);
            this.head.render(param0, param1, 0.0625F, param2, param3, null, param4, param5, param6);
            param0.popPose();
            param0.pushPose();
            float var2 = 1.1F;
            param0.scale(0.625F, 0.45454544F, 0.45454544F);
            param0.translate(0.0, 2.0625, 0.0);
            this.body.render(param0, param1, 0.0625F, param2, param3, null, param4, param5, param6);
            param0.popPose();
            param0.pushPose();
            param0.scale(0.45454544F, 0.41322312F, 0.45454544F);
            param0.translate(0.0, 2.0625, 0.0);
            ImmutableList.of(this.leg0, this.leg1, this.leg2, this.leg3, this.chest1, this.chest2)
                .forEach(param7 -> param7.render(param0, param1, 0.0625F, param2, param3, null, param4, param5, param6));
            param0.popPose();
        } else {
            ImmutableList.of(this.head, this.body, this.leg0, this.leg1, this.leg2, this.leg3, this.chest1, this.chest2)
                .forEach(param7 -> param7.render(param0, param1, 0.0625F, param2, param3, null, param4, param5, param6));
        }

    }
}
