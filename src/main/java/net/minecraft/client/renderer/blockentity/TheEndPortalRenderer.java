package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import java.util.Random;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TheEndPortalRenderer<T extends TheEndPortalBlockEntity> extends BlockEntityRenderer<T> {
    public static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
    public static final ResourceLocation END_PORTAL_LOCATION = new ResourceLocation("textures/entity/end_portal.png");
    private static final Random RANDOM = new Random(31100L);

    public TheEndPortalRenderer(BlockEntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(T param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        RANDOM.setSeed(31100L);
        double var0 = param0.getBlockPos().distSqr(this.renderer.camera.getPosition(), true);
        int var1 = this.getPasses(var0);
        float var2 = this.getOffset();
        Matrix4f var3 = param2.last().pose();
        this.renderCube(param0, var2, 0.15F, var3, param3.getBuffer(RenderType.endPortal(1)));

        for(int var4 = 1; var4 < var1; ++var4) {
            this.renderCube(param0, var2, 2.0F / (float)(18 - var4), var3, param3.getBuffer(RenderType.endPortal(var4 + 1)));
        }

    }

    private void renderCube(T param0, float param1, float param2, Matrix4f param3, VertexConsumer param4) {
        float var0 = (RANDOM.nextFloat() * 0.5F + 0.1F) * param2;
        float var1 = (RANDOM.nextFloat() * 0.5F + 0.4F) * param2;
        float var2 = (RANDOM.nextFloat() * 0.5F + 0.5F) * param2;
        this.renderFace(param0, param3, param4, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, var0, var1, var2, Direction.SOUTH);
        this.renderFace(param0, param3, param4, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, var0, var1, var2, Direction.NORTH);
        this.renderFace(param0, param3, param4, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, var0, var1, var2, Direction.EAST);
        this.renderFace(param0, param3, param4, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, var0, var1, var2, Direction.WEST);
        this.renderFace(param0, param3, param4, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, var0, var1, var2, Direction.DOWN);
        this.renderFace(param0, param3, param4, 0.0F, 1.0F, param1, param1, 1.0F, 1.0F, 0.0F, 0.0F, var0, var1, var2, Direction.UP);
    }

    private void renderFace(
        T param0,
        Matrix4f param1,
        VertexConsumer param2,
        float param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9,
        float param10,
        float param11,
        float param12,
        float param13,
        Direction param14
    ) {
        if (param0.shouldRenderFace(param14)) {
            param2.vertex(param1, param3, param5, param7).color(param11, param12, param13, 1.0F).endVertex();
            param2.vertex(param1, param4, param5, param8).color(param11, param12, param13, 1.0F).endVertex();
            param2.vertex(param1, param4, param6, param9).color(param11, param12, param13, 1.0F).endVertex();
            param2.vertex(param1, param3, param6, param10).color(param11, param12, param13, 1.0F).endVertex();
        }

    }

    protected int getPasses(double param0) {
        int var0;
        if (param0 > 36864.0) {
            var0 = 1;
        } else if (param0 > 25600.0) {
            var0 = 3;
        } else if (param0 > 16384.0) {
            var0 = 5;
        } else if (param0 > 9216.0) {
            var0 = 7;
        } else if (param0 > 4096.0) {
            var0 = 9;
        } else if (param0 > 1024.0) {
            var0 = 11;
        } else if (param0 > 576.0) {
            var0 = 13;
        } else if (param0 > 256.0) {
            var0 = 14;
        } else {
            var0 = 15;
        }

        return var0;
    }

    protected float getOffset() {
        return 0.75F;
    }
}
