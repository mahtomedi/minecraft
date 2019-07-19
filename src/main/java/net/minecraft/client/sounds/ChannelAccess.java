package net.minecraft.client.sounds;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChannelAccess {
    private final Set<ChannelAccess.ChannelHandle> channels = Sets.newIdentityHashSet();
    private final Library library;
    private final Executor executor;

    public ChannelAccess(Library param0, Executor param1) {
        this.library = param0;
        this.executor = param1;
    }

    public ChannelAccess.ChannelHandle createHandle(Library.Pool param0) {
        ChannelAccess.ChannelHandle var0 = new ChannelAccess.ChannelHandle();
        this.executor.execute(() -> {
            Channel var0x = this.library.acquireChannel(param0);
            if (var0x != null) {
                var0.channel = var0x;
                this.channels.add(var0);
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
        private Channel channel;
        private boolean stopped;

        public boolean isStopped() {
            return this.stopped;
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
