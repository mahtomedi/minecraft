package net.minecraft.util.profiling;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class ResultField implements Comparable<ResultField> {
    public final double percentage;
    public final double globalPercentage;
    public final long count;
    public final String name;

    public ResultField(String param0, double param1, double param2, long param3) {
        this.name = param0;
        this.percentage = param1;
        this.globalPercentage = param2;
        this.count = param3;
    }

    public int compareTo(ResultField param0) {
        if (param0.percentage < this.percentage) {
            return -1;
        } else {
            return param0.percentage > this.percentage ? 1 : param0.name.compareTo(this.name);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public int getColor() {
        return (this.name.hashCode() & 11184810) + 4473924;
    }
}
