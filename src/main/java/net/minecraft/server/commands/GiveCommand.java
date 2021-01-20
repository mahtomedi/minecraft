package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class GiveCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("give")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("targets", EntityArgument.players())
                        .then(
                            Commands.argument("item", ItemArgument.item())
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
        for(ServerPlayer var0 : param2) {
            int var1 = param3;

            while(var1 > 0) {
                int var2 = Math.min(param1.getItem().getMaxStackSize(), var1);
                var1 -= var2;
                ItemStack var3 = param1.createItemStack(var2, false);
                boolean var4 = var0.getInventory().add(var3);
                if (var4 && var3.isEmpty()) {
                    var3.setCount(1);
                    ItemEntity var6 = var0.drop(var3, false);
                    if (var6 != null) {
                        var6.makeFakeItem();
                    }

                    var0.level
                        .playSound(
                            null,
                            var0.getX(),
                            var0.getY(),
                            var0.getZ(),
                            SoundEvents.ITEM_PICKUP,
                            SoundSource.PLAYERS,
                            0.2F,
                            ((var0.getRandom().nextFloat() - var0.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
                        );
                    var0.inventoryMenu.broadcastChanges();
                } else {
                    ItemEntity var5 = var0.drop(var3, false);
                    if (var5 != null) {
                        var5.setNoPickUpDelay();
                        var5.setOwner(var0.getUUID());
                    }
                }
            }
        }

        if (param2.size() == 1) {
            param0.sendSuccess(
                new TranslatableComponent(
                    "commands.give.success.single", param3, param1.createItemStack(param3, false).getDisplayName(), param2.iterator().next().getDisplayName()
                ),
                true
            );
        } else {
            param0.sendSuccess(
                new TranslatableComponent("commands.give.success.single", param3, param1.createItemStack(param3, false).getDisplayName(), param2.size()), true
            );
        }

        return param2.size();
    }
}
