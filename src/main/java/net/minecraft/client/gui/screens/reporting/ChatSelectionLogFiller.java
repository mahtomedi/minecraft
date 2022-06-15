package net.minecraft.client.gui.screens.reporting;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChat;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class ChatSelectionLogFiller {
    private static final int CONTEXT_FOLDED_SIZE = 4;
    private final ChatLog log;
    private final Predicate<LoggedChat> canReport;
    private int nextMessageId;

    public ChatSelectionLogFiller(ChatLog param0, Predicate<LoggedChat> param1) {
        this.log = param0;
        this.canReport = param1;
        this.nextMessageId = param0.newest();
    }

    public void fillNextPage(int param0, ChatSelectionLogFiller.Output param1) {
        int var0 = 0;

        while(var0 < param0) {
            ChatLogSegmenter.Results var1 = this.nextSegment();
            if (var1 == null) {
                break;
            }

            if (var1.type().foldable()) {
                var0 += addFoldedMessagesTo(var1.messages(), param1);
            } else {
                param1.acceptMessages(var1.messages());
                var0 += var1.messages().size();
            }
        }

    }

    private static int addFoldedMessagesTo(List<LoggedChat.WithId> param0, ChatSelectionLogFiller.Output param1) {
        int var0 = 8;
        if (param0.size() > 8) {
            int var1 = param0.size() - 8;
            param1.acceptMessages(param0.subList(0, 4));
            param1.acceptDivider(Component.translatable("gui.chatSelection.fold", var1));
            param1.acceptMessages(param0.subList(param0.size() - 4, param0.size()));
            return 9;
        } else {
            param1.acceptMessages(param0);
            return param0.size();
        }
    }

    @Nullable
    private ChatLogSegmenter.Results nextSegment() {
        ChatLogSegmenter var0 = new ChatLogSegmenter(param0 -> this.getMessageType(param0.message()));
        OptionalInt var1 = this.log
            .selectBefore(this.nextMessageId)
            .messagesWithIds()
            .takeWhile(var0::accept)
            .mapToInt(LoggedChat.WithId::id)
            .reduce((param0, param1) -> param1);
        if (var1.isPresent()) {
            this.nextMessageId = this.log.before(var1.getAsInt());
        }

        return var0.build();
    }

    private ChatLogSegmenter.MessageType getMessageType(LoggedChat param0) {
        return this.canReport.test(param0) ? ChatLogSegmenter.MessageType.REPORTABLE : ChatLogSegmenter.MessageType.CONTEXT;
    }

    @OnlyIn(Dist.CLIENT)
    public interface Output {
        default void acceptMessages(Iterable<LoggedChat.WithId> param0) {
            for(LoggedChat.WithId var0 : param0) {
                this.acceptMessage(var0.id(), var0.message());
            }

        }

        void acceptMessage(int var1, LoggedChat var2);

        void acceptDivider(Component var1);
    }
}
