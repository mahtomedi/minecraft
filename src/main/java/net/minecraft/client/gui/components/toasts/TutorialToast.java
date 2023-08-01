package net.minecraft.client.gui.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TutorialToast implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE = new ResourceLocation("toast/tutorial");
    public static final int PROGRESS_BAR_WIDTH = 154;
    public static final int PROGRESS_BAR_HEIGHT = 1;
    public static final int PROGRESS_BAR_X = 3;
    public static final int PROGRESS_BAR_Y = 28;
    private final TutorialToast.Icons icon;
    private final Component title;
    @Nullable
    private final Component message;
    private Toast.Visibility visibility = Toast.Visibility.SHOW;
    private long lastProgressTime;
    private float lastProgress;
    private float progress;
    private final boolean progressable;

    public TutorialToast(TutorialToast.Icons param0, Component param1, @Nullable Component param2, boolean param3) {
        this.icon = param0;
        this.title = param1;
        this.message = param2;
        this.progressable = param3;
    }

    @Override
    public Toast.Visibility render(GuiGraphics param0, ToastComponent param1, long param2) {
        param0.blitSprite(BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
        this.icon.render(param0, 6, 6);
        if (this.message == null) {
            param0.drawString(param1.getMinecraft().font, this.title, 30, 12, -11534256, false);
        } else {
            param0.drawString(param1.getMinecraft().font, this.title, 30, 7, -11534256, false);
            param0.drawString(param1.getMinecraft().font, this.message, 30, 18, -16777216, false);
        }

        if (this.progressable) {
            param0.fill(3, 28, 157, 29, -1);
            float var0 = Mth.clampedLerp(this.lastProgress, this.progress, (float)(param2 - this.lastProgressTime) / 100.0F);
            int var1;
            if (this.progress >= this.lastProgress) {
                var1 = -16755456;
            } else {
                var1 = -11206656;
            }

            param0.fill(3, 28, (int)(3.0F + 154.0F * var0), 29, var1);
            this.lastProgress = var0;
            this.lastProgressTime = param2;
        }

        return this.visibility;
    }

    public void hide() {
        this.visibility = Toast.Visibility.HIDE;
    }

    public void updateProgress(float param0) {
        this.progress = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Icons {
        MOVEMENT_KEYS(new ResourceLocation("toast/movement_keys")),
        MOUSE(new ResourceLocation("toast/mouse")),
        TREE(new ResourceLocation("toast/tree")),
        RECIPE_BOOK(new ResourceLocation("toast/recipe_book")),
        WOODEN_PLANKS(new ResourceLocation("toast/wooden_planks")),
        SOCIAL_INTERACTIONS(new ResourceLocation("toast/social_interactions")),
        RIGHT_CLICK(new ResourceLocation("toast/right_click"));

        private final ResourceLocation sprite;

        private Icons(ResourceLocation param0) {
            this.sprite = param0;
        }

        public void render(GuiGraphics param0, int param1, int param2) {
            RenderSystem.enableBlend();
            param0.blitSprite(this.sprite, param1, param2, 20, 20);
        }
    }
}
