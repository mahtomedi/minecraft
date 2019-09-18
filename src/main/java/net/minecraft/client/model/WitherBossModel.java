package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherBossModel<T extends WitherBoss> extends EntityModel<T> {
    private final ModelPart[] upperBodyParts;
    private final ModelPart[] heads;

    public WitherBossModel(float param0) {
        this.texWidth = 64;
        this.texHeight = 64;
        this.upperBodyParts = new ModelPart[3];
        this.upperBodyParts[0] = new ModelPart(this, 0, 16);
        this.upperBodyParts[0].addBox(-10.0F, 3.9F, -0.5F, 20.0F, 3.0F, 3.0F, param0);
        this.upperBodyParts[1] = new ModelPart(this).setTexSize(this.texWidth, this.texHeight);
        this.upperBodyParts[1].setPos(-2.0F, 6.9F, -0.5F);
        this.upperBodyParts[1].texOffs(0, 22).addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F, param0);
        this.upperBodyParts[1].texOffs(24, 22).addBox(-4.0F, 1.5F, 0.5F, 11.0F, 2.0F, 2.0F, param0);
        this.upperBodyParts[1].texOffs(24, 22).addBox(-4.0F, 4.0F, 0.5F, 11.0F, 2.0F, 2.0F, param0);
        this.upperBodyParts[1].texOffs(24, 22).addBox(-4.0F, 6.5F, 0.5F, 11.0F, 2.0F, 2.0F, param0);
        this.upperBodyParts[2] = new ModelPart(this, 12, 22);
        this.upperBodyParts[2].addBox(0.0F, 0.0F, 0.0F, 3.0F, 6.0F, 3.0F, param0);
        this.heads = new ModelPart[3];
        this.heads[0] = new ModelPart(this, 0, 0);
        this.heads[0].addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, param0);
        this.heads[1] = new ModelPart(this, 32, 0);
        this.heads[1].addBox(-4.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, param0);
        this.heads[1].x = -8.0F;
        this.heads[1].y = 4.0F;
        this.heads[2] = new ModelPart(this, 32, 0);
        this.heads[2].addBox(-4.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, param0);
        this.heads[2].x = 10.0F;
        this.heads[2].y = 4.0F;
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);

        for(ModelPart var0 : this.heads) {
            var0.render(param6);
        }

        for(ModelPart var1 : this.upperBodyParts) {
            var1.render(param6);
        }

    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        float var0 = Mth.cos(param3 * 0.1F);
        this.upperBodyParts[1].xRot = (0.065F + 0.05F * var0) * (float) Math.PI;
        this.upperBodyParts[2].setPos(-2.0F, 6.9F + Mth.cos(this.upperBodyParts[1].xRot) * 10.0F, -0.5F + Mth.sin(this.upperBodyParts[1].xRot) * 10.0F);
        this.upperBodyParts[2].xRot = (0.265F + 0.1F * var0) * (float) Math.PI;
        this.heads[0].yRot = param4 * (float) (Math.PI / 180.0);
        this.heads[0].xRot = param5 * (float) (Math.PI / 180.0);
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        for(int var0 = 1; var0 < 3; ++var0) {
            this.heads[var0].yRot = (param0.getHeadYRot(var0 - 1) - param0.yBodyRot) * (float) (Math.PI / 180.0);
            this.heads[var0].xRot = param0.getHeadXRot(var0 - 1) * (float) (Math.PI / 180.0);
        }

    }
}
