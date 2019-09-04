package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LockIconButton extends Button {
    private boolean locked;

    public LockIconButton(int param0, int param1, Button.OnPress param2) {
        super(param0, param1, 20, 20, I18n.get("narrator.button.difficulty_lock"), param2);
    }

    @Override
    protected String getNarrationMessage() {
        return super.getNarrationMessage()
            + ". "
            + (this.isLocked() ? I18n.get("narrator.button.difficulty_lock.locked") : I18n.get("narrator.button.difficulty_lock.unlocked"));
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean param0) {
        this.locked = param0;
    }

    @Override
    public void renderButton(int param0, int param1, float param2) {
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

        this.blit(this.x, this.y, var0.getX(), var0.getY(), this.width, this.height);
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
