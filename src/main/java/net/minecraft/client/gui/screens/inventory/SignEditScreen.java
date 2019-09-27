package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SignEditScreen extends Screen {
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
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)(this.width / 2), 0.0F, 50.0F);
        float var0 = 93.75F;
        RenderSystem.scalef(-93.75F, -93.75F, -93.75F);
        RenderSystem.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
        BlockState var1 = this.sign.getBlockState();
        float var2;
        if (var1.getBlock() instanceof StandingSignBlock) {
            var2 = (float)(var1.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F;
        } else {
            var2 = var1.getValue(WallSignBlock.FACING).toYRot();
        }

        RenderSystem.rotatef(var2, 0.0F, 1.0F, 0.0F);
        RenderSystem.translatef(0.0F, -1.0625F, 0.0F);
        this.sign.setCursorInfo(this.line, this.signField.getCursorPos(), this.signField.getSelectionPos(), this.frame / 6 % 2 == 0);
        RenderSystem.translatef(-0.5F, -0.75F, -0.5F);
        BlockEntityRenderDispatcher.instance.renderItem(this.sign, new PoseStack(), 15728880);
        this.sign.resetCursorInfo();
        RenderSystem.popMatrix();
        super.render(param0, param1, param2);
    }
}
