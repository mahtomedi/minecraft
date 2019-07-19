package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.model.SignModel;
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
    private static final ResourceLocation OAK_TEXTURE = new ResourceLocation("textures/entity/signs/oak.png");
    private static final ResourceLocation SPRUCE_TEXTURE = new ResourceLocation("textures/entity/signs/spruce.png");
    private static final ResourceLocation BIRCH_TEXTURE = new ResourceLocation("textures/entity/signs/birch.png");
    private static final ResourceLocation ACACIA_TEXTURE = new ResourceLocation("textures/entity/signs/acacia.png");
    private static final ResourceLocation JUNGLE_TEXTURE = new ResourceLocation("textures/entity/signs/jungle.png");
    private static final ResourceLocation DARK_OAK_TEXTURE = new ResourceLocation("textures/entity/signs/dark_oak.png");
    private final SignModel signModel = new SignModel();

    public void render(SignBlockEntity param0, double param1, double param2, double param3, float param4, int param5) {
        BlockState var0 = param0.getBlockState();
        GlStateManager.pushMatrix();
        float var1 = 0.6666667F;
        if (var0.getBlock() instanceof StandingSignBlock) {
            GlStateManager.translatef((float)param1 + 0.5F, (float)param2 + 0.5F, (float)param3 + 0.5F);
            GlStateManager.rotatef(-((float)(var0.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F), 0.0F, 1.0F, 0.0F);
            this.signModel.getStick().visible = true;
        } else {
            GlStateManager.translatef((float)param1 + 0.5F, (float)param2 + 0.5F, (float)param3 + 0.5F);
            GlStateManager.rotatef(-var0.getValue(WallSignBlock.FACING).toYRot(), 0.0F, 1.0F, 0.0F);
            GlStateManager.translatef(0.0F, -0.3125F, -0.4375F);
            this.signModel.getStick().visible = false;
        }

        if (param5 >= 0) {
            this.bindTexture(BREAKING_LOCATIONS[param5]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scalef(4.0F, 2.0F, 1.0F);
            GlStateManager.translatef(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        } else {
            this.bindTexture(this.getTexture(var0.getBlock()));
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        GlStateManager.scalef(0.6666667F, -0.6666667F, -0.6666667F);
        this.signModel.render();
        GlStateManager.popMatrix();
        Font var2 = this.getFont();
        float var3 = 0.010416667F;
        GlStateManager.translatef(0.0F, 0.33333334F, 0.046666667F);
        GlStateManager.scalef(0.010416667F, -0.010416667F, 0.010416667F);
        GlStateManager.normal3f(0.0F, 0.0F, -0.010416667F);
        GlStateManager.depthMask(false);
        int var4 = param0.getColor().getTextColor();
        if (param5 < 0) {
            for(int var5 = 0; var5 < 4; ++var5) {
                String var6 = param0.getRenderMessage(var5, param1x -> {
                    List<Component> var0x = ComponentRenderUtils.wrapComponents(param1x, 90, var2, false, true);
                    return var0x.isEmpty() ? "" : var0x.get(0).getColoredString();
                });
                if (var6 != null) {
                    var2.draw(var6, (float)(-var2.width(var6) / 2), (float)(var5 * 10 - param0.messages.length * 5), var4);
                    if (var5 == param0.getSelectedLine() && param0.getCursorPos() >= 0) {
                        int var7 = var2.width(var6.substring(0, Math.max(Math.min(param0.getCursorPos(), var6.length()), 0)));
                        int var8 = var2.isBidirectional() ? -1 : 1;
                        int var9 = (var7 - var2.width(var6) / 2) * var8;
                        int var10 = var5 * 10 - param0.messages.length * 5;
                        if (param0.isShowCursor()) {
                            if (param0.getCursorPos() < var6.length()) {
                                GuiComponent.fill(var9, var10 - 1, var9 + 1, var10 + 9, 0xFF000000 | var4);
                            } else {
                                var2.draw("_", (float)var9, (float)var10, var4);
                            }
                        }

                        if (param0.getSelectionPos() != param0.getCursorPos()) {
                            int var11 = Math.min(param0.getCursorPos(), param0.getSelectionPos());
                            int var12 = Math.max(param0.getCursorPos(), param0.getSelectionPos());
                            int var13 = (var2.width(var6.substring(0, var11)) - var2.width(var6) / 2) * var8;
                            int var14 = (var2.width(var6.substring(0, var12)) - var2.width(var6) / 2) * var8;
                            this.renderHighlight(Math.min(var13, var14), var10, Math.max(var13, var14), var10 + 9);
                        }
                    }
                }
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        if (param5 >= 0) {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }

    }

    private ResourceLocation getTexture(Block param0) {
        if (param0 == Blocks.OAK_SIGN || param0 == Blocks.OAK_WALL_SIGN) {
            return OAK_TEXTURE;
        } else if (param0 == Blocks.SPRUCE_SIGN || param0 == Blocks.SPRUCE_WALL_SIGN) {
            return SPRUCE_TEXTURE;
        } else if (param0 == Blocks.BIRCH_SIGN || param0 == Blocks.BIRCH_WALL_SIGN) {
            return BIRCH_TEXTURE;
        } else if (param0 == Blocks.ACACIA_SIGN || param0 == Blocks.ACACIA_WALL_SIGN) {
            return ACACIA_TEXTURE;
        } else if (param0 == Blocks.JUNGLE_SIGN || param0 == Blocks.JUNGLE_WALL_SIGN) {
            return JUNGLE_TEXTURE;
        } else {
            return param0 != Blocks.DARK_OAK_SIGN && param0 != Blocks.DARK_OAK_WALL_SIGN ? OAK_TEXTURE : DARK_OAK_TEXTURE;
        }
    }

    private void renderHighlight(int param0, int param1, int param2, int param3) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        GlStateManager.color4f(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture();
        GlStateManager.enableColorLogicOp();
        GlStateManager.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        var1.begin(7, DefaultVertexFormat.POSITION);
        var1.vertex((double)param0, (double)param3, 0.0).endVertex();
        var1.vertex((double)param2, (double)param3, 0.0).endVertex();
        var1.vertex((double)param2, (double)param1, 0.0).endVertex();
        var1.vertex((double)param0, (double)param1, 0.0).endVertex();
        var0.end();
        GlStateManager.disableColorLogicOp();
        GlStateManager.enableTexture();
    }
}
