package net.minecraft.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Random;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GhastModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart[] tentacles = new ModelPart[9];

    public GhastModel() {
        int var0 = -16;
        this.body = new ModelPart(this, 0, 0);
        this.body.addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16);
        this.body.y += 8.0F;
        Random var1 = new Random(1660L);

        for(int var2 = 0; var2 < this.tentacles.length; ++var2) {
            this.tentacles[var2] = new ModelPart(this, 0, 0);
            float var3 = (((float)(var2 % 3) - (float)(var2 / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
            float var4 = ((float)(var2 / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
            int var5 = var1.nextInt(7) + 8;
            this.tentacles[var2].addBox(-1.0F, 0.0F, -1.0F, 2, var5, 2);
            this.tentacles[var2].x = var3;
            this.tentacles[var2].z = var4;
            this.tentacles[var2].y = 15.0F;
        }

    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        for(int var0 = 0; var0 < this.tentacles.length; ++var0) {
            this.tentacles[var0].xRot = 0.2F * Mth.sin(param3 * 0.3F + (float)var0) + 0.4F;
        }

    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0.0F, 0.6F, 0.0F);
        this.body.render(param6);

        for(ModelPart var0 : this.tentacles) {
            var0.render(param6);
        }

        RenderSystem.popMatrix();
    }
}
