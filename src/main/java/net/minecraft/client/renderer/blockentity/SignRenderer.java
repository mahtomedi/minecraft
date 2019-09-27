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
        SignBlockEntity param0, double param1, double param2, double param3, float param4, PoseStack param5, MultiBufferSource param6, int param7
    ) {
        BlockState var0 = param0.getBlockState();
        param5.pushPose();
        float var1 = 0.6666667F;
        if (var0.getBlock() instanceof StandingSignBlock) {
            param5.translate(0.5, 0.5, 0.5);
            param5.mulPose(Vector3f.YP.rotation(-((float)(var0.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F), true));
            this.stick.visible = true;
        } else {
            param5.translate(0.5, 0.5, 0.5);
            param5.mulPose(Vector3f.YP.rotation(-var0.getValue(WallSignBlock.FACING).toYRot(), true));
            param5.translate(0.0, -0.3125, -0.4375);
            this.stick.visible = false;
        }

        TextureAtlasSprite var2 = this.getSprite(this.getTexture(var0.getBlock()));
        param5.pushPose();
        param5.scale(0.6666667F, -0.6666667F, -0.6666667F);
        VertexConsumer var3 = param6.getBuffer(RenderType.SOLID);
        this.sign.render(param5, var3, 0.0625F, param7, var2);
        this.stick.render(param5, var3, 0.0625F, param7, var2);
        param5.popPose();
        Font var4 = this.renderer.getFont();
        float var5 = 0.010416667F;
        param5.translate(0.0, 0.33333334F, 0.046666667F);
        param5.scale(0.010416667F, -0.010416667F, 0.010416667F);
        int var6 = param0.getColor().getTextColor();

        for(int var7 = 0; var7 < 4; ++var7) {
            String var8 = param0.getRenderMessage(var7, param1x -> {
                List<Component> var0x = ComponentRenderUtils.wrapComponents(param1x, 90, var4, false, true);
                return var0x.isEmpty() ? "" : var0x.get(0).getColoredString();
            });
            if (var8 != null) {
                float var9 = (float)(-var4.width(var8) / 2);
                var4.drawInBatch(var8, var9, (float)(var7 * 10 - param0.messages.length * 5), var6, false, param5.getPose(), param6, false, 0, param7);
                if (var7 == param0.getSelectedLine() && param0.getCursorPos() >= 0) {
                    int var10 = var4.width(var8.substring(0, Math.max(Math.min(param0.getCursorPos(), var8.length()), 0)));
                    int var11 = var4.isBidirectional() ? -1 : 1;
                    int var12 = (var10 - var4.width(var8) / 2) * var11;
                    int var13 = var7 * 10 - param0.messages.length * 5;
                    if (param0.isShowCursor()) {
                        if (param0.getCursorPos() < var8.length()) {
                            GuiComponent.fill(var12, var13 - 1, var12 + 1, var13 + 9, 0xFF000000 | var6);
                        } else {
                            var4.drawInBatch("_", (float)var12, (float)var13, var6, false, param5.getPose(), param6, false, 0, param7);
                        }
                    }

                    if (param0.getSelectionPos() != param0.getCursorPos()) {
                        int var14 = Math.min(param0.getCursorPos(), param0.getSelectionPos());
                        int var15 = Math.max(param0.getCursorPos(), param0.getSelectionPos());
                        int var16 = (var4.width(var8.substring(0, var14)) - var4.width(var8) / 2) * var11;
                        int var17 = (var4.width(var8.substring(0, var15)) - var4.width(var8) / 2) * var11;
                        RenderSystem.pushMatrix();
                        RenderSystem.multMatrix(param5.getPose());
                        this.renderHighlight(Math.min(var16, var17), var13, Math.max(var16, var17), var13 + 9);
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
