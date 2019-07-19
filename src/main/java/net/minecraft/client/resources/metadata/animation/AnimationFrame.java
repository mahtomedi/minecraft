package net.minecraft.client.resources.metadata.animation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnimationFrame {
    private final int index;
    private final int time;

    public AnimationFrame(int param0) {
        this(param0, -1);
    }

    public AnimationFrame(int param0, int param1) {
        this.index = param0;
        this.time = param1;
    }

    public boolean isTimeUnknown() {
        return this.time == -1;
    }

    public int getTime() {
        return this.time;
    }

    public int getIndex() {
        return this.index;
    }
}
