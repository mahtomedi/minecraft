package net.minecraft.client.gui.chat;

import com.mojang.text2speech.Narrator;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class NarratorChatListener implements ChatListener {
    public static final Component NO_TITLE = TextComponent.EMPTY;
    private static final Logger LOGGER = LogManager.getLogger();
    public static final NarratorChatListener INSTANCE = new NarratorChatListener();
    private final Narrator narrator = Narrator.getNarrator();

    @Override
    public void handle(ChatType param0, Component param1, UUID param2) {
        if (!Minecraft.getInstance().isBlocked(param2)) {
            NarratorStatus var0 = getStatus();
            if (var0 != NarratorStatus.OFF && this.narrator.active()) {
                if (var0 == NarratorStatus.ALL
                    || var0 == NarratorStatus.CHAT && param0 == ChatType.CHAT
                    || var0 == NarratorStatus.SYSTEM && param0 == ChatType.SYSTEM) {
                    Component var1;
                    if (param1 instanceof TranslatableComponent && "chat.type.text".equals(((TranslatableComponent)param1).getKey())) {
                        var1 = new TranslatableComponent("chat.type.text.narrate", ((TranslatableComponent)param1).getArgs());
                    } else {
                        var1 = param1;
                    }

                    this.doSay(param0.shouldInterrupt(), var1.getString());
                }

            }
        }
    }

    public void sayNow(String param0) {
        NarratorStatus var0 = getStatus();
        if (this.narrator.active() && var0 != NarratorStatus.OFF && var0 != NarratorStatus.CHAT && !param0.isEmpty()) {
            this.narrator.clear();
            this.doSay(true, param0);
        }

    }

    private static NarratorStatus getStatus() {
        return Minecraft.getInstance().options.narratorStatus;
    }

    private void doSay(boolean param0, String param1) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.debug("Narrating: {}", param1);
        }

        this.narrator.say(param1, param0);
    }

    public void updateNarratorStatus(NarratorStatus param0) {
        this.clear();
        this.narrator.say(new TranslatableComponent("options.narrator").append(" : ").append(param0.getName()).getString(), true);
        ToastComponent var0 = Minecraft.getInstance().getToasts();
        if (this.narrator.active()) {
            if (param0 == NarratorStatus.OFF) {
                SystemToast.addOrUpdate(var0, SystemToast.SystemToastIds.NARRATOR_TOGGLE, new TranslatableComponent("narrator.toast.disabled"), null);
            } else {
                SystemToast.addOrUpdate(var0, SystemToast.SystemToastIds.NARRATOR_TOGGLE, new TranslatableComponent("narrator.toast.enabled"), param0.getName());
            }
        } else {
            SystemToast.addOrUpdate(
                var0,
                SystemToast.SystemToastIds.NARRATOR_TOGGLE,
                new TranslatableComponent("narrator.toast.disabled"),
                new TranslatableComponent("options.narrator.notavailable")
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
