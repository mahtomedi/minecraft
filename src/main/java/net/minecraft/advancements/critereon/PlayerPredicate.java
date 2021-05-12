package net.minecraft.advancements.critereon;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;

public class PlayerPredicate {
    public static final PlayerPredicate ANY = new PlayerPredicate.Builder().build();
    private final MinMaxBounds.Ints level;
    @Nullable
    private final GameType gameType;
    private final Map<Stat<?>, MinMaxBounds.Ints> stats;
    private final Object2BooleanMap<ResourceLocation> recipes;
    private final Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements;

    private static PlayerPredicate.AdvancementPredicate advancementPredicateFromJson(JsonElement param0) {
        if (param0.isJsonPrimitive()) {
            boolean var0 = param0.getAsBoolean();
            return new PlayerPredicate.AdvancementDonePredicate(var0);
        } else {
            Object2BooleanMap<String> var1 = new Object2BooleanOpenHashMap<>();
            JsonObject var2 = GsonHelper.convertToJsonObject(param0, "criterion data");
            var2.entrySet().forEach(param1 -> {
                boolean var0x = GsonHelper.convertToBoolean(param1.getValue(), "criterion test");
                var1.put(param1.getKey(), var0x);
            });
            return new PlayerPredicate.AdvancementCriterionsPredicate(var1);
        }
    }

    PlayerPredicate(
        MinMaxBounds.Ints param0,
        @Nullable GameType param1,
        Map<Stat<?>, MinMaxBounds.Ints> param2,
        Object2BooleanMap<ResourceLocation> param3,
        Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> param4
    ) {
        this.level = param0;
        this.gameType = param1;
        this.stats = param2;
        this.recipes = param3;
        this.advancements = param4;
    }

    public boolean matches(Entity param0) {
        if (this == ANY) {
            return true;
        } else if (!(param0 instanceof ServerPlayer)) {
            return false;
        } else {
            ServerPlayer var0 = (ServerPlayer)param0;
            if (!this.level.matches(var0.experienceLevel)) {
                return false;
            } else if (this.gameType != null && this.gameType != var0.gameMode.getGameModeForPlayer()) {
                return false;
            } else {
                StatsCounter var1 = var0.getStats();

                for(Entry<Stat<?>, MinMaxBounds.Ints> var2 : this.stats.entrySet()) {
                    int var3 = var1.getValue(var2.getKey());
                    if (!var2.getValue().matches(var3)) {
                        return false;
                    }
                }

                RecipeBook var4 = var0.getRecipeBook();

                for(it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry<ResourceLocation> var5 : this.recipes.object2BooleanEntrySet()) {
                    if (var4.contains(var5.getKey()) != var5.getBooleanValue()) {
                        return false;
                    }
                }

                if (!this.advancements.isEmpty()) {
                    PlayerAdvancements var6 = var0.getAdvancements();
                    ServerAdvancementManager var7 = var0.getServer().getAdvancements();

                    for(Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> var8 : this.advancements.entrySet()) {
                        Advancement var9 = var7.getAdvancement(var8.getKey());
                        if (var9 == null || !var8.getValue().test(var6.getOrStartProgress(var9))) {
                            return false;
                        }
                    }
                }

                return true;
            }
        }
    }

    public static PlayerPredicate fromJson(@Nullable JsonElement param0) {
        if (param0 != null && !param0.isJsonNull()) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "player");
            MinMaxBounds.Ints var1 = MinMaxBounds.Ints.fromJson(var0.get("level"));
            String var2 = GsonHelper.getAsString(var0, "gamemode", "");
            GameType var3 = GameType.byName(var2, null);
            Map<Stat<?>, MinMaxBounds.Ints> var4 = Maps.newHashMap();
            JsonArray var5 = GsonHelper.getAsJsonArray(var0, "stats", null);
            if (var5 != null) {
                for(JsonElement var6 : var5) {
                    JsonObject var7 = GsonHelper.convertToJsonObject(var6, "stats entry");
                    ResourceLocation var8 = new ResourceLocation(GsonHelper.getAsString(var7, "type"));
                    StatType<?> var9 = Registry.STAT_TYPE.get(var8);
                    if (var9 == null) {
                        throw new JsonParseException("Invalid stat type: " + var8);
                    }

                    ResourceLocation var10 = new ResourceLocation(GsonHelper.getAsString(var7, "stat"));
                    Stat<?> var11 = getStat(var9, var10);
                    MinMaxBounds.Ints var12 = MinMaxBounds.Ints.fromJson(var7.get("value"));
                    var4.put(var11, var12);
                }
            }

            Object2BooleanMap<ResourceLocation> var13 = new Object2BooleanOpenHashMap<>();
            JsonObject var14 = GsonHelper.getAsJsonObject(var0, "recipes", new JsonObject());

            for(Entry<String, JsonElement> var15 : var14.entrySet()) {
                ResourceLocation var16 = new ResourceLocation(var15.getKey());
                boolean var17 = GsonHelper.convertToBoolean(var15.getValue(), "recipe present");
                var13.put(var16, var17);
            }

            Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> var18 = Maps.newHashMap();
            JsonObject var19 = GsonHelper.getAsJsonObject(var0, "advancements", new JsonObject());

            for(Entry<String, JsonElement> var20 : var19.entrySet()) {
                ResourceLocation var21 = new ResourceLocation(var20.getKey());
                PlayerPredicate.AdvancementPredicate var22 = advancementPredicateFromJson(var20.getValue());
                var18.put(var21, var22);
            }

            return new PlayerPredicate(var1, var3, var4, var13, var18);
        } else {
            return ANY;
        }
    }

    private static <T> Stat<T> getStat(StatType<T> param0, ResourceLocation param1) {
        Registry<T> var0 = param0.getRegistry();
        T var1 = var0.get(param1);
        if (var1 == null) {
            throw new JsonParseException("Unknown object " + param1 + " for stat type " + Registry.STAT_TYPE.getKey(param0));
        } else {
            return param0.get(var1);
        }
    }

    private static <T> ResourceLocation getStatValueId(Stat<T> param0) {
        return param0.getType().getRegistry().getKey(param0.getValue());
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            var0.add("level", this.level.serializeToJson());
            if (this.gameType != null) {
                var0.addProperty("gamemode", this.gameType.getName());
            }

            if (!this.stats.isEmpty()) {
                JsonArray var1 = new JsonArray();
                this.stats.forEach((param1, param2) -> {
                    JsonObject var0x = new JsonObject();
                    var0x.addProperty("type", Registry.STAT_TYPE.getKey(param1.getType()).toString());
                    var0x.addProperty("stat", getStatValueId(param1).toString());
                    var0x.add("value", param2.serializeToJson());
                    var1.add(var0x);
                });
                var0.add("stats", var1);
            }

            if (!this.recipes.isEmpty()) {
                JsonObject var2 = new JsonObject();
                this.recipes.forEach((param1, param2) -> var2.addProperty(param1.toString(), param2));
                var0.add("recipes", var2);
            }

            if (!this.advancements.isEmpty()) {
                JsonObject var3 = new JsonObject();
                this.advancements.forEach((param1, param2) -> var3.add(param1.toString(), param2.toJson()));
                var0.add("advancements", var3);
            }

            return var0;
        }
    }

    static class AdvancementCriterionsPredicate implements PlayerPredicate.AdvancementPredicate {
        private final Object2BooleanMap<String> criterions;

        public AdvancementCriterionsPredicate(Object2BooleanMap<String> param0) {
            this.criterions = param0;
        }

        @Override
        public JsonElement toJson() {
            JsonObject var0 = new JsonObject();
            this.criterions.forEach(var0::addProperty);
            return var0;
        }

        public boolean test(AdvancementProgress param0) {
            for(it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry<String> var0 : this.criterions.object2BooleanEntrySet()) {
                CriterionProgress var1 = param0.getCriterion(var0.getKey());
                if (var1 == null || var1.isDone() != var0.getBooleanValue()) {
                    return false;
                }
            }

            return true;
        }
    }

    static class AdvancementDonePredicate implements PlayerPredicate.AdvancementPredicate {
        private final boolean state;

        public AdvancementDonePredicate(boolean param0) {
            this.state = param0;
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(this.state);
        }

        public boolean test(AdvancementProgress param0) {
            return param0.isDone() == this.state;
        }
    }

    interface AdvancementPredicate extends Predicate<AdvancementProgress> {
        JsonElement toJson();
    }

    public static class Builder {
        private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
        @Nullable
        private GameType gameType;
        private final Map<Stat<?>, MinMaxBounds.Ints> stats = Maps.newHashMap();
        private final Object2BooleanMap<ResourceLocation> recipes = new Object2BooleanOpenHashMap<>();
        private final Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements = Maps.newHashMap();

        public static PlayerPredicate.Builder player() {
            return new PlayerPredicate.Builder();
        }

        public PlayerPredicate.Builder setLevel(MinMaxBounds.Ints param0) {
            this.level = param0;
            return this;
        }

        public PlayerPredicate.Builder addStat(Stat<?> param0, MinMaxBounds.Ints param1) {
            this.stats.put(param0, param1);
            return this;
        }

        public PlayerPredicate.Builder addRecipe(ResourceLocation param0, boolean param1) {
            this.recipes.put(param0, param1);
            return this;
        }

        public PlayerPredicate.Builder setGameType(GameType param0) {
            this.gameType = param0;
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
            return new PlayerPredicate(this.level, this.gameType, this.stats, this.recipes, this.advancements);
        }
    }
}
