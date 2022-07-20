package net.minecraft.client.gui.screens.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatLogSegmenter<T extends LoggedChatMessage> {
    private final Function<ChatLog.Entry<T>, ChatLogSegmenter.MessageType> typeFunction;
    private final List<ChatLog.Entry<T>> messages = new ArrayList<>();
    @Nullable
    private ChatLogSegmenter.MessageType segmentType;

    public ChatLogSegmenter(Function<ChatLog.Entry<T>, ChatLogSegmenter.MessageType> param0) {
        this.typeFunction = param0;
    }

    public boolean accept(ChatLog.Entry<T> param0) {
        ChatLogSegmenter.MessageType var0 = this.typeFunction.apply(param0);
        if (this.segmentType != null && var0 != this.segmentType) {
            return false;
        } else {
            this.segmentType = var0;
            this.messages.add(param0);
            return true;
        }
    }

    @Nullable
    public ChatLogSegmenter.Results<T> build() {
        return !this.messages.isEmpty() && this.segmentType != null ? new ChatLogSegmenter.Results<>(this.messages, this.segmentType) : null;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum MessageType {
        REPORTABLE,
        CONTEXT;

        public boolean foldable() {
            return this == CONTEXT;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record Results<T extends LoggedChatMessage>(List<ChatLog.Entry<T>> messages, ChatLogSegmenter.MessageType type) {
    }
}
