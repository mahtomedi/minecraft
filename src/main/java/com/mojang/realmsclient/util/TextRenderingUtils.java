package com.mojang.realmsclient.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextRenderingUtils {
    static List<String> lineBreak(String param0) {
        return Arrays.asList(param0.split("\\n"));
    }

    public static List<TextRenderingUtils.Line> decompose(String param0, TextRenderingUtils.LineSegment... param1) {
        return decompose(param0, Arrays.asList(param1));
    }

    private static List<TextRenderingUtils.Line> decompose(String param0, List<TextRenderingUtils.LineSegment> param1) {
        List<String> var0 = lineBreak(param0);
        return insertLinks(var0, param1);
    }

    private static List<TextRenderingUtils.Line> insertLinks(List<String> param0, List<TextRenderingUtils.LineSegment> param1) {
        int var0 = 0;
        ArrayList<TextRenderingUtils.Line> var1 = new ArrayList<>();

        for(String var2 : param0) {
            List<TextRenderingUtils.LineSegment> var3 = new ArrayList<>();

            for(String var5 : split(var2, "%link")) {
                if (var5.equals("%link")) {
                    var3.add(param1.get(var0++));
                } else {
                    var3.add(TextRenderingUtils.LineSegment.text(var5));
                }
            }

            var1.add(new TextRenderingUtils.Line(var3));
        }

        return var1;
    }

    public static List<String> split(String param0, String param1) {
        if (param1.isEmpty()) {
            throw new IllegalArgumentException("Delimiter cannot be the empty string");
        } else {
            List<String> var0 = new ArrayList<>();

            int var1;
            int var2;
            for(var1 = 0; (var2 = param0.indexOf(param1, var1)) != -1; var1 = var2 + param1.length()) {
                if (var2 > var1) {
                    var0.add(param0.substring(var1, var2));
                }

                var0.add(param1);
            }

            if (var1 < param0.length()) {
                var0.add(param0.substring(var1));
            }

            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Line {
        public final List<TextRenderingUtils.LineSegment> segments;

        Line(List<TextRenderingUtils.LineSegment> param0) {
            this.segments = param0;
        }

        @Override
        public String toString() {
            return "Line{segments=" + this.segments + '}';
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                TextRenderingUtils.Line var0 = (TextRenderingUtils.Line)param0;
                return Objects.equals(this.segments, var0.segments);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.segments);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class LineSegment {
        final String fullText;
        final String linkTitle;
        final String linkUrl;

        private LineSegment(String param0) {
            this.fullText = param0;
            this.linkTitle = null;
            this.linkUrl = null;
        }

        private LineSegment(String param0, String param1, String param2) {
            this.fullText = param0;
            this.linkTitle = param1;
            this.linkUrl = param2;
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                TextRenderingUtils.LineSegment var0 = (TextRenderingUtils.LineSegment)param0;
                return Objects.equals(this.fullText, var0.fullText)
                    && Objects.equals(this.linkTitle, var0.linkTitle)
                    && Objects.equals(this.linkUrl, var0.linkUrl);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.fullText, this.linkTitle, this.linkUrl);
        }

        @Override
        public String toString() {
            return "Segment{fullText='" + this.fullText + '\'' + ", linkTitle='" + this.linkTitle + '\'' + ", linkUrl='" + this.linkUrl + '\'' + '}';
        }

        public String renderedText() {
            return this.isLink() ? this.linkTitle : this.fullText;
        }

        public boolean isLink() {
            return this.linkTitle != null;
        }

        public String getLinkUrl() {
            if (!this.isLink()) {
                throw new IllegalStateException("Not a link: " + this);
            } else {
                return this.linkUrl;
            }
        }

        public static TextRenderingUtils.LineSegment link(String param0, String param1) {
            return new TextRenderingUtils.LineSegment(null, param0, param1);
        }

        static TextRenderingUtils.LineSegment text(String param0) {
            return new TextRenderingUtils.LineSegment(param0);
        }
    }
}
