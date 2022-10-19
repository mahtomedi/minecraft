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
import net.minecraft.Util;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageLink;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatReportBuilder {
    private final UUID reportId;
    private final Instant createdAt;
    private final UUID reportedProfileId;
    private final AbuseReportLimits limits;
    private final IntSet reportedMessages = new IntOpenHashSet();
    private String comments = "";
    @Nullable
    private ReportReason reason;

    private ChatReportBuilder(UUID param0, Instant param1, UUID param2, AbuseReportLimits param3) {
        this.reportId = param0;
        this.createdAt = param1;
        this.reportedProfileId = param2;
        this.limits = param3;
    }

    public ChatReportBuilder(UUID param0, AbuseReportLimits param1) {
        this(UUID.randomUUID(), Instant.now(), param0, param1);
    }

    public void setComments(String param0) {
        this.comments = param0;
    }

    public void setReason(ReportReason param0) {
        this.reason = param0;
    }

    public void toggleReported(int param0) {
        if (this.reportedMessages.contains(param0)) {
            this.reportedMessages.remove(param0);
        } else if (this.reportedMessages.size() < this.limits.maxReportedMessageCount()) {
            this.reportedMessages.add(param0);
        }

    }

    public UUID reportedProfileId() {
        return this.reportedProfileId;
    }

    public IntSet reportedMessages() {
        return this.reportedMessages;
    }

    public String comments() {
        return this.comments;
    }

    @Nullable
    public ReportReason reason() {
        return this.reason;
    }

    public boolean isReported(int param0) {
        return this.reportedMessages.contains(param0);
    }

    @Nullable
    public ChatReportBuilder.CannotBuildReason checkBuildable() {
        if (this.reportedMessages.isEmpty()) {
            return ChatReportBuilder.CannotBuildReason.NO_REPORTED_MESSAGES;
        } else if (this.reportedMessages.size() > this.limits.maxReportedMessageCount()) {
            return ChatReportBuilder.CannotBuildReason.TOO_MANY_MESSAGES;
        } else if (this.reason == null) {
            return ChatReportBuilder.CannotBuildReason.NO_REASON;
        } else {
            return this.comments.length() > this.limits.maxOpinionCommentsLength() ? ChatReportBuilder.CannotBuildReason.COMMENTS_TOO_LONG : null;
        }
    }

    public Either<ChatReportBuilder.Result, ChatReportBuilder.CannotBuildReason> build(ReportingContext param0) {
        ChatReportBuilder.CannotBuildReason var0 = this.checkBuildable();
        if (var0 != null) {
            return Either.right(var0);
        } else {
            String var1 = Objects.requireNonNull(this.reason).backendName();
            ReportEvidence var2 = this.buildEvidence(param0.chatLog());
            ReportedEntity var3 = new ReportedEntity(this.reportedProfileId);
            AbuseReport var4 = new AbuseReport(this.comments, var1, var2, var3, this.createdAt);
            return Either.left(new ChatReportBuilder.Result(this.reportId, var4));
        }
    }

    private ReportEvidence buildEvidence(ChatLog param0) {
        List<ReportChatMessage> var0 = new ArrayList<>();
        ChatReportContextBuilder var1 = new ChatReportContextBuilder(this.limits.leadingContextMessageCount());
        var1.collectAllContext(param0, this.reportedMessages, (param1, param2) -> var0.add(this.buildReportedChatMessage(param2, this.isReported(param1))));
        return new ReportEvidence(Lists.reverse(var0));
    }

    private ReportChatMessage buildReportedChatMessage(LoggedChatMessage.Player param0, boolean param1) {
        SignedMessageLink var0 = param0.message().link();
        SignedMessageBody var1 = param0.message().signedBody();
        List<ByteBuffer> var2 = var1.lastSeen().entries().stream().map(MessageSignature::asByteBuffer).toList();
        ByteBuffer var3 = Util.mapNullable(param0.message().signature(), MessageSignature::asByteBuffer);
        return new ReportChatMessage(var0.index(), var0.sender(), var0.sessionId(), var1.timeStamp(), var1.salt(), var2, var1.content(), var3, param1);
    }

    public ChatReportBuilder copy() {
        ChatReportBuilder var0 = new ChatReportBuilder(this.reportId, this.createdAt, this.reportedProfileId, this.limits);
        var0.reportedMessages.addAll(this.reportedMessages);
        var0.comments = this.comments;
        var0.reason = this.reason;
        return var0;
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
    public static record Result(UUID id, AbuseReport report) {
    }
}
