package net.minecraft.client.model;

import java.util.Arrays;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SquidModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart[] tentacles = new ModelPart[8];
    private final ModelPart root;

    public SquidModel(ModelPart param0) {
        this.root = param0;
        Arrays.setAll(this.tentacles, param1 -> param0.getChild(createTentacleName(param1)));
    }

    private static String createTentacleName(int param0) {
        return "tentacle" + param0;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        CubeDeformation var2 = new CubeDeformation(0.02F);
        int var3 = -16;
        var1.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -8.0F, -6.0F, 12.0F, 16.0F, 12.0F, var2), PartPose.offset(0.0F, 8.0F, 0.0F)
        );
        int var4 = 8;
        CubeListBuilder var5 = CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 18.0F, 2.0F);

        for(int var6 = 0; var6 < 8; ++var6) {
            double var7 = (double)var6 * Math.PI * 2.0 / 8.0;
            float var8 = (float)Math.cos(var7) * 5.0F;
            float var9 = 15.0F;
            float var10 = (float)Math.sin(var7) * 5.0F;
            var7 = (double)var6 * Math.PI * -2.0 / 8.0 + (Math.PI / 2);
            float var11 = (float)var7;
            var1.addOrReplaceChild(createTentacleName(var6), var5, PartPose.offsetAndRotation(var8, 15.0F, var10, 0.0F, var11, 0.0F));
        }

        return LayerDefinition.create(var0, 64, 32);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        for(ModelPart var0 : this.tentacles) {
            var0.xRot = param3;
        }

    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
