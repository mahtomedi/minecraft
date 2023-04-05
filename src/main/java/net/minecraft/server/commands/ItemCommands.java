package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ItemCommands {
    static final Dynamic3CommandExceptionType ERROR_TARGET_NOT_A_CONTAINER = new Dynamic3CommandExceptionType(
        (param0, param1, param2) -> Component.translatable("commands.item.target.not_a_container", param0, param1, param2)
    );
    private static final Dynamic3CommandExceptionType ERROR_SOURCE_NOT_A_CONTAINER = new Dynamic3CommandExceptionType(
        (param0, param1, param2) -> Component.translatable("commands.item.source.not_a_container", param0, param1, param2)
    );
    static final DynamicCommandExceptionType ERROR_TARGET_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.item.target.no_such_slot", param0)
    );
    private static final DynamicCommandExceptionType ERROR_SOURCE_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.item.source.no_such_slot", param0)
    );
    private static final DynamicCommandExceptionType ERROR_TARGET_NO_CHANGES = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.item.target.no_changes", param0)
    );
    private static final Dynamic2CommandExceptionType ERROR_TARGET_NO_CHANGES_KNOWN_ITEM = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("commands.item.target.no_changed.known_item", param0, param1)
    );
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_MODIFIER = (param0, param1) -> {
        LootDataManager var0 = param0.getSource().getServer().getLootData();
        return SharedSuggestionProvider.suggestResource(var0.getKeys(LootDataType.MODIFIER), param1);
    };

    public static void register(CommandDispatcher<CommandSourceStack> param0, CommandBuildContext param1) {
        param0.register(
            Commands.literal("item")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("replace")
                        .then(
                            Commands.literal("block")
                                .then(
                                    Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(
                                            Commands.argument("slot", SlotArgument.slot())
                                                .then(
                                                    Commands.literal("with")
                                                        .then(
                                                            Commands.argument("item", ItemArgument.item(param1))
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
                                                .then(
                                                    Commands.literal("from")
                                                        .then(
                                                            Commands.literal("block")
                                                                .then(
                                                                    Commands.argument("source", BlockPosArgument.blockPos())
                                                                        .then(
                                                                            Commands.argument("sourceSlot", SlotArgument.slot())
                                                                                .executes(
                                                                                    param0x -> blockToBlock(
                                                                                            param0x.getSource(),
                                                                                            BlockPosArgument.getLoadedBlockPos(param0x, "source"),
                                                                                            SlotArgument.getSlot(param0x, "sourceSlot"),
                                                                                            BlockPosArgument.getLoadedBlockPos(param0x, "pos"),
                                                                                            SlotArgument.getSlot(param0x, "slot")
                                                                                        )
                                                                                )
                                                                                .then(
                                                                                    Commands.argument("modifier", ResourceLocationArgument.id())
                                                                                        .suggests(SUGGEST_MODIFIER)
                                                                                        .executes(
                                                                                            param0x -> blockToBlock(
                                                                                                    param0x.getSource(),
                                                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "source"),
                                                                                                    SlotArgument.getSlot(param0x, "sourceSlot"),
                                                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "pos"),
                                                                                                    SlotArgument.getSlot(param0x, "slot"),
                                                                                                    ResourceLocationArgument.getItemModifier(
                                                                                                        param0x, "modifier"
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
                                                                    Commands.argument("source", EntityArgument.entity())
                                                                        .then(
                                                                            Commands.argument("sourceSlot", SlotArgument.slot())
                                                                                .executes(
                                                                                    param0x -> entityToBlock(
                                                                                            param0x.getSource(),
                                                                                            EntityArgument.getEntity(param0x, "source"),
                                                                                            SlotArgument.getSlot(param0x, "sourceSlot"),
                                                                                            BlockPosArgument.getLoadedBlockPos(param0x, "pos"),
                                                                                            SlotArgument.getSlot(param0x, "slot")
                                                                                        )
                                                                                )
                                                                                .then(
                                                                                    Commands.argument("modifier", ResourceLocationArgument.id())
                                                                                        .suggests(SUGGEST_MODIFIER)
                                                                                        .executes(
                                                                                            param0x -> entityToBlock(
                                                                                                    param0x.getSource(),
                                                                                                    EntityArgument.getEntity(param0x, "source"),
                                                                                                    SlotArgument.getSlot(param0x, "sourceSlot"),
                                                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "pos"),
                                                                                                    SlotArgument.getSlot(param0x, "slot"),
                                                                                                    ResourceLocationArgument.getItemModifier(
                                                                                                        param0x, "modifier"
                                                                                                    )
                                                                                                )
                                                                                        )
                                                                                )
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
                                                    Commands.literal("with")
                                                        .then(
                                                            Commands.argument("item", ItemArgument.item(param1))
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
                                                .then(
                                                    Commands.literal("from")
                                                        .then(
                                                            Commands.literal("block")
                                                                .then(
                                                                    Commands.argument("source", BlockPosArgument.blockPos())
                                                                        .then(
                                                                            Commands.argument("sourceSlot", SlotArgument.slot())
                                                                                .executes(
                                                                                    param0x -> blockToEntities(
                                                                                            param0x.getSource(),
                                                                                            BlockPosArgument.getLoadedBlockPos(param0x, "source"),
                                                                                            SlotArgument.getSlot(param0x, "sourceSlot"),
                                                                                            EntityArgument.getEntities(param0x, "targets"),
                                                                                            SlotArgument.getSlot(param0x, "slot")
                                                                                        )
                                                                                )
                                                                                .then(
                                                                                    Commands.argument("modifier", ResourceLocationArgument.id())
                                                                                        .suggests(SUGGEST_MODIFIER)
                                                                                        .executes(
                                                                                            param0x -> blockToEntities(
                                                                                                    param0x.getSource(),
                                                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "source"),
                                                                                                    SlotArgument.getSlot(param0x, "sourceSlot"),
                                                                                                    EntityArgument.getEntities(param0x, "targets"),
                                                                                                    SlotArgument.getSlot(param0x, "slot"),
                                                                                                    ResourceLocationArgument.getItemModifier(
                                                                                                        param0x, "modifier"
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
                                                                    Commands.argument("source", EntityArgument.entity())
                                                                        .then(
                                                                            Commands.argument("sourceSlot", SlotArgument.slot())
                                                                                .executes(
                                                                                    param0x -> entityToEntities(
                                                                                            param0x.getSource(),
                                                                                            EntityArgument.getEntity(param0x, "source"),
                                                                                            SlotArgument.getSlot(param0x, "sourceSlot"),
                                                                                            EntityArgument.getEntities(param0x, "targets"),
                                                                                            SlotArgument.getSlot(param0x, "slot")
                                                                                        )
                                                                                )
                                                                                .then(
                                                                                    Commands.argument("modifier", ResourceLocationArgument.id())
                                                                                        .suggests(SUGGEST_MODIFIER)
                                                                                        .executes(
                                                                                            param0x -> entityToEntities(
                                                                                                    param0x.getSource(),
                                                                                                    EntityArgument.getEntity(param0x, "source"),
                                                                                                    SlotArgument.getSlot(param0x, "sourceSlot"),
                                                                                                    EntityArgument.getEntities(param0x, "targets"),
                                                                                                    SlotArgument.getSlot(param0x, "slot"),
                                                                                                    ResourceLocationArgument.getItemModifier(
                                                                                                        param0x, "modifier"
                                                                                                    )
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("modify")
                        .then(
                            Commands.literal("block")
                                .then(
                                    Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(
                                            Commands.argument("slot", SlotArgument.slot())
                                                .then(
                                                    Commands.argument("modifier", ResourceLocationArgument.id())
                                                        .suggests(SUGGEST_MODIFIER)
                                                        .executes(
                                                            param0x -> modifyBlockItem(
                                                                    param0x.getSource(),
                                                                    BlockPosArgument.getLoadedBlockPos(param0x, "pos"),
                                                                    SlotArgument.getSlot(param0x, "slot"),
                                                                    ResourceLocationArgument.getItemModifier(param0x, "modifier")
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
                                                    Commands.argument("modifier", ResourceLocationArgument.id())
                                                        .suggests(SUGGEST_MODIFIER)
                                                        .executes(
                                                            param0x -> modifyEntityItem(
                                                                    param0x.getSource(),
                                                                    EntityArgument.getEntities(param0x, "targets"),
                                                                    SlotArgument.getSlot(param0x, "slot"),
                                                                    ResourceLocationArgument.getItemModifier(param0x, "modifier")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int modifyBlockItem(CommandSourceStack param0, BlockPos param1, int param2, LootItemFunction param3) throws CommandSyntaxException {
        Container var0 = getContainer(param0, param1, ERROR_TARGET_NOT_A_CONTAINER);
        if (param2 >= 0 && param2 < var0.getContainerSize()) {
            ItemStack var1 = applyModifier(param0, param3, var0.getItem(param2));
            var0.setItem(param2, var1);
            param0.sendSuccess(
                Component.translatable("commands.item.block.set.success", param1.getX(), param1.getY(), param1.getZ(), var1.getDisplayName()), true
            );
            return 1;
        } else {
            throw ERROR_TARGET_INAPPLICABLE_SLOT.create(param2);
        }
    }

    private static int modifyEntityItem(CommandSourceStack param0, Collection<? extends Entity> param1, int param2, LootItemFunction param3) throws CommandSyntaxException {
        Map<Entity, ItemStack> var0 = Maps.newHashMapWithExpectedSize(param1.size());

        for(Entity var1 : param1) {
            SlotAccess var2 = var1.getSlot(param2);
            if (var2 != SlotAccess.NULL) {
                ItemStack var3 = applyModifier(param0, param3, var2.get().copy());
                if (var2.set(var3)) {
                    var0.put(var1, var3);
                    if (var1 instanceof ServerPlayer) {
                        ((ServerPlayer)var1).containerMenu.broadcastChanges();
                    }
                }
            }
        }

        if (var0.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES.create(param2);
        } else {
            if (var0.size() == 1) {
                Entry<Entity, ItemStack> var4 = var0.entrySet().iterator().next();
                param0.sendSuccess(
                    Component.translatable("commands.item.entity.set.success.single", var4.getKey().getDisplayName(), var4.getValue().getDisplayName()), true
                );
            } else {
                param0.sendSuccess(Component.translatable("commands.item.entity.set.success.multiple", var0.size()), true);
            }

            return var0.size();
        }
    }

    private static int setBlockItem(CommandSourceStack param0, BlockPos param1, int param2, ItemStack param3) throws CommandSyntaxException {
        Container var0 = getContainer(param0, param1, ERROR_TARGET_NOT_A_CONTAINER);
        if (param2 >= 0 && param2 < var0.getContainerSize()) {
            var0.setItem(param2, param3);
            param0.sendSuccess(
                Component.translatable("commands.item.block.set.success", param1.getX(), param1.getY(), param1.getZ(), param3.getDisplayName()), true
            );
            return 1;
        } else {
            throw ERROR_TARGET_INAPPLICABLE_SLOT.create(param2);
        }
    }

    private static Container getContainer(CommandSourceStack param0, BlockPos param1, Dynamic3CommandExceptionType param2) throws CommandSyntaxException {
        BlockEntity var0 = param0.getLevel().getBlockEntity(param1);
        if (!(var0 instanceof Container)) {
            throw param2.create(param1.getX(), param1.getY(), param1.getZ());
        } else {
            return (Container)var0;
        }
    }

    private static int setEntityItem(CommandSourceStack param0, Collection<? extends Entity> param1, int param2, ItemStack param3) throws CommandSyntaxException {
        List<Entity> var0 = Lists.newArrayListWithCapacity(param1.size());

        for(Entity var1 : param1) {
            SlotAccess var2 = var1.getSlot(param2);
            if (var2 != SlotAccess.NULL && var2.set(param3.copy())) {
                var0.add(var1);
                if (var1 instanceof ServerPlayer) {
                    ((ServerPlayer)var1).containerMenu.broadcastChanges();
                }
            }
        }

        if (var0.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(param3.getDisplayName(), param2);
        } else {
            if (var0.size() == 1) {
                param0.sendSuccess(
                    Component.translatable("commands.item.entity.set.success.single", var0.iterator().next().getDisplayName(), param3.getDisplayName()), true
                );
            } else {
                param0.sendSuccess(Component.translatable("commands.item.entity.set.success.multiple", var0.size(), param3.getDisplayName()), true);
            }

            return var0.size();
        }
    }

    private static int blockToEntities(CommandSourceStack param0, BlockPos param1, int param2, Collection<? extends Entity> param3, int param4) throws CommandSyntaxException {
        return setEntityItem(param0, param3, param4, getBlockItem(param0, param1, param2));
    }

    private static int blockToEntities(
        CommandSourceStack param0, BlockPos param1, int param2, Collection<? extends Entity> param3, int param4, LootItemFunction param5
    ) throws CommandSyntaxException {
        return setEntityItem(param0, param3, param4, applyModifier(param0, param5, getBlockItem(param0, param1, param2)));
    }

    private static int blockToBlock(CommandSourceStack param0, BlockPos param1, int param2, BlockPos param3, int param4) throws CommandSyntaxException {
        return setBlockItem(param0, param3, param4, getBlockItem(param0, param1, param2));
    }

    private static int blockToBlock(CommandSourceStack param0, BlockPos param1, int param2, BlockPos param3, int param4, LootItemFunction param5) throws CommandSyntaxException {
        return setBlockItem(param0, param3, param4, applyModifier(param0, param5, getBlockItem(param0, param1, param2)));
    }

    private static int entityToBlock(CommandSourceStack param0, Entity param1, int param2, BlockPos param3, int param4) throws CommandSyntaxException {
        return setBlockItem(param0, param3, param4, getEntityItem(param1, param2));
    }

    private static int entityToBlock(CommandSourceStack param0, Entity param1, int param2, BlockPos param3, int param4, LootItemFunction param5) throws CommandSyntaxException {
        return setBlockItem(param0, param3, param4, applyModifier(param0, param5, getEntityItem(param1, param2)));
    }

    private static int entityToEntities(CommandSourceStack param0, Entity param1, int param2, Collection<? extends Entity> param3, int param4) throws CommandSyntaxException {
        return setEntityItem(param0, param3, param4, getEntityItem(param1, param2));
    }

    private static int entityToEntities(
        CommandSourceStack param0, Entity param1, int param2, Collection<? extends Entity> param3, int param4, LootItemFunction param5
    ) throws CommandSyntaxException {
        return setEntityItem(param0, param3, param4, applyModifier(param0, param5, getEntityItem(param1, param2)));
    }

    private static ItemStack applyModifier(CommandSourceStack param0, LootItemFunction param1, ItemStack param2) {
        ServerLevel var0 = param0.getLevel();
        LootContext var1 = new LootContext.Builder(var0)
            .withParameter(LootContextParams.ORIGIN, param0.getPosition())
            .withOptionalParameter(LootContextParams.THIS_ENTITY, param0.getEntity())
            .create(LootContextParamSets.COMMAND);
        var1.pushVisitedElement(LootContext.createVisitedEntry(param1));
        return param1.apply(param2, var1);
    }

    private static ItemStack getEntityItem(Entity param0, int param1) throws CommandSyntaxException {
        SlotAccess var0 = param0.getSlot(param1);
        if (var0 == SlotAccess.NULL) {
            throw ERROR_SOURCE_INAPPLICABLE_SLOT.create(param1);
        } else {
            return var0.get().copy();
        }
    }

    private static ItemStack getBlockItem(CommandSourceStack param0, BlockPos param1, int param2) throws CommandSyntaxException {
        Container var0 = getContainer(param0, param1, ERROR_SOURCE_NOT_A_CONTAINER);
        if (param2 >= 0 && param2 < var0.getContainerSize()) {
            return var0.getItem(param2).copy();
        } else {
            throw ERROR_SOURCE_INAPPLICABLE_SLOT.create(param2);
        }
    }
}
