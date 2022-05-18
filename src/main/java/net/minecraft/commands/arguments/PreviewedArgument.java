package net.minecraft.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public interface PreviewedArgument<T> extends ArgumentType<T> {
    @Nullable
    static CompletableFuture<Component> resolvePreviewed(ArgumentCommandNode<?, ?> param0, CommandContextBuilder<CommandSourceStack> param1) throws CommandSyntaxException {
        ArgumentType var3 = param0.getType();
        return var3 instanceof PreviewedArgument var0 ? var0.resolvePreview(param1, param0.getName()) : null;
    }

    static boolean isPreviewed(CommandNode<?> param0) {
        if (param0 instanceof ArgumentCommandNode var0 && var0.getType() instanceof PreviewedArgument) {
            return true;
        }

        return false;
    }

    @Nullable
    default CompletableFuture<Component> resolvePreview(CommandContextBuilder<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        ParsedArgument<CommandSourceStack, ?> var0 = param0.getArguments().get(param1);
        return var0 != null && this.getValueType().isInstance(var0.getResult())
            ? this.resolvePreview(param0.getSource(), this.getValueType().cast(var0.getResult()))
            : null;
    }

    CompletableFuture<Component> resolvePreview(CommandSourceStack var1, T var2) throws CommandSyntaxException;

    Class<T> getValueType();
}
