package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlazeModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart[] upperBodyParts = new ModelPart[12];
    private final ModelPart head;

    public BlazeModel() {
        for(int var0 = 0; var0 < this.upperBodyParts.length; ++var0) {
            this.upperBodyParts[var0] = new ModelPart(this, 0, 16);
            this.upperBodyParts[var0].addBox(0.0F, 0.0F, 0.0F, 2.0F, 8.0F, 2.0F);
        }

        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.head.render(param6);

        for(ModelPart var0 : this.upperBodyParts) {
            var0.render(param6);
        }

    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        float var0 = param3 * (float) Math.PI * -0.1F;

        for(int var1 = 0; var1 < 4; ++var1) {
            this.upperBodyParts[var1].y = -2.0F + Mth.cos(((float)(var1 * 2) + param3) * 0.25F);
            this.upperBodyParts[var1].x = Mth.cos(var0) * 9.0F;
            this.upperBodyParts[var1].z = Mth.sin(var0) * 9.0F;
            ++var0;
        }

        var0 = (float) (Math.PI / 4) + param3 * (float) Math.PI * 0.03F;

        for(int var2 = 4; var2 < 8; ++var2) {
            this.upperBodyParts[var2].y = 2.0F + Mth.cos(((float)(var2 * 2) + param3) * 0.25F);
            this.upperBodyParts[var2].x = Mth.cos(var0) * 7.0F;
            this.upperBodyParts[var2].z = Mth.sin(var0) * 7.0F;
            ++var0;
        }

        var0 = 0.47123894F + param3 * (float) Math.PI * -0.05F;

        for(int var3 = 8; var3 < 12; ++var3) {
            this.upperBodyParts[var3].y = 11.0F + Mth.cos(((float)var3 * 1.5F + param3) * 0.5F);
            this.upperBodyParts[var3].x = Mth.cos(var0) * 5.0F;
            this.upperBodyParts[var3].z = Mth.sin(var0) * 5.0F;
            ++var0;
        }

        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
    }
}
