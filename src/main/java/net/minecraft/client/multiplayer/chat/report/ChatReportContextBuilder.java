package net.minecraft.client.multiplayer.chat.report;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatReportContextBuilder {
    final int leadingCount;
    private final List<ChatReportContextBuilder.Collector> activeCollectors = new ArrayList<>();

    public ChatReportContextBuilder(int param0) {
        this.leadingCount = param0;
    }

    public void collectAllContext(ChatLog param0, IntCollection param1, ChatReportContextBuilder.Handler param2) {
        IntSortedSet var0 = new IntRBTreeSet(param1);

        for(int var1 = var0.lastInt(); var1 >= param0.start() && (this.isActive() || !var0.isEmpty()); --var1) {
            LoggedChatEvent var3 = param0.lookup(var1);
            if (var3 instanceof LoggedChatMessage.Player var2) {
                boolean var3x = this.acceptContext(var2.message());
                if (var0.remove(var1)) {
                    this.trackContext(var2.message());
                    param2.accept(var1, var2);
                } else if (var3x) {
                    param2.accept(var1, var2);
                }
            }
        }

    }

    public void trackContext(PlayerChatMessage param0) {
        this.activeCollectors.add(new ChatReportContextBuilder.Collector(param0));
    }

    public boolean acceptContext(PlayerChatMessage param0) {
        boolean var0 = false;
        Iterator<ChatReportContextBuilder.Collector> var1 = this.activeCollectors.iterator();

        while(var1.hasNext()) {
            ChatReportContextBuilder.Collector var2 = var1.next();
            if (var2.accept(param0)) {
                var0 = true;
                if (var2.isComplete()) {
                    var1.remove();
                }
            }
        }

        return var0;
    }

    public boolean isActive() {
        return !this.activeCollectors.isEmpty();
    }

    @OnlyIn(Dist.CLIENT)
    class Collector {
        private final Set<MessageSignature> lastSeenSignatures;
        private PlayerChatMessage lastChainMessage;
        private boolean collectingChain = true;
        private int count;

        Collector(PlayerChatMessage param0) {
            this.lastSeenSignatures = new ObjectOpenHashSet<>(param0.signedBody().lastSeen().entries());
            this.lastChainMessage = param0;
        }

        boolean accept(PlayerChatMessage param0) {
            if (param0.equals(this.lastChainMessage)) {
                return false;
            } else {
                boolean var0 = this.lastSeenSignatures.remove(param0.signature());
                if (this.collectingChain && this.lastChainMessage.sender().equals(param0.sender())) {
                    if (this.lastChainMessage.link().isDescendantOf(param0.link())) {
                        var0 = true;
                        this.lastChainMessage = param0;
                    } else {
                        this.collectingChain = false;
                    }
                }

                if (var0) {
                    ++this.count;
                }

                return var0;
            }
        }

        boolean isComplete() {
            return this.count >= ChatReportContextBuilder.this.leadingCount || !this.collectingChain && this.lastSeenSignatures.isEmpty();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface Handler {
        void accept(int var1, LoggedChatMessage.Player var2);
    }
}
