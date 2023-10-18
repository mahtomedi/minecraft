package net.minecraft.client.renderer;

import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SectionBufferBuilderPool {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int MAX_BUILDERS_32_BIT = 4;
    private final Queue<SectionBufferBuilderPack> freeBuffers;
    private volatile int freeBufferCount;

    private SectionBufferBuilderPool(List<SectionBufferBuilderPack> param0) {
        this.freeBuffers = Queues.newArrayDeque(param0);
        this.freeBufferCount = this.freeBuffers.size();
    }

    public static SectionBufferBuilderPool allocate(int param0) {
        int var0 = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3) / SectionBufferBuilderPack.TOTAL_BUFFERS_SIZE);
        int var1 = Math.max(1, Math.min(param0, var0));
        List<SectionBufferBuilderPack> var2 = new ArrayList<>(var1);

        try {
            for(int var3 = 0; var3 < var1; ++var3) {
                var2.add(new SectionBufferBuilderPack());
            }
        } catch (OutOfMemoryError var7) {
            LOGGER.warn("Allocated only {}/{} buffers", var2.size(), var1);
            int var5 = Math.min(var2.size() * 2 / 3, var2.size() - 1);

            for(int var6 = 0; var6 < var5; ++var6) {
                var2.remove(var2.size() - 1).close();
            }
        }

        return new SectionBufferBuilderPool(var2);
    }

    @Nullable
    public SectionBufferBuilderPack acquire() {
        SectionBufferBuilderPack var0 = this.freeBuffers.poll();
        if (var0 != null) {
            this.freeBufferCount = this.freeBuffers.size();
            return var0;
        } else {
            return null;
        }
    }

    public void release(SectionBufferBuilderPack param0) {
        this.freeBuffers.add(param0);
        this.freeBufferCount = this.freeBuffers.size();
    }

    public boolean isEmpty() {
        return this.freeBuffers.isEmpty();
    }

    public int getFreeBufferCount() {
        return this.freeBufferCount;
    }
}
