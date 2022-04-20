package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class EntitySummonArgument implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:pig", "cow");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_ENTITY = new DynamicCommandExceptionType(
        param0 -> Component.translatable("entity.notFound", param0)
    );

    public static EntitySummonArgument id() {
        return new EntitySummonArgument();
    }

    public static ResourceLocation getSummonableEntity(CommandContext<CommandSourceStack> param0, String param1) throws CommandSyntaxException {
        return verifyCanSummon(param0.getArgument(param1, ResourceLocation.class));
    }

    private static ResourceLocation verifyCanSummon(ResourceLocation param0) throws CommandSyntaxException {
        Registry.ENTITY_TYPE.getOptional(param0).filter(EntityType::canSummon).orElseThrow(() -> ERROR_UNKNOWN_ENTITY.create(param0));
        return param0;
    }

    public ResourceLocation parse(StringReader param0) throws CommandSyntaxException {
        return verifyCanSummon(ResourceLocation.read(param0));
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
