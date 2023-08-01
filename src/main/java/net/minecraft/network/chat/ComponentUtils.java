package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;

public class ComponentUtils {
    public static final String DEFAULT_SEPARATOR_TEXT = ", ";
    public static final Component DEFAULT_SEPARATOR = Component.literal(", ").withStyle(ChatFormatting.GRAY);
    public static final Component DEFAULT_NO_STYLE_SEPARATOR = Component.literal(", ");

    public static MutableComponent mergeStyles(MutableComponent param0, Style param1) {
        if (param1.isEmpty()) {
            return param0;
        } else {
            Style var0 = param0.getStyle();
            if (var0.isEmpty()) {
                return param0.setStyle(param1);
            } else {
                return var0.equals(param1) ? param0 : param0.setStyle(var0.applyTo(param1));
            }
        }
    }

    public static Optional<MutableComponent> updateForEntity(
        @Nullable CommandSourceStack param0, Optional<Component> param1, @Nullable Entity param2, int param3
    ) throws CommandSyntaxException {
        return param1.isPresent() ? Optional.of(updateForEntity(param0, param1.get(), param2, param3)) : Optional.empty();
    }

    public static MutableComponent updateForEntity(@Nullable CommandSourceStack param0, Component param1, @Nullable Entity param2, int param3) throws CommandSyntaxException {
        if (param3 > 100) {
            return param1.copy();
        } else {
            MutableComponent var0 = param1.getContents().resolve(param0, param2, param3 + 1);

            for(Component var1 : param1.getSiblings()) {
                var0.append(updateForEntity(param0, var1, param2, param3 + 1));
            }

            return var0.withStyle(resolveStyle(param0, param1.getStyle(), param2, param3));
        }
    }

    private static Style resolveStyle(@Nullable CommandSourceStack param0, Style param1, @Nullable Entity param2, int param3) throws CommandSyntaxException {
        HoverEvent var0 = param1.getHoverEvent();
        if (var0 != null) {
            Component var1 = var0.getValue(HoverEvent.Action.SHOW_TEXT);
            if (var1 != null) {
                HoverEvent var2 = new HoverEvent(HoverEvent.Action.SHOW_TEXT, updateForEntity(param0, var1, param2, param3 + 1));
                return param1.withHoverEvent(var2);
            }
        }

        return param1;
    }

    public static Component formatList(Collection<String> param0) {
        return formatAndSortList(param0, param0x -> Component.literal(param0x).withStyle(ChatFormatting.GREEN));
    }

    public static <T extends Comparable<T>> Component formatAndSortList(Collection<T> param0, Function<T, Component> param1) {
        if (param0.isEmpty()) {
            return CommonComponents.EMPTY;
        } else if (param0.size() == 1) {
            return param1.apply(param0.iterator().next());
        } else {
            List<T> var0 = Lists.newArrayList(param0);
            var0.sort(Comparable::compareTo);
            return formatList(var0, param1);
        }
    }

    public static <T> Component formatList(Collection<? extends T> param0, Function<T, Component> param1) {
        return formatList(param0, DEFAULT_SEPARATOR, param1);
    }

    public static <T> MutableComponent formatList(Collection<? extends T> param0, Optional<? extends Component> param1, Function<T, Component> param2) {
        return formatList(param0, DataFixUtils.orElse(param1, DEFAULT_SEPARATOR), param2);
    }

    public static Component formatList(Collection<? extends Component> param0, Component param1) {
        return formatList(param0, param1, Function.identity());
    }

    public static <T> MutableComponent formatList(Collection<? extends T> param0, Component param1, Function<T, Component> param2) {
        if (param0.isEmpty()) {
            return Component.empty();
        } else if (param0.size() == 1) {
            return param2.apply(param0.iterator().next()).copy();
        } else {
            MutableComponent var0 = Component.empty();
            boolean var1 = true;

            for(T var2 : param0) {
                if (!var1) {
                    var0.append(param1);
                }

                var0.append(param2.apply(var2));
                var1 = false;
            }

            return var0;
        }
    }

    public static MutableComponent wrapInSquareBrackets(Component param0) {
        return Component.translatable("chat.square_brackets", param0);
    }

    public static Component fromMessage(Message param0) {
        return (Component)(param0 instanceof Component ? (Component)param0 : Component.literal(param0.getString()));
    }

    public static boolean isTranslationResolvable(@Nullable Component param0) {
        if (param0 != null) {
            ComponentContents var1 = param0.getContents();
            if (var1 instanceof TranslatableContents var0) {
                String var1x = var0.getKey();
                String var2x = var0.getFallback();
                return var2x != null || Language.getInstance().has(var1x);
            }
        }

        return true;
    }

    public static MutableComponent copyOnClickText(String param0) {
        return wrapInSquareBrackets(
            Component.literal(param0)
                .withStyle(
                    param1 -> param1.withColor(ChatFormatting.GREEN)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, param0))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click")))
                            .withInsertion(param0)
                )
        );
    }
}
