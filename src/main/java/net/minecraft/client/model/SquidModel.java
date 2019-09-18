package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SquidModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart[] tentacles = new ModelPart[8];

    public SquidModel() {
        int var0 = -16;
        this.body = new ModelPart(this, 0, 0);
        this.body.addBox(-6.0F, -8.0F, -6.0F, 12.0F, 16.0F, 12.0F);
        this.body.y += 8.0F;

        for(int var1 = 0; var1 < this.tentacles.length; ++var1) {
            this.tentacles[var1] = new ModelPart(this, 48, 0);
            double var2 = (double)var1 * Math.PI * 2.0 / (double)this.tentacles.length;
            float var3 = (float)Math.cos(var2) * 5.0F;
            float var4 = (float)Math.sin(var2) * 5.0F;
            this.tentacles[var1].addBox(-1.0F, 0.0F, -1.0F, 2.0F, 18.0F, 2.0F);
            this.tentacles[var1].x = var3;
            this.tentacles[var1].z = var4;
            this.tentacles[var1].y = 15.0F;
            var2 = (double)var1 * Math.PI * -2.0 / (double)this.tentacles.length + (Math.PI / 2);
            this.tentacles[var1].yRot = (float)var2;
        }

    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        for(ModelPart var0 : this.tentacles) {
            var0.xRot = param3;
        }

    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.body.render(param6);

        for(ModelPart var0 : this.tentacles) {
            var0.render(param6);
        }

    }
}
