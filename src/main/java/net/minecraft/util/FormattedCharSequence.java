package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.List;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@FunctionalInterface
public interface FormattedCharSequence {
    FormattedCharSequence EMPTY = param0 -> true;

    @OnlyIn(Dist.CLIENT)
    boolean accept(FormattedCharSink var1);

    @OnlyIn(Dist.CLIENT)
    static FormattedCharSequence codepoint(int param0, Style param1) {
        return param2 -> param2.accept(0, param1, param0);
    }

    @OnlyIn(Dist.CLIENT)
    static FormattedCharSequence forward(String param0, Style param1) {
        return param0.isEmpty() ? EMPTY : param2 -> StringDecomposer.iterate(param0, param1, param2);
    }

    @OnlyIn(Dist.CLIENT)
    static FormattedCharSequence backward(String param0, Style param1, Int2IntFunction param2) {
        return param0.isEmpty() ? EMPTY : param3 -> StringDecomposer.iterateBackwards(param0, param1, decorateOutput(param3, param2));
    }

    @OnlyIn(Dist.CLIENT)
    static FormattedCharSink decorateOutput(FormattedCharSink param0, Int2IntFunction param1) {
        return (param2, param3, param4) -> param0.accept(param2, param3, param1.apply(Integer.valueOf(param4)));
    }

    @OnlyIn(Dist.CLIENT)
    static FormattedCharSequence composite(FormattedCharSequence param0, FormattedCharSequence param1) {
        return fromPair(param0, param1);
    }

    @OnlyIn(Dist.CLIENT)
    static FormattedCharSequence composite(List<FormattedCharSequence> param0) {
        int var0 = param0.size();
        switch(var0) {
            case 0:
                return EMPTY;
            case 1:
                return param0.get(0);
            case 2:
                return fromPair(param0.get(0), param0.get(1));
            default:
                return fromList(ImmutableList.copyOf(param0));
        }
    }

    @OnlyIn(Dist.CLIENT)
    static FormattedCharSequence fromPair(FormattedCharSequence param0, FormattedCharSequence param1) {
        return param2 -> param0.accept(param2) && param1.accept(param2);
    }

    @OnlyIn(Dist.CLIENT)
    static FormattedCharSequence fromList(List<FormattedCharSequence> param0) {
        return param1 -> {
            for(FormattedCharSequence var0x : param0) {
                if (!var0x.accept(param1)) {
                    return false;
                }
            }

            return true;
        };
    }
}
