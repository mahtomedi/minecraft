package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public class LocateCommand {
    private static final DynamicCommandExceptionType ERROR_FAILED = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.locate.failed", param0)
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("locate")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("structure", ResourceLocationArgument.id())
                        .suggests(SuggestionProviders.AVAILABLE_STRUCTURES)
                        .executes(param0x -> locate(param0x.getSource(), ResourceLocationArgument.getStructureFeature(param0x, "structure")))
                )
        );
    }

    private static int locate(CommandSourceStack param0, ResourceLocationArgument.LocatedResource<StructureFeature<?>> param1) throws CommandSyntaxException {
        StructureFeature<?> var0 = param1.resource();
        BlockPos var1 = new BlockPos(param0.getPosition());
        BlockPos var2 = param0.getLevel().findNearestMapFeature(var0, var1, 100, false);
        ResourceLocation var3 = param1.id();
        if (var2 == null) {
            throw ERROR_FAILED.create(var3);
        } else {
            return showLocateResult(param0, var3.toString(), var1, var2, "commands.locate.success");
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
