package net.minecraft.data.advancements;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;

public class StoryAdvancements implements Consumer<Consumer<Advancement>> {
    public void accept(Consumer<Advancement> param0) {
        Advancement var0 = Advancement.Builder.advancement()
            .display(
                Blocks.GRASS_BLOCK,
                new TranslatableComponent("advancements.story.root.title"),
                new TranslatableComponent("advancements.story.root.description"),
                new ResourceLocation("textures/gui/advancements/backgrounds/stone.png"),
                FrameType.TASK,
                false,
                false,
                false
            )
            .addCriterion("crafting_table", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.CRAFTING_TABLE))
            .save(param0, "story/root");
        Advancement var1 = Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Items.WOODEN_PICKAXE,
                new TranslatableComponent("advancements.story.mine_stone.title"),
                new TranslatableComponent("advancements.story.mine_stone.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("get_stone", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ItemTags.STONE_TOOL_MATERIALS).build()))
            .save(param0, "story/mine_stone");
        Advancement var2 = Advancement.Builder.advancement()
            .parent(var1)
            .display(
                Items.STONE_PICKAXE,
                new TranslatableComponent("advancements.story.upgrade_tools.title"),
                new TranslatableComponent("advancements.story.upgrade_tools.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("stone_pickaxe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE_PICKAXE))
            .save(param0, "story/upgrade_tools");
        Advancement var3 = Advancement.Builder.advancement()
            .parent(var2)
            .display(
                Items.IRON_INGOT,
                new TranslatableComponent("advancements.story.smelt_iron.title"),
                new TranslatableComponent("advancements.story.smelt_iron.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("iron", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
            .save(param0, "story/smelt_iron");
        Advancement var4 = Advancement.Builder.advancement()
            .parent(var3)
            .display(
                Items.IRON_PICKAXE,
                new TranslatableComponent("advancements.story.iron_tools.title"),
                new TranslatableComponent("advancements.story.iron_tools.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("iron_pickaxe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_PICKAXE))
            .save(param0, "story/iron_tools");
        Advancement var5 = Advancement.Builder.advancement()
            .parent(var4)
            .display(
                Items.DIAMOND,
                new TranslatableComponent("advancements.story.mine_diamond.title"),
                new TranslatableComponent("advancements.story.mine_diamond.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
            .save(param0, "story/mine_diamond");
        Advancement var6 = Advancement.Builder.advancement()
            .parent(var3)
            .display(
                Items.LAVA_BUCKET,
                new TranslatableComponent("advancements.story.lava_bucket.title"),
                new TranslatableComponent("advancements.story.lava_bucket.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("lava_bucket", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LAVA_BUCKET))
            .save(param0, "story/lava_bucket");
        Advancement var7 = Advancement.Builder.advancement()
            .parent(var3)
            .display(
                Items.IRON_CHESTPLATE,
                new TranslatableComponent("advancements.story.obtain_armor.title"),
                new TranslatableComponent("advancements.story.obtain_armor.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .requirements(RequirementsStrategy.OR)
            .addCriterion("iron_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_HELMET))
            .addCriterion("iron_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_CHESTPLATE))
            .addCriterion("iron_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_LEGGINGS))
            .addCriterion("iron_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_BOOTS))
            .save(param0, "story/obtain_armor");
        Advancement.Builder.advancement()
            .parent(var5)
            .display(
                Items.ENCHANTED_BOOK,
                new TranslatableComponent("advancements.story.enchant_item.title"),
                new TranslatableComponent("advancements.story.enchant_item.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("enchanted_item", EnchantedItemTrigger.TriggerInstance.enchantedItem())
            .save(param0, "story/enchant_item");
        Advancement var8 = Advancement.Builder.advancement()
            .parent(var6)
            .display(
                Blocks.OBSIDIAN,
                new TranslatableComponent("advancements.story.form_obsidian.title"),
                new TranslatableComponent("advancements.story.form_obsidian.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("obsidian", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.OBSIDIAN))
            .save(param0, "story/form_obsidian");
        Advancement.Builder.advancement()
            .parent(var7)
            .display(
                Items.SHIELD,
                new TranslatableComponent("advancements.story.deflect_arrow.title"),
                new TranslatableComponent("advancements.story.deflect_arrow.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "deflected_projectile",
                EntityHurtPlayerTrigger.TriggerInstance.entityHurtPlayer(
                    DamagePredicate.Builder.damageInstance().type(DamageSourcePredicate.Builder.damageType().isProjectile(true)).blocked(true)
                )
            )
            .save(param0, "story/deflect_arrow");
        Advancement.Builder.advancement()
            .parent(var5)
            .display(
                Items.DIAMOND_CHESTPLATE,
                new TranslatableComponent("advancements.story.shiny_gear.title"),
                new TranslatableComponent("advancements.story.shiny_gear.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .requirements(RequirementsStrategy.OR)
            .addCriterion("diamond_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_HELMET))
            .addCriterion("diamond_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_CHESTPLATE))
            .addCriterion("diamond_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_LEGGINGS))
            .addCriterion("diamond_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_BOOTS))
            .save(param0, "story/shiny_gear");
        Advancement var9 = Advancement.Builder.advancement()
            .parent(var8)
            .display(
                Items.FLINT_AND_STEEL,
                new TranslatableComponent("advancements.story.enter_the_nether.title"),
                new TranslatableComponent("advancements.story.enter_the_nether.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("entered_nether", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(Level.NETHER))
            .save(param0, "story/enter_the_nether");
        Advancement.Builder.advancement()
            .parent(var9)
            .display(
                Items.GOLDEN_APPLE,
                new TranslatableComponent("advancements.story.cure_zombie_villager.title"),
                new TranslatableComponent("advancements.story.cure_zombie_villager.description"),
                null,
                FrameType.GOAL,
                true,
                true,
                false
            )
            .addCriterion("cured_zombie", CuredZombieVillagerTrigger.TriggerInstance.curedZombieVillager())
            .save(param0, "story/cure_zombie_villager");
        Advancement var10 = Advancement.Builder.advancement()
            .parent(var9)
            .display(
                Items.ENDER_EYE,
                new TranslatableComponent("advancements.story.follow_ender_eye.title"),
                new TranslatableComponent("advancements.story.follow_ender_eye.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("in_stronghold", PlayerTrigger.TriggerInstance.located(LocationPredicate.inStructure(BuiltinStructures.STRONGHOLD)))
            .save(param0, "story/follow_ender_eye");
        Advancement.Builder.advancement()
            .parent(var10)
            .display(
                Blocks.END_STONE,
                new TranslatableComponent("advancements.story.enter_the_end.title"),
                new TranslatableComponent("advancements.story.enter_the_end.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("entered_end", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(Level.END))
            .save(param0, "story/enter_the_end");
    }
}
