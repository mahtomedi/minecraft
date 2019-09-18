package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class BatchedBlockEntityRenderer<T extends BlockEntity> extends BlockEntityRenderer<T> {
    @Override
    public final void setupAndRender(
        T param0, double param1, double param2, double param3, float param4, int param5, BufferBuilder param6, RenderType param7, BlockPos param8
    ) {
        this.renderToBuffer(
            param0,
            (double)param8.getX() - BlockEntityRenderDispatcher.xOff,
            (double)param8.getY() - BlockEntityRenderDispatcher.yOff,
            (double)param8.getZ() - BlockEntityRenderDispatcher.zOff,
            param4,
            param5,
            param7,
            param6
        );
    }

    private void renderToBuffer(T param0, double param1, double param2, double param3, float param4, int param5, RenderType param6, BufferBuilder param7) {
        Level var0 = param0.getLevel();
        int var2;
        int var3;
        if (var0 != null) {
            int var1 = var0.getLightColor(param0.getBlockPos());
            var2 = var1 >> 16;
            var3 = var1 & 65535;
        } else {
            var2 = 240;
            var3 = 240;
        }

        param7.offset(param1, param2, param3);
        this.renderToBuffer(param0, param1, param2, param3, param4, param5, param6, param7, var2, var3);
        param7.offset(0.0, 0.0, 0.0);
    }

    @Override
    public final void render(T param0, double param1, double param2, double param3, float param4, int param5, RenderType param6) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        var1.begin(7, DefaultVertexFormat.BLOCK);
        this.renderToBuffer(param0, param1, param2, param3, param4, param5, param6, var1);
        var1.offset(0.0, 0.0, 0.0);
        param6.setupRenderState();
        var0.end();
        param6.clearRenderState();
    }

    protected abstract void renderToBuffer(
        T var1, double var2, double var4, double var6, float var8, int var9, RenderType var10, BufferBuilder var11, int var12, int var13
    );

    protected TextureAtlasSprite getSprite(ResourceLocation param0) {
        return Minecraft.getInstance().getTextureAtlas().getSprite(param0);
    }
}
