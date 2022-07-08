package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportChatMessage;
import com.mojang.authlib.minecraft.report.ReportEvidence;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatReportBuilder {
    private static final String REPORT_TYPE_CHAT = "CHAT";
    private final UUID id;
    private final Instant createdAt;
    private final UUID reportedProfileId;
    private final AbuseReportLimits limits;
    private final IntSet reportedMessages = new IntOpenHashSet();
    private String comments = "";
    @Nullable
    private ReportReason reason;

    private ChatReportBuilder(UUID param0, Instant param1, UUID param2, AbuseReportLimits param3) {
        this.id = param0;
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
            if (var2.messages.size() > this.limits.maxEvidenceMessageCount()) {
                return Either.right(ChatReportBuilder.CannotBuildReason.TOO_MANY_MESSAGES);
            } else {
                ReportedEntity var3 = new ReportedEntity(this.reportedProfileId);
                AbuseReport var4 = new AbuseReport("CHAT", this.comments, var1, var2, var3, this.createdAt);
                return Either.left(new ChatReportBuilder.Result(this.id, var4));
            }
        }
    }

    private ReportEvidence buildEvidence(ChatLog param0) {
        IntSortedSet var0 = new IntRBTreeSet();
        this.reportedMessages.forEach(param2 -> {
            IntStream var0x = this.selectContextMessages(param0, param2);
            var0x.forEach(var0::add);
        });
        List<ReportChatMessage> var1 = var0.intStream().mapToObj(param1 -> {
            LoggedChat var0x = param0.lookup(param1);
            return var0x instanceof LoggedChat.Player var1x ? this.buildReportedChatMessage(param1, var1x) : null;
        }).filter(Objects::nonNull).toList();
        return new ReportEvidence(var1);
    }

    private ReportChatMessage buildReportedChatMessage(int param0, LoggedChat.Player param1) {
        PlayerChatMessage var0 = param1.message();
        Instant var1 = var0.timeStamp();
        long var2 = var0.salt();
        String var3 = var0.headerSignature().asString();
        String var4 = encodeComponent(var0.signedContent());
        String var5 = var0.unsignedContent().map(ChatReportBuilder::encodeComponent).orElse(null);
        return new ReportChatMessage(param1.profileId(), var1, var2, var3, var4, var5, this.isReported(param0));
    }

    private static String encodeComponent(Component param0x) {
        return Component.Serializer.toStableJson(param0x);
    }

    private IntStream selectContextMessages(ChatLog param0, int param1) {
        int var0 = param0.offsetClamped(param1, -this.limits.leadingContextMessageCount());
        int var1 = param0.offsetClamped(param1, this.limits.trailingContextMessageCount());
        return param0.selectBetween(var0, var1).ids();
    }

    public ChatReportBuilder copy() {
        ChatReportBuilder var0 = new ChatReportBuilder(this.id, this.createdAt, this.reportedProfileId, this.limits);
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
