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
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageLink;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class ChatReportBuilder {
    private final ChatReportBuilder.ChatReport report;
    private final AbuseReportLimits limits;

    public ChatReportBuilder(ChatReportBuilder.ChatReport param0, AbuseReportLimits param1) {
        this.report = param0;
        this.limits = param1;
    }

    public ChatReportBuilder(UUID param0, AbuseReportLimits param1) {
        this.report = new ChatReportBuilder.ChatReport(UUID.randomUUID(), Instant.now(), param0);
        this.limits = param1;
    }

    public ChatReportBuilder.ChatReport report() {
        return this.report;
    }

    public UUID reportedProfileId() {
        return this.report.reportedProfileId;
    }

    public IntSet reportedMessages() {
        return this.report.reportedMessages;
    }

    public String comments() {
        return this.report.comments;
    }

    public void setComments(String param0) {
        this.report.comments = param0;
    }

    @Nullable
    public ReportReason reason() {
        return this.report.reason;
    }

    public void setReason(ReportReason param0) {
        this.report.reason = param0;
    }

    public void toggleReported(int param0) {
        this.report.toggleReported(param0, this.limits);
    }

    public boolean isReported(int param0) {
        return this.report.reportedMessages.contains(param0);
    }

    public boolean hasContent() {
        return StringUtils.isNotEmpty(this.comments()) || !this.reportedMessages().isEmpty() || this.reason() != null;
    }

    @Nullable
    public ChatReportBuilder.CannotBuildReason checkBuildable() {
        if (this.report.reportedMessages.isEmpty()) {
            return ChatReportBuilder.CannotBuildReason.NO_REPORTED_MESSAGES;
        } else if (this.report.reportedMessages.size() > this.limits.maxReportedMessageCount()) {
            return ChatReportBuilder.CannotBuildReason.TOO_MANY_MESSAGES;
        } else if (this.report.reason == null) {
            return ChatReportBuilder.CannotBuildReason.NO_REASON;
        } else {
            return this.report.comments.length() > this.limits.maxOpinionCommentsLength() ? ChatReportBuilder.CannotBuildReason.COMMENTS_TOO_LONG : null;
        }
    }

    public Either<ChatReportBuilder.Result, ChatReportBuilder.CannotBuildReason> build(ReportingContext param0) {
        ChatReportBuilder.CannotBuildReason var0 = this.checkBuildable();
        if (var0 != null) {
            return Either.right(var0);
        } else {
            String var1 = Objects.requireNonNull(this.report.reason).backendName();
            ReportEvidence var2 = this.buildEvidence(param0.chatLog());
            ReportedEntity var3 = new ReportedEntity(this.report.reportedProfileId);
            AbuseReport var4 = new AbuseReport(this.report.comments, var1, var2, var3, this.report.createdAt);
            return Either.left(new ChatReportBuilder.Result(this.report.reportId, var4));
        }
    }

    private ReportEvidence buildEvidence(ChatLog param0) {
        List<ReportChatMessage> var0 = new ArrayList<>();
        ChatReportContextBuilder var1 = new ChatReportContextBuilder(this.limits.leadingContextMessageCount());
        var1.collectAllContext(
            param0, this.report.reportedMessages, (param1, param2) -> var0.add(this.buildReportedChatMessage(param2, this.isReported(param1)))
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

    public ChatReportBuilder copy() {
        return new ChatReportBuilder(this.report.copy(), this.limits);
    }

    @OnlyIn(Dist.CLIENT)
    public static record CannotBuildReason(Component message) {
        public static final ChatReportBuilder.CannotBuildReason NO_REASON = new ChatReportBuilder.CannotBuildReason(
            Component.translatable("gui.chatReport.send.no_reason")
        );
        public static final ChatReportBuilder.CannotBuildReason NO_REPORTED_MESSAGES = new ChatReportBuilder.CannotBuildReason(
            Component.translatable("gui.chatReport.send.no_reported_messages")
        );
        public static final ChatReportBuilder.CannotBuildReason TOO_MANY_MESSAGES = new ChatReportBuilder.CannotBuildReason(
            Component.translatable("gui.chatReport.send.too_many_messages")
        );
        public static final ChatReportBuilder.CannotBuildReason COMMENTS_TOO_LONG = new ChatReportBuilder.CannotBuildReason(
            Component.translatable("gui.chatReport.send.comments_too_long")
        );
    }

    @OnlyIn(Dist.CLIENT)
    public class ChatReport {
        final UUID reportId;
        final Instant createdAt;
        final UUID reportedProfileId;
        final IntSet reportedMessages = new IntOpenHashSet();
        String comments = "";
        @Nullable
        ReportReason reason;

        ChatReport(UUID param1, Instant param2, UUID param3) {
            this.reportId = param1;
            this.createdAt = param2;
            this.reportedProfileId = param3;
        }

        public void toggleReported(int param0, AbuseReportLimits param1) {
            if (this.reportedMessages.contains(param0)) {
                this.reportedMessages.remove(param0);
            } else if (this.reportedMessages.size() < param1.maxReportedMessageCount()) {
                this.reportedMessages.add(param0);
            }

        }

        public ChatReportBuilder.ChatReport copy() {
            ChatReportBuilder.ChatReport var0 = ChatReportBuilder.this.new ChatReport(this.reportId, this.createdAt, this.reportedProfileId);
            var0.reportedMessages.addAll(this.reportedMessages);
            var0.comments = this.comments;
            var0.reason = this.reason;
            return var0;
        }

        public boolean isReportedPlayer(UUID param0) {
            return param0.equals(this.reportedProfileId);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record Result(UUID id, AbuseReport report) {
    }
}
