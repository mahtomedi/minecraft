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
    public static final int MAX_ALLOWED_ITEMSTACKS = 100;

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
        int var0 = param1.getItem().getMaxStackSize();
        int var1 = var0 * 100;
        if (param3 > var1) {
            String var2 = param1.getItem().getDescriptionId();
            param0.sendFailure(new TranslatableComponent("commands.give.failed.toomanyitems", var1, var2));
            return 0;
        } else {
            for(ServerPlayer var3 : param2) {
                int var4 = param3;

                while(var4 > 0) {
                    int var5 = Math.min(var0, var4);
                    var4 -= var5;
                    ItemStack var6 = param1.createItemStack(var5, false);
                    boolean var7 = var3.getInventory().add(var6);
                    if (var7 && var6.isEmpty()) {
                        var6.setCount(1);
                        ItemEntity var9 = var3.drop(var6, false);
                        if (var9 != null) {
                            var9.makeFakeItem();
                        }

                        var3.level
                            .playSound(
                                null,
                                var3.getX(),
                                var3.getY(),
                                var3.getZ(),
                                SoundEvents.ITEM_PICKUP,
                                SoundSource.PLAYERS,
                                0.2F,
                                ((var3.getRandom().nextFloat() - var3.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
                            );
                        var3.containerMenu.broadcastChanges();
                    } else {
                        ItemEntity var8 = var3.drop(var6, false);
                        if (var8 != null) {
                            var8.setNoPickUpDelay();
                            var8.setOwner(var3.getUUID());
                        }
                    }
                }
            }

            if (param2.size() == 1) {
                param0.sendSuccess(
                    new TranslatableComponent(
                        "commands.give.success.single",
                        param3,
                        param1.createItemStack(param3, false).getDisplayName(),
                        param2.iterator().next().getDisplayName()
                    ),
                    true
                );
            } else {
                param0.sendSuccess(
                    new TranslatableComponent("commands.give.success.single", param3, param1.createItemStack(param3, false).getDisplayName(), param2.size()),
                    true
                );
            }

            return param2.size();
        }
    }
}
