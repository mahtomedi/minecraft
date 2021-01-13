package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TropicalFishModelB<T extends Entity> extends ColorableListModel<T> {
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart leftFin;
    private final ModelPart rightFin;
    private final ModelPart topFin;
    private final ModelPart bottomFin;

    public TropicalFishModelB(float param0) {
        this.texWidth = 32;
        this.texHeight = 32;
        int var0 = 19;
        this.body = new ModelPart(this, 0, 20);
        this.body.addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, param0);
        this.body.setPos(0.0F, 19.0F, 0.0F);
        this.tail = new ModelPart(this, 21, 16);
        this.tail.addBox(0.0F, -3.0F, 0.0F, 0.0F, 6.0F, 5.0F, param0);
        this.tail.setPos(0.0F, 19.0F, 3.0F);
        this.leftFin = new ModelPart(this, 2, 16);
        this.leftFin.addBox(-2.0F, 0.0F, 0.0F, 2.0F, 2.0F, 0.0F, param0);
        this.leftFin.setPos(-1.0F, 20.0F, 0.0F);
        this.leftFin.yRot = (float) (Math.PI / 4);
        this.rightFin = new ModelPart(this, 2, 12);
        this.rightFin.addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 0.0F, param0);
        this.rightFin.setPos(1.0F, 20.0F, 0.0F);
        this.rightFin.yRot = (float) (-Math.PI / 4);
        this.topFin = new ModelPart(this, 20, 11);
        this.topFin.addBox(0.0F, -4.0F, 0.0F, 0.0F, 4.0F, 6.0F, param0);
        this.topFin.setPos(0.0F, 16.0F, -3.0F);
        this.bottomFin = new ModelPart(this, 20, 21);
        this.bottomFin.addBox(0.0F, 0.0F, 0.0F, 0.0F, 4.0F, 6.0F, param0);
        this.bottomFin.setPos(0.0F, 22.0F, -3.0F);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.body, this.tail, this.leftFin, this.rightFin, this.topFin, this.bottomFin);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = 1.0F;
        if (!param0.isInWater()) {
            var0 = 1.5F;
        }

        this.tail.yRot = -var0 * 0.45F * Mth.sin(0.6F * param3);
    }
}
