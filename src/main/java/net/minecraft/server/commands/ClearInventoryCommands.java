package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ClearInventoryCommands {
    private static final DynamicCommandExceptionType ERROR_SINGLE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("clear.failed.single", param0)
    );
    private static final DynamicCommandExceptionType ERROR_MULTIPLE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("clear.failed.multiple", param0)
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0, CommandBuildContext param1) {
        param0.register(
            Commands.literal("clear")
                .requires(param0x -> param0x.hasPermission(2))
                .executes(
                    param0x -> clearInventory(param0x.getSource(), Collections.singleton(param0x.getSource().getPlayerOrException()), param0xx -> true, -1)
                )
                .then(
                    Commands.argument("targets", EntityArgument.players())
                        .executes(param0x -> clearInventory(param0x.getSource(), EntityArgument.getPlayers(param0x, "targets"), param0xx -> true, -1))
                        .then(
                            Commands.argument("item", ItemPredicateArgument.itemPredicate(param1))
                                .executes(
                                    param0x -> clearInventory(
                                            param0x.getSource(),
                                            EntityArgument.getPlayers(param0x, "targets"),
                                            ItemPredicateArgument.getItemPredicate(param0x, "item"),
                                            -1
                                        )
                                )
                                .then(
                                    Commands.argument("maxCount", IntegerArgumentType.integer(0))
                                        .executes(
                                            param0x -> clearInventory(
                                                    param0x.getSource(),
                                                    EntityArgument.getPlayers(param0x, "targets"),
                                                    ItemPredicateArgument.getItemPredicate(param0x, "item"),
                                                    IntegerArgumentType.getInteger(param0x, "maxCount")
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int clearInventory(CommandSourceStack param0, Collection<ServerPlayer> param1, Predicate<ItemStack> param2, int param3) throws CommandSyntaxException {
        int var0 = 0;

        for(ServerPlayer var1 : param1) {
            var0 += var1.getInventory().clearOrCountMatchingItems(param2, param3, var1.inventoryMenu.getCraftSlots());
            var1.containerMenu.broadcastChanges();
            var1.inventoryMenu.slotsChanged(var1.getInventory());
        }

        if (var0 == 0) {
            if (param1.size() == 1) {
                throw ERROR_SINGLE.create(param1.iterator().next().getName());
            } else {
                throw ERROR_MULTIPLE.create(param1.size());
            }
        } else {
            if (param3 == 0) {
                if (param1.size() == 1) {
                    param0.sendSuccess(new TranslatableComponent("commands.clear.test.single", var0, param1.iterator().next().getDisplayName()), true);
                } else {
                    param0.sendSuccess(new TranslatableComponent("commands.clear.test.multiple", var0, param1.size()), true);
                }
            } else if (param1.size() == 1) {
                param0.sendSuccess(new TranslatableComponent("commands.clear.success.single", var0, param1.iterator().next().getDisplayName()), true);
            } else {
                param0.sendSuccess(new TranslatableComponent("commands.clear.success.multiple", var0, param1.size()), true);
            }

            return var0;
        }
    }
}
