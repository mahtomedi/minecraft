package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ReplaceItemCommand {
    public static final SimpleCommandExceptionType ERROR_NOT_A_CONTAINER = new SimpleCommandExceptionType(
        new TranslatableComponent("commands.replaceitem.block.failed")
    );
    public static final DynamicCommandExceptionType ERROR_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.replaceitem.slot.inapplicable", param0)
    );
    public static final Dynamic2CommandExceptionType ERROR_ENTITY_SLOT = new Dynamic2CommandExceptionType(
        (param0, param1) -> new TranslatableComponent("commands.replaceitem.entity.failed", param0, param1)
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("replaceitem")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("block")
                        .then(
                            Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(
                                    Commands.argument("slot", SlotArgument.slot())
                                        .then(
                                            Commands.argument("item", ItemArgument.item())
                                                .executes(
                                                    param0x -> setBlockItem(
                                                            param0x.getSource(),
                                                            BlockPosArgument.getLoadedBlockPos(param0x, "pos"),
                                                            SlotArgument.getSlot(param0x, "slot"),
                                                            ItemArgument.getItem(param0x, "item").createItemStack(1, false)
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                        .executes(
                                                            param0x -> setBlockItem(
                                                                    param0x.getSource(),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "pos"),
                                                                    SlotArgument.getSlot(param0x, "slot"),
                                                                    ItemArgument.getItem(param0x, "item")
                                                                        .createItemStack(IntegerArgumentType.getInteger(param0x, "count"), true)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("entity")
                        .then(
                            Commands.argument("targets", EntityArgument.entities())
                                .then(
                                    Commands.argument("slot", SlotArgument.slot())
                                        .then(
                                            Commands.argument("item", ItemArgument.item())
                                                .executes(
                                                    param0x -> setEntityItem(
                                                            param0x.getSource(),
                                                            EntityArgument.getEntities(param0x, "targets"),
                                                            SlotArgument.getSlot(param0x, "slot"),
                                                            ItemArgument.getItem(param0x, "item").createItemStack(1, false)
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                        .executes(
                                                            param0x -> setEntityItem(
                                                                    param0x.getSource(),
                                                                    EntityArgument.getEntities(param0x, "targets"),
                                                                    SlotArgument.getSlot(param0x, "slot"),
                                                                    ItemArgument.getItem(param0x, "item")
                                                                        .createItemStack(IntegerArgumentType.getInteger(param0x, "count"), true)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int setBlockItem(CommandSourceStack param0, BlockPos param1, int param2, ItemStack param3) throws CommandSyntaxException {
        BlockEntity var0 = param0.getLevel().getBlockEntity(param1);
        if (!(var0 instanceof Container)) {
            throw ERROR_NOT_A_CONTAINER.create();
        } else {
            Container var1 = (Container)var0;
            if (param2 >= 0 && param2 < var1.getContainerSize()) {
                var1.setItem(param2, param3);
                param0.sendSuccess(
                    new TranslatableComponent("commands.replaceitem.block.success", param1.getX(), param1.getY(), param1.getZ(), param3.getDisplayName()), true
                );
                return 1;
            } else {
                throw ERROR_INAPPLICABLE_SLOT.create(param2);
            }
        }
    }

    private static int setEntityItem(CommandSourceStack param0, Collection<? extends Entity> param1, int param2, ItemStack param3) throws CommandSyntaxException {
        List<Entity> var0 = Lists.newArrayListWithCapacity(param1.size());

        for(Entity var1 : param1) {
            if (var1 instanceof ServerPlayer) {
                ((ServerPlayer)var1).inventoryMenu.broadcastChanges();
            }

            if (var1.setSlot(param2, param3.copy())) {
                var0.add(var1);
                if (var1 instanceof ServerPlayer) {
                    ((ServerPlayer)var1).inventoryMenu.broadcastChanges();
                }
            }
        }

        if (var0.isEmpty()) {
            throw ERROR_ENTITY_SLOT.create(param3.getDisplayName(), param2);
        } else {
            if (var0.size() == 1) {
                param0.sendSuccess(
                    new TranslatableComponent("commands.replaceitem.entity.success.single", var0.iterator().next().getDisplayName(), param3.getDisplayName()),
                    true
                );
            } else {
                param0.sendSuccess(new TranslatableComponent("commands.replaceitem.entity.success.multiple", var0.size(), param3.getDisplayName()), true);
            }

            return var0.size();
        }
    }
}
