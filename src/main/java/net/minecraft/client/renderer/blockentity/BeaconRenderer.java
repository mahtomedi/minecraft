package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class BeaconRenderer implements BlockEntityRenderer<BeaconBlockEntity> {
    public static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/beacon_beam.png");
    public static final int MAX_RENDER_Y = 1024;

    public BeaconRenderer(BlockEntityRendererProvider.Context param0) {
    }

    public void render(BeaconBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        long var0 = param0.getLevel().getGameTime();
        List<BeaconBlockEntity.BeaconBeamSection> var1 = param0.getBeamSections();
        int var2 = 0;

        for(int var3 = 0; var3 < var1.size(); ++var3) {
            BeaconBlockEntity.BeaconBeamSection var4 = var1.get(var3);
            renderBeaconBeam(param2, param3, param1, var0, var2, var3 == var1.size() - 1 ? 1024 : var4.getHeight(), var4.getColor());
            var2 += var4.getHeight();
        }

    }

    private static void renderBeaconBeam(PoseStack param0, MultiBufferSource param1, float param2, long param3, int param4, int param5, float[] param6) {
        renderBeaconBeam(param0, param1, BEAM_LOCATION, param2, 1.0F, param3, param4, param5, param6, 0.2F, 0.25F);
    }

    public static void renderBeaconBeam(
        PoseStack param0,
        MultiBufferSource param1,
        ResourceLocation param2,
        float param3,
        float param4,
        long param5,
        int param6,
        int param7,
        float[] param8,
        float param9,
        float param10
    ) {
        int var0 = param6 + param7;
        param0.pushPose();
        param0.translate(0.5, 0.0, 0.5);
        float var1 = (float)Math.floorMod(param5, 40) + param3;
        float var2 = param7 < 0 ? var1 : -var1;
        float var3 = Mth.frac(var2 * 0.2F - (float)Mth.floor(var2 * 0.1F));
        float var4 = param8[0];
        float var5 = param8[1];
        float var6 = param8[2];
        param0.pushPose();
        param0.mulPose(Axis.YP.rotationDegrees(var1 * 2.25F - 45.0F));
        float var7 = 0.0F;
        float var10 = 0.0F;
        float var11 = -param9;
        float var12 = 0.0F;
        float var13 = 0.0F;
        float var14 = -param9;
        float var15 = 0.0F;
        float var16 = 1.0F;
        float var17 = -1.0F + var3;
        float var18 = (float)param7 * param4 * (0.5F / param9) + var17;
        renderPart(
            param0,
            param1.getBuffer(RenderType.beaconBeam(param2, false)),
            var4,
            var5,
            var6,
            1.0F,
            param6,
            var0,
            0.0F,
            param9,
            param9,
            0.0F,
            var11,
            0.0F,
            0.0F,
            var14,
            0.0F,
            1.0F,
            var18,
            var17
        );
        param0.popPose();
        var7 = -param10;
        float var20 = -param10;
        var10 = -param10;
        var11 = -param10;
        var15 = 0.0F;
        var16 = 1.0F;
        var17 = -1.0F + var3;
        var18 = (float)param7 * param4 + var17;
        renderPart(
            param0,
            param1.getBuffer(RenderType.beaconBeam(param2, true)),
            var4,
            var5,
            var6,
            0.125F,
            param6,
            var0,
            var7,
            var20,
            param10,
            var10,
            var11,
            param10,
            param10,
            param10,
            0.0F,
            1.0F,
            var18,
            var17
        );
        param0.popPose();
    }

    private static void renderPart(
        PoseStack param0,
        VertexConsumer param1,
        float param2,
        float param3,
        float param4,
        float param5,
        int param6,
        int param7,
        float param8,
        float param9,
        float param10,
        float param11,
        float param12,
        float param13,
        float param14,
        float param15,
        float param16,
        float param17,
        float param18,
        float param19
    ) {
        PoseStack.Pose var0 = param0.last();
        Matrix4f var1 = var0.pose();
        Matrix3f var2 = var0.normal();
        renderQuad(var1, var2, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param16, param17, param18, param19);
        renderQuad(var1, var2, param1, param2, param3, param4, param5, param6, param7, param14, param15, param12, param13, param16, param17, param18, param19);
        renderQuad(var1, var2, param1, param2, param3, param4, param5, param6, param7, param10, param11, param14, param15, param16, param17, param18, param19);
        renderQuad(var1, var2, param1, param2, param3, param4, param5, param6, param7, param12, param13, param8, param9, param16, param17, param18, param19);
    }

    private static void renderQuad(
        Matrix4f param0,
        Matrix3f param1,
        VertexConsumer param2,
        float param3,
        float param4,
        float param5,
        float param6,
        int param7,
        int param8,
        float param9,
        float param10,
        float param11,
        float param12,
        float param13,
        float param14,
        float param15,
        float param16
    ) {
        addVertex(param0, param1, param2, param3, param4, param5, param6, param8, param9, param10, param14, param15);
        addVertex(param0, param1, param2, param3, param4, param5, param6, param7, param9, param10, param14, param16);
        addVertex(param0, param1, param2, param3, param4, param5, param6, param7, param11, param12, param13, param16);
        addVertex(param0, param1, param2, param3, param4, param5, param6, param8, param11, param12, param13, param15);
    }

    private static void addVertex(
        Matrix4f param0,
        Matrix3f param1,
        VertexConsumer param2,
        float param3,
        float param4,
        float param5,
        float param6,
        int param7,
        float param8,
        float param9,
        float param10,
        float param11
    ) {
        param2.vertex(param0, param8, (float)param7, param9)
            .color(param3, param4, param5, param6)
            .uv(param10, param11)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(15728880)
            .normal(param1, 0.0F, 1.0F, 0.0F)
            .endVertex();
    }

    public boolean shouldRenderOffScreen(BeaconBlockEntity param0) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    public boolean shouldRender(BeaconBlockEntity param0, Vec3 param1) {
        return Vec3.atCenterOf(param0.getBlockPos()).multiply(1.0, 0.0, 1.0).closerThan(param1.multiply(1.0, 0.0, 1.0), (double)this.getViewDistance());
    }
}
