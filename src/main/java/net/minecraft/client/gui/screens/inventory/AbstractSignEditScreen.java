package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.stream.IntStream;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSignEditScreen extends Screen {
    protected final SignBlockEntity sign;
    protected final String[] messages;
    protected final WoodType woodType;
    private int frame;
    private int line;
    private TextFieldHelper signField;

    public AbstractSignEditScreen(SignBlockEntity param0, boolean param1) {
        this(param0, param1, Component.translatable("sign.edit"));
    }

    public AbstractSignEditScreen(SignBlockEntity param0, boolean param1, Component param2) {
        super(param2);
        this.woodType = SignBlock.getWoodType(param0.getBlockState().getBlock());
        this.messages = IntStream.range(0, 4)
            .mapToObj(param2x -> param0.getMessage(param2x, param1))
            .map(Component::getString)
            .toArray(param0x -> new String[param0x]);
        this.sign = param0;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, param0 -> this.onDone()).bounds(this.width / 2 - 100, this.height / 4 + 120, 200, 20).build()
        );
        this.sign.setEditable(false);
        this.signField = new TextFieldHelper(
            () -> this.messages[this.line],
            param0 -> {
                this.messages[this.line] = param0;
                this.sign.setMessage(this.line, Component.literal(param0));
            },
            TextFieldHelper.createClipboardGetter(this.minecraft),
            TextFieldHelper.createClipboardSetter(this.minecraft),
            param0 -> this.minecraft.font.width(param0) <= this.sign.getMaxTextLineWidth()
        );
    }

    @Override
    public void removed() {
        ClientPacketListener var0 = this.minecraft.getConnection();
        if (var0 != null) {
            var0.send(new ServerboundSignUpdatePacket(this.sign.getBlockPos(), this.messages[0], this.messages[1], this.messages[2], this.messages[3]));
        }

        this.sign.setEditable(true);
    }

    @Override
    public void tick() {
        ++this.frame;
        if (!this.sign.getType().isValid(this.sign.getBlockState())) {
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
        drawCenteredString(param0, this.font, this.title, this.width / 2, 40, 16777215);
        this.renderSign(param0);
        Lighting.setupFor3DItems();
        super.render(param0, param1, param2, param3);
    }

    protected abstract void renderSignBackground(PoseStack var1, MultiBufferSource.BufferSource var2, BlockState var3);

    protected abstract Vector3f getSignTextScale();

    protected void offsetSign(PoseStack param0, BlockState param1) {
        param0.translate((float)this.width / 2.0F, 90.0F, 50.0F);
    }

    private void renderSign(PoseStack param0) {
        MultiBufferSource.BufferSource var0 = this.minecraft.renderBuffers().bufferSource();
        BlockState var1 = this.sign.getBlockState();
        param0.pushPose();
        this.offsetSign(param0, var1);
        param0.pushPose();
        this.renderSignBackground(param0, var0, var1);
        param0.popPose();
        this.renderSignText(param0, var0);
        param0.popPose();
    }

    private void renderSignText(PoseStack param0, MultiBufferSource.BufferSource param1) {
        param0.translate(0.0F, 0.0F, 4.0F);
        Vector3f var0 = this.getSignTextScale();
        param0.scale(var0.x(), var0.y(), var0.z());
        int var1 = this.sign.getColor().getTextColor();
        boolean var2 = this.frame / 6 % 2 == 0;
        int var3 = this.signField.getCursorPos();
        int var4 = this.signField.getSelectionPos();
        int var5 = 4 * this.sign.getTextLineHeight() / 2;
        int var6 = this.line * this.sign.getTextLineHeight() - var5;
        Matrix4f var7 = param0.last().pose();

        for(int var8 = 0; var8 < this.messages.length; ++var8) {
            String var9 = this.messages[var8];
            if (var9 != null) {
                if (this.font.isBidirectional()) {
                    var9 = this.font.bidirectionalShaping(var9);
                }

                float var10 = (float)(-this.minecraft.font.width(var9) / 2);
                this.minecraft
                    .font
                    .drawInBatch(var9, var10, (float)(var8 * this.sign.getTextLineHeight() - var5), var1, false, var7, param1, false, 0, 15728880, false);
                if (var8 == this.line && var3 >= 0 && var2) {
                    int var11 = this.minecraft.font.width(var9.substring(0, Math.max(Math.min(var3, var9.length()), 0)));
                    int var12 = var11 - this.minecraft.font.width(var9) / 2;
                    if (var3 >= var9.length()) {
                        this.minecraft.font.drawInBatch("_", (float)var12, (float)var6, var1, false, var7, param1, false, 0, 15728880, false);
                    }
                }
            }
        }

        param1.endBatch();

        for(int var13 = 0; var13 < this.messages.length; ++var13) {
            String var14 = this.messages[var13];
            if (var14 != null && var13 == this.line && var3 >= 0) {
                int var15 = this.minecraft.font.width(var14.substring(0, Math.max(Math.min(var3, var14.length()), 0)));
                int var16 = var15 - this.minecraft.font.width(var14) / 2;
                if (var2 && var3 < var14.length()) {
                    fill(param0, var16, var6 - 1, var16 + 1, var6 + this.sign.getTextLineHeight(), 0xFF000000 | var1);
                }

                if (var4 != var3) {
                    int var17 = Math.min(var3, var4);
                    int var18 = Math.max(var3, var4);
                    int var19 = this.minecraft.font.width(var14.substring(0, var17)) - this.minecraft.font.width(var14) / 2;
                    int var20 = this.minecraft.font.width(var14.substring(0, var18)) - this.minecraft.font.width(var14) / 2;
                    int var21 = Math.min(var19, var20);
                    int var22 = Math.max(var19, var20);
                    Tesselator var23 = Tesselator.getInstance();
                    BufferBuilder var24 = var23.getBuilder();
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    RenderSystem.disableTexture();
                    RenderSystem.enableColorLogicOp();
                    RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
                    var24.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    var24.vertex(var7, (float)var21, (float)(var6 + this.sign.getTextLineHeight()), 0.0F).color(0, 0, 255, 255).endVertex();
                    var24.vertex(var7, (float)var22, (float)(var6 + this.sign.getTextLineHeight()), 0.0F).color(0, 0, 255, 255).endVertex();
                    var24.vertex(var7, (float)var22, (float)var6, 0.0F).color(0, 0, 255, 255).endVertex();
                    var24.vertex(var7, (float)var21, (float)var6, 0.0F).color(0, 0, 255, 255).endVertex();
                    BufferUploader.drawWithShader(var24.end());
                    RenderSystem.disableColorLogicOp();
                    RenderSystem.enableTexture();
                }
            }
        }

    }
}
