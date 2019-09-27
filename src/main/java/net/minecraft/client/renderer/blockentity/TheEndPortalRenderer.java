package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

    public void render(T param0, double param1, double param2, double param3, float param4, PoseStack param5, MultiBufferSource param6, int param7) {
        RANDOM.setSeed(31100L);
        double var0 = param1 * param1 + param2 * param2 + param3 * param3;
        int var1 = this.getPasses(var0);
        float var2 = this.getOffset();
        this.renderCube(param0, param1, param2, param3, var2, 0.15F, param6.getBuffer(RenderType.PORTAL(1)));

        for(int var3 = 1; var3 < var1; ++var3) {
            this.renderCube(param0, param1, param2, param3, var2, 2.0F / (float)(18 - var3), param6.getBuffer(RenderType.PORTAL(var3 + 1)));
        }

    }

    private void renderCube(T param0, double param1, double param2, double param3, float param4, float param5, VertexConsumer param6) {
        float var0 = (RANDOM.nextFloat() * 0.5F + 0.1F) * param5;
        float var1 = (RANDOM.nextFloat() * 0.5F + 0.4F) * param5;
        float var2 = (RANDOM.nextFloat() * 0.5F + 0.5F) * param5;
        this.renderFace(
            param0,
            param6,
            Direction.SOUTH,
            param1,
            param1 + 1.0,
            param2,
            param2 + 1.0,
            param3 + 1.0,
            param3 + 1.0,
            param3 + 1.0,
            param3 + 1.0,
            var0,
            var1,
            var2
        );
        this.renderFace(param0, param6, Direction.NORTH, param1, param1 + 1.0, param2 + 1.0, param2, param3, param3, param3, param3, var0, var1, var2);
        this.renderFace(
            param0, param6, Direction.EAST, param1 + 1.0, param1 + 1.0, param2 + 1.0, param2, param3, param3 + 1.0, param3 + 1.0, param3, var0, var1, var2
        );
        this.renderFace(param0, param6, Direction.WEST, param1, param1, param2, param2 + 1.0, param3, param3 + 1.0, param3 + 1.0, param3, var0, var1, var2);
        this.renderFace(param0, param6, Direction.DOWN, param1, param1 + 1.0, param2, param2, param3, param3, param3 + 1.0, param3 + 1.0, var0, var1, var2);
        this.renderFace(
            param0,
            param6,
            Direction.UP,
            param1,
            param1 + 1.0,
            param2 + (double)param4,
            param2 + (double)param4,
            param3 + 1.0,
            param3 + 1.0,
            param3,
            param3,
            var0,
            var1,
            var2
        );
    }

    private void renderFace(
        T param0,
        VertexConsumer param1,
        Direction param2,
        double param3,
        double param4,
        double param5,
        double param6,
        double param7,
        double param8,
        double param9,
        double param10,
        float param11,
        float param12,
        float param13
    ) {
        if (param0.shouldRenderFace(param2)) {
            param1.vertex(param3, param5, param7).color(param11, param12, param13, 1.0F).endVertex();
            param1.vertex(param4, param5, param8).color(param11, param12, param13, 1.0F).endVertex();
            param1.vertex(param4, param6, param9).color(param11, param12, param13, 1.0F).endVertex();
            param1.vertex(param3, param6, param10).color(param11, param12, param13, 1.0F).endVertex();
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
