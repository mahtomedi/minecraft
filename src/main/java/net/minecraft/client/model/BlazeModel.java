package net.minecraft.client.model;

import java.util.Arrays;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlazeModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart[] upperBodyParts;
    private final ModelPart head;

    public BlazeModel(ModelPart param0) {
        this.root = param0;
        this.head = param0.getChild("head");
        this.upperBodyParts = new ModelPart[12];
        Arrays.setAll(this.upperBodyParts, param1 -> param0.getChild(getPartName(param1)));
    }

    private static String getPartName(int param0) {
        return "part" + param0;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        float var2 = 0.0F;
        CubeListBuilder var3 = CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 2.0F, 8.0F, 2.0F);

        for(int var4 = 0; var4 < 4; ++var4) {
            float var5 = Mth.cos(var2) * 9.0F;
            float var6 = -2.0F + Mth.cos((float)(var4 * 2) * 0.25F);
            float var7 = Mth.sin(var2) * 9.0F;
            var1.addOrReplaceChild(getPartName(var4), var3, PartPose.offset(var5, var6, var7));
            ++var2;
        }

        var2 = (float) (Math.PI / 4);

        for(int var8 = 4; var8 < 8; ++var8) {
            float var9 = Mth.cos(var2) * 7.0F;
            float var10 = 2.0F + Mth.cos((float)(var8 * 2) * 0.25F);
            float var11 = Mth.sin(var2) * 7.0F;
            var1.addOrReplaceChild(getPartName(var8), var3, PartPose.offset(var9, var10, var11));
            ++var2;
        }

        var2 = 0.47123894F;

        for(int var12 = 8; var12 < 12; ++var12) {
            float var13 = Mth.cos(var2) * 5.0F;
            float var14 = 11.0F + Mth.cos((float)var12 * 1.5F * 0.5F);
            float var15 = Mth.sin(var2) * 5.0F;
            var1.addOrReplaceChild(getPartName(var12), var3, PartPose.offset(var13, var14, var15));
            ++var2;
        }

        return LayerDefinition.create(var0, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = param3 * (float) Math.PI * -0.1F;

        for(int var1 = 0; var1 < 4; ++var1) {
            this.upperBodyParts[var1].y = -2.0F + Mth.cos(((float)(var1 * 2) + param3) * 0.25F);
            this.upperBodyParts[var1].x = Mth.cos(var0) * 9.0F;
            this.upperBodyParts[var1].z = Mth.sin(var0) * 9.0F;
            ++var0;
        }

        var0 = (float) (Math.PI / 4) + param3 * (float) Math.PI * 0.03F;

        for(int var2 = 4; var2 < 8; ++var2) {
            this.upperBodyParts[var2].y = 2.0F + Mth.cos(((float)(var2 * 2) + param3) * 0.25F);
            this.upperBodyParts[var2].x = Mth.cos(var0) * 7.0F;
            this.upperBodyParts[var2].z = Mth.sin(var0) * 7.0F;
            ++var0;
        }

        var0 = 0.47123894F + param3 * (float) Math.PI * -0.05F;

        for(int var3 = 8; var3 < 12; ++var3) {
            this.upperBodyParts[var3].y = 11.0F + Mth.cos(((float)var3 * 1.5F + param3) * 0.5F);
            this.upperBodyParts[var3].x = Mth.cos(var0) * 5.0F;
            this.upperBodyParts[var3].z = Mth.sin(var0) * 5.0F;
            ++var0;
        }

        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
    }
}
