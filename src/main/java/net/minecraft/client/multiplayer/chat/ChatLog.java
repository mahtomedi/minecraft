package net.minecraft.client.multiplayer.chat;

import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Objects;
import java.util.Spliterators;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ChatLog {
    int NO_MESSAGE = -1;

    void push(LoggedChat var1);

    @Nullable
    LoggedChat lookup(int var1);

    default LoggedChat.WithId lookupWithId(int param0) {
        LoggedChat var0 = this.lookup(param0);
        return var0 != null ? new LoggedChat.WithId(param0, var0) : null;
    }

    default boolean contains(int param0) {
        return this.lookup(param0) != null;
    }

    int offset(int var1, int var2);

    int offsetClamped(int var1, int var2);

    default int before(int param0) {
        return this.offset(param0, -1);
    }

    default int after(int param0) {
        return this.offset(param0, 1);
    }

    int newest();

    int oldest();

    default ChatLog.Selection selectAll() {
        return this.selectAfter(this.oldest());
    }

    default ChatLog.Selection selectAfter(int param0) {
        return this.selectSequence(param0, this::after);
    }

    default ChatLog.Selection selectBefore(int param0) {
        return this.selectSequence(param0, this::before);
    }

    default ChatLog.Selection selectBetween(int param0, int param1) {
        return this.contains(param0) && this.contains(param1)
            ? this.selectSequence(param0, param1x -> param1x == param1 ? -1 : this.after(param1x))
            : this.selectNone();
    }

    default ChatLog.Selection selectSequence(final int param0, final IntUnaryOperator param1) {
        return !this.contains(param0) ? this.selectNone() : new ChatLog.Selection(this, new OfInt() {
            private int nextId = param0;

            @Override
            public int nextInt() {
                int var0 = this.nextId;
                this.nextId = param1.applyAsInt(var0);
                return var0;
            }

            @Override
            public boolean hasNext() {
                return this.nextId != -1;
            }
        });
    }

    private ChatLog.Selection selectNone() {
        return new ChatLog.Selection(this, IntList.of().iterator());
    }

    @OnlyIn(Dist.CLIENT)
    public static class Selection {
        private static final int CHARACTERISTICS = 1041;
        private final ChatLog log;
        private final OfInt ids;

        Selection(ChatLog param0, OfInt param1) {
            this.log = param0;
            this.ids = param1;
        }

        public IntStream ids() {
            return StreamSupport.intStream(Spliterators.spliteratorUnknownSize(this.ids, 1041), false);
        }

        public Stream<LoggedChat> messages() {
            return this.ids().mapToObj(this.log::lookup).filter(Objects::nonNull);
        }

        public Stream<LoggedChat.WithId> messagesWithIds() {
            return this.ids().mapToObj(this.log::lookupWithId).filter(Objects::nonNull);
        }
    }
}
