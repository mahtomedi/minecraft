package net.minecraft.commands.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;

public record StringTemplate(List<String> segments, List<String> variables) {
    public static StringTemplate fromString(String param0, int param1) {
        Builder<String> var0 = ImmutableList.builder();
        Builder<String> var1 = ImmutableList.builder();
        int var2 = param0.length();
        int var3 = 0;
        int var4 = param0.indexOf(36);

        while(var4 != -1) {
            if (var4 != var2 - 1 && param0.charAt(var4 + 1) == '(') {
                var0.add(param0.substring(var3, var4));
                int var5 = param0.indexOf(41, var4 + 1);
                if (var5 == -1) {
                    throw new IllegalArgumentException("Unterminated macro variable in macro '" + param0 + "' on line " + param1);
                }

                String var6 = param0.substring(var4 + 2, var5);
                if (!isValidVariableName(var6)) {
                    throw new IllegalArgumentException("Invalid macro variable name '" + var6 + "' on line " + param1);
                }

                var1.add(var6);
                var3 = var5 + 1;
                var4 = param0.indexOf(36, var3);
            } else {
                var4 = param0.indexOf(36, var4 + 1);
            }
        }

        if (var3 == 0) {
            throw new IllegalArgumentException("Macro without variables on line " + param1);
        } else {
            if (var3 != var2) {
                var0.add(param0.substring(var3));
            }

            return new StringTemplate(var0.build(), var1.build());
        }
    }

    private static boolean isValidVariableName(String param0) {
        for(int var0 = 0; var0 < param0.length(); ++var0) {
            char var1 = param0.charAt(var0);
            if (!Character.isLetterOrDigit(var1) && var1 != '_') {
                return false;
            }
        }

        return true;
    }

    public String substitute(List<String> param0) {
        StringBuilder var0 = new StringBuilder();

        for(int var1 = 0; var1 < this.variables.size(); ++var1) {
            var0.append(this.segments.get(var1)).append(param0.get(var1));
        }

        if (this.segments.size() > this.variables.size()) {
            var0.append(this.segments.get(this.segments.size() - 1));
        }

        return var0.toString();
    }
}
