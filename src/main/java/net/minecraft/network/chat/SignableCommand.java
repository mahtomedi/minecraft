package net.minecraft.network.chat;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.arguments.SignedArgument;

public record SignableCommand<S>(List<SignableCommand.Argument<S>> arguments) {
    public static <S> SignableCommand<S> of(ParseResults<S> param0) {
        String var0 = param0.getReader().getString();
        CommandContextBuilder<S> var1 = param0.getContext();
        CommandContextBuilder<S> var2 = var1;

        List<SignableCommand.Argument<S>> var3;
        CommandContextBuilder<S> var4;
        for(var3 = collectArguments(var0, var1); (var4 = var2.getChild()) != null; var2 = var4) {
            boolean var5 = var4.getRootNode() != var1.getRootNode();
            if (!var5) {
                break;
            }

            var3.addAll(collectArguments(var0, var4));
        }

        return new SignableCommand<>(var3);
    }

    private static <S> List<SignableCommand.Argument<S>> collectArguments(String param0, CommandContextBuilder<S> param1) {
        List<SignableCommand.Argument<S>> var0 = new ArrayList();

        for(ParsedCommandNode<S> var1 : param1.getNodes()) {
            CommandNode var3 = var1.getNode();
            if (var3 instanceof ArgumentCommandNode var2 && var2.getType() instanceof SignedArgument) {
                ParsedArgument<S, ?> var3x = param1.getArguments().get(var2.getName());
                if (var3x != null) {
                    String var4 = var3x.getRange().get(param0);
                    var0.add(new SignableCommand.Argument<S>(var2, var4));
                }
            }
        }

        return var0;
    }

    public static record Argument<S>(ArgumentCommandNode<S, ?> node, String value) {
        public String name() {
            return this.node.getName();
        }
    }
}
