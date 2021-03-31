package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;

public class SubStringSource {
    private final String plainText;
    private final List<Style> charStyles;
    private final Int2IntFunction reverseCharModifier;

    private SubStringSource(String param0, List<Style> param1, Int2IntFunction param2) {
        this.plainText = param0;
        this.charStyles = ImmutableList.copyOf(param1);
        this.reverseCharModifier = param2;
    }

    public String getPlainText() {
        return this.plainText;
    }

    public List<FormattedCharSequence> substring(int param0, int param1, boolean param2) {
        if (param1 == 0) {
            return ImmutableList.of();
        } else {
            List<FormattedCharSequence> var0 = Lists.newArrayList();
            Style var1 = this.charStyles.get(param0);
            int var2 = param0;

            for(int var3 = 1; var3 < param1; ++var3) {
                int var4 = param0 + var3;
                Style var5 = this.charStyles.get(var4);
                if (!var5.equals(var1)) {
                    String var6 = this.plainText.substring(var2, var4);
                    var0.add(param2 ? FormattedCharSequence.backward(var6, var1, this.reverseCharModifier) : FormattedCharSequence.forward(var6, var1));
                    var1 = var5;
                    var2 = var4;
                }
            }

            if (var2 < param0 + param1) {
                String var7 = this.plainText.substring(var2, param0 + param1);
                var0.add(param2 ? FormattedCharSequence.backward(var7, var1, this.reverseCharModifier) : FormattedCharSequence.forward(var7, var1));
            }

            return param2 ? Lists.reverse(var0) : var0;
        }
    }

    public static SubStringSource create(FormattedText param0) {
        return create(param0, param0x -> param0x, param0x -> param0x);
    }

    public static SubStringSource create(FormattedText param0, Int2IntFunction param1, UnaryOperator<String> param2) {
        StringBuilder var0 = new StringBuilder();
        List<Style> var1 = Lists.newArrayList();
        param0.visit((param2x, param3) -> {
            StringDecomposer.iterateFormatted(param3, param2x, (param2xx, param3x, param4) -> {
                var0.appendCodePoint(param4);
                int var0x = Character.charCount(param4);

                for(int var1x = 0; var1x < var0x; ++var1x) {
                    var1.add(param3x);
                }

                return true;
            });
            return Optional.empty();
        }, Style.EMPTY);
        return new SubStringSource(param2.apply(var0.toString()), var1, param1);
    }
}
