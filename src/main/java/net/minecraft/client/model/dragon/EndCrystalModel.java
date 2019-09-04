package net.minecraft.client.model.dragon;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EndCrystalModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart cube;
    private final ModelPart glass = new ModelPart(this, "glass");
    private final ModelPart base;

    public EndCrystalModel(float param0, boolean param1) {
        this.glass.texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
        this.cube = new ModelPart(this, "cube");
        this.cube.texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
        if (param1) {
            this.base = new ModelPart(this, "base");
            this.base.texOffs(0, 16).addBox(-6.0F, 0.0F, -6.0F, 12, 4, 12);
        } else {
            this.base = null;
        }

    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        RenderSystem.pushMatrix();
        RenderSystem.scalef(2.0F, 2.0F, 2.0F);
        RenderSystem.translatef(0.0F, -0.5F, 0.0F);
        if (this.base != null) {
            this.base.render(param6);
        }

        RenderSystem.rotatef(param2, 0.0F, 1.0F, 0.0F);
        RenderSystem.translatef(0.0F, 0.8F + param3, 0.0F);
        RenderSystem.rotatef(60.0F, 0.7071F, 0.0F, 0.7071F);
        this.glass.render(param6);
        float var0 = 0.875F;
        RenderSystem.scalef(0.875F, 0.875F, 0.875F);
        RenderSystem.rotatef(60.0F, 0.7071F, 0.0F, 0.7071F);
        RenderSystem.rotatef(param2, 0.0F, 1.0F, 0.0F);
        this.glass.render(param6);
        RenderSystem.scalef(0.875F, 0.875F, 0.875F);
        RenderSystem.rotatef(60.0F, 0.7071F, 0.0F, 0.7071F);
        RenderSystem.rotatef(param2, 0.0F, 1.0F, 0.0F);
        this.cube.render(param6);
        RenderSystem.popMatrix();
    }
}
