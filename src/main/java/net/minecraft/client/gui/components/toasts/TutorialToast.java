package net.minecraft.client.gui.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TutorialToast implements Toast {
    private final TutorialToast.Icons icon;
    private final String title;
    private final String message;
    private Toast.Visibility visibility = Toast.Visibility.SHOW;
    private long lastProgressTime;
    private float lastProgress;
    private float progress;
    private final boolean progressable;

    public TutorialToast(TutorialToast.Icons param0, Component param1, @Nullable Component param2, boolean param3) {
        this.icon = param0;
        this.title = param1.getColoredString();
        this.message = param2 == null ? null : param2.getColoredString();
        this.progressable = param3;
    }

    @Override
    public Toast.Visibility render(ToastComponent param0, long param1) {
        param0.getMinecraft().getTextureManager().bind(TEXTURE);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        param0.blit(0, 0, 0, 96, 160, 32);
        this.icon.render(param0, 6, 6);
        if (this.message == null) {
            param0.getMinecraft().font.draw(this.title, 30.0F, 12.0F, -11534256);
        } else {
            param0.getMinecraft().font.draw(this.title, 30.0F, 7.0F, -11534256);
            param0.getMinecraft().font.draw(this.message, 30.0F, 18.0F, -16777216);
        }

        if (this.progressable) {
            GuiComponent.fill(3, 28, 157, 29, -1);
            float var0 = (float)Mth.clampedLerp((double)this.lastProgress, (double)this.progress, (double)((float)(param1 - this.lastProgressTime) / 100.0F));
            int var1;
            if (this.progress >= this.lastProgress) {
                var1 = -16755456;
            } else {
                var1 = -11206656;
            }

            GuiComponent.fill(3, 28, (int)(3.0F + 154.0F * var0), 29, var1);
            this.lastProgress = var0;
            this.lastProgressTime = param1;
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
        MOVEMENT_KEYS(0, 0),
        MOUSE(1, 0),
        TREE(2, 0),
        RECIPE_BOOK(0, 1),
        WOODEN_PLANKS(1, 1);

        private final int x;
        private final int y;

        private Icons(int param0, int param1) {
            this.x = param0;
            this.y = param1;
        }

        public void render(GuiComponent param0, int param1, int param2) {
            RenderSystem.enableBlend();
            param0.blit(param1, param2, 176 + this.x * 20, this.y * 20, 20, 20);
            RenderSystem.enableBlend();
        }
    }
}
