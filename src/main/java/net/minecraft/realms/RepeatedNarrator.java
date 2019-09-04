package net.minecraft.realms;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RepeatedNarrator {
    final Duration repeatDelay;
    private final float permitsPerSecond;
    final AtomicReference<RepeatedNarrator.Params> params;

    public RepeatedNarrator(Duration param0) {
        this.repeatDelay = param0;
        this.params = new AtomicReference<>();
        float var0 = (float)param0.toMillis() / 1000.0F;
        this.permitsPerSecond = 1.0F / var0;
    }

    public void narrate(String param0) {
        RepeatedNarrator.Params var0 = this.params
            .updateAndGet(
                param1 -> param1 != null && param0.equals(param1.narration)
                        ? param1
                        : new RepeatedNarrator.Params(param0, RateLimiter.create((double)this.permitsPerSecond))
            );
        if (var0.rateLimiter.tryAcquire(1)) {
            NarratorChatListener var1 = NarratorChatListener.INSTANCE;
            var1.handle(ChatType.SYSTEM, new TextComponent(param0));
        }

    }

    @OnlyIn(Dist.CLIENT)
    static class Params {
        String narration;
        RateLimiter rateLimiter;

        Params(String param0, RateLimiter param1) {
            this.narration = param0;
            this.rateLimiter = param1;
        }
    }
}
