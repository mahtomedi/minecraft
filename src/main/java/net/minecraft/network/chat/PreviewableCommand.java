package net.minecraft.network.chat;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.arguments.PreviewedArgument;

public record PreviewableCommand<S>(List<PreviewableCommand.Argument<S>> arguments) {
    public static <S> PreviewableCommand<S> of(ParseResults<S> param0) {
        CommandContextBuilder<S> var0 = param0.getContext();
        CommandContextBuilder<S> var1 = var0;

        List<PreviewableCommand.Argument<S>> var2;
        CommandContextBuilder<S> var3;
        for(var2 = collectArguments(var0); (var3 = var1.getChild()) != null; var1 = var3) {
            boolean var4 = var3.getRootNode() != var0.getRootNode();
            if (!var4) {
                break;
            }

            var2.addAll(collectArguments(var3));
        }

        return new PreviewableCommand<>(var2);
    }

    private static <S> List<PreviewableCommand.Argument<S>> collectArguments(CommandContextBuilder<S> param0) {
        List<PreviewableCommand.Argument<S>> var0 = new ArrayList<>();

        for(ParsedCommandNode<S> var1 : param0.getNodes()) {
            CommandNode var4 = var1.getNode();
            if (var4 instanceof ArgumentCommandNode var2) {
                ArgumentType var7 = var2.getType();
                if (var7 instanceof PreviewedArgument var3) {
                    ParsedArgument<S, ?> var4x = param0.getArguments().get(var2.getName());
                    if (var4x != null) {
                        var0.add(new PreviewableCommand.Argument<>(var2, var4x, var3));
                    }
                }
            }
        }

        return var0;
    }

    public boolean isPreviewed(CommandNode<?> param0) {
        for(PreviewableCommand.Argument<S> var0 : this.arguments) {
            if (var0.node() == param0) {
                return true;
            }
        }

        return false;
    }

    public static record Argument<S>(ArgumentCommandNode<S, ?> node, ParsedArgument<S, ?> parsedValue, PreviewedArgument<?> previewType) {
        public String name() {
            return this.node.getName();
        }
    }
}
