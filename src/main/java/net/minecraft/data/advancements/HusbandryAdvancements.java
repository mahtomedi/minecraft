package net.minecraft.data.advancements;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.BeeNestDestroyedTrigger;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.BredAnimalsTrigger;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.FilledBucketTrigger;
import net.minecraft.advancements.critereon.FishingRodHookedTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnBlockTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlacedBlockTrigger;
import net.minecraft.advancements.critereon.TameAnimalTrigger;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;

public class HusbandryAdvancements implements Consumer<Consumer<Advancement>> {
    private static final EntityType<?>[] BREEDABLE_ANIMALS = new EntityType[]{
        EntityType.HORSE,
        EntityType.DONKEY,
        EntityType.MULE,
        EntityType.SHEEP,
        EntityType.COW,
        EntityType.MOOSHROOM,
        EntityType.PIG,
        EntityType.CHICKEN,
        EntityType.WOLF,
        EntityType.OCELOT,
        EntityType.RABBIT,
        EntityType.LLAMA,
        EntityType.CAT,
        EntityType.PANDA,
        EntityType.FOX,
        EntityType.BEE,
        EntityType.HOGLIN,
        EntityType.STRIDER
    };
    private static final Item[] FISH = new Item[]{Items.COD, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.SALMON};
    private static final Item[] FISH_BUCKETS = new Item[]{Items.COD_BUCKET, Items.TROPICAL_FISH_BUCKET, Items.PUFFERFISH_BUCKET, Items.SALMON_BUCKET};
    private static final Item[] EDIBLE_ITEMS = new Item[]{
        Items.APPLE,
        Items.MUSHROOM_STEW,
        Items.BREAD,
        Items.PORKCHOP,
        Items.COOKED_PORKCHOP,
        Items.GOLDEN_APPLE,
        Items.ENCHANTED_GOLDEN_APPLE,
        Items.COD,
        Items.SALMON,
        Items.TROPICAL_FISH,
        Items.PUFFERFISH,
        Items.COOKED_COD,
        Items.COOKED_SALMON,
        Items.COOKIE,
        Items.MELON_SLICE,
        Items.BEEF,
        Items.COOKED_BEEF,
        Items.CHICKEN,
        Items.COOKED_CHICKEN,
        Items.ROTTEN_FLESH,
        Items.SPIDER_EYE,
        Items.CARROT,
        Items.POTATO,
        Items.BAKED_POTATO,
        Items.POISONOUS_POTATO,
        Items.GOLDEN_CARROT,
        Items.PUMPKIN_PIE,
        Items.RABBIT,
        Items.COOKED_RABBIT,
        Items.RABBIT_STEW,
        Items.MUTTON,
        Items.COOKED_MUTTON,
        Items.CHORUS_FRUIT,
        Items.BEETROOT,
        Items.BEETROOT_SOUP,
        Items.DRIED_KELP,
        Items.SUSPICIOUS_STEW,
        Items.SWEET_BERRIES,
        Items.HONEY_BOTTLE,
        Items.GLOW_BERRIES
    };

    public void accept(Consumer<Advancement> param0) {
        Advancement var0 = Advancement.Builder.advancement()
            .display(
                Blocks.HAY_BLOCK,
                new TranslatableComponent("advancements.husbandry.root.title"),
                new TranslatableComponent("advancements.husbandry.root.description"),
                new ResourceLocation("textures/gui/advancements/backgrounds/husbandry.png"),
                FrameType.TASK,
                false,
                false,
                false
            )
            .addCriterion("consumed_item", ConsumeItemTrigger.TriggerInstance.usedItem())
            .save(param0, "husbandry/root");
        Advancement var1 = Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Items.WHEAT,
                new TranslatableComponent("advancements.husbandry.plant_seed.title"),
                new TranslatableComponent("advancements.husbandry.plant_seed.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .requirements(RequirementsStrategy.OR)
            .addCriterion("wheat", PlacedBlockTrigger.TriggerInstance.placedBlock(Blocks.WHEAT))
            .addCriterion("pumpkin_stem", PlacedBlockTrigger.TriggerInstance.placedBlock(Blocks.PUMPKIN_STEM))
            .addCriterion("melon_stem", PlacedBlockTrigger.TriggerInstance.placedBlock(Blocks.MELON_STEM))
            .addCriterion("beetroots", PlacedBlockTrigger.TriggerInstance.placedBlock(Blocks.BEETROOTS))
            .addCriterion("nether_wart", PlacedBlockTrigger.TriggerInstance.placedBlock(Blocks.NETHER_WART))
            .save(param0, "husbandry/plant_seed");
        Advancement var2 = Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Items.WHEAT,
                new TranslatableComponent("advancements.husbandry.breed_an_animal.title"),
                new TranslatableComponent("advancements.husbandry.breed_an_animal.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .requirements(RequirementsStrategy.OR)
            .addCriterion("bred", BredAnimalsTrigger.TriggerInstance.bredAnimals())
            .save(param0, "husbandry/breed_an_animal");
        this.addFood(Advancement.Builder.advancement())
            .parent(var1)
            .display(
                Items.APPLE,
                new TranslatableComponent("advancements.husbandry.balanced_diet.title"),
                new TranslatableComponent("advancements.husbandry.balanced_diet.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(100))
            .save(param0, "husbandry/balanced_diet");
        Advancement.Builder.advancement()
            .parent(var1)
            .display(
                Items.NETHERITE_HOE,
                new TranslatableComponent("advancements.husbandry.netherite_hoe.title"),
                new TranslatableComponent("advancements.husbandry.netherite_hoe.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(100))
            .addCriterion("netherite_hoe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HOE))
            .save(param0, "husbandry/obtain_netherite_hoe");
        Advancement var3 = Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Items.LEAD,
                new TranslatableComponent("advancements.husbandry.tame_an_animal.title"),
                new TranslatableComponent("advancements.husbandry.tame_an_animal.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("tamed_animal", TameAnimalTrigger.TriggerInstance.tamedAnimal())
            .save(param0, "husbandry/tame_an_animal");
        this.addBreedable(Advancement.Builder.advancement())
            .parent(var2)
            .display(
                Items.GOLDEN_CARROT,
                new TranslatableComponent("advancements.husbandry.breed_all_animals.title"),
                new TranslatableComponent("advancements.husbandry.breed_all_animals.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(100))
            .save(param0, "husbandry/bred_all_animals");
        Advancement var4 = this.addFish(Advancement.Builder.advancement())
            .parent(var0)
            .requirements(RequirementsStrategy.OR)
            .display(
                Items.FISHING_ROD,
                new TranslatableComponent("advancements.husbandry.fishy_business.title"),
                new TranslatableComponent("advancements.husbandry.fishy_business.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .save(param0, "husbandry/fishy_business");
        this.addFishBuckets(Advancement.Builder.advancement())
            .parent(var4)
            .requirements(RequirementsStrategy.OR)
            .display(
                Items.PUFFERFISH_BUCKET,
                new TranslatableComponent("advancements.husbandry.tactical_fishing.title"),
                new TranslatableComponent("advancements.husbandry.tactical_fishing.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .save(param0, "husbandry/tactical_fishing");
        this.addCatVariants(Advancement.Builder.advancement())
            .parent(var3)
            .display(
                Items.COD,
                new TranslatableComponent("advancements.husbandry.complete_catalogue.title"),
                new TranslatableComponent("advancements.husbandry.complete_catalogue.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(50))
            .save(param0, "husbandry/complete_catalogue");
        Advancement.Builder.advancement()
            .parent(var0)
            .addCriterion(
                "safely_harvest_honey",
                ItemUsedOnBlockTrigger.TriggerInstance.itemUsedOnBlock(
                    LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BlockTags.BEEHIVES).build()).setSmokey(true),
                    ItemPredicate.Builder.item().of(Items.GLASS_BOTTLE)
                )
            )
            .display(
                Items.HONEY_BOTTLE,
                new TranslatableComponent("advancements.husbandry.safely_harvest_honey.title"),
                new TranslatableComponent("advancements.husbandry.safely_harvest_honey.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .save(param0, "husbandry/safely_harvest_honey");
        Advancement.Builder.advancement()
            .parent(var0)
            .addCriterion(
                "silk_touch_nest",
                BeeNestDestroyedTrigger.TriggerInstance.destroyedBeeNest(
                    Blocks.BEE_NEST,
                    ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))),
                    MinMaxBounds.Ints.exactly(3)
                )
            )
            .display(
                Blocks.BEE_NEST,
                new TranslatableComponent("advancements.husbandry.silk_touch_nest.title"),
                new TranslatableComponent("advancements.husbandry.silk_touch_nest.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .save(param0, "husbandry/silk_touch_nest");
    }

    private Advancement.Builder addFood(Advancement.Builder param0) {
        for(Item var0 : EDIBLE_ITEMS) {
            param0.addCriterion(Registry.ITEM.getKey(var0).getPath(), ConsumeItemTrigger.TriggerInstance.usedItem(var0));
        }

        return param0;
    }

    private Advancement.Builder addBreedable(Advancement.Builder param0) {
        for(EntityType<?> var0 : BREEDABLE_ANIMALS) {
            param0.addCriterion(EntityType.getKey(var0).toString(), BredAnimalsTrigger.TriggerInstance.bredAnimals(EntityPredicate.Builder.entity().of(var0)));
        }

        param0.addCriterion(
            EntityType.getKey(EntityType.TURTLE).toString(),
            BredAnimalsTrigger.TriggerInstance.bredAnimals(
                EntityPredicate.Builder.entity().of(EntityType.TURTLE).build(),
                EntityPredicate.Builder.entity().of(EntityType.TURTLE).build(),
                EntityPredicate.ANY
            )
        );
        return param0;
    }

    private Advancement.Builder addFishBuckets(Advancement.Builder param0) {
        for(Item var0 : FISH_BUCKETS) {
            param0.addCriterion(
                Registry.ITEM.getKey(var0).getPath(), FilledBucketTrigger.TriggerInstance.filledBucket(ItemPredicate.Builder.item().of(var0).build())
            );
        }

        return param0;
    }

    private Advancement.Builder addFish(Advancement.Builder param0) {
        for(Item var0 : FISH) {
            param0.addCriterion(
                Registry.ITEM.getKey(var0).getPath(),
                FishingRodHookedTrigger.TriggerInstance.fishedItem(ItemPredicate.ANY, EntityPredicate.ANY, ItemPredicate.Builder.item().of(var0).build())
            );
        }

        return param0;
    }

    private Advancement.Builder addCatVariants(Advancement.Builder param0) {
        Cat.TEXTURE_BY_TYPE
            .forEach(
                (param1, param2) -> param0.addCriterion(
                        param2.getPath(), TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(param2).build())
                    )
            );
        return param0;
    }
}
