package net.minecraft.world.entity.schedule;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import java.util.Collection;
import java.util.List;

public class Timeline {
    private final List<Keyframe> keyframes = Lists.newArrayList();
    private int previousIndex;

    public ImmutableList<Keyframe> getKeyframes() {
        return ImmutableList.copyOf(this.keyframes);
    }

    public Timeline addKeyframe(int param0, float param1) {
        this.keyframes.add(new Keyframe(param0, param1));
        this.sortAndDeduplicateKeyframes();
        return this;
    }

    public Timeline addKeyframes(Collection<Keyframe> param0) {
        this.keyframes.addAll(param0);
        this.sortAndDeduplicateKeyframes();
        return this;
    }

    private void sortAndDeduplicateKeyframes() {
        Int2ObjectSortedMap<Keyframe> var0 = new Int2ObjectAVLTreeMap<>();
        this.keyframes.forEach(param1 -> param1.getTimeStamp());
        this.keyframes.clear();
        this.keyframes.addAll(var0.values());
        this.previousIndex = 0;
    }

    public float getValueAt(int param0) {
        if (this.keyframes.size() <= 0) {
            return 0.0F;
        } else {
            Keyframe var0 = this.keyframes.get(this.previousIndex);
            Keyframe var1 = this.keyframes.get(this.keyframes.size() - 1);
            boolean var2 = param0 < var0.getTimeStamp();
            int var3 = var2 ? 0 : this.previousIndex;
            float var4 = var2 ? var1.getValue() : var0.getValue();

            for(int var5 = var3; var5 < this.keyframes.size(); ++var5) {
                Keyframe var6 = this.keyframes.get(var5);
                if (var6.getTimeStamp() > param0) {
                    break;
                }

                this.previousIndex = var5;
                var4 = var6.getValue();
            }

            return var4;
        }
    }
}
