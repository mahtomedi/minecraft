package net.minecraft.client.gui.screens.reporting;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.client.multiplayer.chat.report.ChatReportContextBuilder;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatSelectionLogFiller {
    private final ChatLog log;
    private final ChatReportContextBuilder contextBuilder;
    private final Predicate<LoggedChatMessage.Player> canReport;
    private int eventId;
    private int missedCount;
    @Nullable
    private PlayerChatMessage lastMessage;

    public ChatSelectionLogFiller(ReportingContext param0, Predicate<LoggedChatMessage.Player> param1) {
        this.log = param0.chatLog();
        this.contextBuilder = new ChatReportContextBuilder(param0.sender().reportLimits().leadingContextMessageCount());
        this.canReport = param1;
        this.eventId = this.log.end();
    }

    public void fillNextPage(int param0, ChatSelectionLogFiller.Output param1) {
        int var0 = 0;

        while(var0 < param0) {
            LoggedChatEvent var1 = this.log.lookup(this.eventId);
            if (var1 == null) {
                break;
            }

            int var2 = this.eventId--;
            if (var1 instanceof LoggedChatMessage.Player var3 && !var3.message().equals(this.lastMessage)) {
                if (this.acceptMessage(var3)) {
                    if (this.missedCount > 0) {
                        param1.acceptDivider(Component.translatable("gui.chatSelection.fold", this.missedCount));
                        this.missedCount = 0;
                    }

                    param1.acceptMessage(var2, var3);
                    ++var0;
                } else {
                    ++this.missedCount;
                }

                this.lastMessage = var3.message();
            }
        }

    }

    private boolean acceptMessage(LoggedChatMessage.Player param0) {
        PlayerChatMessage var0 = param0.message();
        boolean var1 = this.contextBuilder.acceptContext(var0);
        if (this.canReport.test(param0)) {
            this.contextBuilder.trackContext(var0);
            return true;
        } else {
            return var1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface Output {
        void acceptMessage(int var1, LoggedChatMessage.Player var2);

        void acceptDivider(Component var1);
    }
}
