package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PageButton extends Button {
    private static final ResourceLocation PAGE_FORWARD_HIGHLIGHTED_SPRITE = new ResourceLocation("widget/page_forward_highlighted");
    private static final ResourceLocation PAGE_FORWARD_SPRITE = new ResourceLocation("widget/page_forward");
    private static final ResourceLocation PAGE_BACKWARD_HIGHLIGHTED_SPRITE = new ResourceLocation("widget/page_backward_highlighted");
    private static final ResourceLocation PAGE_BACKWARD_SPRITE = new ResourceLocation("widget/page_backward");
    private final boolean isForward;
    private final boolean playTurnSound;

    public PageButton(int param0, int param1, boolean param2, Button.OnPress param3, boolean param4) {
        super(param0, param1, 23, 13, CommonComponents.EMPTY, param3, DEFAULT_NARRATION);
        this.isForward = param2;
        this.playTurnSound = param4;
    }

    @Override
    public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        ResourceLocation var0;
        if (this.isForward) {
            var0 = this.isHoveredOrFocused() ? PAGE_FORWARD_HIGHLIGHTED_SPRITE : PAGE_FORWARD_SPRITE;
        } else {
            var0 = this.isHoveredOrFocused() ? PAGE_BACKWARD_HIGHLIGHTED_SPRITE : PAGE_BACKWARD_SPRITE;
        }

        param0.blitSprite(var0, this.getX(), this.getY(), 23, 13);
    }

    @Override
    public void playDownSound(SoundManager param0) {
        if (this.playTurnSound) {
            param0.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }

    }
}
