package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class RecipeCraftedTrigger extends SimpleCriterionTrigger<RecipeCraftedTrigger.TriggerInstance> {
    @Override
    public Codec<RecipeCraftedTrigger.TriggerInstance> codec() {
        return RecipeCraftedTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer param0, ResourceLocation param1, List<ItemStack> param2) {
        this.trigger(param0, param2x -> param2x.matches(param1, param2));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceLocation recipeId, List<ItemPredicate> ingredients)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<RecipeCraftedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(RecipeCraftedTrigger.TriggerInstance::player),
                        ResourceLocation.CODEC.fieldOf("recipe_id").forGetter(RecipeCraftedTrigger.TriggerInstance::recipeId),
                        ExtraCodecs.strictOptionalField(ItemPredicate.CODEC.listOf(), "ingredients", List.of())
                            .forGetter(RecipeCraftedTrigger.TriggerInstance::ingredients)
                    )
                    .apply(param0, RecipeCraftedTrigger.TriggerInstance::new)
        );

        public static Criterion<RecipeCraftedTrigger.TriggerInstance> craftedItem(ResourceLocation param0, List<ItemPredicate.Builder> param1) {
            return CriteriaTriggers.RECIPE_CRAFTED
                .createCriterion(new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), param0, param1.stream().map(ItemPredicate.Builder::build).toList()));
        }

        public static Criterion<RecipeCraftedTrigger.TriggerInstance> craftedItem(ResourceLocation param0) {
            return CriteriaTriggers.RECIPE_CRAFTED.createCriterion(new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), param0, List.of()));
        }

        boolean matches(ResourceLocation param0, List<ItemStack> param1) {
            if (!param0.equals(this.recipeId)) {
                return false;
            } else {
                List<ItemStack> var0 = new ArrayList<>(param1);

                for(ItemPredicate var1 : this.ingredients) {
                    boolean var2 = false;
                    Iterator<ItemStack> var3 = var0.iterator();

                    while(var3.hasNext()) {
                        if (var1.matches(var3.next())) {
                            var3.remove();
                            var2 = true;
                            break;
                        }
                    }

                    if (!var2) {
                        return false;
                    }
                }

                return true;
            }
        }
    }
}
