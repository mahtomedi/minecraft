package net.minecraft.client;

import com.mojang.logging.LogUtils;
import com.mojang.text2speech.Narrator;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.main.SilentInitException;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class GameNarrator {
    public static final Component NO_TITLE = CommonComponents.EMPTY;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Minecraft minecraft;
    private final Narrator narrator = Narrator.getNarrator();

    public GameNarrator(Minecraft param0) {
        this.minecraft = param0;
    }

    public void sayChat(Component param0) {
        if (this.getStatus().shouldNarrateChat()) {
            String var0 = param0.getString();
            this.logNarratedMessage(var0);
            this.narrator.say(var0, false);
        }

    }

    public void say(Component param0) {
        String var0 = param0.getString();
        if (this.getStatus().shouldNarrateSystem() && !var0.isEmpty()) {
            this.logNarratedMessage(var0);
            this.narrator.say(var0, false);
        }

    }

    public void sayNow(Component param0) {
        this.sayNow(param0.getString());
    }

    public void sayNow(String param0) {
        if (this.getStatus().shouldNarrateSystem() && !param0.isEmpty()) {
            this.logNarratedMessage(param0);
            if (this.narrator.active()) {
                this.narrator.clear();
                this.narrator.say(param0, true);
            }
        }

    }

    private NarratorStatus getStatus() {
        return this.minecraft.options.narrator().get();
    }

    private void logNarratedMessage(String param0) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.debug("Narrating: {}", param0.replaceAll("\n", "\\\\n"));
        }

    }

    public void updateNarratorStatus(NarratorStatus param0) {
        this.clear();
        this.narrator.say(Component.translatable("options.narrator").append(" : ").append(param0.getName()).getString(), true);
        ToastComponent var0 = Minecraft.getInstance().getToasts();
        if (this.narrator.active()) {
            if (param0 == NarratorStatus.OFF) {
                SystemToast.addOrUpdate(var0, SystemToast.SystemToastId.NARRATOR_TOGGLE, Component.translatable("narrator.toast.disabled"), null);
            } else {
                SystemToast.addOrUpdate(var0, SystemToast.SystemToastId.NARRATOR_TOGGLE, Component.translatable("narrator.toast.enabled"), param0.getName());
            }
        } else {
            SystemToast.addOrUpdate(
                var0,
                SystemToast.SystemToastId.NARRATOR_TOGGLE,
                Component.translatable("narrator.toast.disabled"),
                Component.translatable("options.narrator.notavailable")
            );
        }

    }

    public boolean isActive() {
        return this.narrator.active();
    }

    public void clear() {
        if (this.getStatus() != NarratorStatus.OFF && this.narrator.active()) {
            this.narrator.clear();
        }
    }

    public void destroy() {
        this.narrator.destroy();
    }

    public void checkStatus(boolean param0) {
        if (param0
            && !this.isActive()
            && !TinyFileDialogs.tinyfd_messageBox(
                "Minecraft",
                "Failed to initialize text-to-speech library. Do you want to continue?\nIf this problem persists, please report it at bugs.mojang.com",
                "yesno",
                "error",
                true
            )) {
            throw new GameNarrator.NarratorInitException("Narrator library is not active");
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class NarratorInitException extends SilentInitException {
        public NarratorInitException(String param0) {
            super(param0);
        }
    }
}
