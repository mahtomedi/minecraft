package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public class LocateCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.locate.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        LiteralArgumentBuilder<CommandSourceStack> var0 = Commands.literal("locate").requires(param0x -> param0x.hasPermission(2));

        for(Entry<String, StructureFeature<?>> var1 : StructureFeature.STRUCTURES_REGISTRY.entrySet()) {
            var0 = var0.then(Commands.literal(var1.getKey()).executes(param1 -> locate(param1.getSource(), var1.getValue())));
        }

        param0.register(var0);
    }

    private static int locate(CommandSourceStack param0, StructureFeature<?> param1) throws CommandSyntaxException {
        BlockPos var0 = new BlockPos(param0.getPosition());
        BlockPos var1 = param0.getLevel().findNearestMapFeature(param1, var0, 100, false);
        if (var1 == null) {
            throw ERROR_FAILED.create();
        } else {
            return showLocateResult(param0, param1.getFeatureName(), var0, var1, "commands.locate.success");
        }
    }

    public static int showLocateResult(CommandSourceStack param0, String param1, BlockPos param2, BlockPos param3, String param4) {
        int var0 = Mth.floor(dist(param2.getX(), param2.getZ(), param3.getX(), param3.getZ()));
        Component var1 = ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("chat.coordinates", param3.getX(), "~", param3.getZ()))
            .withStyle(
                param1x -> param1x.withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + param3.getX() + " ~ " + param3.getZ()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.coordinates.tooltip")))
            );
        param0.sendSuccess(new TranslatableComponent(param4, param1, var1, var0), false);
        return var0;
    }

    private static float dist(int param0, int param1, int param2, int param3) {
        int var0 = param2 - param0;
        int var1 = param3 - param1;
        return Mth.sqrt((float)(var0 * var0 + var1 * var1));
    }
}
