package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LeashKnotModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart knot;

    public LeashKnotModel() {
        this(0, 0, 32, 32);
    }

    public LeashKnotModel(int param0, int param1, int param2, int param3) {
        this.texWidth = param2;
        this.texHeight = param3;
        this.knot = new ModelPart(this, param0, param1);
        this.knot.addBox(-3.0F, -6.0F, -3.0F, 6.0F, 8.0F, 6.0F, 0.0F);
        this.knot.setPos(0.0F, 0.0F, 0.0F);
    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.knot.render(param6);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        super.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.knot.yRot = param4 * (float) (Math.PI / 180.0);
        this.knot.xRot = param5 * (float) (Math.PI / 180.0);
    }
}
