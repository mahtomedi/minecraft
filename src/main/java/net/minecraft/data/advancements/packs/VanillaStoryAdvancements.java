package net.minecraft.data.advancements.packs;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.ChangeDimensionTrigger;
import net.minecraft.advancements.critereon.CuredZombieVillagerTrigger;
import net.minecraft.advancements.critereon.DamagePredicate;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EnchantedItemTrigger;
import net.minecraft.advancements.critereon.EntityHurtPlayerTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;

public class VanillaStoryAdvancements implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider param0, Consumer<AdvancementHolder> param1) {
        AdvancementHolder var0 = Advancement.Builder.advancement()
            .display(
                Blocks.GRASS_BLOCK,
                Component.translatable("advancements.story.root.title"),
                Component.translatable("advancements.story.root.description"),
                new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"),
                AdvancementType.TASK,
                false,
                false,
                false
            )
            .addCriterion("crafting_table", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.CRAFTING_TABLE))
            .save(param1, "story/root");
        AdvancementHolder var1 = Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Items.WOODEN_PICKAXE,
                Component.translatable("advancements.story.mine_stone.title"),
                Component.translatable("advancements.story.mine_stone.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion("get_stone", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ItemTags.STONE_TOOL_MATERIALS)))
            .save(param1, "story/mine_stone");
        AdvancementHolder var2 = Advancement.Builder.advancement()
            .parent(var1)
            .display(
                Items.STONE_PICKAXE,
                Component.translatable("advancements.story.upgrade_tools.title"),
                Component.translatable("advancements.story.upgrade_tools.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion("stone_pickaxe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE_PICKAXE))
            .save(param1, "story/upgrade_tools");
        AdvancementHolder var3 = Advancement.Builder.advancement()
            .parent(var2)
            .display(
                Items.IRON_INGOT,
                Component.translatable("advancements.story.smelt_iron.title"),
                Component.translatable("advancements.story.smelt_iron.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion("iron", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
            .save(param1, "story/smelt_iron");
        AdvancementHolder var4 = Advancement.Builder.advancement()
            .parent(var3)
            .display(
                Items.IRON_PICKAXE,
                Component.translatable("advancements.story.iron_tools.title"),
                Component.translatable("advancements.story.iron_tools.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion("iron_pickaxe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_PICKAXE))
            .save(param1, "story/iron_tools");
        AdvancementHolder var5 = Advancement.Builder.advancement()
            .parent(var4)
            .display(
                Items.DIAMOND,
                Component.translatable("advancements.story.mine_diamond.title"),
                Component.translatable("advancements.story.mine_diamond.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion("diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
            .save(param1, "story/mine_diamond");
        AdvancementHolder var6 = Advancement.Builder.advancement()
            .parent(var3)
            .display(
                Items.LAVA_BUCKET,
                Component.translatable("advancements.story.lava_bucket.title"),
                Component.translatable("advancements.story.lava_bucket.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion("lava_bucket", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LAVA_BUCKET))
            .save(param1, "story/lava_bucket");
        AdvancementHolder var7 = Advancement.Builder.advancement()
            .parent(var3)
            .display(
                Items.IRON_CHESTPLATE,
                Component.translatable("advancements.story.obtain_armor.title"),
                Component.translatable("advancements.story.obtain_armor.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .requirements(AdvancementRequirements.Strategy.OR)
            .addCriterion("iron_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_HELMET))
            .addCriterion("iron_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_CHESTPLATE))
            .addCriterion("iron_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_LEGGINGS))
            .addCriterion("iron_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_BOOTS))
            .save(param1, "story/obtain_armor");
        Advancement.Builder.advancement()
            .parent(var5)
            .display(
                Items.ENCHANTED_BOOK,
                Component.translatable("advancements.story.enchant_item.title"),
                Component.translatable("advancements.story.enchant_item.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion("enchanted_item", EnchantedItemTrigger.TriggerInstance.enchantedItem())
            .save(param1, "story/enchant_item");
        AdvancementHolder var8 = Advancement.Builder.advancement()
            .parent(var6)
            .display(
                Blocks.OBSIDIAN,
                Component.translatable("advancements.story.form_obsidian.title"),
                Component.translatable("advancements.story.form_obsidian.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion("obsidian", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.OBSIDIAN))
            .save(param1, "story/form_obsidian");
        Advancement.Builder.advancement()
            .parent(var7)
            .display(
                Items.SHIELD,
                Component.translatable("advancements.story.deflect_arrow.title"),
                Component.translatable("advancements.story.deflect_arrow.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "deflected_projectile",
                EntityHurtPlayerTrigger.TriggerInstance.entityHurtPlayer(
                    DamagePredicate.Builder.damageInstance()
                        .type(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE)))
                        .blocked(true)
                )
            )
            .save(param1, "story/deflect_arrow");
        Advancement.Builder.advancement()
            .parent(var5)
            .display(
                Items.DIAMOND_CHESTPLATE,
                Component.translatable("advancements.story.shiny_gear.title"),
                Component.translatable("advancements.story.shiny_gear.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .requirements(AdvancementRequirements.Strategy.OR)
            .addCriterion("diamond_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_HELMET))
            .addCriterion("diamond_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_CHESTPLATE))
            .addCriterion("diamond_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_LEGGINGS))
            .addCriterion("diamond_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_BOOTS))
            .save(param1, "story/shiny_gear");
        AdvancementHolder var9 = Advancement.Builder.advancement()
            .parent(var8)
            .display(
                Items.FLINT_AND_STEEL,
                Component.translatable("advancements.story.enter_the_nether.title"),
                Component.translatable("advancements.story.enter_the_nether.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion("entered_nether", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(Level.NETHER))
            .save(param1, "story/enter_the_nether");
        Advancement.Builder.advancement()
            .parent(var9)
            .display(
                Items.GOLDEN_APPLE,
                Component.translatable("advancements.story.cure_zombie_villager.title"),
                Component.translatable("advancements.story.cure_zombie_villager.description"),
                null,
                AdvancementType.GOAL,
                true,
                true,
                false
            )
            .addCriterion("cured_zombie", CuredZombieVillagerTrigger.TriggerInstance.curedZombieVillager())
            .save(param1, "story/cure_zombie_villager");
        AdvancementHolder var10 = Advancement.Builder.advancement()
            .parent(var9)
            .display(
                Items.ENDER_EYE,
                Component.translatable("advancements.story.follow_ender_eye.title"),
                Component.translatable("advancements.story.follow_ender_eye.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion("in_stronghold", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inStructure(BuiltinStructures.STRONGHOLD)))
            .save(param1, "story/follow_ender_eye");
        Advancement.Builder.advancement()
            .parent(var10)
            .display(
                Blocks.END_STONE,
                Component.translatable("advancements.story.enter_the_end.title"),
                Component.translatable("advancements.story.enter_the_end.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion("entered_end", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(Level.END))
            .save(param1, "story/enter_the_end");
    }
}
