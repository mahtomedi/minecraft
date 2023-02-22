package net.minecraft.client.multiplayer.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatLog {
    private final LoggedChatEvent[] buffer;
    private int nextId;

    public static Codec<ChatLog> codec(int param0) {
        return Codec.list(LoggedChatEvent.CODEC)
            .comapFlatMap(
                param1 -> {
                    int var0x = param1.size();
                    return var0x > param0
                        ? DataResult.error(() -> "Expected: a buffer of size less than or equal to " + param0 + " but: " + var0x + " is greater than " + param0)
                        : DataResult.success(new ChatLog(param0, param1));
                },
                ChatLog::loggedChatEvents
            );
    }

    public ChatLog(int param0) {
        this.buffer = new LoggedChatEvent[param0];
    }

    private ChatLog(int param0, List<LoggedChatEvent> param1) {
        this.buffer = (LoggedChatEvent[])param1.toArray(param1x -> new LoggedChatEvent[param0]);
        this.nextId = param1.size();
    }

    private List<LoggedChatEvent> loggedChatEvents() {
        List<LoggedChatEvent> var0 = new ArrayList<>(this.size());

        for(int var1 = this.start(); var1 <= this.end(); ++var1) {
            var0.add(this.lookup(var1));
        }

        return var0;
    }

    public void push(LoggedChatEvent param0) {
        this.buffer[this.index(this.nextId++)] = param0;
    }

    @Nullable
    public LoggedChatEvent lookup(int param0) {
        return param0 >= this.start() && param0 <= this.end() ? this.buffer[this.index(param0)] : null;
    }

    private int index(int param0) {
        return param0 % this.buffer.length;
    }

    public int start() {
        return Math.max(this.nextId - this.buffer.length, 0);
    }

    public int end() {
        return this.nextId - 1;
    }

    private int size() {
        return this.end() - this.start() + 1;
    }
}
