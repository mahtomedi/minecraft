package net.minecraft.client.gui.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdvancementToast implements Toast {
    private final Advancement advancement;
    private boolean playedSound;

    public AdvancementToast(Advancement param0) {
        this.advancement = param0;
    }

    @Override
    public Toast.Visibility render(ToastComponent param0, long param1) {
        param0.getMinecraft().getTextureManager().bind(TEXTURE);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        DisplayInfo var0 = this.advancement.getDisplay();
        param0.blit(0, 0, 0, 0, 160, 32);
        if (var0 != null) {
            List<String> var1 = param0.getMinecraft().font.split(var0.getTitle().getColoredString(), 125);
            int var2 = var0.getFrame() == FrameType.CHALLENGE ? 16746751 : 16776960;
            if (var1.size() == 1) {
                param0.getMinecraft().font.draw(I18n.get("advancements.toast." + var0.getFrame().getName()), 30.0F, 7.0F, var2 | 0xFF000000);
                param0.getMinecraft().font.draw(var0.getTitle().getColoredString(), 30.0F, 18.0F, -1);
            } else {
                int var3 = 1500;
                float var4 = 300.0F;
                if (param1 < 1500L) {
                    int var5 = Mth.floor(Mth.clamp((float)(1500L - param1) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
                    param0.getMinecraft().font.draw(I18n.get("advancements.toast." + var0.getFrame().getName()), 30.0F, 11.0F, var2 | var5);
                } else {
                    int var6 = Mth.floor(Mth.clamp((float)(param1 - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
                    int var7 = 16 - var1.size() * 9 / 2;

                    for(String var8 : var1) {
                        param0.getMinecraft().font.draw(var8, 30.0F, (float)var7, 16777215 | var6);
                        var7 += 9;
                    }
                }
            }

            if (!this.playedSound && param1 > 0L) {
                this.playedSound = true;
                if (var0.getFrame() == FrameType.CHALLENGE) {
                    param0.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
                }
            }

            param0.getMinecraft().getItemRenderer().renderAndDecorateItem(null, var0.getIcon(), 8, 8);
            return param1 >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
        } else {
            return Toast.Visibility.HIDE;
        }
    }
}
