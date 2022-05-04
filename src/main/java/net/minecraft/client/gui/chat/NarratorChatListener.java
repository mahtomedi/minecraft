package net.minecraft.client.gui.chat;

import com.mojang.logging.LogUtils;
import com.mojang.text2speech.Narrator;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class NarratorChatListener implements ChatListener {
    public static final Component NO_TITLE = CommonComponents.EMPTY;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final NarratorChatListener INSTANCE = new NarratorChatListener();
    private final Narrator narrator = Narrator.getNarrator();

    @Override
    public void handle(ChatType param0, Component param1, @Nullable ChatSender param2) {
        NarratorStatus var0 = getStatus();
        if (var0 != NarratorStatus.OFF) {
            if (!this.narrator.active()) {
                this.logNarratedMessage(param1.getString());
            } else {
                param0.narration().ifPresent(param3 -> {
                    if (var0.shouldNarrate(param3.priority())) {
                        Component var0x = param3.decorate(param1, param2);
                        String var1x = var0x.getString();
                        this.logNarratedMessage(var1x);
                        this.narrator.say(var1x, param3.priority().interrupts());
                    }

                });
            }
        }
    }

    public void sayNow(Component param0) {
        this.sayNow(param0.getString());
    }

    public void sayNow(String param0) {
        NarratorStatus var0 = getStatus();
        if (var0 != NarratorStatus.OFF && var0 != NarratorStatus.CHAT && !param0.isEmpty()) {
            this.logNarratedMessage(param0);
            if (this.narrator.active()) {
                this.narrator.clear();
                this.narrator.say(param0, true);
            }
        }

    }

    private static NarratorStatus getStatus() {
        return Minecraft.getInstance().options.narrator().get();
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
                SystemToast.addOrUpdate(var0, SystemToast.SystemToastIds.NARRATOR_TOGGLE, Component.translatable("narrator.toast.disabled"), null);
            } else {
                SystemToast.addOrUpdate(var0, SystemToast.SystemToastIds.NARRATOR_TOGGLE, Component.translatable("narrator.toast.enabled"), param0.getName());
            }
        } else {
            SystemToast.addOrUpdate(
                var0,
                SystemToast.SystemToastIds.NARRATOR_TOGGLE,
                Component.translatable("narrator.toast.disabled"),
                Component.translatable("options.narrator.notavailable")
            );
        }

    }

    public boolean isActive() {
        return this.narrator.active();
    }

    public void clear() {
        if (getStatus() != NarratorStatus.OFF && this.narrator.active()) {
            this.narrator.clear();
        }
    }

    public void destroy() {
        this.narrator.destroy();
    }
}
