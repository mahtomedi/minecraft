package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

public class DataPackCommand {
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_PACK = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.datapack.unknown", param0)
    );
    private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_ENABLED = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.datapack.enable.failed", param0)
    );
    private static final DynamicCommandExceptionType ERROR_PACK_ALREADY_DISABLED = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("commands.datapack.disable.failed", param0)
    );
    private static final SuggestionProvider<CommandSourceStack> SELECTED_PACKS = (param0, param1) -> SharedSuggestionProvider.suggest(
            param0.getSource().getServer().getPackRepository().getSelectedIds().stream().map(StringArgumentType::escapeIfRequired), param1
        );
    private static final SuggestionProvider<CommandSourceStack> UNSELECTED_PACKS = (param0, param1) -> {
        PackRepository<?> var0 = param0.getSource().getServer().getPackRepository();
        Collection<String> var1 = var0.getSelectedIds();
        return SharedSuggestionProvider.suggest(
            var0.getAvailableIds().stream().filter(param1x -> !var1.contains(param1x)).map(StringArgumentType::escapeIfRequired), param1
        );
    };

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("datapack")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.literal("enable")
                        .then(
                            Commands.argument("name", StringArgumentType.string())
                                .suggests(UNSELECTED_PACKS)
                                .executes(
                                    param0x -> enablePack(
                                            param0x.getSource(),
                                            getPack(param0x, "name", true),
                                            (param0xx, param1) -> param1.getDefaultPosition().insert(param0xx, param1, param0xxx -> param0xxx, false)
                                        )
                                )
                                .then(
                                    Commands.literal("after")
                                        .then(
                                            Commands.argument("existing", StringArgumentType.string())
                                                .suggests(SELECTED_PACKS)
                                                .executes(
                                                    param0x -> enablePack(
                                                            param0x.getSource(),
                                                            getPack(param0x, "name", true),
                                                            (param1, param2) -> param1.add(param1.indexOf(getPack(param0x, "existing", false)) + 1, param2)
                                                        )
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("before")
                                        .then(
                                            Commands.argument("existing", StringArgumentType.string())
                                                .suggests(SELECTED_PACKS)
                                                .executes(
                                                    param0x -> enablePack(
                                                            param0x.getSource(),
                                                            getPack(param0x, "name", true),
                                                            (param1, param2) -> param1.add(param1.indexOf(getPack(param0x, "existing", false)), param2)
                                                        )
                                                )
                                        )
                                )
                                .then(Commands.literal("last").executes(param0x -> enablePack(param0x.getSource(), getPack(param0x, "name", true), List::add)))
                                .then(
                                    Commands.literal("first")
                                        .executes(
                                            param0x -> enablePack(
                                                    param0x.getSource(), getPack(param0x, "name", true), (param0xx, param1) -> param0xx.add(0, param1)
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("disable")
                        .then(
                            Commands.argument("name", StringArgumentType.string())
                                .suggests(SELECTED_PACKS)
                                .executes(param0x -> disablePack(param0x.getSource(), getPack(param0x, "name", false)))
                        )
                )
                .then(
                    Commands.literal("list")
                        .executes(param0x -> listPacks(param0x.getSource()))
                        .then(Commands.literal("available").executes(param0x -> listAvailablePacks(param0x.getSource())))
                        .then(Commands.literal("enabled").executes(param0x -> listEnabledPacks(param0x.getSource())))
                )
        );
    }

    private static int enablePack(CommandSourceStack param0, Pack param1, DataPackCommand.Inserter param2) throws CommandSyntaxException {
        PackRepository<?> var0 = param0.getServer().getPackRepository();
        List<Pack> var1 = Lists.newArrayList(var0.getSelectedPacks());
        param2.apply(var1, param1);
        param0.sendSuccess(new TranslatableComponent("commands.datapack.enable.success", param1.getChatLink(true)), true);
        ReloadCommand.reloadPacks(var1.stream().map(Pack::getId).collect(Collectors.toList()), param0);
        return var1.size();
    }

    private static int disablePack(CommandSourceStack param0, Pack param1) {
        PackRepository<?> var0 = param0.getServer().getPackRepository();
        List<Pack> var1 = Lists.newArrayList(var0.getSelectedPacks());
        var1.remove(param1);
        ReloadCommand.reloadPacks(var1.stream().map(Pack::getId).collect(Collectors.toList()), param0);
        param0.sendSuccess(new TranslatableComponent("commands.datapack.disable.success", param1.getChatLink(true)), true);
        return var1.size();
    }

    private static int listPacks(CommandSourceStack param0) {
        return listEnabledPacks(param0) + listAvailablePacks(param0);
    }

    private static int listAvailablePacks(CommandSourceStack param0) {
        PackRepository<?> var0 = param0.getServer().getPackRepository();
        var0.reload();
        Collection<? extends Pack> var1 = var0.getSelectedPacks();
        Collection<? extends Pack> var2 = var0.getAvailablePacks();
        List<Pack> var3 = var2.stream().filter(param1 -> !var1.contains(param1)).collect(Collectors.toList());
        if (var3.isEmpty()) {
            param0.sendSuccess(new TranslatableComponent("commands.datapack.list.available.none"), false);
        } else {
            param0.sendSuccess(
                new TranslatableComponent(
                    "commands.datapack.list.available.success", var3.size(), ComponentUtils.formatList(var3, param0x -> param0x.getChatLink(false))
                ),
                false
            );
        }

        return var3.size();
    }

    private static int listEnabledPacks(CommandSourceStack param0) {
        PackRepository<?> var0 = param0.getServer().getPackRepository();
        var0.reload();
        Collection<? extends Pack> var1 = var0.getSelectedPacks();
        if (var1.isEmpty()) {
            param0.sendSuccess(new TranslatableComponent("commands.datapack.list.enabled.none"), false);
        } else {
            param0.sendSuccess(
                new TranslatableComponent(
                    "commands.datapack.list.enabled.success", var1.size(), ComponentUtils.formatList(var1, param0x -> param0x.getChatLink(true))
                ),
                false
            );
        }

        return var1.size();
    }

    private static Pack getPack(CommandContext<CommandSourceStack> param0, String param1, boolean param2) throws CommandSyntaxException {
        String var0 = StringArgumentType.getString(param0, param1);
        PackRepository<?> var1 = param0.getSource().getServer().getPackRepository();
        Pack var2 = var1.getPack(var0);
        if (var2 == null) {
            throw ERROR_UNKNOWN_PACK.create(var0);
        } else {
            boolean var3 = var1.getSelectedPacks().contains(var2);
            if (param2 && var3) {
                throw ERROR_PACK_ALREADY_ENABLED.create(var0);
            } else if (!param2 && !var3) {
                throw ERROR_PACK_ALREADY_DISABLED.create(var0);
            } else {
                return var2;
            }
        }
    }

    interface Inserter {
        void apply(List<Pack> var1, Pack var2) throws CommandSyntaxException;
    }
}
