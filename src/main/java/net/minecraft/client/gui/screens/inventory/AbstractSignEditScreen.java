package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSignEditScreen extends Screen {
    private final SignBlockEntity sign;
    private SignText text;
    private final String[] messages;
    private final boolean isFrontText;
    protected final WoodType woodType;
    private int frame;
    private int line;
    @Nullable
    private TextFieldHelper signField;

    public AbstractSignEditScreen(SignBlockEntity param0, boolean param1, boolean param2) {
        this(param0, param1, param2, Component.translatable("sign.edit"));
    }

    public AbstractSignEditScreen(SignBlockEntity param0, boolean param1, boolean param2, Component param3) {
        super(param3);
        this.sign = param0;
        this.text = param0.getText(param1);
        this.isFrontText = param1;
        this.woodType = SignBlock.getWoodType(param0.getBlockState().getBlock());
        this.messages = IntStream.range(0, 4)
            .mapToObj(param1x -> this.text.getMessage(param1x, param2))
            .map(Component::getString)
            .toArray(param0x -> new String[param0x]);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, param0 -> this.onDone()).bounds(this.width / 2 - 100, this.height / 4 + 144, 200, 20).build()
        );
        this.signField = new TextFieldHelper(
            () -> this.messages[this.line],
            this::setMessage,
            TextFieldHelper.createClipboardGetter(this.minecraft),
            TextFieldHelper.createClipboardSetter(this.minecraft),
            param0 -> this.minecraft.font.width(param0) <= this.sign.getMaxTextLineWidth()
        );
    }

    @Override
    public void tick() {
        ++this.frame;
        if (!this.isValid()) {
            this.onDone();
        }

    }

    private boolean isValid() {
        return this.minecraft != null
            && this.minecraft.player != null
            && !this.sign.isRemoved()
            && !this.sign.playerIsTooFarAwayToEdit(this.minecraft.player.getUUID());
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
    public boolean charTyped(char param0, int param1) {
        this.signField.charTyped(param0);
        return true;
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        Lighting.setupForFlatItems();
        this.renderBackground(param0);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 40, 16777215);
        this.renderSign(param0);
        Lighting.setupFor3DItems();
        super.render(param0, param1, param2, param3);
    }

    @Override
    public void onClose() {
        this.onDone();
    }

    @Override
    public void removed() {
        ClientPacketListener var0 = this.minecraft.getConnection();
        if (var0 != null) {
            var0.send(
                new ServerboundSignUpdatePacket(
                    this.sign.getBlockPos(), this.isFrontText, this.messages[0], this.messages[1], this.messages[2], this.messages[3]
                )
            );
        }

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected abstract void renderSignBackground(GuiGraphics var1, BlockState var2);

    protected abstract Vector3f getSignTextScale();

    protected void offsetSign(GuiGraphics param0, BlockState param1) {
        param0.pose().translate((float)this.width / 2.0F, 90.0F, 50.0F);
    }

    private void renderSign(GuiGraphics param0) {
        BlockState var0 = this.sign.getBlockState();
        param0.pose().pushPose();
        this.offsetSign(param0, var0);
        param0.pose().pushPose();
        this.renderSignBackground(param0, var0);
        param0.pose().popPose();
        this.renderSignText(param0);
        param0.pose().popPose();
    }

    private void renderSignText(GuiGraphics param0) {
        param0.pose().translate(0.0F, 0.0F, 4.0F);
        Vector3f var0 = this.getSignTextScale();
        param0.pose().scale(var0.x(), var0.y(), var0.z());
        int var1 = this.text.getColor().getTextColor();
        boolean var2 = this.frame / 6 % 2 == 0;
        int var3 = this.signField.getCursorPos();
        int var4 = this.signField.getSelectionPos();
        int var5 = 4 * this.sign.getTextLineHeight() / 2;
        int var6 = this.line * this.sign.getTextLineHeight() - var5;

        for(int var7 = 0; var7 < this.messages.length; ++var7) {
            String var8 = this.messages[var7];
            if (var8 != null) {
                if (this.font.isBidirectional()) {
                    var8 = this.font.bidirectionalShaping(var8);
                }

                int var9 = -this.font.width(var8) / 2;
                param0.drawString(this.font, var8, var9, var7 * this.sign.getTextLineHeight() - var5, var1, false);
                if (var7 == this.line && var3 >= 0 && var2) {
                    int var10 = this.font.width(var8.substring(0, Math.max(Math.min(var3, var8.length()), 0)));
                    int var11 = var10 - this.font.width(var8) / 2;
                    if (var3 >= var8.length()) {
                        param0.drawString(this.font, "_", var11, var6, var1, false);
                    }
                }
            }
        }

        for(int var12 = 0; var12 < this.messages.length; ++var12) {
            String var13 = this.messages[var12];
            if (var13 != null && var12 == this.line && var3 >= 0) {
                int var14 = this.font.width(var13.substring(0, Math.max(Math.min(var3, var13.length()), 0)));
                int var15 = var14 - this.font.width(var13) / 2;
                if (var2 && var3 < var13.length()) {
                    param0.fill(var15, var6 - 1, var15 + 1, var6 + this.sign.getTextLineHeight(), 0xFF000000 | var1);
                }

                if (var4 != var3) {
                    int var16 = Math.min(var3, var4);
                    int var17 = Math.max(var3, var4);
                    int var18 = this.font.width(var13.substring(0, var16)) - this.font.width(var13) / 2;
                    int var19 = this.font.width(var13.substring(0, var17)) - this.font.width(var13) / 2;
                    int var20 = Math.min(var18, var19);
                    int var21 = Math.max(var18, var19);
                    param0.fill(RenderType.guiTextHighlight(), var20, var6, var21, var6 + this.sign.getTextLineHeight(), -16776961);
                }
            }
        }

    }

    private void setMessage(String param0) {
        this.messages[this.line] = param0;
        this.text = this.text.setMessage(this.line, Component.literal(param0));
        this.sign.setText(this.text, this.isFrontText);
    }

    private void onDone() {
        this.minecraft.setScreen(null);
    }
}
