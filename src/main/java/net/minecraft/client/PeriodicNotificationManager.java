package net.minecraft.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.math.LongMath;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class PeriodicNotificationManager
    extends SimplePreparableReloadListener<Map<String, List<PeriodicNotificationManager.Notification>>>
    implements AutoCloseable {
    private static final Codec<Map<String, List<PeriodicNotificationManager.Notification>>> CODEC = Codec.unboundedMap(
        Codec.STRING,
        RecordCodecBuilder.create(
                param0 -> param0.group(
                            Codec.LONG.optionalFieldOf("delay", Long.valueOf(0L)).forGetter(PeriodicNotificationManager.Notification::delay),
                            Codec.LONG.fieldOf("period").forGetter(PeriodicNotificationManager.Notification::period),
                            Codec.STRING.fieldOf("title").forGetter(PeriodicNotificationManager.Notification::title),
                            Codec.STRING.fieldOf("message").forGetter(PeriodicNotificationManager.Notification::message)
                        )
                        .apply(param0, PeriodicNotificationManager.Notification::new)
            )
            .listOf()
    );
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourceLocation notifications;
    private final Object2BooleanFunction<String> selector;
    @Nullable
    private java.util.Timer timer;
    @Nullable
    private PeriodicNotificationManager.NotificationTask notificationTask;

    public PeriodicNotificationManager(ResourceLocation param0, Object2BooleanFunction<String> param1) {
        this.notifications = param0;
        this.selector = param1;
    }

    protected Map<String, List<PeriodicNotificationManager.Notification>> prepare(ResourceManager param0, ProfilerFiller param1) {
        try {
            Map var4;
            try (Reader var0 = param0.openAsReader(this.notifications)) {
                var4 = (Map)CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(var0)).result().orElseThrow();
            }

            return var4;
        } catch (Exception var8) {
            LOGGER.warn("Failed to load {}", this.notifications, var8);
            return ImmutableMap.of();
        }
    }

    protected void apply(Map<String, List<PeriodicNotificationManager.Notification>> param0, ResourceManager param1, ProfilerFiller param2) {
        List<PeriodicNotificationManager.Notification> var0 = param0.entrySet()
            .stream()
            .filter(param0x -> this.selector.apply(param0x.getKey()))
            .map(Entry::getValue)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        if (var0.isEmpty()) {
            this.stopTimer();
        } else if (var0.stream().anyMatch(param0x -> param0x.period == 0L)) {
            Util.logAndPauseIfInIde("A periodic notification in " + this.notifications + " has a period of zero minutes");
            this.stopTimer();
        } else {
            long var1 = this.calculateInitialDelay(var0);
            long var2 = this.calculateOptimalPeriod(var0, var1);
            if (this.timer == null) {
                this.timer = new java.util.Timer();
            }

            if (this.notificationTask == null) {
                this.notificationTask = new PeriodicNotificationManager.NotificationTask(var0, var1, var2);
            } else {
                this.notificationTask = this.notificationTask.reset(var0, var2);
            }

            this.timer.scheduleAtFixedRate(this.notificationTask, TimeUnit.MINUTES.toMillis(var1), TimeUnit.MINUTES.toMillis(var2));
        }
    }

    @Override
    public void close() {
        this.stopTimer();
    }

    private void stopTimer() {
        if (this.timer != null) {
            this.timer.cancel();
        }

    }

    private long calculateOptimalPeriod(List<PeriodicNotificationManager.Notification> param0, long param1) {
        return param0.stream().mapToLong(param1x -> {
            long var0 = param1x.delay - param1;
            return LongMath.gcd(var0, param1x.period);
        }).reduce(LongMath::gcd).orElseThrow(() -> new IllegalStateException("Empty notifications from: " + this.notifications));
    }

    private long calculateInitialDelay(List<PeriodicNotificationManager.Notification> param0) {
        return param0.stream().mapToLong(param0x -> param0x.delay).min().orElse(0L);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Notification(long delay, long period, String title, String message) {
        public Notification(long param0, long param1, String param2, String param3) {
            this.delay = param0 != 0L ? param0 : param1;
            this.period = param1;
            this.title = param2;
            this.message = param3;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class NotificationTask extends TimerTask {
        private final Minecraft minecraft = Minecraft.getInstance();
        private final List<PeriodicNotificationManager.Notification> notifications;
        private final long period;
        private final AtomicLong elapsed;

        public NotificationTask(List<PeriodicNotificationManager.Notification> param0, long param1, long param2) {
            this.notifications = param0;
            this.period = param2;
            this.elapsed = new AtomicLong(param1);
        }

        public PeriodicNotificationManager.NotificationTask reset(List<PeriodicNotificationManager.Notification> param0, long param1) {
            this.cancel();
            return new PeriodicNotificationManager.NotificationTask(param0, this.elapsed.get(), param1);
        }

        @Override
        public void run() {
            long var0 = this.elapsed.getAndAdd(this.period);
            long var1 = this.elapsed.get();

            for(PeriodicNotificationManager.Notification var2 : this.notifications) {
                if (var0 >= var2.delay) {
                    long var3 = var0 / var2.period;
                    long var4 = var1 / var2.period;
                    if (var3 != var4) {
                        this.minecraft
                            .execute(
                                () -> SystemToast.add(
                                        Minecraft.getInstance().getToasts(),
                                        SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                                        Component.translatable(var2.title, var3),
                                        Component.translatable(var2.message, var3)
                                    )
                            );
                        return;
                    }
                }
            }

        }
    }
}
