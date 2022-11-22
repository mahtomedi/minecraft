package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.ChestRaftModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.RaftModel;
import net.minecraft.client.model.WaterPatchModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class BoatRenderer extends EntityRenderer<Boat> {
    private final Map<Boat.Type, Pair<ResourceLocation, ListModel<Boat>>> boatResources;

    public BoatRenderer(EntityRendererProvider.Context param0, boolean param1) {
        super(param0);
        this.shadowRadius = 0.8F;
        this.boatResources = Stream.of(Boat.Type.values())
            .collect(
                ImmutableMap.toImmutableMap(
                    param0x -> param0x,
                    param2 -> Pair.of(new ResourceLocation(getTextureLocation(param2, param1)), this.createBoatModel(param0, param2, param1))
                )
            );
    }

    private ListModel<Boat> createBoatModel(EntityRendererProvider.Context param0, Boat.Type param1, boolean param2) {
        ModelLayerLocation var0 = param2 ? ModelLayers.createChestBoatModelName(param1) : ModelLayers.createBoatModelName(param1);
        ModelPart var1 = param0.bakeLayer(var0);
        if (param1 == Boat.Type.BAMBOO) {
            return (ListModel<Boat>)(param2 ? new ChestRaftModel(var1) : new RaftModel(var1));
        } else {
            return (ListModel<Boat>)(param2 ? new ChestBoatModel(var1) : new BoatModel(var1));
        }
    }

    private static String getTextureLocation(Boat.Type param0, boolean param1) {
        return param1 ? "textures/entity/chest_boat/" + param0.getName() + ".png" : "textures/entity/boat/" + param0.getName() + ".png";
    }

    public void render(Boat param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        param3.translate(0.0F, 0.375F, 0.0F);
        param3.mulPose(Axis.YP.rotationDegrees(180.0F - param1));
        float var0 = (float)param0.getHurtTime() - param2;
        float var1 = param0.getDamage() - param2;
        if (var1 < 0.0F) {
            var1 = 0.0F;
        }

        if (var0 > 0.0F) {
            param3.mulPose(Axis.XP.rotationDegrees(Mth.sin(var0) * var0 * var1 / 10.0F * (float)param0.getHurtDir()));
        }

        float var2 = param0.getBubbleAngle(param2);
        if (!Mth.equal(var2, 0.0F)) {
            param3.mulPose(new Quaternionf().setAngleAxis(param0.getBubbleAngle(param2) * (float) (Math.PI / 180.0), 1.0F, 0.0F, 1.0F));
        }

        Pair<ResourceLocation, ListModel<Boat>> var3 = this.boatResources.get(param0.getVariant());
        ResourceLocation var4 = var3.getFirst();
        ListModel<Boat> var5 = var3.getSecond();
        param3.scale(-1.0F, -1.0F, 1.0F);
        param3.mulPose(Axis.YP.rotationDegrees(90.0F));
        var5.setupAnim(param0, param2, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer var6 = param4.getBuffer(var5.renderType(var4));
        var5.renderToBuffer(param3, var6, param5, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        if (!param0.isUnderWater()) {
            VertexConsumer var7 = param4.getBuffer(RenderType.waterMask());
            if (var5 instanceof WaterPatchModel var8) {
                var8.waterPatch().render(param3, var7, param5, OverlayTexture.NO_OVERLAY);
            }
        }

        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public ResourceLocation getTextureLocation(Boat param0) {
        return this.boatResources.get(param0.getVariant()).getFirst();
    }
}
