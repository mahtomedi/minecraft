package net.minecraft.client;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

@OnlyIn(Dist.CLIENT)
public class StringSplitter {
    private final StringSplitter.WidthProvider widthProvider;

    public StringSplitter(StringSplitter.WidthProvider param0) {
        this.widthProvider = param0;
    }

    public float stringWidth(@Nullable String param0) {
        if (param0 == null) {
            return 0.0F;
        } else {
            MutableFloat var0 = new MutableFloat();
            StringDecomposer.iterateFormatted(param0, Style.EMPTY, (param1, param2, param3) -> {
                var0.add(this.widthProvider.getWidth(param3, param2));
                return true;
            });
            return var0.floatValue();
        }
    }

    public float stringWidth(FormattedText param0) {
        MutableFloat var0 = new MutableFloat();
        StringDecomposer.iterateFormatted(param0, Style.EMPTY, (param1, param2, param3) -> {
            var0.add(this.widthProvider.getWidth(param3, param2));
            return true;
        });
        return var0.floatValue();
    }

    public int plainIndexAtWidth(String param0, int param1, Style param2) {
        StringSplitter.WidthLimitedCharSink var0 = new StringSplitter.WidthLimitedCharSink((float)param1);
        StringDecomposer.iterate(param0, param2, var0);
        return var0.getPosition();
    }

    public String plainHeadByWidth(String param0, int param1, Style param2) {
        return param0.substring(0, this.plainIndexAtWidth(param0, param1, param2));
    }

    public String plainTailByWidth(String param0, int param1, Style param2) {
        MutableFloat var0 = new MutableFloat();
        MutableInt var1 = new MutableInt(param0.length());
        StringDecomposer.iterateBackwards(param0, param2, (param3, param4, param5) -> {
            float var0x = var0.addAndGet(this.widthProvider.getWidth(param5, param4));
            if (var0x > (float)param1) {
                return false;
            } else {
                var1.setValue(param3);
                return true;
            }
        });
        return param0.substring(var1.intValue());
    }

    @Nullable
    public Style componentStyleAtWidth(FormattedText param0, int param1) {
        StringSplitter.WidthLimitedCharSink var0 = new StringSplitter.WidthLimitedCharSink((float)param1);
        return param0.<Style>visit(
                (param1x, param2) -> StringDecomposer.iterateFormatted(param2, param1x, var0) ? Optional.empty() : Optional.of(param1x), Style.EMPTY
            )
            .orElse(null);
    }

    public FormattedText headByWidth(FormattedText param0, int param1, Style param2) {
        final StringSplitter.WidthLimitedCharSink var0 = new StringSplitter.WidthLimitedCharSink((float)param1);
        return param0.visit(new FormattedText.StyledContentConsumer<FormattedText>() {
            private final ComponentCollector collector = new ComponentCollector();

            @Override
            public Optional<FormattedText> accept(Style param0, String param1) {
                var0.resetPosition();
                if (!StringDecomposer.iterateFormatted(param1, param0, var0)) {
                    String var0 = param1.substring(0, var0.getPosition());
                    if (!var0.isEmpty()) {
                        this.collector.append(FormattedText.of(var0, param0));
                    }

                    return Optional.of(this.collector.getResultOrEmpty());
                } else {
                    if (!param1.isEmpty()) {
                        this.collector.append(FormattedText.of(param1, param0));
                    }

                    return Optional.empty();
                }
            }
        }, param2).orElse(param0);
    }

    public static int getWordPosition(String param0, int param1, int param2, boolean param3) {
        int var0 = param2;
        boolean var1 = param1 < 0;
        int var2 = Math.abs(param1);

        for(int var3 = 0; var3 < var2; ++var3) {
            if (var1) {
                while(param3 && var0 > 0 && (param0.charAt(var0 - 1) == ' ' || param0.charAt(var0 - 1) == '\n')) {
                    --var0;
                }

                while(var0 > 0 && param0.charAt(var0 - 1) != ' ' && param0.charAt(var0 - 1) != '\n') {
                    --var0;
                }
            } else {
                int var4 = param0.length();
                int var5 = param0.indexOf(32, var0);
                int var6 = param0.indexOf(10, var0);
                if (var5 == -1 && var6 == -1) {
                    var0 = -1;
                } else if (var5 != -1 && var6 != -1) {
                    var0 = Math.min(var5, var6);
                } else if (var5 != -1) {
                    var0 = var5;
                } else {
                    var0 = var6;
                }

                if (var0 == -1) {
                    var0 = var4;
                } else {
                    while(param3 && var0 < var4 && (param0.charAt(var0) == ' ' || param0.charAt(var0) == '\n')) {
                        ++var0;
                    }
                }
            }
        }

        return var0;
    }

    public void splitLines(String param0, int param1, Style param2, boolean param3, StringSplitter.LinePosConsumer param4) {
        int var0 = 0;
        int var1 = param0.length();

        StringSplitter.LineBreakFinder var3;
        for(Style var2 = param2; var0 < var1; var2 = var3.getSplitStyle()) {
            var3 = new StringSplitter.LineBreakFinder((float)param1);
            boolean var4 = StringDecomposer.iterateFormatted(param0, var0, var2, param2, var3);
            if (var4) {
                param4.accept(var2, var0, var1);
                break;
            }

            int var5 = var3.getSplitPosition();
            char var6 = param0.charAt(var5);
            int var7 = var6 != '\n' && var6 != ' ' ? var5 : var5 + 1;
            param4.accept(var2, var0, param3 ? var7 : var5);
            var0 = var7;
        }

    }

    public List<FormattedText> splitLines(String param0, int param1, Style param2) {
        List<FormattedText> var0 = Lists.newArrayList();
        this.splitLines(param0, param1, param2, false, (param2x, param3, param4) -> var0.add(FormattedText.of(param0.substring(param3, param4), param2x)));
        return var0;
    }

    public List<FormattedText> splitLines(FormattedText param0, int param1, Style param2) {
        List<FormattedText> var0 = Lists.newArrayList();
        List<StringSplitter.LineComponent> var1 = Lists.newArrayList();
        param0.visit((param1x, param2x) -> {
            if (!param2x.isEmpty()) {
                var1.add(new StringSplitter.LineComponent(param2x, param1x));
            }

            return Optional.empty();
        }, param2);
        StringSplitter.FlatComponents var2 = new StringSplitter.FlatComponents(var1);
        boolean var3 = true;
        boolean var4 = false;

        while(var3) {
            var3 = false;
            StringSplitter.LineBreakFinder var5 = new StringSplitter.LineBreakFinder((float)param1);

            for(StringSplitter.LineComponent var6 : var2.parts) {
                boolean var7 = StringDecomposer.iterateFormatted(var6.contents, 0, var6.style, param2, var5);
                if (!var7) {
                    int var8 = var5.getSplitPosition();
                    Style var9 = var5.getSplitStyle();
                    char var10 = var2.charAt(var8);
                    boolean var11 = var10 == '\n';
                    boolean var12 = var11 || var10 == ' ';
                    var4 = var11;
                    var0.add(var2.splitAt(var8, var12 ? 1 : 0, var9));
                    var3 = true;
                    break;
                }

                var5.addToOffset(var6.contents.length());
            }
        }

        FormattedText var13 = var2.getRemainder();
        if (var13 != null) {
            var0.add(var13);
        } else if (var4) {
            var0.add(FormattedText.EMPTY);
        }

        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    static class FlatComponents {
        private final List<StringSplitter.LineComponent> parts;
        private String flatParts;

        public FlatComponents(List<StringSplitter.LineComponent> param0) {
            this.parts = param0;
            this.flatParts = param0.stream().map(param0x -> param0x.contents).collect(Collectors.joining());
        }

        public char charAt(int param0) {
            return this.flatParts.charAt(param0);
        }

        public FormattedText splitAt(int param0, int param1, Style param2) {
            ComponentCollector var0 = new ComponentCollector();
            ListIterator<StringSplitter.LineComponent> var1 = this.parts.listIterator();
            int var2 = param0;
            boolean var3 = false;

            while(var1.hasNext()) {
                StringSplitter.LineComponent var4 = var1.next();
                String var5 = var4.contents;
                int var6 = var5.length();
                if (!var3) {
                    if (var2 > var6) {
                        var0.append(var4);
                        var1.remove();
                        var2 -= var6;
                    } else {
                        String var7 = var5.substring(0, var2);
                        if (!var7.isEmpty()) {
                            var0.append(FormattedText.of(var7, var4.style));
                        }

                        var2 += param1;
                        var3 = true;
                    }
                }

                if (var3) {
                    if (var2 <= var6) {
                        String var8 = var5.substring(var2);
                        if (var8.isEmpty()) {
                            var1.remove();
                        } else {
                            var1.set(new StringSplitter.LineComponent(var8, param2));
                        }
                        break;
                    }

                    var1.remove();
                    var2 -= var6;
                }
            }

            this.flatParts = this.flatParts.substring(param0 + param1);
            return var0.getResultOrEmpty();
        }

        @Nullable
        public FormattedText getRemainder() {
            ComponentCollector var0 = new ComponentCollector();
            this.parts.forEach(var0::append);
            this.parts.clear();
            return var0.getResult();
        }
    }

    @OnlyIn(Dist.CLIENT)
    class LineBreakFinder implements StringDecomposer.Output {
        private final float maxWidth;
        private int lineBreak = -1;
        private Style lineBreakStyle = Style.EMPTY;
        private boolean hadNonZeroWidthChar;
        private float width;
        private int lastSpace = -1;
        private Style lastSpaceStyle = Style.EMPTY;
        private int nextChar;
        private int offset;

        public LineBreakFinder(float param0) {
            this.maxWidth = Math.max(param0, 1.0F);
        }

        @Override
        public boolean onChar(int param0, Style param1, int param2) {
            int var0 = param0 + this.offset;
            switch(param2) {
                case 10:
                    return this.finishIteration(var0, param1);
                case 32:
                    this.lastSpace = var0;
                    this.lastSpaceStyle = param1;
                default:
                    float var1 = StringSplitter.this.widthProvider.getWidth(param2, param1);
                    this.width += var1;
                    if (!this.hadNonZeroWidthChar || !(this.width > this.maxWidth)) {
                        this.hadNonZeroWidthChar |= var1 != 0.0F;
                        this.nextChar = var0 + Character.charCount(param2);
                        return true;
                    } else {
                        return this.lastSpace != -1 ? this.finishIteration(this.lastSpace, this.lastSpaceStyle) : this.finishIteration(var0, param1);
                    }
            }
        }

        private boolean finishIteration(int param0, Style param1) {
            this.lineBreak = param0;
            this.lineBreakStyle = param1;
            return false;
        }

        private boolean lineBreakFound() {
            return this.lineBreak != -1;
        }

        public int getSplitPosition() {
            return this.lineBreakFound() ? this.lineBreak : this.nextChar;
        }

        public Style getSplitStyle() {
            return this.lineBreakStyle;
        }

        public void addToOffset(int param0) {
            this.offset += param0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class LineComponent implements FormattedText {
        private final String contents;
        private final Style style;

        public LineComponent(String param0, Style param1) {
            this.contents = param0;
            this.style = param1;
        }

        @Override
        public <T> Optional<T> visit(FormattedText.ContentConsumer<T> param0) {
            return param0.accept(this.contents);
        }

        @Override
        public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> param0, Style param1) {
            return param0.accept(this.style.applyTo(param1), this.contents);
        }
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface LinePosConsumer {
        void accept(Style var1, int var2, int var3);
    }

    @OnlyIn(Dist.CLIENT)
    class WidthLimitedCharSink implements StringDecomposer.Output {
        private float maxWidth;
        private int position;

        public WidthLimitedCharSink(float param0) {
            this.maxWidth = param0;
        }

        @Override
        public boolean onChar(int param0, Style param1, int param2) {
            this.maxWidth -= StringSplitter.this.widthProvider.getWidth(param2, param1);
            if (this.maxWidth >= 0.0F) {
                this.position = param0 + Character.charCount(param2);
                return true;
            } else {
                return false;
            }
        }

        public int getPosition() {
            return this.position;
        }

        public void resetPosition() {
            this.position = 0;
        }
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface WidthProvider {
        float getWidth(int var1, Style var2);
    }
}
