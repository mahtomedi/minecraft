package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
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

        public LootContext create(Optional<ResourceLocation> param0) {
            ServerLevel var0 = this.getLevel();
            MinecraftServer var1 = var0.getServer();
            RandomSource var2 = Optional.ofNullable(this.random).or(() -> param0.map(var0::getRandomSequence)).orElseGet(var0::getRandom);
            return new LootContext(this.params, var2, var1.getLootData());
        }
    }

    public static enum EntityTarget implements StringRepresentable {
        THIS("this", LootContextParams.THIS_ENTITY),
        KILLER("killer", LootContextParams.KILLER_ENTITY),
        DIRECT_KILLER("direct_killer", LootContextParams.DIRECT_KILLER_ENTITY),
        KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER);

        public static final StringRepresentable.EnumCodec<LootContext.EntityTarget> CODEC = StringRepresentable.fromEnum(LootContext.EntityTarget::values);
        private final String name;
        private final LootContextParam<? extends Entity> param;

        private EntityTarget(String param0, LootContextParam<? extends Entity> param1) {
            this.name = param0;
            this.param = param1;
        }

        public LootContextParam<? extends Entity> getParam() {
            return this.param;
        }

        public static LootContext.EntityTarget getByName(String param0) {
            LootContext.EntityTarget var0 = CODEC.byName(param0);
            if (var0 != null) {
                return var0;
            } else {
                throw new IllegalArgumentException("Invalid entity target " + param0);
            }
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static record VisitedEntry<T>(LootDataType<T> type, T value) {
    }
}
