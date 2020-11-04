package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class LootCommand {
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_LOOT_TABLE = (param0, param1) -> {
        LootTables var0 = param0.getSource().getServer().getLootTables();
        return SharedSuggestionProvider.suggestResource(var0.getIds(), param1);
    };
    private static final DynamicCommandExceptionType ERROR_NO_HELD_ITEMS = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.drop.no_held_items", param0)
    );
    private static final DynamicCommandExceptionType ERROR_NO_LOOT_TABLE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.drop.no_loot_table", param0)
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            addTargets(
                Commands.literal("loot").requires(param0x -> param0x.hasPermission(2)),
                (param0x, param1) -> param0x.then(
                            Commands.literal("fish")
                                .then(
                                    Commands.argument("loot_table", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_LOOT_TABLE)
                                        .then(
                                            Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(
                                                    param1x -> dropFishingLoot(
                                                            param1x,
                                                            ResourceLocationArgument.getId(param1x, "loot_table"),
                                                            BlockPosArgument.getLoadedBlockPos(param1x, "pos"),
                                                            ItemStack.EMPTY,
                                                            param1
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("tool", ItemArgument.item())
                                                        .executes(
                                                            param1x -> dropFishingLoot(
                                                                    param1x,
                                                                    ResourceLocationArgument.getId(param1x, "loot_table"),
                                                                    BlockPosArgument.getLoadedBlockPos(param1x, "pos"),
                                                                    ItemArgument.getItem(param1x, "tool").createItemStack(1, false),
                                                                    param1
                                                                )
                                                        )
                                                )
                                                .then(
                                                    Commands.literal("mainhand")
                                                        .executes(
                                                            param1x -> dropFishingLoot(
                                                                    param1x,
                                                                    ResourceLocationArgument.getId(param1x, "loot_table"),
                                                                    BlockPosArgument.getLoadedBlockPos(param1x, "pos"),
                                                                    getSourceHandItem(param1x.getSource(), EquipmentSlot.MAINHAND),
                                                                    param1
                                                                )
                                                        )
                                                )
                                                .then(
                                                    Commands.literal("offhand")
                                                        .executes(
                                                            param1x -> dropFishingLoot(
                                                                    param1x,
                                                                    ResourceLocationArgument.getId(param1x, "loot_table"),
                                                                    BlockPosArgument.getLoadedBlockPos(param1x, "pos"),
                                                                    getSourceHandItem(param1x.getSource(), EquipmentSlot.OFFHAND),
                                                                    param1
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("loot")
                                .then(
                                    Commands.argument("loot_table", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_LOOT_TABLE)
                                        .executes(param1x -> dropChestLoot(param1x, ResourceLocationArgument.getId(param1x, "loot_table"), param1))
                                )
                        )
                        .then(
                            Commands.literal("kill")
                                .then(
                                    Commands.argument("target", EntityArgument.entity())
                                        .executes(param1x -> dropKillLoot(param1x, EntityArgument.getEntity(param1x, "target"), param1))
                                )
                        )
                        .then(
                            Commands.literal("mine")
                                .then(
                                    Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(
                                            param1x -> dropBlockLoot(param1x, BlockPosArgument.getLoadedBlockPos(param1x, "pos"), ItemStack.EMPTY, param1)
                                        )
                                        .then(
                                            Commands.argument("tool", ItemArgument.item())
                                                .executes(
                                                    param1x -> dropBlockLoot(
                                                            param1x,
                                                            BlockPosArgument.getLoadedBlockPos(param1x, "pos"),
                                                            ItemArgument.getItem(param1x, "tool").createItemStack(1, false),
                                                            param1
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("mainhand")
                                                .executes(
                                                    param1x -> dropBlockLoot(
                                                            param1x,
                                                            BlockPosArgument.getLoadedBlockPos(param1x, "pos"),
                                                            getSourceHandItem(param1x.getSource(), EquipmentSlot.MAINHAND),
                                                            param1
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("offhand")
                                                .executes(
                                                    param1x -> dropBlockLoot(
                                                            param1x,
                                                            BlockPosArgument.getLoadedBlockPos(param1x, "pos"),
                                                            getSourceHandItem(param1x.getSource(), EquipmentSlot.OFFHAND),
                                                            param1
                                                        )
                                                )
                                        )
                                )
                        )
            )
        );
    }

    private static <T extends ArgumentBuilder<CommandSourceStack, T>> T addTargets(T param0, LootCommand.TailProvider param1) {
        return param0.then(
                Commands.literal("replace")
                    .then(
                        Commands.literal("entity")
                            .then(
                                Commands.argument("entities", EntityArgument.entities())
                                    .then(
                                        param1.construct(
                                                Commands.argument("slot", SlotArgument.slot()),
                                                (param0x, param1x, param2) -> entityReplace(
                                                        EntityArgument.getEntities(param0x, "entities"),
                                                        SlotArgument.getSlot(param0x, "slot"),
                                                        param1x.size(),
                                                        param1x,
                                                        param2
                                                    )
                                            )
                                            .then(
                                                param1.construct(
                                                    Commands.argument("count", IntegerArgumentType.integer(0)),
                                                    (param0x, param1x, param2) -> entityReplace(
                                                            EntityArgument.getEntities(param0x, "entities"),
                                                            SlotArgument.getSlot(param0x, "slot"),
                                                            IntegerArgumentType.getInteger(param0x, "count"),
                                                            param1x,
                                                            param2
                                                        )
                                                )
                                            )
                                    )
                            )
                    )
                    .then(
                        Commands.literal("block")
                            .then(
                                Commands.argument("targetPos", BlockPosArgument.blockPos())
                                    .then(
                                        param1.construct(
                                                Commands.argument("slot", SlotArgument.slot()),
                                                (param0x, param1x, param2) -> blockReplace(
                                                        param0x.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(param0x, "targetPos"),
                                                        SlotArgument.getSlot(param0x, "slot"),
                                                        param1x.size(),
                                                        param1x,
                                                        param2
                                                    )
                                            )
                                            .then(
                                                param1.construct(
                                                    Commands.argument("count", IntegerArgumentType.integer(0)),
                                                    (param0x, param1x, param2) -> blockReplace(
                                                            param0x.getSource(),
                                                            BlockPosArgument.getLoadedBlockPos(param0x, "targetPos"),
                                                            IntegerArgumentType.getInteger(param0x, "slot"),
                                                            IntegerArgumentType.getInteger(param0x, "count"),
                                                            param1x,
                                                            param2
                                                        )
                                                )
                                            )
                                    )
                            )
                    )
            )
            .then(
                Commands.literal("insert")
                    .then(
                        param1.construct(
                            Commands.argument("targetPos", BlockPosArgument.blockPos()),
                            (param0x, param1x, param2) -> blockDistribute(
                                    param0x.getSource(), BlockPosArgument.getLoadedBlockPos(param0x, "targetPos"), param1x, param2
                                )
                        )
                    )
            )
            .then(
                Commands.literal("give")
                    .then(
                        param1.construct(
                            Commands.argument("players", EntityArgument.players()),
                            (param0x, param1x, param2) -> playerGive(EntityArgument.getPlayers(param0x, "players"), param1x, param2)
                        )
                    )
            )
            .then(
                Commands.literal("spawn")
                    .then(
                        param1.construct(
                            Commands.argument("targetPos", Vec3Argument.vec3()),
                            (param0x, param1x, param2) -> dropInWorld(param0x.getSource(), Vec3Argument.getVec3(param0x, "targetPos"), param1x, param2)
                        )
                    )
            );
    }

    private static Container getContainer(CommandSourceStack param0, BlockPos param1) throws CommandSyntaxException {
        BlockEntity var0 = param0.getLevel().getBlockEntity(param1);
        if (!(var0 instanceof Container)) {
            throw ReplaceItemCommand.ERROR_NOT_A_CONTAINER.create();
        } else {
            return (Container)var0;
        }
    }

    private static int blockDistribute(CommandSourceStack param0, BlockPos param1, List<ItemStack> param2, LootCommand.Callback param3) throws CommandSyntaxException {
        Container var0 = getContainer(param0, param1);
        List<ItemStack> var1 = Lists.newArrayListWithCapacity(param2.size());

        for(ItemStack var2 : param2) {
            if (distributeToContainer(var0, var2.copy())) {
                var0.setChanged();
                var1.add(var2);
            }
        }

        param3.accept(var1);
        return var1.size();
    }

    private static boolean distributeToContainer(Container param0, ItemStack param1) {
        boolean var0 = false;

        for(int var1 = 0; var1 < param0.getContainerSize() && !param1.isEmpty(); ++var1) {
            ItemStack var2 = param0.getItem(var1);
            if (param0.canPlaceItem(var1, param1)) {
                if (var2.isEmpty()) {
                    param0.setItem(var1, param1);
                    var0 = true;
                    break;
                }

                if (canMergeItems(var2, param1)) {
                    int var3 = param1.getMaxStackSize() - var2.getCount();
                    int var4 = Math.min(param1.getCount(), var3);
                    param1.shrink(var4);
                    var2.grow(var4);
                    var0 = true;
                }
            }
        }

        return var0;
    }

    private static int blockReplace(CommandSourceStack param0, BlockPos param1, int param2, int param3, List<ItemStack> param4, LootCommand.Callback param5) throws CommandSyntaxException {
        Container var0 = getContainer(param0, param1);
        int var1 = var0.getContainerSize();
        if (param2 >= 0 && param2 < var1) {
            List<ItemStack> var2 = Lists.newArrayListWithCapacity(param4.size());

            for(int var3 = 0; var3 < param3; ++var3) {
                int var4 = param2 + var3;
                ItemStack var5 = var3 < param4.size() ? param4.get(var3) : ItemStack.EMPTY;
                if (var0.canPlaceItem(var4, var5)) {
                    var0.setItem(var4, var5);
                    var2.add(var5);
                }
            }

            param5.accept(var2);
            return var2.size();
        } else {
            throw ReplaceItemCommand.ERROR_INAPPLICABLE_SLOT.create(param2);
        }
    }

    private static boolean canMergeItems(ItemStack param0, ItemStack param1) {
        return param0.is(param1.getItem())
            && param0.getDamageValue() == param1.getDamageValue()
            && param0.getCount() <= param0.getMaxStackSize()
            && Objects.equals(param0.getTag(), param1.getTag());
    }

    private static int playerGive(Collection<ServerPlayer> param0, List<ItemStack> param1, LootCommand.Callback param2) throws CommandSyntaxException {
        List<ItemStack> var0 = Lists.newArrayListWithCapacity(param1.size());

        for(ItemStack var1 : param1) {
            for(ServerPlayer var2 : param0) {
                if (var2.getInventory().add(var1.copy())) {
                    var0.add(var1);
                }
            }
        }

        param2.accept(var0);
        return var0.size();
    }

    private static void setSlots(Entity param0, List<ItemStack> param1, int param2, int param3, List<ItemStack> param4) {
        for(int var0 = 0; var0 < param3; ++var0) {
            ItemStack var1 = var0 < param1.size() ? param1.get(var0) : ItemStack.EMPTY;
            if (param0.setSlot(param2 + var0, var1.copy())) {
                param4.add(var1);
            }
        }

    }

    private static int entityReplace(Collection<? extends Entity> param0, int param1, int param2, List<ItemStack> param3, LootCommand.Callback param4) throws CommandSyntaxException {
        List<ItemStack> var0 = Lists.newArrayListWithCapacity(param3.size());

        for(Entity var1 : param0) {
            if (var1 instanceof ServerPlayer) {
                ServerPlayer var2 = (ServerPlayer)var1;
                var2.inventoryMenu.broadcastChanges();
                setSlots(var1, param3, param1, param2, var0);
                var2.inventoryMenu.broadcastChanges();
            } else {
                setSlots(var1, param3, param1, param2, var0);
            }
        }

        param4.accept(var0);
        return var0.size();
    }

    private static int dropInWorld(CommandSourceStack param0, Vec3 param1, List<ItemStack> param2, LootCommand.Callback param3) throws CommandSyntaxException {
        ServerLevel var0 = param0.getLevel();
        param2.forEach(param2x -> {
            ItemEntity var0x = new ItemEntity(var0, param1.x, param1.y, param1.z, param2x.copy());
            var0x.setDefaultPickUpDelay();
            var0.addFreshEntity(var0x);
        });
        param3.accept(param2);
        return param2.size();
    }

    private static void callback(CommandSourceStack param0, List<ItemStack> param1) {
        if (param1.size() == 1) {
            ItemStack var0 = param1.get(0);
            param0.sendSuccess(new TranslatableComponent("commands.drop.success.single", var0.getCount(), var0.getDisplayName()), false);
        } else {
            param0.sendSuccess(new TranslatableComponent("commands.drop.success.multiple", param1.size()), false);
        }

    }

    private static void callback(CommandSourceStack param0, List<ItemStack> param1, ResourceLocation param2) {
        if (param1.size() == 1) {
            ItemStack var0 = param1.get(0);
            param0.sendSuccess(new TranslatableComponent("commands.drop.success.single_with_table", var0.getCount(), var0.getDisplayName(), param2), false);
        } else {
            param0.sendSuccess(new TranslatableComponent("commands.drop.success.multiple_with_table", param1.size(), param2), false);
        }

    }

    private static ItemStack getSourceHandItem(CommandSourceStack param0, EquipmentSlot param1) throws CommandSyntaxException {
        Entity var0 = param0.getEntityOrException();
        if (var0 instanceof LivingEntity) {
            return ((LivingEntity)var0).getItemBySlot(param1);
        } else {
            throw ERROR_NO_HELD_ITEMS.create(var0.getDisplayName());
        }
    }

    private static int dropBlockLoot(CommandContext<CommandSourceStack> param0, BlockPos param1, ItemStack param2, LootCommand.DropConsumer param3) throws CommandSyntaxException {
        CommandSourceStack var0 = param0.getSource();
        ServerLevel var1 = var0.getLevel();
        BlockState var2 = var1.getBlockState(param1);
        BlockEntity var3 = var1.getBlockEntity(param1);
        LootContext.Builder var4 = new LootContext.Builder(var1)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(param1))
            .withParameter(LootContextParams.BLOCK_STATE, var2)
            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, var3)
            .withOptionalParameter(LootContextParams.THIS_ENTITY, var0.getEntity())
            .withParameter(LootContextParams.TOOL, param2);
        List<ItemStack> var5 = var2.getDrops(var4);
        return param3.accept(param0, var5, param2x -> callback(var0, param2x, var2.getBlock().getLootTable()));
    }

    private static int dropKillLoot(CommandContext<CommandSourceStack> param0, Entity param1, LootCommand.DropConsumer param2) throws CommandSyntaxException {
        if (!(param1 instanceof LivingEntity)) {
            throw ERROR_NO_LOOT_TABLE.create(param1.getDisplayName());
        } else {
            ResourceLocation var0 = ((LivingEntity)param1).getLootTable();
            CommandSourceStack var1 = param0.getSource();
            LootContext.Builder var2 = new LootContext.Builder(var1.getLevel());
            Entity var3 = var1.getEntity();
            if (var3 instanceof Player) {
                var2.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, (Player)var3);
            }

            var2.withParameter(LootContextParams.DAMAGE_SOURCE, DamageSource.MAGIC);
            var2.withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, var3);
            var2.withOptionalParameter(LootContextParams.KILLER_ENTITY, var3);
            var2.withParameter(LootContextParams.THIS_ENTITY, param1);
            var2.withParameter(LootContextParams.ORIGIN, var1.getPosition());
            LootTable var4 = var1.getServer().getLootTables().get(var0);
            List<ItemStack> var5 = var4.getRandomItems(var2.create(LootContextParamSets.ENTITY));
            return param2.accept(param0, var5, param2x -> callback(var1, param2x, var0));
        }
    }

    private static int dropChestLoot(CommandContext<CommandSourceStack> param0, ResourceLocation param1, LootCommand.DropConsumer param2) throws CommandSyntaxException {
        CommandSourceStack var0 = param0.getSource();
        LootContext.Builder var1 = new LootContext.Builder(var0.getLevel())
            .withOptionalParameter(LootContextParams.THIS_ENTITY, var0.getEntity())
            .withParameter(LootContextParams.ORIGIN, var0.getPosition());
        return drop(param0, param1, var1.create(LootContextParamSets.CHEST), param2);
    }

    private static int dropFishingLoot(
        CommandContext<CommandSourceStack> param0, ResourceLocation param1, BlockPos param2, ItemStack param3, LootCommand.DropConsumer param4
    ) throws CommandSyntaxException {
        CommandSourceStack var0 = param0.getSource();
        LootContext var1 = new LootContext.Builder(var0.getLevel())
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(param2))
            .withParameter(LootContextParams.TOOL, param3)
            .withOptionalParameter(LootContextParams.THIS_ENTITY, var0.getEntity())
            .create(LootContextParamSets.FISHING);
        return drop(param0, param1, var1, param4);
    }

    private static int drop(CommandContext<CommandSourceStack> param0, ResourceLocation param1, LootContext param2, LootCommand.DropConsumer param3) throws CommandSyntaxException {
        CommandSourceStack var0 = param0.getSource();
        LootTable var1 = var0.getServer().getLootTables().get(param1);
        List<ItemStack> var2 = var1.getRandomItems(param2);
        return param3.accept(param0, var2, param1x -> callback(var0, param1x));
    }

    @FunctionalInterface
    interface Callback {
        void accept(List<ItemStack> var1) throws CommandSyntaxException;
    }

    @FunctionalInterface
    interface DropConsumer {
        int accept(CommandContext<CommandSourceStack> var1, List<ItemStack> var2, LootCommand.Callback var3) throws CommandSyntaxException;
    }

    @FunctionalInterface
    interface TailProvider {
        ArgumentBuilder<CommandSourceStack, ?> construct(ArgumentBuilder<CommandSourceStack, ?> var1, LootCommand.DropConsumer var2);
    }
}
