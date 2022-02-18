package net.minecraft.commands.synchronization;

import com.google.common.collect.Maps;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class SuggestionProviders {
    private static final Map<ResourceLocation, SuggestionProvider<SharedSuggestionProvider>> PROVIDERS_BY_NAME = Maps.newHashMap();
    private static final ResourceLocation DEFAULT_NAME = new ResourceLocation("ask_server");
    public static final SuggestionProvider<SharedSuggestionProvider> ASK_SERVER = register(
        DEFAULT_NAME, (param0, param1) -> param0.getSource().customSuggestion(param0, param1)
    );
    public static final SuggestionProvider<CommandSourceStack> ALL_RECIPES = register(
        new ResourceLocation("all_recipes"), (param0, param1) -> SharedSuggestionProvider.suggestResource(param0.getSource().getRecipeNames(), param1)
    );
    public static final SuggestionProvider<CommandSourceStack> AVAILABLE_SOUNDS = register(
        new ResourceLocation("available_sounds"),
        (param0, param1) -> SharedSuggestionProvider.suggestResource(param0.getSource().getAvailableSoundEvents(), param1)
    );
    public static final SuggestionProvider<CommandSourceStack> AVAILABLE_FEATURES = register(
        new ResourceLocation("available_features"),
        (param0, param1) -> SharedSuggestionProvider.suggestResource(
                param0.getSource().registryAccess().registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY).keySet(), param1
            )
    );
    public static final SuggestionProvider<CommandSourceStack> SUMMONABLE_ENTITIES = register(
        new ResourceLocation("summonable_entities"),
        (param0, param1) -> SharedSuggestionProvider.suggestResource(
                Registry.ENTITY_TYPE.stream().filter(EntityType::canSummon),
                param1,
                EntityType::getKey,
                param0x -> new TranslatableComponent(Util.makeDescriptionId("entity", EntityType.getKey(param0x)))
            )
    );

    public static <S extends SharedSuggestionProvider> SuggestionProvider<S> register(
        ResourceLocation param0, SuggestionProvider<SharedSuggestionProvider> param1
    ) {
        if (PROVIDERS_BY_NAME.containsKey(param0)) {
            throw new IllegalArgumentException("A command suggestion provider is already registered with the name " + param0);
        } else {
            PROVIDERS_BY_NAME.put(param0, param1);
            return new SuggestionProviders.Wrapper(param0, param1);
        }
    }

    public static SuggestionProvider<SharedSuggestionProvider> getProvider(ResourceLocation param0) {
        return PROVIDERS_BY_NAME.getOrDefault(param0, ASK_SERVER);
    }

    public static ResourceLocation getName(SuggestionProvider<SharedSuggestionProvider> param0) {
        return param0 instanceof SuggestionProviders.Wrapper ? ((SuggestionProviders.Wrapper)param0).name : DEFAULT_NAME;
    }

    public static SuggestionProvider<SharedSuggestionProvider> safelySwap(SuggestionProvider<SharedSuggestionProvider> param0) {
        return param0 instanceof SuggestionProviders.Wrapper ? param0 : ASK_SERVER;
    }

    protected static class Wrapper implements SuggestionProvider<SharedSuggestionProvider> {
        private final SuggestionProvider<SharedSuggestionProvider> delegate;
        final ResourceLocation name;

        public Wrapper(ResourceLocation param0, SuggestionProvider<SharedSuggestionProvider> param1) {
            this.delegate = param1;
            this.name = param0;
        }

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<SharedSuggestionProvider> param0, SuggestionsBuilder param1) throws CommandSyntaxException {
            return this.delegate.getSuggestions(param0, param1);
        }
    }
}
