package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameRules;

public class GameRuleCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        final LiteralArgumentBuilder<CommandSourceStack> var0 = Commands.literal("gamerule").requires(param0x -> param0x.hasPermission(2));
        GameRules.visitGameRuleTypes(
            new GameRules.GameRuleTypeVisitor() {
                @Override
                public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> param0, GameRules.Type<T> param1) {
                    var0.then(
                        Commands.literal(param0.getId())
                            .executes(param1x -> GameRuleCommand.queryRule(param1x.getSource(), param0))
                            .then(param1.createArgument("value").executes(param1x -> GameRuleCommand.setRule(param1x, param0)))
                    );
                }
            }
        );
        param0.register(var0);
    }

    static <T extends GameRules.Value<T>> int setRule(CommandContext<CommandSourceStack> param0, GameRules.Key<T> param1) {
        CommandSourceStack var0 = param0.getSource();
        T var1 = var0.getServer().getGameRules().getRule(param1);
        var1.setFromArgument(param0, "value");
        var0.sendSuccess(Component.translatable("commands.gamerule.set", param1.getId(), var1.toString()), true);
        return var1.getCommandResult();
    }

    static <T extends GameRules.Value<T>> int queryRule(CommandSourceStack param0, GameRules.Key<T> param1) {
        T var0 = param0.getServer().getGameRules().getRule(param1);
        param0.sendSuccess(Component.translatable("commands.gamerule.query", param1.getId(), var0.toString()), false);
        return var0.getCommandResult();
    }
}
