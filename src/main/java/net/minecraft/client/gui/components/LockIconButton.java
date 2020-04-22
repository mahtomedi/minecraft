package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LockIconButton extends Button {
    private boolean locked;

    public LockIconButton(int param0, int param1, Button.OnPress param2) {
        super(param0, param1, 20, 20, new TranslatableComponent("narrator.button.difficulty_lock"), param2);
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return super.createNarrationMessage()
            .append(". ")
            .append(
                this.isLocked()
                    ? new TranslatableComponent("narrator.button.difficulty_lock.locked")
                    : new TranslatableComponent("narrator.button.difficulty_lock.unlocked")
            );
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean param0) {
        this.locked = param0;
    }

    @Override
    public void renderButton(PoseStack param0, int param1, int param2, float param3) {
        Minecraft.getInstance().getTextureManager().bind(Button.WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        LockIconButton.Icon var0;
        if (!this.active) {
            var0 = this.locked ? LockIconButton.Icon.LOCKED_DISABLED : LockIconButton.Icon.UNLOCKED_DISABLED;
        } else if (this.isHovered()) {
            var0 = this.locked ? LockIconButton.Icon.LOCKED_HOVER : LockIconButton.Icon.UNLOCKED_HOVER;
        } else {
            var0 = this.locked ? LockIconButton.Icon.LOCKED : LockIconButton.Icon.UNLOCKED;
        }

        this.blit(param0, this.x, this.y, var0.getX(), var0.getY(), this.width, this.height);
    }

    @OnlyIn(Dist.CLIENT)
    static enum Icon {
        LOCKED(0, 146),
        LOCKED_HOVER(0, 166),
        LOCKED_DISABLED(0, 186),
        UNLOCKED(20, 146),
        UNLOCKED_HOVER(20, 166),
        UNLOCKED_DISABLED(20, 186);

        private final int x;
        private final int y;

        private Icon(int param0, int param1) {
            this.x = param0;
            this.y = param1;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }
    }
}
