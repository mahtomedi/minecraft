package net.minecraft.client.multiplayer.chat;

import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatLog {
    private final LoggedChatEvent[] buffer;
    private int nextId;

    public ChatLog(int param0) {
        this.buffer = new LoggedChatEvent[param0];
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
}
