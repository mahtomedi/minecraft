package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FallingBlockRenderer extends EntityRenderer<FallingBlockEntity> {
    public FallingBlockRenderer(EntityRenderDispatcher param0) {
        super(param0);
        this.shadowRadius = 0.5F;
    }

    public void render(
        FallingBlockEntity param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7
    ) {
        BlockState var0 = param0.getBlockState();
        if (var0.getRenderShape() == RenderShape.MODEL) {
            Level var1 = param0.getLevel();
            if (var0 != var1.getBlockState(new BlockPos(param0)) && var0.getRenderShape() != RenderShape.INVISIBLE) {
                param6.pushPose();
                BlockPos var2 = new BlockPos(param0.getX(), param0.getBoundingBox().maxY, param0.getZ());
                param6.translate((double)(-(var2.getX() & 15)) - 0.5, (double)(-(var2.getY() & 15)), (double)(-(var2.getZ() & 15)) - 0.5);
                BlockRenderDispatcher var3 = Minecraft.getInstance().getBlockRenderer();
                var3.getModelRenderer()
                    .tesselateBlock(
                        var1,
                        var3.getBlockModel(var0),
                        var0,
                        var2,
                        param6,
                        param7.getBuffer(RenderType.getChunkRenderType(var0)),
                        false,
                        new Random(),
                        var0.getSeed(param0.getStartPos()),
                        OverlayTexture.NO_OVERLAY
                    );
                param6.popPose();
                super.render(param0, param1, param2, param3, param4, param5, param6, param7);
            }
        }
    }

    public ResourceLocation getTextureLocation(FallingBlockEntity param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
