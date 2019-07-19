package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PageButton extends Button {
    private final boolean isForward;
    private final boolean playTurnSound;

    public PageButton(int param0, int param1, boolean param2, Button.OnPress param3, boolean param4) {
        super(param0, param1, 23, 13, "", param3);
        this.isForward = param2;
        this.playTurnSound = param4;
    }

    @Override
    public void renderButton(int param0, int param1, float param2) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getInstance().getTextureManager().bind(BookViewScreen.BOOK_LOCATION);
        int var0 = 0;
        int var1 = 192;
        if (this.isHovered()) {
            var0 += 23;
        }

        if (!this.isForward) {
            var1 += 13;
        }

        this.blit(this.x, this.y, var0, var1, 23, 13);
    }

    @Override
    public void playDownSound(SoundManager param0) {
        if (this.playTurnSound) {
            param0.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }

    }
}
