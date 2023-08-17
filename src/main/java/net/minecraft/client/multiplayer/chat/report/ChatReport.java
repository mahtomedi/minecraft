package net.minecraft.client.multiplayer.chat.report;

import com.google.common.collect.Lists;
import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportChatMessage;
import com.mojang.authlib.minecraft.report.ReportEvidence;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.reporting.ChatReportScreen;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageLink;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class ChatReport extends Report {
    final IntSet reportedMessages = new IntOpenHashSet();

    ChatReport(UUID param0, Instant param1, UUID param2) {
        super(param0, param1, param2);
    }

    public void toggleReported(int param0, AbuseReportLimits param1) {
        if (this.reportedMessages.contains(param0)) {
            this.reportedMessages.remove(param0);
        } else if (this.reportedMessages.size() < param1.maxReportedMessageCount()) {
            this.reportedMessages.add(param0);
        }

    }

    public ChatReport copy() {
        ChatReport var0 = new ChatReport(this.reportId, this.createdAt, this.reportedProfileId);
        var0.reportedMessages.addAll(this.reportedMessages);
        var0.comments = this.comments;
        var0.reason = this.reason;
        return var0;
    }

    @Override
    public Screen createScreen(Screen param0, ReportingContext param1) {
        return new ChatReportScreen(param0, param1, this);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder extends Report.Builder<ChatReport> {
        public Builder(ChatReport param0, AbuseReportLimits param1) {
            super(param0, param1);
        }

        public Builder(UUID param0, AbuseReportLimits param1) {
            super(new ChatReport(UUID.randomUUID(), Instant.now(), param0), param1);
        }

        public IntSet reportedMessages() {
            return this.report.reportedMessages;
        }

        public void toggleReported(int param0) {
            this.report.toggleReported(param0, this.limits);
        }

        public boolean isReported(int param0) {
            return this.report.reportedMessages.contains(param0);
        }

        @Override
        public boolean hasContent() {
            return StringUtils.isNotEmpty(this.comments()) || !this.reportedMessages().isEmpty() || this.reason() != null;
        }

        @Nullable
        @Override
        public Report.CannotBuildReason checkBuildable() {
            if (this.report.reportedMessages.isEmpty()) {
                return Report.CannotBuildReason.NO_REPORTED_MESSAGES;
            } else if (this.report.reportedMessages.size() > this.limits.maxReportedMessageCount()) {
                return Report.CannotBuildReason.TOO_MANY_MESSAGES;
            } else if (this.report.reason == null) {
                return Report.CannotBuildReason.NO_REASON;
            } else {
                return this.report.comments.length() > this.limits.maxOpinionCommentsLength() ? Report.CannotBuildReason.COMMENT_TOO_LONG : null;
            }
        }

        @Override
        public Either<Report.Result, Report.CannotBuildReason> build(ReportingContext param0) {
            Report.CannotBuildReason var0 = this.checkBuildable();
            if (var0 != null) {
                return Either.right(var0);
            } else {
                String var1 = Objects.requireNonNull(this.report.reason).backendName();
                ReportEvidence var2 = this.buildEvidence(param0);
                ReportedEntity var3 = new ReportedEntity(this.report.reportedProfileId);
                AbuseReport var4 = AbuseReport.chat(this.report.comments, var1, var2, var3, this.report.createdAt);
                return Either.left(new Report.Result(this.report.reportId, ReportType.CHAT, var4));
            }
        }

        private ReportEvidence buildEvidence(ReportingContext param0) {
            List<ReportChatMessage> var0 = new ArrayList<>();
            ChatReportContextBuilder var1 = new ChatReportContextBuilder(this.limits.leadingContextMessageCount());
            var1.collectAllContext(
                param0.chatLog(), this.report.reportedMessages, (param1, param2) -> var0.add(this.buildReportedChatMessage(param2, this.isReported(param1)))
            );
            return new ReportEvidence(Lists.reverse(var0));
        }

        private ReportChatMessage buildReportedChatMessage(LoggedChatMessage.Player param0, boolean param1) {
            SignedMessageLink var0 = param0.message().link();
            SignedMessageBody var1 = param0.message().signedBody();
            List<ByteBuffer> var2 = var1.lastSeen().entries().stream().map(MessageSignature::asByteBuffer).toList();
            ByteBuffer var3 = Optionull.map(param0.message().signature(), MessageSignature::asByteBuffer);
            return new ReportChatMessage(var0.index(), var0.sender(), var0.sessionId(), var1.timeStamp(), var1.salt(), var2, var1.content(), var3, param1);
        }

        public ChatReport.Builder copy() {
            return new ChatReport.Builder(this.report.copy(), this.limits);
        }
    }
}
