package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BoatRenderer extends EntityRenderer<Boat> {
    private final Map<Boat.Type, Pair<ResourceLocation, BoatModel>> boatResources;

    public BoatRenderer(EntityRendererProvider.Context param0) {
        super(param0);
        this.shadowRadius = 0.8F;
        this.boatResources = Stream.of(Boat.Type.values())
            .collect(
                ImmutableMap.toImmutableMap(
                    param0x -> param0x,
                    param1 -> Pair.of(
                            new ResourceLocation("textures/entity/boat/" + param1.getName() + ".png"),
                            new BoatModel(param0.getLayer(ModelLayers.createBoatModelName(param1)))
                        )
                )
            );
    }

    public void render(Boat param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        param3.translate(0.0, 0.375, 0.0);
        param3.mulPose(Vector3f.YP.rotationDegrees(180.0F - param1));
        float var0 = (float)param0.getHurtTime() - param2;
        float var1 = param0.getDamage() - param2;
        if (var1 < 0.0F) {
            var1 = 0.0F;
        }

        if (var0 > 0.0F) {
            param3.mulPose(Vector3f.XP.rotationDegrees(Mth.sin(var0) * var0 * var1 / 10.0F * (float)param0.getHurtDir()));
        }

        float var2 = param0.getBubbleAngle(param2);
        if (!Mth.equal(var2, 0.0F)) {
            param3.mulPose(new Quaternion(new Vector3f(1.0F, 0.0F, 1.0F), param0.getBubbleAngle(param2), true));
        }

        Pair<ResourceLocation, BoatModel> var3 = this.boatResources.get(param0.getBoatType());
        ResourceLocation var4 = var3.getFirst();
        BoatModel var5 = var3.getSecond();
        param3.scale(-1.0F, -1.0F, 1.0F);
        param3.mulPose(Vector3f.YP.rotationDegrees(90.0F));
        var5.setupAnim(param0, param2, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer var6 = param4.getBuffer(var5.renderType(var4));
        var5.renderToBuffer(param3, var6, param5, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        if (!param0.isUnderWater()) {
            VertexConsumer var7 = param4.getBuffer(RenderType.waterMask());
            var5.waterPatch().render(param3, var7, param5, OverlayTexture.NO_OVERLAY);
        }

        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public ResourceLocation getTextureLocation(Boat param0) {
        return this.boatResources.get(param0.getBoatType()).getFirst();
    }
}
