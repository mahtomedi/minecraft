package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Unit;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.ItemModifierManager;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.PredicateManager;
import org.slf4j.Logger;

public class ReloadableServerResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
    private final CommandBuildContext commandBuildContext;
    private final Commands commands;
    private final RecipeManager recipes = new RecipeManager();
    private final TagManager tagManager;
    private final PredicateManager predicateManager = new PredicateManager();
    private final LootTables lootTables = new LootTables(this.predicateManager);
    private final ItemModifierManager itemModifierManager = new ItemModifierManager(this.predicateManager, this.lootTables);
    private final ServerAdvancementManager advancements = new ServerAdvancementManager(this.predicateManager);
    private final ServerFunctionLibrary functionLibrary;

    public ReloadableServerResources(RegistryAccess.Frozen param0, Commands.CommandSelection param1, int param2) {
        this.tagManager = new TagManager(param0);
        this.commandBuildContext = new CommandBuildContext(param0);
        this.commands = new Commands(param1, this.commandBuildContext);
        this.commandBuildContext.missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy.CREATE_NEW);
        this.functionLibrary = new ServerFunctionLibrary(param2, this.commands.getDispatcher());
    }

    public ServerFunctionLibrary getFunctionLibrary() {
        return this.functionLibrary;
    }

    public PredicateManager getPredicateManager() {
        return this.predicateManager;
    }

    public LootTables getLootTables() {
        return this.lootTables;
    }

    public ItemModifierManager getItemModifierManager() {
        return this.itemModifierManager;
    }

    public RecipeManager getRecipeManager() {
        return this.recipes;
    }

    public Commands getCommands() {
        return this.commands;
    }

    public ServerAdvancementManager getAdvancements() {
        return this.advancements;
    }

    public List<PreparableReloadListener> listeners() {
        return List.of(this.tagManager, this.predicateManager, this.recipes, this.lootTables, this.itemModifierManager, this.functionLibrary, this.advancements);
    }

    public static CompletableFuture<ReloadableServerResources> loadResources(
        ResourceManager param0, RegistryAccess.Frozen param1, Commands.CommandSelection param2, int param3, Executor param4, Executor param5
    ) {
        ReloadableServerResources var0 = new ReloadableServerResources(param1, param2, param3);
        return SimpleReloadInstance.create(param0, var0.listeners(), param4, param5, DATA_RELOAD_INITIAL_TASK, LOGGER.isDebugEnabled())
            .done()
            .whenComplete((param1x, param2x) -> var0.commandBuildContext.missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy.FAIL))
            .thenApply(param1x -> var0);
    }

    public void updateRegistryTags(RegistryAccess param0) {
        this.tagManager.getResult().forEach(param1 -> updateRegistryTags(param0, param1));
        Blocks.rebuildCache();
    }

    private static <T> void updateRegistryTags(RegistryAccess param0, TagManager.LoadResult<T> param1) {
        ResourceKey<? extends Registry<T>> var0 = param1.key();
        Map<TagKey<T>, List<Holder<T>>> var1 = param1.tags()
            .entrySet()
            .stream()
            .collect(Collectors.toUnmodifiableMap(param1x -> TagKey.create(var0, param1x.getKey()), param0x -> List.copyOf(param0x.getValue())));
        param0.registryOrThrow(var0).bindTags(var1);
    }
}
