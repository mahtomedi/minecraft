package net.minecraft.client.gui.components;

import net.minecraft.Util;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LerpingBossEvent extends BossEvent {
    protected float targetPercent;
    protected long setTime;

    public LerpingBossEvent(ClientboundBossEventPacket param0) {
        super(param0.getId(), param0.getName(), param0.getColor(), param0.getOverlay());
        this.targetPercent = param0.getPercent();
        this.percent = param0.getPercent();
        this.setTime = Util.getMillis();
        this.setDarkenScreen(param0.shouldDarkenScreen());
        this.setPlayBossMusic(param0.shouldPlayMusic());
        this.setCreateWorldFog(param0.shouldCreateWorldFog());
    }

    @Override
    public void setPercent(float param0) {
        this.percent = this.getPercent();
        this.targetPercent = param0;
        this.setTime = Util.getMillis();
    }

    @Override
    public float getPercent() {
        long var0 = Util.getMillis() - this.setTime;
        float var1 = Mth.clamp((float)var0 / 100.0F, 0.0F, 1.0F);
        return Mth.lerp(var1, this.percent, this.targetPercent);
    }

    public void update(ClientboundBossEventPacket param0) {
        switch(param0.getOperation()) {
            case UPDATE_NAME:
                this.setName(param0.getName());
                break;
            case UPDATE_PCT:
                this.setPercent(param0.getPercent());
                break;
            case UPDATE_STYLE:
                this.setColor(param0.getColor());
                this.setOverlay(param0.getOverlay());
                break;
            case UPDATE_PROPERTIES:
                this.setDarkenScreen(param0.shouldDarkenScreen());
                this.setPlayBossMusic(param0.shouldPlayMusic());
        }

    }
}
