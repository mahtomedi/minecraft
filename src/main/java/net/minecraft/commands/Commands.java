package net.minecraft.commands;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import jdk.jfr.FlightRecorder;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraft.server.commands.LocateBiomeCommand;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.commands.OpCommand;
import net.minecraft.server.commands.PardonCommand;
import net.minecraft.server.commands.PardonIpCommand;
import net.minecraft.server.commands.ParticleCommand;
import net.minecraft.server.commands.PerfCommand;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Commands {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int LEVEL_ALL = 0;
    public static final int LEVEL_MODERATORS = 1;
    public static final int LEVEL_GAMEMASTERS = 2;
    public static final int LEVEL_ADMINS = 3;
    public static final int LEVEL_OWNERS = 4;
    private final CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();

    public Commands(Commands.CommandSelection param0) {
        AdvancementCommands.register(this.dispatcher);
        AttributeCommand.register(this.dispatcher);
        ExecuteCommand.register(this.dispatcher);
        BossBarCommands.register(this.dispatcher);
        ClearInventoryCommands.register(this.dispatcher);
        CloneCommands.register(this.dispatcher);
        DataCommands.register(this.dispatcher);
        DataPackCommand.register(this.dispatcher);
        DebugCommand.register(this.dispatcher);
        DefaultGameModeCommands.register(this.dispatcher);
        DifficultyCommand.register(this.dispatcher);
        EffectCommands.register(this.dispatcher);
        EmoteCommands.register(this.dispatcher);
        EnchantCommand.register(this.dispatcher);
        ExperienceCommand.register(this.dispatcher);
        FillCommand.register(this.dispatcher);
        ForceLoadCommand.register(this.dispatcher);
        FunctionCommand.register(this.dispatcher);
        GameModeCommand.register(this.dispatcher);
        GameRuleCommand.register(this.dispatcher);
        GiveCommand.register(this.dispatcher);
        HelpCommand.register(this.dispatcher);
        ItemCommands.register(this.dispatcher);
        KickCommand.register(this.dispatcher);
        KillCommand.register(this.dispatcher);
        ListPlayersCommand.register(this.dispatcher);
        LocateCommand.register(this.dispatcher);
        LocateBiomeCommand.register(this.dispatcher);
        LootCommand.register(this.dispatcher);
        MsgCommand.register(this.dispatcher);
        ParticleCommand.register(this.dispatcher);
        PlaySoundCommand.register(this.dispatcher);
        ReloadCommand.register(this.dispatcher);
        RecipeCommand.register(this.dispatcher);
        SayCommand.register(this.dispatcher);
        ScheduleCommand.register(this.dispatcher);
        ScoreboardCommand.register(this.dispatcher);
        SeedCommand.register(this.dispatcher, param0 != Commands.CommandSelection.INTEGRATED);
        SetBlockCommand.register(this.dispatcher);
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
        if (FlightRecorder.isAvailable()) {
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

        this.dispatcher
            .findAmbiguities(
                (param0x, param1, param2, param3) -> LOGGER.warn(
                        "Ambiguity between arguments {} and {} with inputs: {}", this.dispatcher.getPath(param1), this.dispatcher.getPath(param2), param3
                    )
            );
        this.dispatcher.setConsumer((param0x, param1, param2) -> param0x.getSource().onCommandComplete(param0x, param1, param2));
    }

    public int performCommand(CommandSourceStack param0, String param1) {
        StringReader var0 = new StringReader(param1);
        if (var0.canRead() && var0.peek() == '/') {
            var0.skip();
        }

        param0.getServer().getProfiler().push(param1);

        try {
            try {
                return this.dispatcher.execute(var0, param0);
            } catch (CommandRuntimeException var13) {
                param0.sendFailure(var13.getComponent());
                return 0;
            } catch (CommandSyntaxException var14) {
                param0.sendFailure(ComponentUtils.fromMessage(var14.getRawMessage()));
                if (var14.getInput() != null && var14.getCursor() >= 0) {
                    int var3 = Math.min(var14.getInput().length(), var14.getCursor());
                    MutableComponent var4 = new TextComponent("")
                        .withStyle(ChatFormatting.GRAY)
                        .withStyle(param1x -> param1x.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, param1)));
                    if (var3 > 10) {
                        var4.append("...");
                    }

                    var4.append(var14.getInput().substring(Math.max(0, var3 - 10), var3));
                    if (var3 < var14.getInput().length()) {
                        Component var5 = new TextComponent(var14.getInput().substring(var3))
                            .withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.UNDERLINE});
                        var4.append(var5);
                    }

                    var4.append(new TranslatableComponent("command.context.here").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.ITALIC}));
                    param0.sendFailure(var4);
                }
            } catch (Exception var15) {
                MutableComponent var7 = new TextComponent(var15.getMessage() == null ? var15.getClass().getName() : var15.getMessage());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("Command exception: {}", param1, var15);
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

                param0.sendFailure(
                    new TranslatableComponent("command.failed").withStyle(param1x -> param1x.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, var7)))
                );
                if (SharedConstants.IS_RUNNING_IN_IDE) {
                    param0.sendFailure(new TextComponent(Util.describeError(var15)));
                    LOGGER.error("'{}' threw an exception", param1, var15);
                }

                return 0;
            }

            return 0;
        } finally {
            param0.getServer().getProfiler().pop();
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
        RootCommandNode<CommandSourceStack> var0 = new Commands(Commands.CommandSelection.ALL).getDispatcher().getRoot();
        Set<ArgumentType<?>> var1 = ArgumentTypes.findUsedArgumentTypes(var0);
        Set<ArgumentType<?>> var2 = var1.stream().filter(param0 -> !ArgumentTypes.isTypeRegistered(param0)).collect(Collectors.toSet());
        if (!var2.isEmpty()) {
            LOGGER.warn(
                "Missing type registration for following arguments:\n {}", var2.stream().map(param0 -> "\t" + param0).collect(Collectors.joining(",\n"))
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
