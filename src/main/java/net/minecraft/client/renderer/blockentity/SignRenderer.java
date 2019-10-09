package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SignRenderer extends BlockEntityRenderer<SignBlockEntity> {
    private final ModelPart sign = new ModelPart(64, 32, 0, 0);
    private final ModelPart stick;

    public SignRenderer(BlockEntityRenderDispatcher param0) {
        super(param0);
        this.sign.addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F, 0.0F);
        this.stick = new ModelPart(64, 32, 0, 14);
        this.stick.addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F, 0.0F);
    }

    public void render(
        SignBlockEntity param0, double param1, double param2, double param3, float param4, PoseStack param5, MultiBufferSource param6, int param7, int param8
    ) {
        BlockState var0 = param0.getBlockState();
        param5.pushPose();
        float var1 = 0.6666667F;
        if (var0.getBlock() instanceof StandingSignBlock) {
            param5.translate(0.5, 0.5, 0.5);
            float var2 = -((float)(var0.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F);
            param5.mulPose(Vector3f.YP.rotationDegrees(var2));
            this.stick.visible = true;
        } else {
            param5.translate(0.5, 0.5, 0.5);
            float var3 = -var0.getValue(WallSignBlock.FACING).toYRot();
            param5.mulPose(Vector3f.YP.rotationDegrees(var3));
            param5.translate(0.0, -0.3125, -0.4375);
            this.stick.visible = false;
        }

        TextureAtlasSprite var4 = this.getSprite(this.getTexture(var0.getBlock()));
        param5.pushPose();
        param5.scale(0.6666667F, -0.6666667F, -0.6666667F);
        VertexConsumer var5 = param6.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        this.sign.render(param5, var5, 0.0625F, param7, param8, var4);
        this.stick.render(param5, var5, 0.0625F, param7, param8, var4);
        param5.popPose();
        Font var6 = this.renderer.getFont();
        float var7 = 0.010416667F;
        param5.translate(0.0, 0.33333334F, 0.046666667F);
        param5.scale(0.010416667F, -0.010416667F, 0.010416667F);
        int var8 = param0.getColor().getTextColor();

        for(int var9 = 0; var9 < 4; ++var9) {
            String var10 = param0.getRenderMessage(var9, param1x -> {
                List<Component> var0x = ComponentRenderUtils.wrapComponents(param1x, 90, var6, false, true);
                return var0x.isEmpty() ? "" : var0x.get(0).getColoredString();
            });
            if (var10 != null) {
                float var11 = (float)(-var6.width(var10) / 2);
                var6.drawInBatch(var10, var11, (float)(var9 * 10 - param0.messages.length * 5), var8, false, param5.getPose(), param6, false, 0, param7);
                if (var9 == param0.getSelectedLine() && param0.getCursorPos() >= 0) {
                    int var12 = var6.width(var10.substring(0, Math.max(Math.min(param0.getCursorPos(), var10.length()), 0)));
                    int var13 = var6.isBidirectional() ? -1 : 1;
                    int var14 = (var12 - var6.width(var10) / 2) * var13;
                    int var15 = var9 * 10 - param0.messages.length * 5;
                    if (param0.isShowCursor()) {
                        if (param0.getCursorPos() < var10.length()) {
                            GuiComponent.fill(var14, var15 - 1, var14 + 1, var15 + 9, 0xFF000000 | var8);
                        } else {
                            var6.drawInBatch("_", (float)var14, (float)var15, var8, false, param5.getPose(), param6, false, 0, param7);
                        }
                    }

                    if (param0.getSelectionPos() != param0.getCursorPos()) {
                        int var16 = Math.min(param0.getCursorPos(), param0.getSelectionPos());
                        int var17 = Math.max(param0.getCursorPos(), param0.getSelectionPos());
                        int var18 = (var6.width(var10.substring(0, var16)) - var6.width(var10) / 2) * var13;
                        int var19 = (var6.width(var10.substring(0, var17)) - var6.width(var10) / 2) * var13;
                        RenderSystem.pushMatrix();
                        RenderSystem.multMatrix(param5.getPose());
                        this.renderHighlight(Math.min(var18, var19), var15, Math.max(var18, var19), var15 + 9);
                        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                        RenderSystem.popMatrix();
                    }
                }
            }
        }

        param5.popPose();
    }

    private ResourceLocation getTexture(Block param0) {
        if (param0 == Blocks.OAK_SIGN || param0 == Blocks.OAK_WALL_SIGN) {
            return ModelBakery.OAK_SIGN_TEXTURE;
        } else if (param0 == Blocks.SPRUCE_SIGN || param0 == Blocks.SPRUCE_WALL_SIGN) {
            return ModelBakery.SPRUCE_SIGN_TEXTURE;
        } else if (param0 == Blocks.BIRCH_SIGN || param0 == Blocks.BIRCH_WALL_SIGN) {
            return ModelBakery.BIRCH_SIGN_TEXTURE;
        } else if (param0 == Blocks.ACACIA_SIGN || param0 == Blocks.ACACIA_WALL_SIGN) {
            return ModelBakery.ACACIA_SIGN_TEXTURE;
        } else if (param0 == Blocks.JUNGLE_SIGN || param0 == Blocks.JUNGLE_WALL_SIGN) {
            return ModelBakery.JUNGLE_SIGN_TEXTURE;
        } else {
            return param0 != Blocks.DARK_OAK_SIGN && param0 != Blocks.DARK_OAK_WALL_SIGN ? ModelBakery.OAK_SIGN_TEXTURE : ModelBakery.DARK_OAK_SIGN_TEXTURE;
        }
    }

    private void renderHighlight(int param0, int param1, int param2, int param3) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        RenderSystem.color4f(0.0F, 0.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        var1.begin(7, DefaultVertexFormat.POSITION);
        var1.vertex((double)param0, (double)param3, 0.0).endVertex();
        var1.vertex((double)param2, (double)param3, 0.0).endVertex();
        var1.vertex((double)param2, (double)param1, 0.0).endVertex();
        var1.vertex((double)param0, (double)param1, 0.0).endVertex();
        var1.end();
        BufferUploader.end(var1);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }
}
