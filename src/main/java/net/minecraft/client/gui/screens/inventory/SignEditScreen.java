package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import java.util.Arrays;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.CommonComponents;
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
    private final String[] messages = Util.make(new String[4], param0x -> Arrays.fill(param0x, ""));

    public SignEditScreen(SignBlockEntity param0) {
        super(new TranslatableComponent("sign.edit"));
        this.sign = param0;
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120, 200, 20, CommonComponents.GUI_DONE, param0 -> this.onDone()));
        this.sign.setEditable(false);
        this.signField = new TextFieldHelper(
            () -> this.messages[this.line],
            param0 -> {
                this.messages[this.line] = param0;
                this.sign.setMessage(this.line, new TextComponent(param0));
            },
            TextFieldHelper.createClipboardGetter(this.minecraft),
            TextFieldHelper.createClipboardSetter(this.minecraft),
            param0 -> this.minecraft.font.width(param0) <= 90
        );
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        ClientPacketListener var0 = this.minecraft.getConnection();
        if (var0 != null) {
            var0.send(new ServerboundSignUpdatePacket(this.sign.getBlockPos(), this.messages[0], this.messages[1], this.messages[2], this.messages[3]));
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
            this.signField.setCursorToEnd();
            return true;
        } else if (param0 == 264 || param0 == 257 || param0 == 335) {
            this.line = this.line + 1 & 3;
            this.signField.setCursorToEnd();
            return true;
        } else {
            return this.signField.keyPressed(param0) ? true : super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        Lighting.setupForFlatItems();
        this.renderBackground(param0);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 40, 16777215);
        param0.pushPose();
        param0.translate((double)(this.width / 2), 0.0, 50.0);
        float var0 = 93.75F;
        param0.scale(93.75F, -93.75F, 93.75F);
        param0.translate(0.0, -1.3125, 0.0);
        BlockState var1 = this.sign.getBlockState();
        boolean var2 = var1.getBlock() instanceof StandingSignBlock;
        if (!var2) {
            param0.translate(0.0, -0.3125, 0.0);
        }

        boolean var3 = this.frame / 6 % 2 == 0;
        float var4 = 0.6666667F;
        param0.pushPose();
        param0.scale(0.6666667F, -0.6666667F, -0.6666667F);
        MultiBufferSource.BufferSource var5 = this.minecraft.renderBuffers().bufferSource();
        Material var6 = SignRenderer.getMaterial(var1.getBlock());
        VertexConsumer var7 = var6.buffer(var5, this.signModel::renderType);
        this.signModel.sign.render(param0, var7, 15728880, OverlayTexture.NO_OVERLAY);
        if (var2) {
            this.signModel.stick.render(param0, var7, 15728880, OverlayTexture.NO_OVERLAY);
        }

        param0.popPose();
        float var8 = 0.010416667F;
        param0.translate(0.0, 0.33333334F, 0.046666667F);
        param0.scale(0.010416667F, -0.010416667F, 0.010416667F);
        int var9 = this.sign.getColor().getTextColor();
        int var10 = this.signField.getCursorPos();
        int var11 = this.signField.getSelectionPos();
        int var12 = this.line * 10 - this.messages.length * 5;
        Matrix4f var13 = param0.last().pose();

        for(int var14 = 0; var14 < this.messages.length; ++var14) {
            String var15 = this.messages[var14];
            if (var15 != null) {
                if (this.font.isBidirectional()) {
                    var15 = this.font.bidirectionalShaping(var15);
                }

                float var16 = (float)(-this.minecraft.font.width(var15) / 2);
                this.minecraft
                    .font
                    .drawInBatch(var15, var16, (float)(var14 * 10 - this.messages.length * 5), var9, false, var13, var5, false, 0, 15728880, false);
                if (var14 == this.line && var10 >= 0 && var3) {
                    int var17 = this.minecraft.font.width(var15.substring(0, Math.max(Math.min(var10, var15.length()), 0)));
                    int var18 = var17 - this.minecraft.font.width(var15) / 2;
                    if (var10 >= var15.length()) {
                        this.minecraft.font.drawInBatch("_", (float)var18, (float)var12, var9, false, var13, var5, false, 0, 15728880, false);
                    }
                }
            }
        }

        var5.endBatch();

        for(int var19 = 0; var19 < this.messages.length; ++var19) {
            String var20 = this.messages[var19];
            if (var20 != null && var19 == this.line && var10 >= 0) {
                int var21 = this.minecraft.font.width(var20.substring(0, Math.max(Math.min(var10, var20.length()), 0)));
                int var22 = var21 - this.minecraft.font.width(var20) / 2;
                if (var3 && var10 < var20.length()) {
                    fill(param0, var22, var12 - 1, var22 + 1, var12 + 9, 0xFF000000 | var9);
                }

                if (var11 != var10) {
                    int var23 = Math.min(var10, var11);
                    int var24 = Math.max(var10, var11);
                    int var25 = this.minecraft.font.width(var20.substring(0, var23)) - this.minecraft.font.width(var20) / 2;
                    int var26 = this.minecraft.font.width(var20.substring(0, var24)) - this.minecraft.font.width(var20) / 2;
                    int var27 = Math.min(var25, var26);
                    int var28 = Math.max(var25, var26);
                    Tesselator var29 = Tesselator.getInstance();
                    BufferBuilder var30 = var29.getBuilder();
                    RenderSystem.disableTexture();
                    RenderSystem.enableColorLogicOp();
                    RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
                    var30.begin(7, DefaultVertexFormat.POSITION_COLOR);
                    var30.vertex(var13, (float)var27, (float)(var12 + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    var30.vertex(var13, (float)var28, (float)(var12 + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    var30.vertex(var13, (float)var28, (float)var12, 0.0F).color(0, 0, 255, 255).endVertex();
                    var30.vertex(var13, (float)var27, (float)var12, 0.0F).color(0, 0, 255, 255).endVertex();
                    var30.end();
                    BufferUploader.end(var30);
                    RenderSystem.disableColorLogicOp();
                    RenderSystem.enableTexture();
                }
            }
        }

        param0.popPose();
        Lighting.setupFor3DItems();
        super.render(param0, param1, param2, param3);
    }
}
