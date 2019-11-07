package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerModel<T extends Shulker> extends ListModel<T> {
    private final ModelPart base;
    private final ModelPart lid = new ModelPart(64, 64, 0, 0);
    private final ModelPart head;

    public ShulkerModel() {
        this.base = new ModelPart(64, 64, 0, 28);
        this.head = new ModelPart(64, 64, 0, 52);
        this.lid.addBox(-8.0F, -16.0F, -8.0F, 16.0F, 12.0F, 16.0F);
        this.lid.setPos(0.0F, 24.0F, 0.0F);
        this.base.addBox(-8.0F, -8.0F, -8.0F, 16.0F, 8.0F, 16.0F);
        this.base.setPos(0.0F, 24.0F, 0.0F);
        this.head.addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F);
        this.head.setPos(0.0F, 12.0F, 0.0F);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = param3 - (float)param0.tickCount;
        float var1 = (0.5F + param0.getClientPeekAmount(var0)) * (float) Math.PI;
        float var2 = -1.0F + Mth.sin(var1);
        float var3 = 0.0F;
        if (var1 > (float) Math.PI) {
            var3 = Mth.sin(param3 * 0.1F) * 0.7F;
        }

        this.lid.setPos(0.0F, 16.0F + Mth.sin(var1) * 8.0F + var3, 0.0F);
        if (param0.getClientPeekAmount(var0) > 0.3F) {
            this.lid.yRot = var2 * var2 * var2 * var2 * (float) Math.PI * 0.125F;
        } else {
            this.lid.yRot = 0.0F;
        }

        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.base, this.lid);
    }

    public ModelPart getBase() {
        return this.base;
    }

    public ModelPart getLid() {
        return this.lid;
    }

    public ModelPart getHead() {
        return this.head;
    }
}
