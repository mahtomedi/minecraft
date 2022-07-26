package net.minecraft.client.sounds;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChannelAccess {
    private final Set<ChannelAccess.ChannelHandle> channels = Sets.newIdentityHashSet();
    final Library library;
    final Executor executor;

    public ChannelAccess(Library param0, Executor param1) {
        this.library = param0;
        this.executor = param1;
    }

    public CompletableFuture<ChannelAccess.ChannelHandle> createHandle(Library.Pool param0) {
        CompletableFuture<ChannelAccess.ChannelHandle> var0 = new CompletableFuture<>();
        this.executor.execute(() -> {
            Channel var0x = this.library.acquireChannel(param0);
            if (var0x != null) {
                ChannelAccess.ChannelHandle var1x = new ChannelAccess.ChannelHandle(var0x);
                this.channels.add(var1x);
                var0.complete(var1x);
            } else {
                var0.complete(null);
            }

        });
        return var0;
    }

    public void executeOnChannels(Consumer<Stream<Channel>> param0) {
        this.executor.execute(() -> param0.accept(this.channels.stream().map(param0x -> param0x.channel).filter(Objects::nonNull)));
    }

    public void scheduleTick() {
        this.executor.execute(() -> {
            Iterator<ChannelAccess.ChannelHandle> var0 = this.channels.iterator();

            while(var0.hasNext()) {
                ChannelAccess.ChannelHandle var1 = var0.next();
                var1.channel.updateStream();
                if (var1.channel.stopped()) {
                    var1.release();
                    var0.remove();
                }
            }

        });
    }

    public void clear() {
        this.channels.forEach(ChannelAccess.ChannelHandle::release);
        this.channels.clear();
    }

    @OnlyIn(Dist.CLIENT)
    public class ChannelHandle {
        @Nullable
        Channel channel;
        private boolean stopped;

        public boolean isStopped() {
            return this.stopped;
        }

        public ChannelHandle(Channel param1) {
            this.channel = param1;
        }

        public void execute(Consumer<Channel> param0) {
            ChannelAccess.this.executor.execute(() -> {
                if (this.channel != null) {
                    param0.accept(this.channel);
                }

            });
        }

        public void release() {
            this.stopped = true;
            ChannelAccess.this.library.releaseChannel(this.channel);
            this.channel = null;
        }
    }
}
