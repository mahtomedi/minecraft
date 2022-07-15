package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportChatMessage;
import com.mojang.authlib.minecraft.report.ReportChatMessageBody;
import com.mojang.authlib.minecraft.report.ReportChatMessageContent;
import com.mojang.authlib.minecraft.report.ReportChatMessageHeader;
import com.mojang.authlib.minecraft.report.ReportEvidence;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.authlib.minecraft.report.ReportChatMessageBody.LastSeenSignature;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.client.multiplayer.chat.LoggedChatMessageLink;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageBody;
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
            if (var2.messages.size() > this.limits.maxEvidenceMessageCount()) {
                return Either.right(ChatReportBuilder.CannotBuildReason.TOO_MANY_MESSAGES);
            } else {
                ReportedEntity var3 = new ReportedEntity(this.reportedProfileId);
                AbuseReport var4 = new AbuseReport(this.comments, var1, var2, var3, this.createdAt);
                return Either.left(new ChatReportBuilder.Result(this.reportId, var4));
            }
        }
    }

    private ReportEvidence buildEvidence(ChatLog param0) {
        Int2ObjectSortedMap<ReportChatMessage> var0 = new Int2ObjectRBTreeMap<>();
        this.reportedMessages.forEach(param2 -> {
            Int2ObjectMap<LoggedChatMessage.Player> var0x = collectReferencedContext(param0, param2, this.limits);
            Set<UUID> var1x = new ObjectOpenHashSet();

            for(Entry<LoggedChatMessage.Player> var2 : Int2ObjectMaps.fastIterable(var0x)) {
                int var3 = var2.getIntKey();
                LoggedChatMessage.Player var4 = var2.getValue();
                var0.put(var3, this.buildReportedChatMessage(var3, var4));
                var1x.add(var4.profileId());
            }

            for(UUID var5 : var1x) {
                this.chainForPlayer(param0, var0x, var5).forEach(param1x -> {
                    LoggedChatMessageLink var0xx = param1x.event();
                    if (var0xx instanceof LoggedChatMessage.Player var1xx) {
                        var0.putIfAbsent(param1x.id(), this.buildReportedChatMessage(param1x.id(), var1xx));
                    } else {
                        var0.putIfAbsent(param1x.id(), this.buildReportedChatHeader(var0xx));
                    }

                });
            }

        });
        return new ReportEvidence(new ArrayList<>(var0.values()));
    }

    private Stream<ChatLog.Entry<LoggedChatMessageLink>> chainForPlayer(ChatLog param0, Int2ObjectMap<LoggedChatMessage.Player> param1, UUID param2) {
        int var0 = Integer.MAX_VALUE;
        int var1 = Integer.MIN_VALUE;

        for(Entry<LoggedChatMessage.Player> var2 : Int2ObjectMaps.fastIterable(param1)) {
            LoggedChatMessage.Player var3 = var2.getValue();
            if (var3.profileId().equals(param2)) {
                int var4 = var2.getIntKey();
                var0 = Math.min(var0, var4);
                var1 = Math.max(var1, var4);
            }
        }

        return param0.selectBetween(var0, var1)
            .entries()
            .map(param0x -> param0x.tryCast(LoggedChatMessageLink.class))
            .filter(Objects::nonNull)
            .filter(param1x -> param1x.event().header().sender().equals(param2));
    }

    private static Int2ObjectMap<LoggedChatMessage.Player> collectReferencedContext(ChatLog param0, int param1, AbuseReportLimits param2) {
        Int2ObjectMap<LoggedChatMessage.Player> var0 = new Int2ObjectOpenHashMap<>();
        walkLastSeenReferenceGraph(param0, param1, (param2x, param3) -> {
            var0.put(param2x, param3);
            return var0.size() < param2.leadingContextMessageCount();
        });
        trailingContext(param0, param1, param2.trailingContextMessageCount()).forEach(param1x -> var0.put(param1x.id(), param1x.event()));
        return var0;
    }

    private static Stream<ChatLog.Entry<LoggedChatMessage.Player>> trailingContext(ChatLog param0, int param1, int param2) {
        return param0.selectAfter(param0.after(param1))
            .entries()
            .map(param0x -> param0x.tryCast(LoggedChatMessage.Player.class))
            .filter(Objects::nonNull)
            .limit((long)param2);
    }

    private static void walkLastSeenReferenceGraph(ChatLog param0, int param1, ChatReportBuilder.LastSeenVisitor param2) {
        IntPriorityQueue var0 = new IntArrayPriorityQueue(IntComparators.OPPOSITE_COMPARATOR);
        var0.enqueue(param1);
        IntSet var1 = new IntOpenHashSet();
        var1.add(param1);

        while(!var0.isEmpty()) {
            int var2 = var0.dequeueInt();
            LoggedChatEvent var7 = param0.lookup(var2);
            if (var7 instanceof LoggedChatMessage.Player var3) {
                if (!param2.accept(var2, var3)) {
                    break;
                }

                for(int var4 : lastSeenReferences(param0, var2, var3)) {
                    if (var1.add(var4)) {
                        var0.enqueue(var4);
                    }
                }
            }
        }

    }

    private static IntCollection lastSeenReferences(ChatLog param0, int param1, LoggedChatMessage.Player param2) {
        Set<MessageSignature> var0 = param2.message()
            .signedBody()
            .lastSeen()
            .entries()
            .stream()
            .map(LastSeenMessages.Entry::lastSignature)
            .collect(Collectors.toSet());
        IntList var1 = new IntArrayList();
        Iterator<ChatLog.Entry<LoggedChatEvent>> var2 = param0.selectBefore(param1).entries().iterator();

        while(var2.hasNext() && !var0.isEmpty()) {
            ChatLog.Entry<LoggedChatEvent> var3 = var2.next();
            LoggedChatEvent var8 = var3.event();
            if (var8 instanceof LoggedChatMessage.Player var4 && var0.remove(var4.headerSignature())) {
                var1.add(var3.id());
            }
        }

        return var1;
    }

    private ReportChatMessage buildReportedChatMessage(int param0, LoggedChatMessage.Player param1) {
        PlayerChatMessage var0 = param1.message();
        SignedMessageBody var1 = var0.signedBody();
        Instant var2 = var0.timeStamp();
        long var3 = var0.salt();
        ByteBuffer var4 = var0.headerSignature().asByteBuffer();
        ByteBuffer var5 = Util.mapNullable(var0.signedHeader().previousSignature(), MessageSignature::asByteBuffer);
        ByteBuffer var6 = ByteBuffer.wrap(var1.hash().asBytes());
        ReportChatMessageContent var7 = new ReportChatMessageContent(
            encodeComponent(var0.signedContent().plain()), var0.signedContent().isDecorated() ? encodeComponent(var0.signedContent().decorated()) : null
        );
        String var8 = var0.unsignedContent().map(ChatReportBuilder::encodeComponent).orElse(null);
        List<LastSeenSignature> var9 = var1.lastSeen()
            .entries()
            .stream()
            .map(param0x -> new LastSeenSignature(param0x.profileId(), param0x.lastSignature().asByteBuffer()))
            .toList();
        return new ReportChatMessage(
            new ReportChatMessageHeader(var5, param1.profileId(), var6, var4), new ReportChatMessageBody(var2, var3, var9, var7), var8, this.isReported(param0)
        );
    }

    private ReportChatMessage buildReportedChatHeader(LoggedChatMessageLink param0) {
        ByteBuffer var0 = param0.headerSignature().asByteBuffer();
        ByteBuffer var1 = Util.mapNullable(param0.header().previousSignature(), MessageSignature::asByteBuffer);
        return new ReportChatMessage(new ReportChatMessageHeader(var1, param0.header().sender(), ByteBuffer.wrap(param0.bodyDigest()), var0), null, null, false);
    }

    private static String encodeComponent(Component param0x) {
        return Component.Serializer.toStableJson(param0x);
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
    interface LastSeenVisitor {
        boolean accept(int var1, LoggedChatMessage.Player var2);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Result(UUID id, AbuseReport report) {
    }
}
