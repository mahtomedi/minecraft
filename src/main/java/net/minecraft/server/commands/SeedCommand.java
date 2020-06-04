package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class SeedCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0, boolean param1) {
        param0.register(
            Commands.literal("seed")
                .requires(param1x -> !param1 || param1x.hasPermission(2))
                .executes(
                    param0x -> {
                        long var0x = param0x.getSource().getLevel().getSeed();
                        Component var1x = ComponentUtils.wrapInSquareBrackets(
                            new TextComponent(String.valueOf(var0x))
                                .withStyle(
                                    param1x -> param1x.withColor(ChatFormatting.GREEN)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(var0x)))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.copy.click")))
                                            .withInsertion(String.valueOf(var0x))
                                )
                        );
                        param0x.getSource().sendSuccess(new TranslatableComponent("commands.seed.success", var1x), false);
                        return (int)var0x;
                    }
                )
        );
    }
}
