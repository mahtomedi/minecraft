package net.minecraft.client.gui.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
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
    public Toast.Visibility render(PoseStack param0, ToastComponent param1, long param2) {
        param1.getMinecraft().getTextureManager().bind(TEXTURE);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        DisplayInfo var0 = this.advancement.getDisplay();
        param1.blit(param0, 0, 0, 0, 0, 160, 32);
        if (var0 != null) {
            List<Component> var1 = param1.getMinecraft().font.split(var0.getTitle(), 125);
            int var2 = var0.getFrame() == FrameType.CHALLENGE ? 16746751 : 16776960;
            if (var1.size() == 1) {
                param1.getMinecraft().font.draw(param0, I18n.get("advancements.toast." + var0.getFrame().getName()), 30.0F, 7.0F, var2 | 0xFF000000);
                param1.getMinecraft().font.draw(param0, var1.get(0), 30.0F, 18.0F, -1);
            } else {
                int var3 = 1500;
                float var4 = 300.0F;
                if (param2 < 1500L) {
                    int var5 = Mth.floor(Mth.clamp((float)(1500L - param2) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
                    param1.getMinecraft().font.draw(param0, I18n.get("advancements.toast." + var0.getFrame().getName()), 30.0F, 11.0F, var2 | var5);
                } else {
                    int var6 = Mth.floor(Mth.clamp((float)(param2 - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
                    int var7 = 16 - var1.size() * 9 / 2;

                    for(Component var8 : var1) {
                        param1.getMinecraft().font.draw(param0, var8, 30.0F, (float)var7, 16777215 | var6);
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

            param1.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(var0.getIcon(), 8, 8);
            return param2 >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
        } else {
            return Toast.Visibility.HIDE;
        }
    }
}
