package net.minecraft.commands;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.RegistryAccess;
import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.server.commands.AdvancementCommands;
import net.minecraft.server.commands.AttributeCommand;
import net.minecraft.server.commands.BanIpCommands;
import net.minecraft.server.commands.BanListCommands;
import net.minecraft.server.commands.BanPlayerCommands;
import net.minecraft.server.commands.BossBarCommands;
import net.minecraft.server.commands.ClearInventoryCommands;
import net.minecraft.server.commands.CloneCommands;
import net.minecraft.server.commands.DataPackCommand;
import net.minecraft.server.commands.DeOpCommands;
import net.minecraft.server.commands.DebugCommand;
import net.minecraft.server.commands.DefaultGameModeCommands;
import net.minecraft.server.commands.DifficultyCommand;
import net.minecraft.server.commands.EffectCommands;
import net.minecraft.server.commands.EmoteCommands;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.commands.ExperienceCommand;
import net.minecraft.server.commands.FillCommand;
import net.minecraft.server.commands.ForceLoadCommand;
import net.minecraft.server.commands.FunctionCommand;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.server.commands.GiveCommand;
import net.minecraft.server.commands.HelpCommand;
import net.minecraft.server.commands.ItemCommands;
import net.minecraft.server.commands.JfrCommand;
import net.minecraft.server.commands.KickCommand;
import net.minecraft.server.commands.KillCommand;
import net.minecraft.server.commands.ListPlayersCommand;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.commands.OpCommand;
import net.minecraft.server.commands.PardonCommand;
import net.minecraft.server.commands.PardonIpCommand;
import net.minecraft.server.commands.ParticleCommand;
import net.minecraft.server.commands.PerfCommand;
import net.minecraft.server.commands.PlaceCommand;
import net.minecraft.server.commands.PlaySoundCommand;
import net.minecraft.server.commands.PublishCommand;
import net.minecraft.server.commands.RecipeCommand;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.server.commands.SaveOffCommand;
import net.minecraft.server.commands.SaveOnCommand;
import net.minecraft.server.commands.SayCommand;
import net.minecraft.server.commands.ScheduleCommand;
import net.minecraft.server.commands.ScoreboardCommand;
import net.minecraft.server.commands.SeedCommand;
import net.minecraft.server.commands.SetBlockCommand;
import net.minecraft.server.commands.SetPlayerIdleTimeoutCommand;
import net.minecraft.server.commands.SetSpawnCommand;
import net.minecraft.server.commands.SetWorldSpawnCommand;
import net.minecraft.server.commands.SpectateCommand;
import net.minecraft.server.commands.SpreadPlayersCommand;
import net.minecraft.server.commands.StopCommand;
import net.minecraft.server.commands.StopSoundCommand;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.server.commands.TagCommand;
import net.minecraft.server.commands.TeamCommand;
import net.minecraft.server.commands.TeamMsgCommand;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.commands.TellRawCommand;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.commands.TitleCommand;
import net.minecraft.server.commands.TriggerCommand;
import net.minecraft.server.commands.WeatherCommand;
import net.minecraft.server.commands.WhitelistCommand;
import net.minecraft.server.commands.WorldBorderCommand;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class Commands {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int LEVEL_ALL = 0;
    public static final int LEVEL_MODERATORS = 1;
    public static final int LEVEL_GAMEMASTERS = 2;
    public static final int LEVEL_ADMINS = 3;
    public static final int LEVEL_OWNERS = 4;
    private final CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();

    public Commands(Commands.CommandSelection param0, CommandBuildContext param1) {
        AdvancementCommands.register(this.dispatcher);
        AttributeCommand.register(this.dispatcher);
        ExecuteCommand.register(this.dispatcher, param1);
        BossBarCommands.register(this.dispatcher);
        ClearInventoryCommands.register(this.dispatcher, param1);
        CloneCommands.register(this.dispatcher, param1);
        DataCommands.register(this.dispatcher);
        DataPackCommand.register(this.dispatcher);
        DebugCommand.register(this.dispatcher);
        DefaultGameModeCommands.register(this.dispatcher);
        DifficultyCommand.register(this.dispatcher);
        EffectCommands.register(this.dispatcher);
        EmoteCommands.register(this.dispatcher);
        EnchantCommand.register(this.dispatcher);
        ExperienceCommand.register(this.dispatcher);
        FillCommand.register(this.dispatcher, param1);
        ForceLoadCommand.register(this.dispatcher);
        FunctionCommand.register(this.dispatcher);
        GameModeCommand.register(this.dispatcher);
        GameRuleCommand.register(this.dispatcher);
        GiveCommand.register(this.dispatcher, param1);
        HelpCommand.register(this.dispatcher);
        ItemCommands.register(this.dispatcher, param1);
        KickCommand.register(this.dispatcher);
        KillCommand.register(this.dispatcher);
        ListPlayersCommand.register(this.dispatcher);
        LocateCommand.register(this.dispatcher);
        LootCommand.register(this.dispatcher, param1);
        MsgCommand.register(this.dispatcher);
        ParticleCommand.register(this.dispatcher);
        PlaceCommand.register(this.dispatcher);
        PlaySoundCommand.register(this.dispatcher);
        ReloadCommand.register(this.dispatcher);
        RecipeCommand.register(this.dispatcher);
        SayCommand.register(this.dispatcher);
        ScheduleCommand.register(this.dispatcher);
        ScoreboardCommand.register(this.dispatcher);
        SeedCommand.register(this.dispatcher, param0 != Commands.CommandSelection.INTEGRATED);
        SetBlockCommand.register(this.dispatcher, param1);
        SetSpawnCommand.register(this.dispatcher);
        SetWorldSpawnCommand.register(this.dispatcher);
        SpectateCommand.register(this.dispatcher);
        SpreadPlayersCommand.register(this.dispatcher);
        StopSoundCommand.register(this.dispatcher);
        SummonCommand.register(this.dispatcher);
        TagCommand.register(this.dispatcher);
        TeamCommand.register(this.dispatcher);
        TeamMsgCommand.register(this.dispatcher);
        TeleportCommand.register(this.dispatcher);
        TellRawCommand.register(this.dispatcher);
        TimeCommand.register(this.dispatcher);
        TitleCommand.register(this.dispatcher);
        TriggerCommand.register(this.dispatcher);
        WeatherCommand.register(this.dispatcher);
        WorldBorderCommand.register(this.dispatcher);
        if (JvmProfiler.INSTANCE.isAvailable()) {
            JfrCommand.register(this.dispatcher);
        }

        if (SharedConstants.IS_RUNNING_IN_IDE) {
            TestCommand.register(this.dispatcher);
        }

        if (param0.includeDedicated) {
            BanIpCommands.register(this.dispatcher);
            BanListCommands.register(this.dispatcher);
            BanPlayerCommands.register(this.dispatcher);
            DeOpCommands.register(this.dispatcher);
            OpCommand.register(this.dispatcher);
            PardonCommand.register(this.dispatcher);
            PardonIpCommand.register(this.dispatcher);
            PerfCommand.register(this.dispatcher);
            SaveAllCommand.register(this.dispatcher);
            SaveOffCommand.register(this.dispatcher);
            SaveOnCommand.register(this.dispatcher);
            SetPlayerIdleTimeoutCommand.register(this.dispatcher);
            StopCommand.register(this.dispatcher);
            WhitelistCommand.register(this.dispatcher);
        }

        if (param0.includeIntegrated) {
            PublishCommand.register(this.dispatcher);
        }

        this.dispatcher.setConsumer((param0x, param1x, param2) -> param0x.getSource().onCommandComplete(param0x, param1x, param2));
    }

    public static <S> ParseResults<S> mapSource(ParseResults<S> param0, UnaryOperator<S> param1) {
        CommandContextBuilder<S> var0 = param0.getContext();
        CommandContextBuilder<S> var1 = var0.withSource(param1.apply(var0.getSource()));
        return new ParseResults<>(var1, param0.getReader(), param0.getExceptions());
    }

    public int performPrefixedCommand(CommandSourceStack param0, String param1) {
        param1 = param1.startsWith("/") ? param1.substring(1) : param1;
        return this.performCommand(this.dispatcher.parse(param1, param0), param1);
    }

    public int performCommand(ParseResults<CommandSourceStack> param0, String param1) {
        CommandSourceStack var0 = param0.getContext().getSource();
        var0.getServer().getProfiler().push(() -> "/" + param1);

        try {
            try {
                return this.dispatcher.execute(param0);
            } catch (CommandRuntimeException var13) {
                var0.sendFailure(var13.getComponent());
                return 0;
            } catch (CommandSyntaxException var14) {
                var0.sendFailure(ComponentUtils.fromMessage(var14.getRawMessage()));
                if (var14.getInput() != null && var14.getCursor() >= 0) {
                    int var3 = Math.min(var14.getInput().length(), var14.getCursor());
                    MutableComponent var4 = Component.empty()
                        .withStyle(ChatFormatting.GRAY)
                        .withStyle(param1x -> param1x.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + param1)));
                    if (var3 > 10) {
                        var4.append(CommonComponents.ELLIPSIS);
                    }

                    var4.append(var14.getInput().substring(Math.max(0, var3 - 10), var3));
                    if (var3 < var14.getInput().length()) {
                        Component var5 = Component.literal(var14.getInput().substring(var3)).withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE);
                        var4.append(var5);
                    }

                    var4.append(Component.translatable("command.context.here").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                    var0.sendFailure(var4);
                }
            } catch (Exception var15) {
                MutableComponent var7 = Component.literal(var15.getMessage() == null ? var15.getClass().getName() : var15.getMessage());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("Command exception: /{}", param1, var15);
                    StackTraceElement[] var8 = var15.getStackTrace();

                    for(int var9 = 0; var9 < Math.min(var8.length, 3); ++var9) {
                        var7.append("\n\n")
                            .append(var8[var9].getMethodName())
                            .append("\n ")
                            .append(var8[var9].getFileName())
                            .append(":")
                            .append(String.valueOf(var8[var9].getLineNumber()));
                    }
                }

                var0.sendFailure(
                    Component.translatable("command.failed").withStyle(param1x -> param1x.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, var7)))
                );
                if (SharedConstants.IS_RUNNING_IN_IDE) {
                    var0.sendFailure(Component.literal(Util.describeError(var15)));
                    LOGGER.error("'/{}' threw an exception", param1, var15);
                }

                return 0;
            }

            return 0;
        } finally {
            var0.getServer().getProfiler().pop();
        }
    }

    public void sendCommands(ServerPlayer param0) {
        Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> var0 = Maps.newHashMap();
        RootCommandNode<SharedSuggestionProvider> var1 = new RootCommandNode<>();
        var0.put(this.dispatcher.getRoot(), var1);
        this.fillUsableCommands(this.dispatcher.getRoot(), var1, param0.createCommandSourceStack(), var0);
        param0.connection.send(new ClientboundCommandsPacket(var1));
    }

    private void fillUsableCommands(
        CommandNode<CommandSourceStack> param0,
        CommandNode<SharedSuggestionProvider> param1,
        CommandSourceStack param2,
        Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> param3
    ) {
        for(CommandNode<CommandSourceStack> var0 : param0.getChildren()) {
            if (var0.canUse(param2)) {
                ArgumentBuilder<SharedSuggestionProvider, ?> var1 = var0.createBuilder();
                var1.requires(param0x -> true);
                if (var1.getCommand() != null) {
                    var1.executes(param0x -> 0);
                }

                if (var1 instanceof RequiredArgumentBuilder var2 && var2.getSuggestionsProvider() != null) {
                    var2.suggests(SuggestionProviders.safelySwap(var2.getSuggestionsProvider()));
                }

                if (var1.getRedirect() != null) {
                    var1.redirect(param3.get(var1.getRedirect()));
                }

                CommandNode<SharedSuggestionProvider> var3 = var1.build();
                param3.put(var0, var3);
                param1.addChild(var3);
                if (!var0.getChildren().isEmpty()) {
                    this.fillUsableCommands(var0, var3, param2, param3);
                }
            }
        }

    }

    public static LiteralArgumentBuilder<CommandSourceStack> literal(String param0) {
        return LiteralArgumentBuilder.literal(param0);
    }

    public static <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String param0, ArgumentType<T> param1) {
        return RequiredArgumentBuilder.argument(param0, param1);
    }

    public static Predicate<String> createValidator(Commands.ParseFunction param0) {
        return param1 -> {
            try {
                param0.parse(new StringReader(param1));
                return true;
            } catch (CommandSyntaxException var3) {
                return false;
            }
        };
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return this.dispatcher;
    }

    @Nullable
    public static <S> CommandSyntaxException getParseException(ParseResults<S> param0) {
        if (!param0.getReader().canRead()) {
            return null;
        } else if (param0.getExceptions().size() == 1) {
            return param0.getExceptions().values().iterator().next();
        } else {
            return param0.getContext().getRange().isEmpty()
                ? CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(param0.getReader())
                : CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(param0.getReader());
        }
    }

    public static void validate() {
        CommandBuildContext var0 = new CommandBuildContext(RegistryAccess.BUILTIN.get());
        var0.missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy.RETURN_EMPTY);
        CommandDispatcher<CommandSourceStack> var1 = new Commands(Commands.CommandSelection.ALL, var0).getDispatcher();
        RootCommandNode<CommandSourceStack> var2 = var1.getRoot();
        var1.findAmbiguities(
            (param1, param2, param3, param4) -> LOGGER.warn(
                    "Ambiguity between arguments {} and {} with inputs: {}", var1.getPath(param2), var1.getPath(param3), param4
                )
        );
        Set<ArgumentType<?>> var3 = ArgumentUtils.findUsedArgumentTypes(var2);
        Set<ArgumentType<?>> var4 = var3.stream().filter(param0 -> !ArgumentTypeInfos.isClassRecognized(param0.getClass())).collect(Collectors.toSet());
        if (!var4.isEmpty()) {
            LOGGER.warn(
                "Missing type registration for following arguments:\n {}", var4.stream().map(param0 -> "\t" + param0).collect(Collectors.joining(",\n"))
            );
            throw new IllegalStateException("Unregistered argument types");
        }
    }

    public static enum CommandSelection {
        ALL(true, true),
        DEDICATED(false, true),
        INTEGRATED(true, false);

        final boolean includeIntegrated;
        final boolean includeDedicated;

        private CommandSelection(boolean param0, boolean param1) {
            this.includeIntegrated = param0;
            this.includeDedicated = param1;
        }
    }

    @FunctionalInterface
    public interface ParseFunction {
        void parse(StringReader var1) throws CommandSyntaxException;
    }
}
