package net.minecraft.client.model;

import java.util.Random;
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
public class GhastModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart[] tentacles = new ModelPart[9];

    public GhastModel(ModelPart param0) {
        this.root = param0;

        for(int var0 = 0; var0 < this.tentacles.length; ++var0) {
            this.tentacles[var0] = param0.getChild(createTentacleName(var0));
        }

    }

    private static String createTentacleName(int param0) {
        return "tentacle" + param0;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        var1.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(0.0F, 17.6F, 0.0F)
        );
        Random var2 = new Random(1660L);

        for(int var3 = 0; var3 < 9; ++var3) {
            float var4 = (((float)(var3 % 3) - (float)(var3 / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
            float var5 = ((float)(var3 / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
            int var6 = var2.nextInt(7) + 8;
            var1.addOrReplaceChild(
                createTentacleName(var3),
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, (float)var6, 2.0F),
                PartPose.offset(var4, 24.6F, var5)
            );
        }

        return LayerDefinition.create(var0, 64, 32);
    }

    @Override
    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
        for(int var0 = 0; var0 < this.tentacles.length; ++var0) {
            this.tentacles[var0].xRot = 0.2F * Mth.sin(param3 * 0.3F + (float)var0) + 0.4F;
        }

    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
