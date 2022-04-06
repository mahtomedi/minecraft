package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.Structure;

public class LocateCommand {
    private static final DynamicCommandExceptionType ERROR_FAILED = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.locate.failed", param0)
    );
    private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.locate.invalid", param0)
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("locate")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("structure", ResourceOrTagLocationArgument.resourceOrTag(Registry.STRUCTURE_REGISTRY))
                        .executes(param0x -> locate(param0x.getSource(), ResourceOrTagLocationArgument.getStructure(param0x, "structure")))
                )
        );
    }

    private static int locate(CommandSourceStack param0, ResourceOrTagLocationArgument.Result<Structure> param1) throws CommandSyntaxException {
        Registry<Structure> var0 = param0.getLevel().registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
        HolderSet<Structure> var1 = param1.unwrap()
            .map(param1x -> var0.getHolder(param1x).map(param0x -> HolderSet.direct(param0x)), var0::getTag)
            .orElseThrow(() -> ERROR_INVALID.create(param1.asPrintable()));
        BlockPos var2 = new BlockPos(param0.getPosition());
        ServerLevel var3 = param0.getLevel();
        Pair<BlockPos, Holder<Structure>> var4 = var3.getChunkSource().getGenerator().findNearestMapStructure(var3, var1, var2, 100, false);
        if (var4 == null) {
            throw ERROR_FAILED.create(param1.asPrintable());
        } else {
            return showLocateResult(param0, param1, var2, var4, "commands.locate.success", false);
        }
    }

    public static int showLocateResult(
        CommandSourceStack param0,
        ResourceOrTagLocationArgument.Result<?> param1,
        BlockPos param2,
        Pair<BlockPos, ? extends Holder<?>> param3,
        String param4,
        boolean param5
    ) {
        BlockPos var0 = param3.getFirst();
        String var1 = param1.unwrap()
            .map(
                param0x -> param0x.location().toString(),
                param1x -> "#"
                        + param1x.location()
                        + " ("
                        + (String)param3.getSecond().unwrapKey().map(param0x -> param0x.location().toString()).orElse("[unregistered]")
                        + ")"
            );
        int var2 = param5 ? Mth.floor(Mth.sqrt((float)param2.distSqr(var0))) : Mth.floor(dist(param2.getX(), param2.getZ(), var0.getX(), var0.getZ()));
        String var3 = param5 ? String.valueOf(var0.getY()) : "~";
        Component var4 = ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("chat.coordinates", var0.getX(), var3, var0.getZ()))
            .withStyle(
                param2x -> param2x.withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + var0.getX() + " " + var3 + " " + var0.getZ()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.coordinates.tooltip")))
            );
        param0.sendSuccess(new TranslatableComponent(param4, var1, var4, var2), false);
        return var2;
    }

    private static float dist(int param0, int param1, int param2, int param3) {
        int var0 = param2 - param0;
        int var1 = param3 - param1;
        return Mth.sqrt((float)(var0 * var0 + var1 * var1));
    }
}
