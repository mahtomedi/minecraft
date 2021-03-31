package net.minecraft.client.model;

import java.util.Arrays;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LavaSlimeModel<T extends Slime> extends HierarchicalModel<T> {
    private static final int SEGMENT_COUNT = 8;
    private final ModelPart root;
    private final ModelPart[] bodyCubes = new ModelPart[8];

    public LavaSlimeModel(ModelPart param0) {
        this.root = param0;
        Arrays.setAll(this.bodyCubes, param1 -> param0.getChild(getSegmentName(param1)));
    }

    private static String getSegmentName(int param0) {
        return "cube" + param0;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();

        for(int var2 = 0; var2 < 8; ++var2) {
            int var3 = 0;
            int var4 = var2;
            if (var2 == 2) {
                var3 = 24;
                var4 = 10;
            } else if (var2 == 3) {
                var3 = 24;
                var4 = 19;
            }

            var1.addOrReplaceChild(
                getSegmentName(var2), CubeListBuilder.create().texOffs(var3, var4).addBox(-4.0F, (float)(16 + var2), -4.0F, 8.0F, 1.0F, 8.0F), PartPose.ZERO
            );
        }

        var1.addOrReplaceChild("inside_cube", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 18.0F, -2.0F, 4.0F, 4.0F, 4.0F), PartPose.ZERO);
        return LayerDefinition.create(var0, 64, 32);
    }

    public void setupAnim(T param0, float param1, float param2, float param3, float param4, float param5) {
    }

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
        float var0 = Mth.lerp(param3, param0.oSquish, param0.squish);
        if (var0 < 0.0F) {
            var0 = 0.0F;
        }

        for(int var1 = 0; var1 < this.bodyCubes.length; ++var1) {
            this.bodyCubes[var1].y = (float)(-(4 - var1)) * var0 * 1.7F;
        }

    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
