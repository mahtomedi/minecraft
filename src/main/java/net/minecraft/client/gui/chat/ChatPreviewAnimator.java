package net.minecraft.client.gui.chat;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatPreviewAnimator {
    private static final long FADE_DURATION = 200L;
    @Nullable
    private Component residualPreview;
    private long fadeTime;
    private long lastTime;

    public void reset(long param0) {
        this.residualPreview = null;
        this.fadeTime = 0L;
        this.lastTime = param0;
    }

    public ChatPreviewAnimator.State get(long param0, @Nullable Component param1) {
        long var0 = param0 - this.lastTime;
        this.lastTime = param0;
        return param1 != null ? this.getEnabled(var0, param1) : this.getDisabled(var0);
    }

    private ChatPreviewAnimator.State getEnabled(long param0, Component param1) {
        this.residualPreview = param1;
        if (this.fadeTime < 200L) {
            this.fadeTime = Math.min(this.fadeTime + param0, 200L);
        }

        return new ChatPreviewAnimator.State(param1, alpha(this.fadeTime));
    }

    private ChatPreviewAnimator.State getDisabled(long param0) {
        if (this.fadeTime > 0L) {
            this.fadeTime = Math.max(this.fadeTime - param0, 0L);
        }

        return this.fadeTime > 0L ? new ChatPreviewAnimator.State(this.residualPreview, alpha(this.fadeTime)) : ChatPreviewAnimator.State.DISABLED;
    }

    private static float alpha(long param0) {
        return (float)param0 / 200.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public static record State(@Nullable Component preview, float alpha) {
        public static final ChatPreviewAnimator.State DISABLED = new ChatPreviewAnimator.State(null, 0.0F);
    }
}
