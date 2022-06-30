package net.minecraft.realms;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.GameNarrator;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RepeatedNarrator {
    private final float permitsPerSecond;
    private final AtomicReference<RepeatedNarrator.Params> params = new AtomicReference<>();

    public RepeatedNarrator(Duration param0) {
        this.permitsPerSecond = 1000.0F / (float)param0.toMillis();
    }

    public void narrate(GameNarrator param0, Component param1) {
        RepeatedNarrator.Params var0 = this.params
            .updateAndGet(
                param1x -> param1x != null && param1.equals(param1x.narration)
                        ? param1x
                        : new RepeatedNarrator.Params(param1, RateLimiter.create((double)this.permitsPerSecond))
            );
        if (var0.rateLimiter.tryAcquire(1)) {
            param0.sayNow(param1);
        }

    }

    @OnlyIn(Dist.CLIENT)
    static class Params {
        final Component narration;
        final RateLimiter rateLimiter;

        Params(Component param0, RateLimiter param1) {
            this.narration = param0;
            this.rateLimiter = param1;
        }
    }
}
