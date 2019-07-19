package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

public class ResourceLocationArgument implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_ID = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("argument.id.unknown", param0)
    );
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_ADVANCEMENT = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("advancement.advancementNotFound", param0)
    );
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_RECIPE = new DynamicCommandExceptionType(
        param0 -> new TranslatableComponent("recipe.notFound", param0)
    );

    public static ResourceLocationArgument id() {
        return new ResourceLocationArgument();
    }

    public static Advancement getAdvancement(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        ResourceLocation var0 = param0.getArgument(param1, ResourceLocation.class);
        Advancement var1 = param0.getSource().getServer().getAdvancements().getAdvancement(var0);
        if (var1 == null) {
            throw ERROR_UNKNOWN_ADVANCEMENT.create(var0);
        } else {
            return var1;
        }
    }

    public static Recipe<?> getRecipe(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        RecipeManager var0 = param0.getSource().getServer().getRecipeManager();
        ResourceLocation var1 = param0.getArgument(param1, ResourceLocation.class);
        return var0.byKey(var1).orElseThrow(() -> ERROR_UNKNOWN_RECIPE.create(var1));
    }

    public static ResourceLocation getId(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, ResourceLocation.class);
    }

    public ResourceLocation parse(StringReader param0) throws CommandSyntaxException {
        return ResourceLocation.read(param0);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
