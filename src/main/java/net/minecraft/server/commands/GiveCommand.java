package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class GiveCommand {
    public static final int MAX_ALLOWED_ITEMSTACKS = 100;

    public static void register(CommandDispatcher<CommandSourceStack> param0, CommandBuildContext param1) {
        param0.register(
            Commands.literal("give")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("targets", EntityArgument.players())
                        .then(
                            Commands.argument("item", ItemArgument.item(param1))
                                .executes(
                                    param0x -> giveItem(
                                            param0x.getSource(), ItemArgument.getItem(param0x, "item"), EntityArgument.getPlayers(param0x, "targets"), 1
                                        )
                                )
                                .then(
                                    Commands.argument("count", IntegerArgumentType.integer(1))
                                        .executes(
                                            param0x -> giveItem(
                                                    param0x.getSource(),
                                                    ItemArgument.getItem(param0x, "item"),
                                                    EntityArgument.getPlayers(param0x, "targets"),
                                                    IntegerArgumentType.getInteger(param0x, "count")
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int giveItem(CommandSourceStack param0, ItemInput param1, Collection<ServerPlayer> param2, int param3) throws CommandSyntaxException {
        int var0 = param1.getItem().getMaxStackSize();
        int var1 = var0 * 100;
        if (param3 > var1) {
            param0.sendFailure(Component.translatable("commands.give.failed.toomanyitems", var1, param1.createItemStack(param3, false).getDisplayName()));
            return 0;
        } else {
            for(ServerPlayer var2 : param2) {
                int var3 = param3;

                while(var3 > 0) {
                    int var4 = Math.min(var0, var3);
                    var3 -= var4;
                    ItemStack var5 = param1.createItemStack(var4, false);
                    boolean var6 = var2.getInventory().add(var5);
                    if (var6 && var5.isEmpty()) {
                        var5.setCount(1);
                        ItemEntity var8 = var2.drop(var5, false);
                        if (var8 != null) {
                            var8.makeFakeItem();
                        }

                        var2.level
                            .playSound(
                                null,
                                var2.getX(),
                                var2.getY(),
                                var2.getZ(),
                                SoundEvents.ITEM_PICKUP,
                                SoundSource.PLAYERS,
                                0.2F,
                                ((var2.getRandom().nextFloat() - var2.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
                            );
                        var2.containerMenu.broadcastChanges();
                    } else {
                        ItemEntity var7 = var2.drop(var5, false);
                        if (var7 != null) {
                            var7.setNoPickUpDelay();
                            var7.setTarget(var2.getUUID());
                        }
                    }
                }
            }

            if (param2.size() == 1) {
                param0.sendSuccess(
                    Component.translatable(
                        "commands.give.success.single",
                        param3,
                        param1.createItemStack(param3, false).getDisplayName(),
                        param2.iterator().next().getDisplayName()
                    ),
                    true
                );
            } else {
                param0.sendSuccess(
                    Component.translatable("commands.give.success.single", param3, param1.createItemStack(param3, false).getDisplayName(), param2.size()), true
                );
            }

            return param2.size();
        }
    }
}
