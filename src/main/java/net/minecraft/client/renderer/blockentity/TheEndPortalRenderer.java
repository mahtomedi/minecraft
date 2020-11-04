package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TheEndPortalRenderer<T extends TheEndPortalBlockEntity> implements BlockEntityRenderer<T> {
    public static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
    public static final ResourceLocation END_PORTAL_LOCATION = new ResourceLocation("textures/entity/end_portal.png");
    private static final Random RANDOM = new Random(31100L);
    private static final List<RenderType> RENDER_TYPES = IntStream.range(0, 16)
        .mapToObj(param0 -> RenderType.endPortal(param0 + 1))
        .collect(ImmutableList.toImmutableList());
    private final BlockEntityRenderDispatcher renderer;

    public TheEndPortalRenderer(BlockEntityRendererProvider.Context param0) {
        this.renderer = param0.getBlockEntityRenderDispatcher();
    }

    public void render(T param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        RANDOM.setSeed(31100L);
        double var0 = param0.getBlockPos().distSqr(this.renderer.camera.getPosition(), true);
        int var1 = this.getPasses(var0);
        float var2 = this.getOffset();
        Matrix4f var3 = param2.last().pose();
        this.renderCube(param0, var2, 0.15F, var3, param3.getBuffer(RENDER_TYPES.get(0)));

        for(int var4 = 1; var4 < var1; ++var4) {
            this.renderCube(param0, var2, 2.0F / (float)(18 - var4), var3, param3.getBuffer(RENDER_TYPES.get(var4)));
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
        if (param0 > 36864.0) {
            return 1;
        } else if (param0 > 25600.0) {
            return 3;
        } else if (param0 > 16384.0) {
            return 5;
        } else if (param0 > 9216.0) {
            return 7;
        } else if (param0 > 4096.0) {
            return 9;
        } else if (param0 > 1024.0) {
            return 11;
        } else if (param0 > 576.0) {
            return 13;
        } else {
            return param0 > 256.0 ? 14 : 15;
        }
    }

    protected float getOffset() {
        return 0.75F;
    }
}
