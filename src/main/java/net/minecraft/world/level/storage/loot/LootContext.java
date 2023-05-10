package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Sets;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootContext {
    private final LootParams params;
    private final RandomSource random;
    private final LootDataResolver lootDataResolver;
    private final Set<LootContext.VisitedEntry<?>> visitedElements = Sets.newLinkedHashSet();

    LootContext(LootParams param0, RandomSource param1, LootDataResolver param2) {
        this.params = param0;
        this.random = param1;
        this.lootDataResolver = param2;
    }

    public boolean hasParam(LootContextParam<?> param0) {
        return this.params.hasParam(param0);
    }

    public <T> T getParam(LootContextParam<T> param0) {
        return this.params.getParameter(param0);
    }

    public void addDynamicDrops(ResourceLocation param0, Consumer<ItemStack> param1) {
        this.params.addDynamicDrops(param0, param1);
    }

    @Nullable
    public <T> T getParamOrNull(LootContextParam<T> param0) {
        return this.params.getParamOrNull(param0);
    }

    public boolean hasVisitedElement(LootContext.VisitedEntry<?> param0) {
        return this.visitedElements.contains(param0);
    }

    public boolean pushVisitedElement(LootContext.VisitedEntry<?> param0) {
        return this.visitedElements.add(param0);
    }

    public void popVisitedElement(LootContext.VisitedEntry<?> param0) {
        this.visitedElements.remove(param0);
    }

    public LootDataResolver getResolver() {
        return this.lootDataResolver;
    }

    public RandomSource getRandom() {
        return this.random;
    }

    public float getLuck() {
        return this.params.getLuck();
    }

    public ServerLevel getLevel() {
        return this.params.getLevel();
    }

    public static LootContext.VisitedEntry<LootTable> createVisitedEntry(LootTable param0) {
        return new LootContext.VisitedEntry<>(LootDataType.TABLE, param0);
    }

    public static LootContext.VisitedEntry<LootItemCondition> createVisitedEntry(LootItemCondition param0) {
        return new LootContext.VisitedEntry<>(LootDataType.PREDICATE, param0);
    }

    public static LootContext.VisitedEntry<LootItemFunction> createVisitedEntry(LootItemFunction param0) {
        return new LootContext.VisitedEntry<>(LootDataType.MODIFIER, param0);
    }

    public static class Builder {
        private final LootParams params;
        @Nullable
        private RandomSource random;

        public Builder(LootParams param0) {
            this.params = param0;
        }

        public LootContext.Builder withOptionalRandomSeed(long param0) {
            if (param0 != 0L) {
                this.random = RandomSource.create(param0);
            }

            return this;
        }

        public ServerLevel getLevel() {
            return this.params.getLevel();
        }

        public LootContext create(ResourceLocation param0) {
            ServerLevel var0 = this.getLevel();
            MinecraftServer var1 = var0.getServer();
            RandomSource var2;
            if (this.random != null) {
                var2 = this.random;
            } else {
                var2 = var0.getRandomSequence(param0);
            }

            return new LootContext(this.params, var2, var1.getLootData());
        }
    }

    public static enum EntityTarget {
        THIS("this", LootContextParams.THIS_ENTITY),
        KILLER("killer", LootContextParams.KILLER_ENTITY),
        DIRECT_KILLER("direct_killer", LootContextParams.DIRECT_KILLER_ENTITY),
        KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER);

        final String name;
        private final LootContextParam<? extends Entity> param;

        private EntityTarget(String param0, LootContextParam<? extends Entity> param1) {
            this.name = param0;
            this.param = param1;
        }

        public LootContextParam<? extends Entity> getParam() {
            return this.param;
        }

        public static LootContext.EntityTarget getByName(String param0) {
            for(LootContext.EntityTarget var0 : values()) {
                if (var0.name.equals(param0)) {
                    return var0;
                }
            }

            throw new IllegalArgumentException("Invalid entity target " + param0);
        }

        public static class Serializer extends TypeAdapter<LootContext.EntityTarget> {
            public void write(JsonWriter param0, LootContext.EntityTarget param1) throws IOException {
                param0.value(param1.name);
            }

            public LootContext.EntityTarget read(JsonReader param0) throws IOException {
                return LootContext.EntityTarget.getByName(param0.nextString());
            }
        }
    }

    public static record VisitedEntry<T>(LootDataType<T> type, T value) {
    }
}
