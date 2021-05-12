package com.mojang.blaze3d.preprocessor;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

@OnlyIn(Dist.CLIENT)
public abstract class GlslPreprocessor {
    private static final String C_COMMENT = "/\\*(?:[^*]|\\*+[^*/])*\\*+/";
    private static final String LINE_COMMENT = "//[^\\v]*";
    private static final Pattern REGEX_MOJ_IMPORT = Pattern.compile(
        "(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*moj_import(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(?:\"(.*)\"|<(.*)>))"
    );
    private static final Pattern REGEX_VERSION = Pattern.compile(
        "(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*version(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(\\d+))\\b"
    );
    private static final Pattern REGEX_ENDS_WITH_WHITESPACE = Pattern.compile("(?:^|\\v)(?:\\s|/\\*(?:[^*]|\\*+[^*/])*\\*+/|(//[^\\v]*))*\\z");

    public List<String> process(String param0) {
        GlslPreprocessor.Context var0 = new GlslPreprocessor.Context();
        List<String> var1 = this.processImports(param0, var0, "");
        var1.set(0, this.setVersion(var1.get(0), var0.glslVersion));
        return var1;
    }

    private List<String> processImports(String param0, GlslPreprocessor.Context param1, String param2) {
        int var0 = param1.sourceId;
        int var1 = 0;
        String var2 = "";
        List<String> var3 = Lists.newArrayList();
        Matcher var4 = REGEX_MOJ_IMPORT.matcher(param0);

        while(var4.find()) {
            if (!isDirectiveDisabled(param0, var4, var1)) {
                String var5 = var4.group(2);
                boolean var6 = var5 != null;
                if (!var6) {
                    var5 = var4.group(3);
                }

                if (var5 != null) {
                    String var7 = param0.substring(var1, var4.start(1));
                    String var8 = param2 + var5;
                    String var9 = this.applyImport(var6, var8);
                    if (!Strings.isEmpty(var9)) {
                        if (!StringUtil.endsWithNewLine(var9)) {
                            var9 = var9 + System.lineSeparator();
                        }

                        ++param1.sourceId;
                        int var10 = param1.sourceId;
                        List<String> var11 = this.processImports(var9, param1, var6 ? FileUtil.getFullResourcePath(var8) : "");
                        var11.set(0, String.format("#line %d %d\n%s", 0, var10, this.processVersions(var11.get(0), param1)));
                        if (!StringUtils.isBlank(var7)) {
                            var3.add(var7);
                        }

                        var3.addAll(var11);
                    } else {
                        String var12 = var6 ? String.format("/*#moj_import \"%s\"*/", var5) : String.format("/*#moj_import <%s>*/", var5);
                        var3.add(var2 + var7 + var12);
                    }

                    int var13 = StringUtil.lineCount(param0.substring(0, var4.end(1)));
                    var2 = String.format("#line %d %d", var13, var0);
                    var1 = var4.end(1);
                }
            }
        }

        String var14 = param0.substring(var1);
        if (!StringUtils.isBlank(var14)) {
            var3.add(var2 + var14);
        }

        return var3;
    }

    private String processVersions(String param0, GlslPreprocessor.Context param1) {
        Matcher var0 = REGEX_VERSION.matcher(param0);
        if (var0.find() && isDirectiveEnabled(param0, var0)) {
            param1.glslVersion = Math.max(param1.glslVersion, Integer.parseInt(var0.group(2)));
            return param0.substring(0, var0.start(1)) + "/*" + param0.substring(var0.start(1), var0.end(1)) + "*/" + param0.substring(var0.end(1));
        } else {
            return param0;
        }
    }

    private String setVersion(String param0, int param1) {
        Matcher var0 = REGEX_VERSION.matcher(param0);
        return var0.find() && isDirectiveEnabled(param0, var0)
            ? param0.substring(0, var0.start(2)) + Math.max(param1, Integer.parseInt(var0.group(2))) + param0.substring(var0.end(2))
            : param0;
    }

    private static boolean isDirectiveEnabled(String param0, Matcher param1) {
        return !isDirectiveDisabled(param0, param1, 0);
    }

    private static boolean isDirectiveDisabled(String param0, Matcher param1, int param2) {
        int var0 = param1.start() - param2;
        if (var0 == 0) {
            return false;
        } else {
            Matcher var1 = REGEX_ENDS_WITH_WHITESPACE.matcher(param0.substring(param2, param1.start()));
            if (!var1.find()) {
                return true;
            } else {
                int var2 = var1.end(1);
                return var2 == param1.start();
            }
        }
    }

    @Nullable
    public abstract String applyImport(boolean var1, String var2);

    @OnlyIn(Dist.CLIENT)
    static final class Context {
        int glslVersion;
        int sourceId;
    }
}
