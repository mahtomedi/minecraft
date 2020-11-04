package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerModel<T extends Shulker> extends ListModel<T> {
    private final ModelPart base;
    private final ModelPart lid;
    private final ModelPart head;

    public ShulkerModel(ModelPart param0) {
        super(RenderType::entityCutoutNoCullZOffset);
        this.lid = param0.getChild("lid");
        this.base = param0.getChild("base");
        this.head = param0.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "lid", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 12.0F, 16.0F), PartPose.offset(0.0F, 24.0F, 0.0F)
        );
        var1.addOrReplaceChild(
            "base", CubeListBuilder.create().texOffs(0, 28).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 8.0F, 16.0F), PartPose.offset(0.0F, 24.0F, 0.0F)
        );
        var1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 52).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 12.0F, 0.0F));
        return LayerDefinition.create(var0, 64, 64);
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
        this.head.yRot = (param0.yHeadRot - 180.0F - param0.yBodyRot) * (float) (Math.PI / 180.0);
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
