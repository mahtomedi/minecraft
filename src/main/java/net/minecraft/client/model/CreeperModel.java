package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreeperModel<T extends Entity> extends ListModel<T> {
    private final ModelPart head;
    private final ModelPart hair;
    private final ModelPart body;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;

    public CreeperModel() {
        this(0.0F);
    }

    public CreeperModel(float param0) {
        int var0 = 6;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, param0);
        this.head.setPos(0.0F, 6.0F, 0.0F);
        this.hair = new ModelPart(this, 32, 0);
        this.hair.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, param0 + 0.5F);
        this.hair.setPos(0.0F, 6.0F, 0.0F);
        this.body = new ModelPart(this, 16, 16);
        this.body.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, param0);
        this.body.setPos(0.0F, 6.0F, 0.0F);
        this.leg0 = new ModelPart(this, 0, 16);
        this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, param0);
        this.leg0.setPos(-2.0F, 18.0F, 4.0F);
        this.leg1 = new ModelPart(this, 0, 16);
        this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, param0);
        this.leg1.setPos(2.0F, 18.0F, 4.0F);
        this.leg2 = new ModelPart(this, 0, 16);
        this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, param0);
        this.leg2.setPos(-2.0F, 18.0F, -4.0F);
        this.leg3 = new ModelPart(this, 0, 16);
        this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, param0);
        this.leg3.setPos(2.0F, 18.0F, -4.0F);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.head, this.body, this.leg0, this.leg1, this.leg2, this.leg3);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.leg0.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
        this.leg1.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.leg2.xRot = Mth.cos(param1 * 0.6662F + (float) Math.PI) * 1.4F * param2;
        this.leg3.xRot = Mth.cos(param1 * 0.6662F) * 1.4F * param2;
    }
}
