package net.minecraft.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SlimeModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart cube;
    private final ModelPart eye0;
    private final ModelPart eye1;
    private final ModelPart mouth;

    public SlimeModel(int param0) {
        if (param0 > 0) {
            this.cube = new ModelPart(this, 0, param0);
            this.cube.addBox(-3.0F, 17.0F, -3.0F, 6, 6, 6);
            this.eye0 = new ModelPart(this, 32, 0);
            this.eye0.addBox(-3.25F, 18.0F, -3.5F, 2, 2, 2);
            this.eye1 = new ModelPart(this, 32, 4);
            this.eye1.addBox(1.25F, 18.0F, -3.5F, 2, 2, 2);
            this.mouth = new ModelPart(this, 32, 8);
            this.mouth.addBox(0.0F, 21.0F, -3.5F, 1, 1, 1);
        } else {
            this.cube = new ModelPart(this, 0, param0);
            this.cube.addBox(-4.0F, 16.0F, -4.0F, 8, 8, 8);
            this.eye0 = null;
            this.eye1 = null;
            this.mouth = null;
        }

    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        GlStateManager.translatef(0.0F, 0.001F, 0.0F);
        this.cube.render(param6);
        if (this.eye0 != null) {
            this.eye0.render(param6);
            this.eye1.render(param6);
            this.mouth.render(param6);
        }

    }
}
