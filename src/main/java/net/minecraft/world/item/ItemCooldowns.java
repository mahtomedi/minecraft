package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.Mth;

public class ItemCooldowns {
    private final Map<Item, ItemCooldowns.CooldownInstance> cooldowns = Maps.newHashMap();
    private int tickCount;

    public boolean isOnCooldown(Item param0) {
        return this.getCooldownPercent(param0, 0.0F) > 0.0F;
    }

    public float getCooldownPercent(Item param0, float param1) {
        ItemCooldowns.CooldownInstance var0 = this.cooldowns.get(param0);
        if (var0 != null) {
            float var1 = (float)(var0.endTime - var0.startTime);
            float var2 = (float)var0.endTime - ((float)this.tickCount + param1);
            return Mth.clamp(var2 / var1, 0.0F, 1.0F);
        } else {
            return 0.0F;
        }
    }

    public void tick() {
        ++this.tickCount;
        if (!this.cooldowns.isEmpty()) {
            Iterator<Entry<Item, ItemCooldowns.CooldownInstance>> var0 = this.cooldowns.entrySet().iterator();

            while(var0.hasNext()) {
                Entry<Item, ItemCooldowns.CooldownInstance> var1 = var0.next();
                if (var1.getValue().endTime <= this.tickCount) {
                    var0.remove();
                    this.onCooldownEnded(var1.getKey());
                }
            }
        }

    }

    public void addCooldown(Item param0, int param1) {
        this.cooldowns.put(param0, new ItemCooldowns.CooldownInstance(this.tickCount, this.tickCount + param1));
        this.onCooldownStarted(param0, param1);
    }

    public void removeCooldown(Item param0) {
        this.cooldowns.remove(param0);
        this.onCooldownEnded(param0);
    }

    protected void onCooldownStarted(Item param0, int param1) {
    }

    protected void onCooldownEnded(Item param0) {
    }

    class CooldownInstance {
        final int startTime;
        final int endTime;

        CooldownInstance(int param0, int param1) {
            this.startTime = param0;
            this.endTime = param1;
        }
    }
}
