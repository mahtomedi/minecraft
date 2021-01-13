package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LeashKnotModel<T extends Entity> extends ListModel<T> {
    private final ModelPart knot;

    public LeashKnotModel() {
        this.texWidth = 32;
        this.texHeight = 32;
        this.knot = new ModelPart(this, 0, 0);
        this.knot.addBox(-3.0F, -6.0F, -3.0F, 6.0F, 8.0F, 6.0F, 0.0F);
        this.knot.setPos(0.0F, 0.0F, 0.0F);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.knot);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        this.knot.yRot = param4 * (float) (Math.PI / 180.0);
        this.knot.xRot = param5 * (float) (Math.PI / 180.0);
    }
}
