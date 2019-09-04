package net.minecraft.util.profiling;

import java.io.File;
import java.util.Collections;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EmptyProfileResults implements ProfileResults {
    public static final EmptyProfileResults EMPTY = new EmptyProfileResults();

    private EmptyProfileResults() {
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<ResultField> getTimes(String param0) {
        return Collections.emptyList();
    }

    @Override
    public boolean saveResults(File param0) {
        return false;
    }

    @Override
    public long getStartTimeNano() {
        return 0L;
    }

    @Override
    public int getStartTimeTicks() {
        return 0;
    }

    @Override
    public long getEndTimeNano() {
        return 0L;
    }

    @Override
    public int getEndTimeTicks() {
        return 0;
    }
}
