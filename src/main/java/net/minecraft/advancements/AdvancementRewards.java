package net.minecraft.advancements;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CacheableFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record AdvancementRewards(int experience, List<ResourceLocation> loot, List<ResourceLocation> recipes, Optional<CacheableFunction> function) {
    public static final Codec<AdvancementRewards> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(Codec.INT, "experience", 0).forGetter(AdvancementRewards::experience),
                    ExtraCodecs.strictOptionalField(ResourceLocation.CODEC.listOf(), "loot", List.of()).forGetter(AdvancementRewards::loot),
                    ExtraCodecs.strictOptionalField(ResourceLocation.CODEC.listOf(), "recipes", List.of()).forGetter(AdvancementRewards::recipes),
                    ExtraCodecs.strictOptionalField(CacheableFunction.CODEC, "function").forGetter(AdvancementRewards::function)
                )
                .apply(param0, AdvancementRewards::new)
    );
    public static final AdvancementRewards EMPTY = new AdvancementRewards(0, List.of(), List.of(), Optional.empty());

    public void grant(ServerPlayer param0) {
        param0.giveExperiencePoints(this.experience);
        LootParams var0 = new LootParams.Builder(param0.serverLevel())
            .withParameter(LootContextParams.THIS_ENTITY, param0)
            .withParameter(LootContextParams.ORIGIN, param0.position())
            .create(LootContextParamSets.ADVANCEMENT_REWARD);
        boolean var1 = false;

        for(ResourceLocation var2 : this.loot) {
            for(ItemStack var3 : param0.server.getLootData().getLootTable(var2).getRandomItems(var0)) {
                if (param0.addItem(var3)) {
                    param0.level()
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
                    ItemEntity var4 = param0.drop(var3, false);
                    if (var4 != null) {
                        var4.setNoPickUpDelay();
                        var4.setTarget(param0.getUUID());
                    }
                }
            }
        }

        if (var1) {
            param0.containerMenu.broadcastChanges();
        }

        if (!this.recipes.isEmpty()) {
            param0.awardRecipesByKey(this.recipes);
        }

        MinecraftServer var5 = param0.server;
        this.function
            .flatMap(param1 -> param1.get(var5.getFunctions()))
            .ifPresent(param2 -> var5.getFunctions().execute(param2, param0.createCommandSourceStack().withSuppressedOutput().withPermission(2)));
    }

    public static class Builder {
        private int experience;
        private final ImmutableList.Builder<ResourceLocation> loot = ImmutableList.builder();
        private final ImmutableList.Builder<ResourceLocation> recipes = ImmutableList.builder();
        private Optional<ResourceLocation> function = Optional.empty();

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
            this.function = Optional.of(param0);
            return this;
        }

        public AdvancementRewards build() {
            return new AdvancementRewards(this.experience, this.loot.build(), this.recipes.build(), this.function.map(CacheableFunction::new));
        }
    }
}
