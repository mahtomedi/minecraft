package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;

public class ComponentUtils {
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

    public static MutableComponent updateForEntity(@Nullable CommandSourceStack param0, Component param1, @Nullable Entity param2, int param3) throws CommandSyntaxException {
        if (param3 > 100) {
            return param1.copy();
        } else {
            MutableComponent var0 = param1 instanceof ContextAwareComponent
                ? ((ContextAwareComponent)param1).resolve(param0, param2, param3 + 1)
                : param1.plainCopy();

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

    public static Component getDisplayName(GameProfile param0) {
        if (param0.getName() != null) {
            return new TextComponent(param0.getName());
        } else {
            return param0.getId() != null ? new TextComponent(param0.getId().toString()) : new TextComponent("(unknown)");
        }
    }

    public static Component formatList(Collection<String> param0) {
        return formatAndSortList(param0, param0x -> new TextComponent(param0x).withStyle(ChatFormatting.GREEN));
    }

    public static <T extends Comparable<T>> Component formatAndSortList(Collection<T> param0, Function<T, Component> param1) {
        if (param0.isEmpty()) {
            return TextComponent.EMPTY;
        } else if (param0.size() == 1) {
            return param1.apply(param0.iterator().next());
        } else {
            List<T> var0 = Lists.newArrayList(param0);
            var0.sort(Comparable::compareTo);
            return formatList(var0, param1);
        }
    }

    public static <T> MutableComponent formatList(Collection<T> param0, Function<T, Component> param1) {
        if (param0.isEmpty()) {
            return new TextComponent("");
        } else if (param0.size() == 1) {
            return param1.apply(param0.iterator().next()).copy();
        } else {
            MutableComponent var0 = new TextComponent("");
            boolean var1 = true;

            for(T var2 : param0) {
                if (!var1) {
                    var0.append(new TextComponent(", ").withStyle(ChatFormatting.GRAY));
                }

                var0.append(param1.apply(var2));
                var1 = false;
            }

            return var0;
        }
    }

    public static MutableComponent wrapInSquareBrackets(Component param0) {
        return new TextComponent("[").append(param0).append("]");
    }

    public static Component fromMessage(Message param0) {
        return (Component)(param0 instanceof Component ? (Component)param0 : new TextComponent(param0.getString()));
    }
}
