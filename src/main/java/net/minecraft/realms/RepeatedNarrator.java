package net.minecraft.realms;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RepeatedNarrator {
    private final float permitsPerSecond;
    private final AtomicReference<RepeatedNarrator.Params> params = new AtomicReference<>();

    public RepeatedNarrator(Duration param0) {
        this.permitsPerSecond = 1000.0F / (float)param0.toMillis();
    }

    public void narrate(String param0) {
        RepeatedNarrator.Params var0 = this.params
            .updateAndGet(
                param1 -> param1 != null && param0.equals(param1.narration)
                        ? param1
                        : new RepeatedNarrator.Params(param0, RateLimiter.create((double)this.permitsPerSecond))
            );
        if (var0.rateLimiter.tryAcquire(1)) {
            NarratorChatListener.INSTANCE.handle(ChatType.SYSTEM, new TextComponent(param0), Util.NIL_UUID);
        }

    }

    @OnlyIn(Dist.CLIENT)
    static class Params {
        private final String narration;
        private final RateLimiter rateLimiter;

        Params(String param0, RateLimiter param1) {
            this.narration = param0;
            this.rateLimiter = param1;
        }
    }
}
