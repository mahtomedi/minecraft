package net.minecraft.advancements;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class AdvancementRewards {
    public static final AdvancementRewards EMPTY = new AdvancementRewards(
        0, new ResourceLocation[0], new ResourceLocation[0], CommandFunction.CacheableFunction.NONE
    );
    private final int experience;
    private final ResourceLocation[] loot;
    private final ResourceLocation[] recipes;
    private final CommandFunction.CacheableFunction function;

    public AdvancementRewards(int param0, ResourceLocation[] param1, ResourceLocation[] param2, CommandFunction.CacheableFunction param3) {
        this.experience = param0;
        this.loot = param1;
        this.recipes = param2;
        this.function = param3;
    }

    public ResourceLocation[] getRecipes() {
        return this.recipes;
    }

    public void grant(ServerPlayer param0) {
        param0.giveExperiencePoints(this.experience);
        LootContext var0 = new LootContext.Builder(param0.getLevel())
            .withParameter(LootContextParams.THIS_ENTITY, param0)
            .withParameter(LootContextParams.ORIGIN, param0.position())
            .withRandom(param0.getRandom())
            .create(LootContextParamSets.ADVANCEMENT_REWARD);
        boolean var1 = false;

        for(ResourceLocation var2 : this.loot) {
            for(ItemStack var3 : param0.server.getLootTables().get(var2).getRandomItems(var0)) {
                if (param0.addItem(var3)) {
                    param0.level
                        .playSound(
                            null,
                            param0.getX(),
                            param0.getY(),
                            param0.getZ(),
                            SoundEvents.ITEM_PICKUP,
                            SoundSource.PLAYERS,
                            0.2F,
                            ((param0.getRandom().nextFloat() - param0.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
                        );
                    var1 = true;
                } else {
                    param0.drop(var3, false);
                }
            }
        }

        if (var1) {
            param0.containerMenu.broadcastChanges();
        }

        if (this.recipes.length > 0) {
            param0.awardRecipesByKey(this.recipes);
        }

        MinecraftServer var4 = param0.server;
        this.function
            .get(var4.getFunctions())
            .ifPresent(param2 -> var4.getFunctions().execute(param2, param0.createCommandSourceStack().withSuppressedOutput().withPermission(2)));
    }

    @Override
    public String toString() {
        return "AdvancementRewards{experience="
            + this.experience
            + ", loot="
            + Arrays.toString((Object[])this.loot)
            + ", recipes="
            + Arrays.toString((Object[])this.recipes)
            + ", function="
            + this.function
            + "}";
    }

    public JsonElement serializeToJson() {
        if (this == EMPTY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            if (this.experience != 0) {
                var0.addProperty("experience", this.experience);
            }

            if (this.loot.length > 0) {
                JsonArray var1 = new JsonArray();

                for(ResourceLocation var2 : this.loot) {
                    var1.add(var2.toString());
                }

                var0.add("loot", var1);
            }

            if (this.recipes.length > 0) {
                JsonArray var3 = new JsonArray();

                for(ResourceLocation var4 : this.recipes) {
                    var3.add(var4.toString());
                }

                var0.add("recipes", var3);
            }

            if (this.function.getId() != null) {
                var0.addProperty("function", this.function.getId().toString());
            }

            return var0;
        }
    }

    public static AdvancementRewards deserialize(JsonObject param0) throws JsonParseException {
        int var0 = GsonHelper.getAsInt(param0, "experience", 0);
        JsonArray var1 = GsonHelper.getAsJsonArray(param0, "loot", new JsonArray());
        ResourceLocation[] var2 = new ResourceLocation[var1.size()];

        for(int var3 = 0; var3 < var2.length; ++var3) {
            var2[var3] = new ResourceLocation(GsonHelper.convertToString(var1.get(var3), "loot[" + var3 + "]"));
        }

        JsonArray var4 = GsonHelper.getAsJsonArray(param0, "recipes", new JsonArray());
        ResourceLocation[] var5 = new ResourceLocation[var4.size()];

        for(int var6 = 0; var6 < var5.length; ++var6) {
            var5[var6] = new ResourceLocation(GsonHelper.convertToString(var4.get(var6), "recipes[" + var6 + "]"));
        }

        CommandFunction.CacheableFunction var7;
        if (param0.has("function")) {
            var7 = new CommandFunction.CacheableFunction(new ResourceLocation(GsonHelper.getAsString(param0, "function")));
        } else {
            var7 = CommandFunction.CacheableFunction.NONE;
        }

        return new AdvancementRewards(var0, var2, var5, var7);
    }

    public static class Builder {
        private int experience;
        private final List<ResourceLocation> loot = Lists.newArrayList();
        private final List<ResourceLocation> recipes = Lists.newArrayList();
        @Nullable
        private ResourceLocation function;

        public static AdvancementRewards.Builder experience(int param0) {
            return new AdvancementRewards.Builder().addExperience(param0);
        }

        public AdvancementRewards.Builder addExperience(int param0) {
            this.experience += param0;
            return this;
        }

        public static AdvancementRewards.Builder loot(ResourceLocation param0) {
            return new AdvancementRewards.Builder().addLootTable(param0);
        }

        public AdvancementRewards.Builder addLootTable(ResourceLocation param0) {
            this.loot.add(param0);
            return this;
        }

        public static AdvancementRewards.Builder recipe(ResourceLocation param0) {
            return new AdvancementRewards.Builder().addRecipe(param0);
        }

        public AdvancementRewards.Builder addRecipe(ResourceLocation param0) {
            this.recipes.add(param0);
            return this;
        }

        public static AdvancementRewards.Builder function(ResourceLocation param0) {
            return new AdvancementRewards.Builder().runs(param0);
        }

        public AdvancementRewards.Builder runs(ResourceLocation param0) {
            this.function = param0;
            return this;
        }

        public AdvancementRewards build() {
            return new AdvancementRewards(
                this.experience,
                this.loot.toArray(new ResourceLocation[0]),
                this.recipes.toArray(new ResourceLocation[0]),
                this.function == null ? CommandFunction.CacheableFunction.NONE : new CommandFunction.CacheableFunction(this.function)
            );
        }
    }
}
