package net.minecraft.client.gui.components;

import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LerpingBossEvent extends BossEvent {
    private static final long LERP_MILLISECONDS = 100L;
    protected float targetPercent;
    protected long setTime;

    public LerpingBossEvent(
        UUID param0,
        Component param1,
        float param2,
        BossEvent.BossBarColor param3,
        BossEvent.BossBarOverlay param4,
        boolean param5,
        boolean param6,
        boolean param7
    ) {
        super(param0, param1, param3, param4);
        this.targetPercent = param2;
        this.progress = param2;
        this.setTime = Util.getMillis();
        this.setDarkenScreen(param5);
        this.setPlayBossMusic(param6);
        this.setCreateWorldFog(param7);
    }

    @Override
    public void setProgress(float param0) {
        this.progress = this.getProgress();
        this.targetPercent = param0;
        this.setTime = Util.getMillis();
    }

    @Override
    public float getProgress() {
        long var0 = Util.getMillis() - this.setTime;
        float var1 = Mth.clamp((float)var0 / 100.0F, 0.0F, 1.0F);
        return Mth.lerp(var1, this.progress, this.targetPercent);
    }
}
