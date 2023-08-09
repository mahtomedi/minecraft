package net.minecraft.advancements.critereon;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public record PlayerPredicate(
    MinMaxBounds.Ints level,
    Optional<GameType> gameType,
    List<PlayerPredicate.StatMatcher<?>> stats,
    Object2BooleanMap<ResourceLocation> recipes,
    Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements,
    Optional<EntityPredicate> lookingAt
) implements EntitySubPredicate {
    public static final int LOOKING_AT_RANGE = 100;
    public static final MapCodec<PlayerPredicate> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "level", MinMaxBounds.Ints.ANY).forGetter(PlayerPredicate::level),
                    GameType.CODEC.optionalFieldOf("gamemode").forGetter(PlayerPredicate::gameType),
                    ExtraCodecs.strictOptionalField(PlayerPredicate.StatMatcher.CODEC.listOf(), "stats", List.of()).forGetter(PlayerPredicate::stats),
                    ExtraCodecs.strictOptionalField(ExtraCodecs.object2BooleanMap(ResourceLocation.CODEC), "recipes", Object2BooleanMaps.emptyMap())
                        .forGetter(PlayerPredicate::recipes),
                    ExtraCodecs.strictOptionalField(
                            Codec.unboundedMap(ResourceLocation.CODEC, PlayerPredicate.AdvancementPredicate.CODEC), "advancements", Map.of()
                        )
                        .forGetter(PlayerPredicate::advancements),
                    ExtraCodecs.strictOptionalField(EntityPredicate.CODEC, "looking_at").forGetter(PlayerPredicate::lookingAt)
                )
                .apply(param0, PlayerPredicate::new)
    );

    @Override
    public boolean matches(Entity param0, ServerLevel param1, @Nullable Vec3 param2) {
        if (!(param0 instanceof ServerPlayer)) {
            return false;
        } else {
            ServerPlayer var0 = (ServerPlayer)param0;
            if (!this.level.matches(var0.experienceLevel)) {
                return false;
            } else if (this.gameType.isPresent() && this.gameType.get() != var0.gameMode.getGameModeForPlayer()) {
                return false;
            } else {
                StatsCounter var2 = var0.getStats();

                for(PlayerPredicate.StatMatcher<?> var3 : this.stats) {
                    if (!var3.matches(var2)) {
                        return false;
                    }
                }

                RecipeBook var4 = var0.getRecipeBook();

                for(Entry<ResourceLocation> var5 : this.recipes.object2BooleanEntrySet()) {
                    if (var4.contains(var5.getKey()) != var5.getBooleanValue()) {
                        return false;
                    }
                }

                if (!this.advancements.isEmpty()) {
                    PlayerAdvancements var6 = var0.getAdvancements();
                    ServerAdvancementManager var7 = var0.getServer().getAdvancements();

                    for(java.util.Map.Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> var8 : this.advancements.entrySet()) {
                        Advancement var9 = var7.getAdvancement(var8.getKey());
                        if (var9 == null || !var8.getValue().test(var6.getOrStartProgress(var9))) {
                            return false;
                        }
                    }
                }

                if (this.lookingAt.isPresent()) {
                    Vec3 var10 = var0.getEyePosition();
                    Vec3 var11 = var0.getViewVector(1.0F);
                    Vec3 var12 = var10.add(var11.x * 100.0, var11.y * 100.0, var11.z * 100.0);
                    EntityHitResult var13 = ProjectileUtil.getEntityHitResult(
                        var0.level(), var0, var10, var12, new AABB(var10, var12).inflate(1.0), param0x -> !param0x.isSpectator(), 0.0F
                    );
                    if (var13 == null || var13.getType() != HitResult.Type.ENTITY) {
                        return false;
                    }

                    Entity var14 = var13.getEntity();
                    if (!this.lookingAt.get().matches(var0, var14) || !var0.hasLineOfSight(var14)) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    @Override
    public EntitySubPredicate.Type type() {
        return EntitySubPredicate.Types.PLAYER;
    }

    static record AdvancementCriterionsPredicate(Object2BooleanMap<String> criterions) implements PlayerPredicate.AdvancementPredicate {
        public static final Codec<PlayerPredicate.AdvancementCriterionsPredicate> CODEC = ExtraCodecs.object2BooleanMap(Codec.STRING)
            .xmap(PlayerPredicate.AdvancementCriterionsPredicate::new, PlayerPredicate.AdvancementCriterionsPredicate::criterions);

        public boolean test(AdvancementProgress param0) {
            for(Entry<String> var0 : this.criterions.object2BooleanEntrySet()) {
                CriterionProgress var1 = param0.getCriterion(var0.getKey());
                if (var1 == null || var1.isDone() != var0.getBooleanValue()) {
                    return false;
                }
            }

            return true;
        }
    }

    static record AdvancementDonePredicate(boolean state) implements PlayerPredicate.AdvancementPredicate {
        public static final Codec<PlayerPredicate.AdvancementDonePredicate> CODEC = Codec.BOOL
            .xmap(PlayerPredicate.AdvancementDonePredicate::new, PlayerPredicate.AdvancementDonePredicate::state);

        public boolean test(AdvancementProgress param0) {
            return param0.isDone() == this.state;
        }
    }

    interface AdvancementPredicate extends Predicate<AdvancementProgress> {
        Codec<PlayerPredicate.AdvancementPredicate> CODEC = Codec.either(
                PlayerPredicate.AdvancementDonePredicate.CODEC, PlayerPredicate.AdvancementCriterionsPredicate.CODEC
            )
            .xmap(param0 -> param0.map(param0x -> param0x, param0x -> param0x), param0 -> {
                if (param0 instanceof PlayerPredicate.AdvancementDonePredicate var0) {
                    return Either.left(var0);
                } else if (param0 instanceof PlayerPredicate.AdvancementCriterionsPredicate var1) {
                    return Either.right(var1);
                } else {
                    throw new UnsupportedOperationException();
                }
            });
    }

    public static class Builder {
        private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
        private Optional<GameType> gameType = Optional.empty();
        private final ImmutableList.Builder<PlayerPredicate.StatMatcher<?>> stats = ImmutableList.builder();
        private final Object2BooleanMap<ResourceLocation> recipes = new Object2BooleanOpenHashMap<>();
        private final Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements = Maps.newHashMap();
        private Optional<EntityPredicate> lookingAt = Optional.empty();

        public static PlayerPredicate.Builder player() {
            return new PlayerPredicate.Builder();
        }

        public PlayerPredicate.Builder setLevel(MinMaxBounds.Ints param0) {
            this.level = param0;
            return this;
        }

        public <T> PlayerPredicate.Builder addStat(StatType<T> param0, Holder.Reference<T> param1, MinMaxBounds.Ints param2) {
            this.stats.add(new PlayerPredicate.StatMatcher<>(param0, param1, param2));
            return this;
        }

        public PlayerPredicate.Builder addRecipe(ResourceLocation param0, boolean param1) {
            this.recipes.put(param0, param1);
            return this;
        }

        public PlayerPredicate.Builder setGameType(GameType param0) {
            this.gameType = Optional.of(param0);
            return this;
        }

        public PlayerPredicate.Builder setLookingAt(Optional<EntityPredicate> param0) {
            this.lookingAt = param0;
            return this;
        }

        public PlayerPredicate.Builder checkAdvancementDone(ResourceLocation param0, boolean param1) {
            this.advancements.put(param0, new PlayerPredicate.AdvancementDonePredicate(param1));
            return this;
        }

        public PlayerPredicate.Builder checkAdvancementCriterions(ResourceLocation param0, Map<String, Boolean> param1) {
            this.advancements.put(param0, new PlayerPredicate.AdvancementCriterionsPredicate(new Object2BooleanOpenHashMap<>(param1)));
            return this;
        }

        public PlayerPredicate build() {
            return new PlayerPredicate(this.level, this.gameType, this.stats.build(), this.recipes, this.advancements, this.lookingAt);
        }
    }

    static record StatMatcher<T>(StatType<T> type, Holder<T> value, MinMaxBounds.Ints range, Supplier<Stat<T>> stat) {
        public static final Codec<PlayerPredicate.StatMatcher<?>> CODEC = BuiltInRegistries.STAT_TYPE
            .byNameCodec()
            .dispatch(PlayerPredicate.StatMatcher::type, PlayerPredicate.StatMatcher::createTypedCodec);

        public StatMatcher(StatType<T> param0, Holder<T> param1, MinMaxBounds.Ints param2) {
            this(param0, param1, param2, Suppliers.memoize(() -> param0.get(param1.value())));
        }

        private static <T> Codec<PlayerPredicate.StatMatcher<T>> createTypedCodec(StatType<T> param0) {
            return RecordCodecBuilder.create(
                param1 -> param1.group(
                            param0.getRegistry().holderByNameCodec().fieldOf("stat").forGetter(PlayerPredicate.StatMatcher::value),
                            ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "value", MinMaxBounds.Ints.ANY)
                                .forGetter(PlayerPredicate.StatMatcher::range)
                        )
                        .apply(param1, (param1x, param2) -> new PlayerPredicate.StatMatcher<>(param0, param1x, param2))
            );
        }

        public boolean matches(StatsCounter param0) {
            return this.range.matches(param0.getValue(this.stat.get()));
        }
    }
}
