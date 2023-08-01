package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LockIconButton extends Button {
    private boolean locked;

    public LockIconButton(int param0, int param1, Button.OnPress param2) {
        super(param0, param1, 20, 20, Component.translatable("narrator.button.difficulty_lock"), param2, DEFAULT_NARRATION);
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return CommonComponents.joinForNarration(
            super.createNarrationMessage(),
            this.isLocked()
                ? Component.translatable("narrator.button.difficulty_lock.locked")
                : Component.translatable("narrator.button.difficulty_lock.unlocked")
        );
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean param0) {
        this.locked = param0;
    }

    @Override
    public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        LockIconButton.Icon var0;
        if (!this.active) {
            var0 = this.locked ? LockIconButton.Icon.LOCKED_DISABLED : LockIconButton.Icon.UNLOCKED_DISABLED;
        } else if (this.isHoveredOrFocused()) {
            var0 = this.locked ? LockIconButton.Icon.LOCKED_HOVER : LockIconButton.Icon.UNLOCKED_HOVER;
        } else {
            var0 = this.locked ? LockIconButton.Icon.LOCKED : LockIconButton.Icon.UNLOCKED;
        }

        param0.blitSprite(var0.sprite, this.getX(), this.getY(), this.width, this.height);
    }

    @OnlyIn(Dist.CLIENT)
    static enum Icon {
        LOCKED(new ResourceLocation("widget/locked_button")),
        LOCKED_HOVER(new ResourceLocation("widget/locked_button_highlighted")),
        LOCKED_DISABLED(new ResourceLocation("widget/locked_button_disabled")),
        UNLOCKED(new ResourceLocation("widget/unlocked_button")),
        UNLOCKED_HOVER(new ResourceLocation("widget/unlocked_button_highlighted")),
        UNLOCKED_DISABLED(new ResourceLocation("widget/unlocked_button_disabled"));

        final ResourceLocation sprite;

        private Icon(ResourceLocation param0) {
            this.sprite = param0;
        }
    }
}
