package net.minecraft.client.multiplayer.chat;

import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RollingMemoryChatLog implements ChatLog {
    private final LoggedChat[] buffer;
    private int newestId = -1;
    private int oldestId = -1;

    public RollingMemoryChatLog(int param0) {
        this.buffer = new LoggedChat[param0];
    }

    @Override
    public void push(LoggedChat param0) {
        int var0 = this.nextId();
        this.buffer[this.index(var0)] = param0;
    }

    private int nextId() {
        int var0 = ++this.newestId;
        if (var0 >= this.buffer.length) {
            ++this.oldestId;
        } else {
            this.oldestId = 0;
        }

        return var0;
    }

    @Nullable
    @Override
    public LoggedChat lookup(int param0) {
        return this.contains(param0) ? this.buffer[this.index(param0)] : null;
    }

    private int index(int param0) {
        return param0 % this.buffer.length;
    }

    @Override
    public boolean contains(int param0) {
        return param0 >= this.oldestId && param0 <= this.newestId;
    }

    @Override
    public int offset(int param0, int param1) {
        int var0 = param0 + param1;
        return this.contains(var0) ? var0 : -1;
    }

    @Override
    public int offsetClamped(int param0, int param1) {
        return Mth.clamp(param0 + param1, this.oldestId, this.newestId);
    }

    @Override
    public int newest() {
        return this.newestId;
    }

    @Override
    public int oldest() {
        return this.oldestId;
    }
}
