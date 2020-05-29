package net.minecraft.client;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Unit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StringDecomposer {
    private static final Optional<Object> STOP_ITERATION = Optional.of(Unit.INSTANCE);

    private static boolean feedChar(Style param0, StringDecomposer.Output param1, int param2, char param3) {
        return Character.isSurrogate(param3) ? param1.onChar(param2, param0, 65533) : param1.onChar(param2, param0, param3);
    }

    public static boolean iterate(String param0, Style param1, StringDecomposer.Output param2) {
        int var0 = param0.length();

        for(int var1 = 0; var1 < var0; ++var1) {
            char var2 = param0.charAt(var1);
            if (Character.isHighSurrogate(var2)) {
                if (var1 + 1 >= var0) {
                    if (!param2.onChar(var1, param1, 65533)) {
                        return false;
                    }
                    break;
                }

                char var3 = param0.charAt(var1 + 1);
                if (Character.isLowSurrogate(var3)) {
                    if (!param2.onChar(var1, param1, Character.toCodePoint(var2, var3))) {
                        return false;
                    }

                    ++var1;
                } else if (!param2.onChar(var1, param1, 65533)) {
                    return false;
                }
            } else if (!feedChar(param1, param2, var1, var2)) {
                return false;
            }
        }

        return true;
    }

    public static boolean iterateBackwards(String param0, Style param1, StringDecomposer.Output param2) {
        int var0 = param0.length();

        for(int var1 = var0 - 1; var1 >= 0; --var1) {
            char var2 = param0.charAt(var1);
            if (Character.isLowSurrogate(var2)) {
                if (var1 - 1 < 0) {
                    if (!param2.onChar(0, param1, 65533)) {
                        return false;
                    }
                    break;
                }

                char var3 = param0.charAt(var1 - 1);
                if (Character.isHighSurrogate(var3)) {
                    if (!param2.onChar(--var1, param1, Character.toCodePoint(var3, var2))) {
                        return false;
                    }
                } else if (!param2.onChar(var1, param1, 65533)) {
                    return false;
                }
            } else if (!feedChar(param1, param2, var1, var2)) {
                return false;
            }
        }

        return true;
    }

    public static boolean iterateFormatted(String param0, Style param1, StringDecomposer.Output param2) {
        return iterateFormatted(param0, 0, param1, param2);
    }

    public static boolean iterateFormatted(String param0, int param1, Style param2, StringDecomposer.Output param3) {
        return iterateFormatted(param0, param1, param2, param2, param3);
    }

    public static boolean iterateFormatted(String param0, int param1, Style param2, Style param3, StringDecomposer.Output param4) {
        int var0 = param0.length();
        Style var1 = param2;

        for(int var2 = param1; var2 < var0; ++var2) {
            char var3 = param0.charAt(var2);
            if (var3 == 167) {
                if (var2 + 1 >= var0) {
                    break;
                }

                char var4 = param0.charAt(var2 + 1);
                ChatFormatting var5 = ChatFormatting.getByCode(var4);
                if (var5 != null) {
                    var1 = var5 == ChatFormatting.RESET ? param3 : var1.applyLegacyFormat(var5);
                }

                ++var2;
            } else if (Character.isHighSurrogate(var3)) {
                if (var2 + 1 >= var0) {
                    if (!param4.onChar(var2, var1, 65533)) {
                        return false;
                    }
                    break;
                }

                char var6 = param0.charAt(var2 + 1);
                if (Character.isLowSurrogate(var6)) {
                    if (!param4.onChar(var2, var1, Character.toCodePoint(var3, var6))) {
                        return false;
                    }

                    ++var2;
                } else if (!param4.onChar(var2, var1, 65533)) {
                    return false;
                }
            } else if (!feedChar(var1, param4, var2, var3)) {
                return false;
            }
        }

        return true;
    }

    public static boolean iterateFormatted(FormattedText param0, Style param1, StringDecomposer.Output param2) {
        return !param0.visit((param1x, param2x) -> iterateFormatted(param2x, 0, param1x, param2) ? Optional.empty() : STOP_ITERATION, param1).isPresent();
    }

    public static String filterBrokenSurrogates(String param0) {
        StringBuilder var0 = new StringBuilder();
        iterate(param0, Style.EMPTY, (param1, param2, param3) -> {
            var0.appendCodePoint(param3);
            return true;
        });
        return var0.toString();
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface Output {
        boolean onChar(int var1, Style var2, int var3);
    }
}
