package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerBulletModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart main;

    public ShulkerBulletModel() {
        this.texWidth = 64;
        this.texHeight = 32;
        this.main = new ModelPart(this);
        this.main.texOffs(0, 0).addBox(-4.0F, -4.0F, -1.0F, 8, 8, 2, 0.0F);
        this.main.texOffs(0, 10).addBox(-1.0F, -4.0F, -4.0F, 2, 8, 8, 0.0F);
        this.main.texOffs(20, 0).addBox(-4.0F, -1.0F, -4.0F, 8, 2, 8, 0.0F);
        this.main.setPos(0.0F, 0.0F, 0.0F);
    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.main.render(param6);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        super.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.main.yRot = param4 * (float) (Math.PI / 180.0);
        this.main.xRot = param5 * (float) (Math.PI / 180.0);
    }
}
