package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
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

public class LocateCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.locate.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("locate")
                .requires(param0x -> param0x.hasPermission(2))
                .then(Commands.literal("Pillager_Outpost").executes(param0x -> locate(param0x.getSource(), "Pillager_Outpost")))
                .then(Commands.literal("Mineshaft").executes(param0x -> locate(param0x.getSource(), "Mineshaft")))
                .then(Commands.literal("Mansion").executes(param0x -> locate(param0x.getSource(), "Mansion")))
                .then(Commands.literal("Igloo").executes(param0x -> locate(param0x.getSource(), "Igloo")))
                .then(Commands.literal("Ruined_Portal").executes(param0x -> locate(param0x.getSource(), "Ruined_Portal")))
                .then(Commands.literal("Desert_Pyramid").executes(param0x -> locate(param0x.getSource(), "Desert_Pyramid")))
                .then(Commands.literal("Jungle_Pyramid").executes(param0x -> locate(param0x.getSource(), "Jungle_Pyramid")))
                .then(Commands.literal("Swamp_Hut").executes(param0x -> locate(param0x.getSource(), "Swamp_Hut")))
                .then(Commands.literal("Stronghold").executes(param0x -> locate(param0x.getSource(), "Stronghold")))
                .then(Commands.literal("Monument").executes(param0x -> locate(param0x.getSource(), "Monument")))
                .then(Commands.literal("Fortress").executes(param0x -> locate(param0x.getSource(), "Fortress")))
                .then(Commands.literal("EndCity").executes(param0x -> locate(param0x.getSource(), "EndCity")))
                .then(Commands.literal("Ocean_Ruin").executes(param0x -> locate(param0x.getSource(), "Ocean_Ruin")))
                .then(Commands.literal("Buried_Treasure").executes(param0x -> locate(param0x.getSource(), "Buried_Treasure")))
                .then(Commands.literal("Shipwreck").executes(param0x -> locate(param0x.getSource(), "Shipwreck")))
                .then(Commands.literal("Village").executes(param0x -> locate(param0x.getSource(), "Village")))
                .then(Commands.literal("Nether_Fossil").executes(param0x -> locate(param0x.getSource(), "Nether_Fossil")))
                .then(Commands.literal("Bastion_Remnant").executes(param0x -> locate(param0x.getSource(), "Bastion_Remnant")))
        );
    }

    private static int locate(CommandSourceStack param0, String param1) throws CommandSyntaxException {
        BlockPos var0 = new BlockPos(param0.getPosition());
        BlockPos var1 = param0.getLevel().findNearestMapFeature(param1, var0, 100, false);
        if (var1 == null) {
            throw ERROR_FAILED.create();
        } else {
            return showLocateResult(param0, param1, var0, var1, "commands.locate.success");
        }
    }

    public static int showLocateResult(CommandSourceStack param0, String param1, BlockPos param2, BlockPos param3, String param4) {
        int var0 = Mth.floor(dist(param2.getX(), param2.getZ(), param3.getX(), param3.getZ()));
        Component var1 = ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("chat.coordinates", param3.getX(), "~", param3.getZ()))
            .withStyle(
                param1x -> param1x.setColor(ChatFormatting.GREEN)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + param3.getX() + " ~ " + param3.getZ()))
                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.coordinates.tooltip")))
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
