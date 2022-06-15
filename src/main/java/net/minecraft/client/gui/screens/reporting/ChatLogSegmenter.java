package net.minecraft.client.gui.screens.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.chat.LoggedChat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatLogSegmenter {
    private final Function<LoggedChat.WithId, ChatLogSegmenter.MessageType> typeFunction;
    private final List<LoggedChat.WithId> messages = new ArrayList<>();
    @Nullable
    private ChatLogSegmenter.MessageType segmentType;

    public ChatLogSegmenter(Function<LoggedChat.WithId, ChatLogSegmenter.MessageType> param0) {
        this.typeFunction = param0;
    }

    public boolean accept(LoggedChat.WithId param0) {
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
    public ChatLogSegmenter.Results build() {
        return !this.messages.isEmpty() && this.segmentType != null ? new ChatLogSegmenter.Results(this.messages, this.segmentType) : null;
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
    public static record Results(List<LoggedChat.WithId> messages, ChatLogSegmenter.MessageType type) {
    }
}
