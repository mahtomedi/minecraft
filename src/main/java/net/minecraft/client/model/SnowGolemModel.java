package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SnowGolemModel<T extends Entity> extends ListModel<T> {
    private final ModelPart piece1;
    private final ModelPart piece2;
    private final ModelPart head;
    private final ModelPart arm1;
    private final ModelPart arm2;

    public SnowGolemModel() {
        float var0 = 4.0F;
        float var1 = 0.0F;
        this.head = new ModelPart(this, 0, 0).setTexSize(64, 64);
        this.head.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, -0.5F);
        this.head.setPos(0.0F, 4.0F, 0.0F);
        this.arm1 = new ModelPart(this, 32, 0).setTexSize(64, 64);
        this.arm1.addBox(-1.0F, 0.0F, -1.0F, 12.0F, 2.0F, 2.0F, -0.5F);
        this.arm1.setPos(0.0F, 6.0F, 0.0F);
        this.arm2 = new ModelPart(this, 32, 0).setTexSize(64, 64);
        this.arm2.addBox(-1.0F, 0.0F, -1.0F, 12.0F, 2.0F, 2.0F, -0.5F);
        this.arm2.setPos(0.0F, 6.0F, 0.0F);
        this.piece1 = new ModelPart(this, 0, 16).setTexSize(64, 64);
        this.piece1.addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, -0.5F);
        this.piece1.setPos(0.0F, 13.0F, 0.0F);
        this.piece2 = new ModelPart(this, 0, 36).setTexSize(64, 64);
        this.piece2.addBox(-6.0F, -12.0F, -6.0F, 12.0F, 12.0F, 12.0F, -0.5F);
        this.piece2.setPos(0.0F, 24.0F, 0.0F);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.piece1.yRot = param4 * (float) (Math.PI / 180.0) * 0.25F;
        float var0 = Mth.sin(this.piece1.yRot);
        float var1 = Mth.cos(this.piece1.yRot);
        this.arm1.zRot = 1.0F;
        this.arm2.zRot = -1.0F;
        this.arm1.yRot = 0.0F + this.piece1.yRot;
        this.arm2.yRot = (float) Math.PI + this.piece1.yRot;
        this.arm1.x = var1 * 5.0F;
        this.arm1.z = -var0 * 5.0F;
        this.arm2.x = -var1 * 5.0F;
        this.arm2.z = var0 * 5.0F;
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.piece1, this.piece2, this.head, this.arm1, this.arm2);
    }

    public ModelPart getHead() {
        return this.head;
    }
}
