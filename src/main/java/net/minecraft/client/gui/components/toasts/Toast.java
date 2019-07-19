package net.minecraft.client.gui.components.toasts;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface Toast {
    ResourceLocation TEXTURE = new ResourceLocation("textures/gui/toasts.png");
    Object NO_TOKEN = new Object();

    Toast.Visibility render(ToastComponent var1, long var2);

    default Object getToken() {
        return NO_TOKEN;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Visibility {
        SHOW(SoundEvents.UI_TOAST_IN),
        HIDE(SoundEvents.UI_TOAST_OUT);

        private final SoundEvent soundEvent;

        private Visibility(SoundEvent param0) {
            this.soundEvent = param0;
        }

        public void playSound(SoundManager param0) {
            param0.play(SimpleSoundInstance.forUI(this.soundEvent, 1.0F, 1.0F));
        }
    }
}
