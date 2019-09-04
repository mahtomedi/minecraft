package net.minecraft.client.gui.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SystemToast implements Toast {
    private final SystemToast.SystemToastIds id;
    private String title;
    private String message;
    private long lastChanged;
    private boolean changed;

    public SystemToast(SystemToast.SystemToastIds param0, Component param1, @Nullable Component param2) {
        this.id = param0;
        this.title = param1.getString();
        this.message = param2 == null ? null : param2.getString();
    }

    @Override
    public Toast.Visibility render(ToastComponent param0, long param1) {
        if (this.changed) {
            this.lastChanged = param1;
            this.changed = false;
        }

        param0.getMinecraft().getTextureManager().bind(TEXTURE);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        param0.blit(0, 0, 0, 64, 160, 32);
        if (this.message == null) {
            param0.getMinecraft().font.draw(this.title, 18.0F, 12.0F, -256);
        } else {
            param0.getMinecraft().font.draw(this.title, 18.0F, 7.0F, -256);
            param0.getMinecraft().font.draw(this.message, 18.0F, 18.0F, -1);
        }

        return param1 - this.lastChanged < 5000L ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    public void reset(Component param0, @Nullable Component param1) {
        this.title = param0.getString();
        this.message = param1 == null ? null : param1.getString();
        this.changed = true;
    }

    public SystemToast.SystemToastIds getToken() {
        return this.id;
    }

    public static void addOrUpdate(ToastComponent param0, SystemToast.SystemToastIds param1, Component param2, @Nullable Component param3) {
        SystemToast var0 = param0.getToast(SystemToast.class, param1);
        if (var0 == null) {
            param0.addToast(new SystemToast(param1, param2, param3));
        } else {
            var0.reset(param2, param3);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static enum SystemToastIds {
        TUTORIAL_HINT,
        NARRATOR_TOGGLE,
        WORLD_BACKUP;
    }
}
