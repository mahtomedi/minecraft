package net.minecraft.client.gui.components.toasts;

import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdvancementToast implements Toast {
    public static final int DISPLAY_TIME = 5000;
    private final Advancement advancement;
    private boolean playedSound;

    public AdvancementToast(Advancement param0) {
        this.advancement = param0;
    }

    @Override
    public Toast.Visibility render(GuiGraphics param0, ToastComponent param1, long param2) {
        DisplayInfo var0 = this.advancement.getDisplay();
        param0.blit(TEXTURE, 0, 0, 0, 0, this.width(), this.height());
        if (var0 != null) {
            List<FormattedCharSequence> var1 = param1.getMinecraft().font.split(var0.getTitle(), 125);
            int var2 = var0.getFrame() == FrameType.CHALLENGE ? 16746751 : 16776960;
            if (var1.size() == 1) {
                param0.drawString(param1.getMinecraft().font, var0.getFrame().getDisplayName(), 30, 7, var2 | 0xFF000000, false);
                param0.drawString(param1.getMinecraft().font, var1.get(0), 30, 18, -1, false);
            } else {
                int var3 = 1500;
                float var4 = 300.0F;
                if (param2 < 1500L) {
                    int var5 = Mth.floor(Mth.clamp((float)(1500L - param2) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
                    param0.drawString(param1.getMinecraft().font, var0.getFrame().getDisplayName(), 30, 11, var2 | var5, false);
                } else {
                    int var6 = Mth.floor(Mth.clamp((float)(param2 - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
                    int var7 = this.height() / 2 - var1.size() * 9 / 2;

                    for(FormattedCharSequence var8 : var1) {
                        param0.drawString(param1.getMinecraft().font, var8, 30, var7, 16777215 | var6, false);
                        var7 += 9;
                    }
                }
            }

            if (!this.playedSound && param2 > 0L) {
                this.playedSound = true;
                if (var0.getFrame() == FrameType.CHALLENGE) {
                    param1.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
                }
            }

            param0.renderFakeItem(var0.getIcon(), 8, 8);
            return (double)param2 >= 5000.0 * param1.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
        } else {
            return Toast.Visibility.HIDE;
        }
    }
}
