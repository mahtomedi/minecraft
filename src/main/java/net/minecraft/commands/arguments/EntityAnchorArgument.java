package net.minecraft.commands.arguments;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityAnchorArgument implements ArgumentType<EntityAnchorArgument.Anchor> {
    private static final Collection<String> EXAMPLES = Arrays.asList("eyes", "feet");
    private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("argument.anchor.invalid", param0)
    );

    public static EntityAnchorArgument.Anchor getAnchor(CommandContext<CommandSourceStack> param0, String param1) {
        return param0.getArgument(param1, EntityAnchorArgument.Anchor.class);
    }

    public static EntityAnchorArgument anchor() {
        return new EntityAnchorArgument();
    }

    public EntityAnchorArgument.Anchor parse(StringReader param0) throws CommandSyntaxException {
        int var0 = param0.getCursor();
        String var1 = param0.readUnquotedString();
        EntityAnchorArgument.Anchor var2 = EntityAnchorArgument.Anchor.getByName(var1);
        if (var2 == null) {
            param0.setCursor(var0);
            throw ERROR_INVALID.createWithContext(param0, var1);
        } else {
            return var2;
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> param0, SuggestionsBuilder param1) {
        return SharedSuggestionProvider.suggest(EntityAnchorArgument.Anchor.BY_NAME.keySet(), param1);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static enum Anchor {
        FEET("feet", (param0, param1) -> param0),
        EYES("eyes", (param0, param1) -> new Vec3(param0.x, param0.y + (double)param1.getEyeHeight(), param0.z));

        static final Map<String, EntityAnchorArgument.Anchor> BY_NAME = Util.make(Maps.newHashMap(), param0 -> {
            for(EntityAnchorArgument.Anchor var0 : values()) {
                param0.put(var0.name, var0);
            }

        });
        private final String name;
        private final BiFunction<Vec3, Entity, Vec3> transform;

        private Anchor(String param0, BiFunction<Vec3, Entity, Vec3> param1) {
            this.name = param0;
            this.transform = param1;
        }

        @Nullable
        public static EntityAnchorArgument.Anchor getByName(String param0) {
            return BY_NAME.get(param0);
        }

        public Vec3 apply(Entity param0) {
            return this.transform.apply(param0.position(), param0);
        }

        public Vec3 apply(CommandSourceStack param0) {
            Entity var0 = param0.getEntity();
            return var0 == null ? param0.getPosition() : this.transform.apply(param0.getPosition(), var0);
        }
    }
}
