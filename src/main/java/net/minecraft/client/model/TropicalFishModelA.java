package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TropicalFishModelA<T extends Entity> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart leftFin;
    private final ModelPart rightFin;
    private final ModelPart topFin;

    public TropicalFishModelA() {
        this(0.0F);
    }

    public TropicalFishModelA(float param0) {
        this.texWidth = 32;
        this.texHeight = 32;
        int var0 = 22;
        this.body = new ModelPart(this, 0, 0);
        this.body.addBox(-1.0F, -1.5F, -3.0F, 2, 3, 6, param0);
        this.body.setPos(0.0F, 22.0F, 0.0F);
        this.tail = new ModelPart(this, 22, -6);
        this.tail.addBox(0.0F, -1.5F, 0.0F, 0, 3, 6, param0);
        this.tail.setPos(0.0F, 22.0F, 3.0F);
        this.leftFin = new ModelPart(this, 2, 16);
        this.leftFin.addBox(-2.0F, -1.0F, 0.0F, 2, 2, 0, param0);
        this.leftFin.setPos(-1.0F, 22.5F, 0.0F);
        this.leftFin.yRot = (float) (Math.PI / 4);
        this.rightFin = new ModelPart(this, 2, 12);
        this.rightFin.addBox(0.0F, -1.0F, 0.0F, 2, 2, 0, param0);
        this.rightFin.setPos(1.0F, 22.5F, 0.0F);
        this.rightFin.yRot = (float) (-Math.PI / 4);
        this.topFin = new ModelPart(this, 10, -5);
        this.topFin.addBox(0.0F, -3.0F, 0.0F, 0, 3, 6, param0);
        this.topFin.setPos(0.0F, 20.5F, -3.0F);
    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.body.render(param6);
        this.tail.render(param6);
        this.leftFin.render(param6);
        this.rightFin.render(param6);
        this.topFin.render(param6);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        float var0 = 1.0F;
        if (!param0.isInWater()) {
            var0 = 1.5F;
        }

        this.tail.yRot = -var0 * 0.45F * Mth.sin(0.6F * param3);
    }
}
