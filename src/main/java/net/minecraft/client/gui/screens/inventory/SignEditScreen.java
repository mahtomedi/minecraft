package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SignEditScreen extends Screen {
    private final SignRenderer.SignModel signModel = new SignRenderer.SignModel();
    private final SignBlockEntity sign;
    private int frame;
    private int line;
    private TextFieldHelper signField;

    public SignEditScreen(SignBlockEntity param0) {
        super(new TranslatableComponent("sign.edit"));
        this.sign = param0;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, I18n.get("gui.done"), param0 -> this.onDone()));
        this.sign.setEditable(false);
        this.signField = new TextFieldHelper(
            this.minecraft, () -> this.sign.getMessage(this.line).getString(), param0 -> this.sign.setMessage(this.line, new TextComponent(param0)), 90
        );
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        ClientPacketListener var0 = this.minecraft.getConnection();
        if (var0 != null) {
            var0.send(
                new ServerboundSignUpdatePacket(
                    this.sign.getBlockPos(), this.sign.getMessage(0), this.sign.getMessage(1), this.sign.getMessage(2), this.sign.getMessage(3)
                )
            );
        }

        this.sign.setEditable(true);
    }

    @Override
    public void tick() {
        ++this.frame;
        if (!this.sign.getType().isValid(this.sign.getBlockState().getBlock())) {
            this.onDone();
        }

    }

    private void onDone() {
        this.sign.setChanged();
        this.minecraft.setScreen(null);
    }

    @Override
    public boolean charTyped(char param0, int param1) {
        this.signField.charTyped(param0);
        return true;
    }

    @Override
    public void onClose() {
        this.onDone();
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 265) {
            this.line = this.line - 1 & 3;
            this.signField.setEnd();
            return true;
        } else if (param0 == 264 || param0 == 257 || param0 == 335) {
            this.line = this.line + 1 & 3;
            this.signField.setEnd();
            return true;
        } else {
            return this.signField.keyPressed(param0) ? true : super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 40, 16777215);
        PoseStack var0 = new PoseStack();
        var0.pushPose();
        var0.translate((double)(this.width / 2), 0.0, 50.0);
        float var1 = 93.75F;
        var0.scale(93.75F, -93.75F, 93.75F);
        var0.translate(0.0, -1.3125, 0.0);
        BlockState var2 = this.sign.getBlockState();
        boolean var3 = var2.getBlock() instanceof StandingSignBlock;
        if (!var3) {
            var0.translate(0.0, -0.3125, 0.0);
        }

        boolean var4 = this.frame / 6 % 2 == 0;
        float var5 = 0.6666667F;
        var0.pushPose();
        var0.scale(0.6666667F, -0.6666667F, -0.6666667F);
        MultiBufferSource.BufferSource var6 = this.minecraft.renderBuffers().bufferSource();
        Material var7 = SignRenderer.getMaterial(var2.getBlock());
        VertexConsumer var8 = var7.buffer(var6, this.signModel::renderType);
        this.signModel.sign.render(var0, var8, 15728880, OverlayTexture.NO_OVERLAY);
        if (var3) {
            this.signModel.stick.render(var0, var8, 15728880, OverlayTexture.NO_OVERLAY);
        }

        var0.popPose();
        float var9 = 0.010416667F;
        var0.translate(0.0, 0.33333334F, 0.046666667F);
        var0.scale(0.010416667F, -0.010416667F, 0.010416667F);
        int var10 = this.sign.getColor().getTextColor();
        String[] var11 = new String[4];

        for(int var12 = 0; var12 < var11.length; ++var12) {
            var11[var12] = this.sign.getRenderMessage(var12, param0x -> {
                List<Component> var0x = ComponentRenderUtils.wrapComponents(param0x, 90, this.minecraft.font, false, true);
                return var0x.isEmpty() ? "" : var0x.get(0).getColoredString();
            });
        }

        Matrix4f var13 = var0.last().pose();
        int var14 = this.signField.getCursorPos();
        int var15 = this.signField.getSelectionPos();
        int var16 = this.minecraft.font.isBidirectional() ? -1 : 1;
        int var17 = this.line * 10 - this.sign.messages.length * 5;

        for(int var18 = 0; var18 < var11.length; ++var18) {
            String var19 = var11[var18];
            if (var19 != null) {
                float var20 = (float)(-this.minecraft.font.width(var19) / 2);
                this.minecraft
                    .font
                    .drawInBatch(var19, var20, (float)(var18 * 10 - this.sign.messages.length * 5), var10, false, var13, var6, false, 0, 15728880);
                if (var18 == this.line && var14 >= 0 && var4) {
                    int var21 = this.minecraft.font.width(var19.substring(0, Math.max(Math.min(var14, var19.length()), 0)));
                    int var22 = (var21 - this.minecraft.font.width(var19) / 2) * var16;
                    if (var14 >= var19.length()) {
                        this.minecraft.font.drawInBatch("_", (float)var22, (float)var17, var10, false, var13, var6, false, 0, 15728880);
                    }
                }
            }
        }

        var6.endBatch();

        for(int var23 = 0; var23 < var11.length; ++var23) {
            String var24 = var11[var23];
            if (var24 != null && var23 == this.line && var14 >= 0) {
                int var25 = this.minecraft.font.width(var24.substring(0, Math.max(Math.min(var14, var24.length()), 0)));
                int var26 = (var25 - this.minecraft.font.width(var24) / 2) * var16;
                if (var4 && var14 < var24.length()) {
                    fill(var13, var26, var17 - 1, var26 + 1, var17 + 9, 0xFF000000 | var10);
                }

                if (var15 != var14) {
                    int var27 = Math.min(var14, var15);
                    int var28 = Math.max(var14, var15);
                    int var29 = (this.minecraft.font.width(var24.substring(0, var27)) - this.minecraft.font.width(var24) / 2) * var16;
                    int var30 = (this.minecraft.font.width(var24.substring(0, var28)) - this.minecraft.font.width(var24) / 2) * var16;
                    int var31 = Math.min(var29, var30);
                    int var32 = Math.max(var29, var30);
                    Tesselator var33 = Tesselator.getInstance();
                    BufferBuilder var34 = var33.getBuilder();
                    RenderSystem.disableTexture();
                    RenderSystem.enableColorLogicOp();
                    RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
                    var34.begin(7, DefaultVertexFormat.POSITION_COLOR);
                    var34.vertex(var13, (float)var31, (float)(var17 + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    var34.vertex(var13, (float)var32, (float)(var17 + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    var34.vertex(var13, (float)var32, (float)var17, 0.0F).color(0, 0, 255, 255).endVertex();
                    var34.vertex(var13, (float)var31, (float)var17, 0.0F).color(0, 0, 255, 255).endVertex();
                    var34.end();
                    BufferUploader.end(var34);
                    RenderSystem.disableColorLogicOp();
                    RenderSystem.enableTexture();
                }
            }
        }

        var0.popPose();
        super.render(param0, param1, param2);
    }
}
