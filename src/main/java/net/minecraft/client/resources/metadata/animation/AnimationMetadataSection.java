package net.minecraft.client.resources.metadata.animation;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnimationMetadataSection {
    public static final AnimationMetadataSectionSerializer SERIALIZER = new AnimationMetadataSectionSerializer();
    private final List<AnimationFrame> frames;
    private final int frameWidth;
    private final int frameHeight;
    private final int defaultFrameTime;
    private final boolean interpolatedFrames;

    public AnimationMetadataSection(List<AnimationFrame> param0, int param1, int param2, int param3, boolean param4) {
        this.frames = param0;
        this.frameWidth = param1;
        this.frameHeight = param2;
        this.defaultFrameTime = param3;
        this.interpolatedFrames = param4;
    }

    public int getFrameHeight() {
        return this.frameHeight;
    }

    public int getFrameWidth() {
        return this.frameWidth;
    }

    public int getFrameCount() {
        return this.frames.size();
    }

    public int getDefaultFrameTime() {
        return this.defaultFrameTime;
    }

    public boolean isInterpolatedFrames() {
        return this.interpolatedFrames;
    }

    private AnimationFrame getFrame(int param0) {
        return this.frames.get(param0);
    }

    public int getFrameTime(int param0) {
        AnimationFrame var0 = this.getFrame(param0);
        return var0.isTimeUnknown() ? this.defaultFrameTime : var0.getTime();
    }

    public int getFrameIndex(int param0) {
        return this.frames.get(param0).getIndex();
    }

    public Set<Integer> getUniqueFrameIndices() {
        Set<Integer> var0 = Sets.newHashSet();

        for(AnimationFrame var1 : this.frames) {
            var0.add(var1.getIndex());
        }

        return var0;
    }
}
