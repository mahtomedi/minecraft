package net.minecraft.util.profiling;

import java.io.File;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ProfileResults {
    @OnlyIn(Dist.CLIENT)
    List<ResultField> getTimes(String var1);

    boolean saveResults(File var1);

    long getStartTimeNano();

    int getStartTimeTicks();

    long getEndTimeNano();

    int getEndTimeTicks();

    default long getNanoDuration() {
        return this.getEndTimeNano() - this.getStartTimeNano();
    }

    default int getTickDuration() {
        return this.getEndTimeTicks() - this.getStartTimeTicks();
    }

    String getProfilerResults();

    static String demanglePath(String param0) {
        return param0.replace('\u001e', '.');
    }
}
