package net.minecraft.data.advancements.packs;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ChanneledLightningTrigger;
import net.minecraft.advancements.critereon.DamagePredicate;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.DistanceTrigger;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.KilledByCrossbowTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.LightningBoltPredicate;
import net.minecraft.advancements.critereon.LightningStrikeTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.LootTableTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerHurtEntityTrigger;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.RecipeCraftedTrigger;
import net.minecraft.advancements.critereon.ShotCrossbowTrigger;
import net.minecraft.advancements.critereon.SlideDownBlockTrigger;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.advancements.critereon.SummonedEntityTrigger;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.advancements.critereon.TargetBlockTrigger;
import net.minecraft.advancements.critereon.TradeTrigger;
import net.minecraft.advancements.critereon.UsedTotemTrigger;
import net.minecraft.advancements.critereon.UsingItemTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class VanillaAdventureAdvancements implements AdvancementSubProvider {
    private static final int DISTANCE_FROM_BOTTOM_TO_TOP = 384;
    private static final int Y_COORDINATE_AT_TOP = 320;
    private static final int Y_COORDINATE_AT_BOTTOM = -64;
    private static final int BEDROCK_THICKNESS = 5;
    private static final EntityType<?>[] MOBS_TO_KILL = new EntityType[]{
        EntityType.BLAZE,
        EntityType.CAVE_SPIDER,
        EntityType.CREEPER,
        EntityType.DROWNED,
        EntityType.ELDER_GUARDIAN,
        EntityType.ENDER_DRAGON,
        EntityType.ENDERMAN,
        EntityType.ENDERMITE,
        EntityType.EVOKER,
        EntityType.GHAST,
        EntityType.GUARDIAN,
        EntityType.HOGLIN,
        EntityType.HUSK,
        EntityType.MAGMA_CUBE,
        EntityType.PHANTOM,
        EntityType.PIGLIN,
        EntityType.PIGLIN_BRUTE,
        EntityType.PILLAGER,
        EntityType.RAVAGER,
        EntityType.SHULKER,
        EntityType.SILVERFISH,
        EntityType.SKELETON,
        EntityType.SLIME,
        EntityType.SPIDER,
        EntityType.STRAY,
        EntityType.VEX,
        EntityType.VINDICATOR,
        EntityType.WITCH,
        EntityType.WITHER_SKELETON,
        EntityType.WITHER,
        EntityType.ZOGLIN,
        EntityType.ZOMBIE_VILLAGER,
        EntityType.ZOMBIE,
        EntityType.ZOMBIFIED_PIGLIN
    };

    private static Criterion<LightningStrikeTrigger.TriggerInstance> fireCountAndBystander(MinMaxBounds.Ints param0, Optional<EntityPredicate> param1) {
        return LightningStrikeTrigger.TriggerInstance.lightningStrike(
            Optional.of(
                EntityPredicate.Builder.entity()
                    .distance(DistancePredicate.absolute(MinMaxBounds.Doubles.atMost(30.0)))
                    .subPredicate(LightningBoltPredicate.blockSetOnFire(param0))
                    .build()
            ),
            param1
        );
    }

    private static Criterion<UsingItemTrigger.TriggerInstance> lookAtThroughItem(EntityType<?> param0, Item param1) {
        return UsingItemTrigger.TriggerInstance.lookingAt(
            EntityPredicate.Builder.entity().subPredicate(PlayerPredicate.Builder.player().setLookingAt(EntityPredicate.Builder.entity().of(param0)).build()),
            ItemPredicate.Builder.item().of(param1)
        );
    }

    @Override
    public void generate(HolderLookup.Provider param0, Consumer<AdvancementHolder> param1) {
        AdvancementHolder var0 = Advancement.Builder.advancement()
            .display(
                Items.MAP,
                Component.translatable("advancements.adventure.root.title"),
                Component.translatable("advancements.adventure.root.description"),
                new ResourceLocation("textures/gui/advancements/backgrounds/adventure.png"),
                FrameType.TASK,
                false,
                false,
                false
            )
            .requirements(AdvancementRequirements.Strategy.OR)
            .addCriterion("killed_something", KilledTrigger.TriggerInstance.playerKilledEntity())
            .addCriterion("killed_by_something", KilledTrigger.TriggerInstance.entityKilledPlayer())
            .save(param1, "adventure/root");
        AdvancementHolder var1 = Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Blocks.RED_BED,
                Component.translatable("advancements.adventure.sleep_in_bed.title"),
                Component.translatable("advancements.adventure.sleep_in_bed.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("slept_in_bed", PlayerTrigger.TriggerInstance.sleptInBed())
            .save(param1, "adventure/sleep_in_bed");
        createAdventuringTime(param1, var1, MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD);
        AdvancementHolder var2 = Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Items.EMERALD,
                Component.translatable("advancements.adventure.trade.title"),
                Component.translatable("advancements.adventure.trade.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("traded", TradeTrigger.TriggerInstance.tradedWithVillager())
            .save(param1, "adventure/trade");
        Advancement.Builder.advancement()
            .parent(var2)
            .display(
                Items.EMERALD,
                Component.translatable("advancements.adventure.trade_at_world_height.title"),
                Component.translatable("advancements.adventure.trade_at_world_height.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "trade_at_world_height",
                TradeTrigger.TriggerInstance.tradedWithVillager(
                    EntityPredicate.Builder.entity().located(LocationPredicate.Builder.atYLocation(MinMaxBounds.Doubles.atLeast(319.0)))
                )
            )
            .save(param1, "adventure/trade_at_world_height");
        AdvancementHolder var3 = addMobsToKill(Advancement.Builder.advancement())
            .parent(var0)
            .display(
                Items.IRON_SWORD,
                Component.translatable("advancements.adventure.kill_a_mob.title"),
                Component.translatable("advancements.adventure.kill_a_mob.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .requirements(AdvancementRequirements.Strategy.OR)
            .save(param1, "adventure/kill_a_mob");
        addMobsToKill(Advancement.Builder.advancement())
            .parent(var3)
            .display(
                Items.DIAMOND_SWORD,
                Component.translatable("advancements.adventure.kill_all_mobs.title"),
                Component.translatable("advancements.adventure.kill_all_mobs.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(100))
            .save(param1, "adventure/kill_all_mobs");
        AdvancementHolder var4 = Advancement.Builder.advancement()
            .parent(var3)
            .display(
                Items.BOW,
                Component.translatable("advancements.adventure.shoot_arrow.title"),
                Component.translatable("advancements.adventure.shoot_arrow.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "shot_arrow",
                PlayerHurtEntityTrigger.TriggerInstance.playerHurtEntityWithDamage(
                    DamagePredicate.Builder.damageInstance()
                        .type(
                            DamageSourcePredicate.Builder.damageType()
                                .tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))
                                .direct(EntityPredicate.Builder.entity().of(EntityTypeTags.ARROWS))
                        )
                )
            )
            .save(param1, "adventure/shoot_arrow");
        AdvancementHolder var5 = Advancement.Builder.advancement()
            .parent(var3)
            .display(
                Items.TRIDENT,
                Component.translatable("advancements.adventure.throw_trident.title"),
                Component.translatable("advancements.adventure.throw_trident.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "shot_trident",
                PlayerHurtEntityTrigger.TriggerInstance.playerHurtEntityWithDamage(
                    DamagePredicate.Builder.damageInstance()
                        .type(
                            DamageSourcePredicate.Builder.damageType()
                                .tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))
                                .direct(EntityPredicate.Builder.entity().of(EntityType.TRIDENT))
                        )
                )
            )
            .save(param1, "adventure/throw_trident");
        Advancement.Builder.advancement()
            .parent(var5)
            .display(
                Items.TRIDENT,
                Component.translatable("advancements.adventure.very_very_frightening.title"),
                Component.translatable("advancements.adventure.very_very_frightening.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "struck_villager", ChanneledLightningTrigger.TriggerInstance.channeledLightning(EntityPredicate.Builder.entity().of(EntityType.VILLAGER))
            )
            .save(param1, "adventure/very_very_frightening");
        Advancement.Builder.advancement()
            .parent(var2)
            .display(
                Blocks.CARVED_PUMPKIN,
                Component.translatable("advancements.adventure.summon_iron_golem.title"),
                Component.translatable("advancements.adventure.summon_iron_golem.description"),
                null,
                FrameType.GOAL,
                true,
                true,
                false
            )
            .addCriterion("summoned_golem", SummonedEntityTrigger.TriggerInstance.summonedEntity(EntityPredicate.Builder.entity().of(EntityType.IRON_GOLEM)))
            .save(param1, "adventure/summon_iron_golem");
        Advancement.Builder.advancement()
            .parent(var4)
            .display(
                Items.ARROW,
                Component.translatable("advancements.adventure.sniper_duel.title"),
                Component.translatable("advancements.adventure.sniper_duel.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(50))
            .addCriterion(
                "killed_skeleton",
                KilledTrigger.TriggerInstance.playerKilledEntity(
                    EntityPredicate.Builder.entity().of(EntityType.SKELETON).distance(DistancePredicate.horizontal(MinMaxBounds.Doubles.atLeast(50.0))),
                    DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))
                )
            )
            .save(param1, "adventure/sniper_duel");
        Advancement.Builder.advancement()
            .parent(var3)
            .display(
                Items.TOTEM_OF_UNDYING,
                Component.translatable("advancements.adventure.totem_of_undying.title"),
                Component.translatable("advancements.adventure.totem_of_undying.description"),
                null,
                FrameType.GOAL,
                true,
                true,
                false
            )
            .addCriterion("used_totem", UsedTotemTrigger.TriggerInstance.usedTotem(Items.TOTEM_OF_UNDYING))
            .save(param1, "adventure/totem_of_undying");
        AdvancementHolder var6 = Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Items.CROSSBOW,
                Component.translatable("advancements.adventure.ol_betsy.title"),
                Component.translatable("advancements.adventure.ol_betsy.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("shot_crossbow", ShotCrossbowTrigger.TriggerInstance.shotCrossbow(Items.CROSSBOW))
            .save(param1, "adventure/ol_betsy");
        Advancement.Builder.advancement()
            .parent(var6)
            .display(
                Items.CROSSBOW,
                Component.translatable("advancements.adventure.whos_the_pillager_now.title"),
                Component.translatable("advancements.adventure.whos_the_pillager_now.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("kill_pillager", KilledByCrossbowTrigger.TriggerInstance.crossbowKilled(EntityPredicate.Builder.entity().of(EntityType.PILLAGER)))
            .save(param1, "adventure/whos_the_pillager_now");
        Advancement.Builder.advancement()
            .parent(var6)
            .display(
                Items.CROSSBOW,
                Component.translatable("advancements.adventure.two_birds_one_arrow.title"),
                Component.translatable("advancements.adventure.two_birds_one_arrow.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(65))
            .addCriterion(
                "two_birds",
                KilledByCrossbowTrigger.TriggerInstance.crossbowKilled(
                    EntityPredicate.Builder.entity().of(EntityType.PHANTOM), EntityPredicate.Builder.entity().of(EntityType.PHANTOM)
                )
            )
            .save(param1, "adventure/two_birds_one_arrow");
        Advancement.Builder.advancement()
            .parent(var6)
            .display(
                Items.CROSSBOW,
                Component.translatable("advancements.adventure.arbalistic.title"),
                Component.translatable("advancements.adventure.arbalistic.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                true
            )
            .rewards(AdvancementRewards.Builder.experience(85))
            .addCriterion("arbalistic", KilledByCrossbowTrigger.TriggerInstance.crossbowKilled(MinMaxBounds.Ints.exactly(5)))
            .save(param1, "adventure/arbalistic");
        AdvancementHolder var7 = Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Raid.getLeaderBannerInstance(),
                Component.translatable("advancements.adventure.voluntary_exile.title"),
                Component.translatable("advancements.adventure.voluntary_exile.description"),
                null,
                FrameType.TASK,
                true,
                true,
                true
            )
            .addCriterion(
                "voluntary_exile",
                KilledTrigger.TriggerInstance.playerKilledEntity(
                    EntityPredicate.Builder.entity().of(EntityTypeTags.RAIDERS).equipment(EntityEquipmentPredicate.CAPTAIN)
                )
            )
            .save(param1, "adventure/voluntary_exile");
        Advancement.Builder.advancement()
            .parent(var7)
            .display(
                Raid.getLeaderBannerInstance(),
                Component.translatable("advancements.adventure.hero_of_the_village.title"),
                Component.translatable("advancements.adventure.hero_of_the_village.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                true
            )
            .rewards(AdvancementRewards.Builder.experience(100))
            .addCriterion("hero_of_the_village", PlayerTrigger.TriggerInstance.raidWon())
            .save(param1, "adventure/hero_of_the_village");
        Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Blocks.HONEY_BLOCK.asItem(),
                Component.translatable("advancements.adventure.honey_block_slide.title"),
                Component.translatable("advancements.adventure.honey_block_slide.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("honey_block_slide", SlideDownBlockTrigger.TriggerInstance.slidesDownBlock(Blocks.HONEY_BLOCK))
            .save(param1, "adventure/honey_block_slide");
        Advancement.Builder.advancement()
            .parent(var4)
            .display(
                Blocks.TARGET.asItem(),
                Component.translatable("advancements.adventure.bullseye.title"),
                Component.translatable("advancements.adventure.bullseye.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(50))
            .addCriterion(
                "bullseye",
                TargetBlockTrigger.TriggerInstance.targetHit(
                    MinMaxBounds.Ints.exactly(15),
                    Optional.of(
                        EntityPredicate.wrap(EntityPredicate.Builder.entity().distance(DistancePredicate.horizontal(MinMaxBounds.Doubles.atLeast(30.0))))
                    )
                )
            )
            .save(param1, "adventure/bullseye");
        Advancement.Builder.advancement()
            .parent(var1)
            .display(
                Items.LEATHER_BOOTS,
                Component.translatable("advancements.adventure.walk_on_powder_snow_with_leather_boots.title"),
                Component.translatable("advancements.adventure.walk_on_powder_snow_with_leather_boots.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "walk_on_powder_snow_with_leather_boots", PlayerTrigger.TriggerInstance.walkOnBlockWithEquipment(Blocks.POWDER_SNOW, Items.LEATHER_BOOTS)
            )
            .save(param1, "adventure/walk_on_powder_snow_with_leather_boots");
        Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Items.LIGHTNING_ROD,
                Component.translatable("advancements.adventure.lightning_rod_with_villager_no_fire.title"),
                Component.translatable("advancements.adventure.lightning_rod_with_villager_no_fire.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "lightning_rod_with_villager_no_fire",
                fireCountAndBystander(MinMaxBounds.Ints.exactly(0), Optional.of(EntityPredicate.Builder.entity().of(EntityType.VILLAGER).build()))
            )
            .save(param1, "adventure/lightning_rod_with_villager_no_fire");
        AdvancementHolder var8 = Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Items.SPYGLASS,
                Component.translatable("advancements.adventure.spyglass_at_parrot.title"),
                Component.translatable("advancements.adventure.spyglass_at_parrot.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("spyglass_at_parrot", lookAtThroughItem(EntityType.PARROT, Items.SPYGLASS))
            .save(param1, "adventure/spyglass_at_parrot");
        AdvancementHolder var9 = Advancement.Builder.advancement()
            .parent(var8)
            .display(
                Items.SPYGLASS,
                Component.translatable("advancements.adventure.spyglass_at_ghast.title"),
                Component.translatable("advancements.adventure.spyglass_at_ghast.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("spyglass_at_ghast", lookAtThroughItem(EntityType.GHAST, Items.SPYGLASS))
            .save(param1, "adventure/spyglass_at_ghast");
        Advancement.Builder.advancement()
            .parent(var1)
            .display(
                Items.JUKEBOX,
                Component.translatable("advancements.adventure.play_jukebox_in_meadows.title"),
                Component.translatable("advancements.adventure.play_jukebox_in_meadows.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "play_jukebox_in_meadows",
                ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                    LocationPredicate.Builder.location().setBiome(Biomes.MEADOW).setBlock(BlockPredicate.Builder.block().of(Blocks.JUKEBOX)),
                    ItemPredicate.Builder.item().of(ItemTags.MUSIC_DISCS)
                )
            )
            .save(param1, "adventure/play_jukebox_in_meadows");
        Advancement.Builder.advancement()
            .parent(var9)
            .display(
                Items.SPYGLASS,
                Component.translatable("advancements.adventure.spyglass_at_dragon.title"),
                Component.translatable("advancements.adventure.spyglass_at_dragon.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("spyglass_at_dragon", lookAtThroughItem(EntityType.ENDER_DRAGON, Items.SPYGLASS))
            .save(param1, "adventure/spyglass_at_dragon");
        Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Items.WATER_BUCKET,
                Component.translatable("advancements.adventure.fall_from_world_height.title"),
                Component.translatable("advancements.adventure.fall_from_world_height.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "fall_from_world_height",
                DistanceTrigger.TriggerInstance.fallFromHeight(
                    EntityPredicate.Builder.entity().located(LocationPredicate.Builder.atYLocation(MinMaxBounds.Doubles.atMost(-59.0))),
                    DistancePredicate.vertical(MinMaxBounds.Doubles.atLeast(379.0)),
                    LocationPredicate.Builder.atYLocation(MinMaxBounds.Doubles.atLeast(319.0))
                )
            )
            .save(param1, "adventure/fall_from_world_height");
        Advancement.Builder.advancement()
            .parent(var3)
            .display(
                Blocks.SCULK_CATALYST,
                Component.translatable("advancements.adventure.kill_mob_near_sculk_catalyst.title"),
                Component.translatable("advancements.adventure.kill_mob_near_sculk_catalyst.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                false
            )
            .addCriterion("kill_mob_near_sculk_catalyst", KilledTrigger.TriggerInstance.playerKilledEntityNearSculkCatalyst())
            .save(param1, "adventure/kill_mob_near_sculk_catalyst");
        Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Blocks.SCULK_SENSOR,
                Component.translatable("advancements.adventure.avoid_vibration.title"),
                Component.translatable("advancements.adventure.avoid_vibration.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("avoid_vibration", PlayerTrigger.TriggerInstance.avoidVibration())
            .save(param1, "adventure/avoid_vibration");
        AdvancementHolder var10 = respectingTheRemnantsCriterions(Advancement.Builder.advancement())
            .parent(var0)
            .display(
                Items.BRUSH,
                Component.translatable("advancements.adventure.salvage_sherd.title"),
                Component.translatable("advancements.adventure.salvage_sherd.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .save(param1, "adventure/salvage_sherd");
        Advancement.Builder.advancement()
            .parent(var10)
            .display(
                DecoratedPotBlockEntity.createDecoratedPotItem(
                    new DecoratedPotBlockEntity.Decorations(Items.BRICK, Items.HEART_POTTERY_SHERD, Items.BRICK, Items.EXPLORER_POTTERY_SHERD)
                ),
                Component.translatable("advancements.adventure.craft_decorated_pot_using_only_sherds.title"),
                Component.translatable("advancements.adventure.craft_decorated_pot_using_only_sherds.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "pot_crafted_using_only_sherds",
                RecipeCraftedTrigger.TriggerInstance.craftedItem(
                    new ResourceLocation("minecraft:decorated_pot"),
                    List.of(
                        ItemPredicate.Builder.item().of(ItemTags.DECORATED_POT_SHERDS),
                        ItemPredicate.Builder.item().of(ItemTags.DECORATED_POT_SHERDS),
                        ItemPredicate.Builder.item().of(ItemTags.DECORATED_POT_SHERDS),
                        ItemPredicate.Builder.item().of(ItemTags.DECORATED_POT_SHERDS)
                    )
                )
            )
            .save(param1, "adventure/craft_decorated_pot_using_only_sherds");
        AdvancementHolder var11 = craftingANewLook(Advancement.Builder.advancement())
            .parent(var0)
            .display(
                new ItemStack(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE),
                Component.translatable("advancements.adventure.trim_with_any_armor_pattern.title"),
                Component.translatable("advancements.adventure.trim_with_any_armor_pattern.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .save(param1, "adventure/trim_with_any_armor_pattern");
        smithingWithStyle(Advancement.Builder.advancement())
            .parent(var11)
            .display(
                new ItemStack(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE),
                Component.translatable("advancements.adventure.trim_with_all_exclusive_armor_patterns.title"),
                Component.translatable("advancements.adventure.trim_with_all_exclusive_armor_patterns.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(150))
            .save(param1, "adventure/trim_with_all_exclusive_armor_patterns");
        Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Items.CHISELED_BOOKSHELF,
                Component.translatable("advancements.adventure.read_power_from_chiseled_bookshelf.title"),
                Component.translatable("advancements.adventure.read_power_from_chiseled_bookshelf.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .requirements(AdvancementRequirements.Strategy.OR)
            .addCriterion("chiseled_bookshelf", placedBlockReadByComparator(Blocks.CHISELED_BOOKSHELF))
            .addCriterion("comparator", placedComparatorReadingBlock(Blocks.CHISELED_BOOKSHELF))
            .save(param1, "adventure/read_power_of_chiseled_bookshelf");
    }

    private static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlockReadByComparator(Block param0) {
        LootItemCondition.Builder[] var0 = ComparatorBlock.FACING.getPossibleValues().stream().map(param0x -> {
            StatePropertiesPredicate.Builder var0x = StatePropertiesPredicate.Builder.properties().hasProperty(ComparatorBlock.FACING, param0x);
            BlockPredicate.Builder var1x = BlockPredicate.Builder.block().of(Blocks.COMPARATOR).setProperties(var0x);
            return LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(var1x), new BlockPos(param0x.getOpposite().getNormal()));
        }).toArray(param0x -> new LootItemCondition.Builder[param0x]);
        return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
            LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0), AnyOfCondition.anyOf(var0)
        );
    }

    private static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedComparatorReadingBlock(Block param0) {
        LootItemCondition.Builder[] var0 = ComparatorBlock.FACING
            .getPossibleValues()
            .stream()
            .map(
                param1 -> {
                    StatePropertiesPredicate.Builder var0x = StatePropertiesPredicate.Builder.properties().hasProperty(ComparatorBlock.FACING, param1);
                    LootItemBlockStatePropertyCondition.Builder var1x = new LootItemBlockStatePropertyCondition.Builder(Blocks.COMPARATOR).setProperties(var0x);
                    LootItemCondition.Builder var2 = LocationCheck.checkLocation(
                        LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(param0)), new BlockPos(param1.getNormal())
                    );
                    return AllOfCondition.allOf(var1x, var2);
                }
            )
            .toArray(param0x -> new LootItemCondition.Builder[param0x]);
        return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(AnyOfCondition.anyOf(var0));
    }

    private static Advancement.Builder smithingWithStyle(Advancement.Builder param0) {
        param0.requirements(AdvancementRequirements.Strategy.AND);
        Set<Item> var0 = Set.of(
            Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE
        );
        VanillaRecipeProvider.smithingTrims()
            .filter(param1 -> var0.contains(param1.template()))
            .forEach(param1 -> param0.addCriterion("armor_trimmed_" + param1.id(), RecipeCraftedTrigger.TriggerInstance.craftedItem(param1.id())));
        return param0;
    }

    private static Advancement.Builder craftingANewLook(Advancement.Builder param0) {
        param0.requirements(AdvancementRequirements.Strategy.OR);
        VanillaRecipeProvider.smithingTrims()
            .map(VanillaRecipeProvider.TrimTemplate::id)
            .forEach(param1 -> param0.addCriterion("armor_trimmed_" + param1, RecipeCraftedTrigger.TriggerInstance.craftedItem(param1)));
        return param0;
    }

    private static Advancement.Builder respectingTheRemnantsCriterions(Advancement.Builder param0) {
        List<Pair<String, Criterion<LootTableTrigger.TriggerInstance>>> var0 = List.of(
            Pair.of("desert_pyramid", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY)),
            Pair.of("desert_well", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY)),
            Pair.of("ocean_ruin_cold", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY)),
            Pair.of("ocean_ruin_warm", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY)),
            Pair.of("trail_ruins_rare", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE)),
            Pair.of("trail_ruins_common", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON))
        );
        var0.forEach(param1 -> param0.addCriterion(param1.getFirst(), param1.getSecond()));
        String var1 = "has_sherd";
        param0.addCriterion("has_sherd", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ItemTags.DECORATED_POT_SHERDS)));
        param0.requirements(
            new AdvancementRequirements(new String[][]{var0.stream().map(Pair::getFirst).toArray(param0x -> new String[param0x]), {"has_sherd"}})
        );
        return param0;
    }

    protected static void createAdventuringTime(Consumer<AdvancementHolder> param0, AdvancementHolder param1, MultiNoiseBiomeSourceParameterList.Preset param2) {
        addBiomes(Advancement.Builder.advancement(), param2.usedBiomes().toList())
            .parent(param1)
            .display(
                Items.DIAMOND_BOOTS,
                Component.translatable("advancements.adventure.adventuring_time.title"),
                Component.translatable("advancements.adventure.adventuring_time.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(500))
            .save(param0, "adventure/adventuring_time");
    }

    private static Advancement.Builder addMobsToKill(Advancement.Builder param0) {
        for(EntityType<?> var0 : MOBS_TO_KILL) {
            param0.addCriterion(
                BuiltInRegistries.ENTITY_TYPE.getKey(var0).toString(),
                KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(var0))
            );
        }

        return param0;
    }

    protected static Advancement.Builder addBiomes(Advancement.Builder param0, List<ResourceKey<Biome>> param1) {
        for(ResourceKey<Biome> var0 : param1) {
            param0.addCriterion(var0.location().toString(), PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inBiome(var0)));
        }

        return param0;
    }
}
