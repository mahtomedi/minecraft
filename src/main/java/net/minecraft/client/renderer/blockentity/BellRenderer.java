package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BellRenderer implements BlockEntityRenderer<BellBlockEntity> {
    public static final Material BELL_RESOURCE_LOCATION = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/bell/bell_body"));
    private static final String BELL_BODY = "bell_body";
    private final ModelPart bellBody;

    public BellRenderer(BlockEntityRendererProvider.Context param0) {
        ModelPart var0 = param0.bakeLayer(ModelLayers.BELL);
        this.bellBody = var0.getChild("bell_body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        PartDefinition var2 = var1.addOrReplaceChild(
            "bell_body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 7.0F, 6.0F), PartPose.offset(8.0F, 12.0F, 8.0F)
        );
        var2.addOrReplaceChild(
            "bell_base", CubeListBuilder.create().texOffs(0, 13).addBox(4.0F, 4.0F, 4.0F, 8.0F, 2.0F, 8.0F), PartPose.offset(-8.0F, -12.0F, -8.0F)
        );
        return LayerDefinition.create(var0, 32, 32);
    }

    public void render(BellBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        float var0 = (float)param0.ticks + param1;
        float var1 = 0.0F;
        float var2 = 0.0F;
        if (param0.shaking) {
            float var3 = Mth.sin(var0 / (float) Math.PI) / (4.0F + var0 / 3.0F);
            if (param0.clickDirection == Direction.NORTH) {
                var1 = -var3;
            } else if (param0.clickDirection == Direction.SOUTH) {
                var1 = var3;
            } else if (param0.clickDirection == Direction.EAST) {
                var2 = -var3;
            } else if (param0.clickDirection == Direction.WEST) {
                var2 = var3;
            }
        }

        this.bellBody.xRot = var1;
        this.bellBody.zRot = var2;
        VertexConsumer var4 = BELL_RESOURCE_LOCATION.buffer(param3, RenderType::entitySolid);
        this.bellBody.render(param2, var4, param4, param5);
    }
}
