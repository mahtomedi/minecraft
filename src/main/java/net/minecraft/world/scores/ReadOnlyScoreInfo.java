package net.minecraft.world.scores;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;

public interface ReadOnlyScoreInfo {
    int value();

    boolean isLocked();

    @Nullable
    NumberFormat numberFormat();

    default MutableComponent formatValue(NumberFormat param0) {
        return Objects.requireNonNullElse(this.numberFormat(), param0).format(this.value());
    }

    static MutableComponent safeFormatValue(@Nullable ReadOnlyScoreInfo param0, NumberFormat param1) {
        return param0 != null ? param0.formatValue(param1) : param1.format(0);
    }
}
