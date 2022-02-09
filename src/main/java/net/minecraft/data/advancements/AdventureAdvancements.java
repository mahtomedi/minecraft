package net.minecraft.data.advancements;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ChanneledLightningTrigger;
import net.minecraft.advancements.critereon.DamagePredicate;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.DistanceTrigger;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnBlockTrigger;
import net.minecraft.advancements.critereon.KilledByCrossbowTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.LighthingBoltPredicate;
import net.minecraft.advancements.critereon.LightningStrikeTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.LocationTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerHurtEntityTrigger;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.advancements.critereon.ShotCrossbowTrigger;
import net.minecraft.advancements.critereon.SlideDownBlockTrigger;
import net.minecraft.advancements.critereon.SummonedEntityTrigger;
import net.minecraft.advancements.critereon.TargetBlockTrigger;
import net.minecraft.advancements.critereon.TradeTrigger;
import net.minecraft.advancements.critereon.UsedTotemTrigger;
import net.minecraft.advancements.critereon.UsingItemTrigger;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Blocks;

public class AdventureAdvancements implements Consumer<Consumer<Advancement>> {
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

    private static LightningStrikeTrigger.TriggerInstance fireCountAndBystander(MinMaxBounds.Ints param0, EntityPredicate param1) {
        return LightningStrikeTrigger.TriggerInstance.lighthingStrike(
            EntityPredicate.Builder.entity()
                .distance(DistancePredicate.absolute(MinMaxBounds.Doubles.atMost(30.0)))
                .lighthingBolt(LighthingBoltPredicate.blockSetOnFire(param0))
                .build(),
            param1
        );
    }

    private static UsingItemTrigger.TriggerInstance lookAtThroughItem(EntityType<?> param0, Item param1) {
        return UsingItemTrigger.TriggerInstance.lookingAt(
            EntityPredicate.Builder.entity().player(PlayerPredicate.Builder.player().setLookingAt(EntityPredicate.Builder.entity().of(param0).build()).build()),
            ItemPredicate.Builder.item().of(param1)
        );
    }

    public void accept(Consumer<Advancement> param0) {
        Advancement var0 = Advancement.Builder.advancement()
            .display(
                Items.MAP,
                new TranslatableComponent("advancements.adventure.root.title"),
                new TranslatableComponent("advancements.adventure.root.description"),
                new ResourceLocation("textures/gui/advancements/backgrounds/adventure.png"),
                FrameType.TASK,
                false,
                false,
                false
            )
            .requirements(RequirementsStrategy.OR)
            .addCriterion("killed_something", KilledTrigger.TriggerInstance.playerKilledEntity())
            .addCriterion("killed_by_something", KilledTrigger.TriggerInstance.entityKilledPlayer())
            .save(param0, "adventure/root");
        Advancement var1 = Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Blocks.RED_BED,
                new TranslatableComponent("advancements.adventure.sleep_in_bed.title"),
                new TranslatableComponent("advancements.adventure.sleep_in_bed.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("slept_in_bed", LocationTrigger.TriggerInstance.sleptInBed())
            .save(param0, "adventure/sleep_in_bed");
        addBiomes(Advancement.Builder.advancement(), this.getAllOverworldBiomes())
            .parent(var1)
            .display(
                Items.DIAMOND_BOOTS,
                new TranslatableComponent("advancements.adventure.adventuring_time.title"),
                new TranslatableComponent("advancements.adventure.adventuring_time.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(500))
            .save(param0, "adventure/adventuring_time");
        Advancement var2 = Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Items.EMERALD,
                new TranslatableComponent("advancements.adventure.trade.title"),
                new TranslatableComponent("advancements.adventure.trade.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("traded", TradeTrigger.TriggerInstance.tradedWithVillager())
            .save(param0, "adventure/trade");
        Advancement.Builder.advancement()
            .parent(var2)
            .display(
                Items.EMERALD,
                new TranslatableComponent("advancements.adventure.trade_at_world_height.title"),
                new TranslatableComponent("advancements.adventure.trade_at_world_height.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "trade_at_world_height",
                TradeTrigger.TriggerInstance.tradedWithVillager(
                    EntityPredicate.Builder.entity().located(LocationPredicate.atYLocation(MinMaxBounds.Doubles.atLeast(319.0)))
                )
            )
            .save(param0, "adventure/trade_at_world_height");
        Advancement var3 = this.addMobsToKill(Advancement.Builder.advancement())
            .parent(var0)
            .display(
                Items.IRON_SWORD,
                new TranslatableComponent("advancements.adventure.kill_a_mob.title"),
                new TranslatableComponent("advancements.adventure.kill_a_mob.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .requirements(RequirementsStrategy.OR)
            .save(param0, "adventure/kill_a_mob");
        this.addMobsToKill(Advancement.Builder.advancement())
            .parent(var3)
            .display(
                Items.DIAMOND_SWORD,
                new TranslatableComponent("advancements.adventure.kill_all_mobs.title"),
                new TranslatableComponent("advancements.adventure.kill_all_mobs.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(100))
            .save(param0, "adventure/kill_all_mobs");
        Advancement var4 = Advancement.Builder.advancement()
            .parent(var3)
            .display(
                Items.BOW,
                new TranslatableComponent("advancements.adventure.shoot_arrow.title"),
                new TranslatableComponent("advancements.adventure.shoot_arrow.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "shot_arrow",
                PlayerHurtEntityTrigger.TriggerInstance.playerHurtEntity(
                    DamagePredicate.Builder.damageInstance()
                        .type(DamageSourcePredicate.Builder.damageType().isProjectile(true).direct(EntityPredicate.Builder.entity().of(EntityTypeTags.ARROWS)))
                )
            )
            .save(param0, "adventure/shoot_arrow");
        Advancement var5 = Advancement.Builder.advancement()
            .parent(var3)
            .display(
                Items.TRIDENT,
                new TranslatableComponent("advancements.adventure.throw_trident.title"),
                new TranslatableComponent("advancements.adventure.throw_trident.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "shot_trident",
                PlayerHurtEntityTrigger.TriggerInstance.playerHurtEntity(
                    DamagePredicate.Builder.damageInstance()
                        .type(DamageSourcePredicate.Builder.damageType().isProjectile(true).direct(EntityPredicate.Builder.entity().of(EntityType.TRIDENT)))
                )
            )
            .save(param0, "adventure/throw_trident");
        Advancement.Builder.advancement()
            .parent(var5)
            .display(
                Items.TRIDENT,
                new TranslatableComponent("advancements.adventure.very_very_frightening.title"),
                new TranslatableComponent("advancements.adventure.very_very_frightening.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "struck_villager",
                ChanneledLightningTrigger.TriggerInstance.channeledLightning(EntityPredicate.Builder.entity().of(EntityType.VILLAGER).build())
            )
            .save(param0, "adventure/very_very_frightening");
        Advancement.Builder.advancement()
            .parent(var2)
            .display(
                Blocks.CARVED_PUMPKIN,
                new TranslatableComponent("advancements.adventure.summon_iron_golem.title"),
                new TranslatableComponent("advancements.adventure.summon_iron_golem.description"),
                null,
                FrameType.GOAL,
                true,
                true,
                false
            )
            .addCriterion("summoned_golem", SummonedEntityTrigger.TriggerInstance.summonedEntity(EntityPredicate.Builder.entity().of(EntityType.IRON_GOLEM)))
            .save(param0, "adventure/summon_iron_golem");
        Advancement.Builder.advancement()
            .parent(var4)
            .display(
                Items.ARROW,
                new TranslatableComponent("advancements.adventure.sniper_duel.title"),
                new TranslatableComponent("advancements.adventure.sniper_duel.description"),
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
                    DamageSourcePredicate.Builder.damageType().isProjectile(true)
                )
            )
            .save(param0, "adventure/sniper_duel");
        Advancement.Builder.advancement()
            .parent(var3)
            .display(
                Items.TOTEM_OF_UNDYING,
                new TranslatableComponent("advancements.adventure.totem_of_undying.title"),
                new TranslatableComponent("advancements.adventure.totem_of_undying.description"),
                null,
                FrameType.GOAL,
                true,
                true,
                false
            )
            .addCriterion("used_totem", UsedTotemTrigger.TriggerInstance.usedTotem(Items.TOTEM_OF_UNDYING))
            .save(param0, "adventure/totem_of_undying");
        Advancement var6 = Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Items.CROSSBOW,
                new TranslatableComponent("advancements.adventure.ol_betsy.title"),
                new TranslatableComponent("advancements.adventure.ol_betsy.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("shot_crossbow", ShotCrossbowTrigger.TriggerInstance.shotCrossbow(Items.CROSSBOW))
            .save(param0, "adventure/ol_betsy");
        Advancement.Builder.advancement()
            .parent(var6)
            .display(
                Items.CROSSBOW,
                new TranslatableComponent("advancements.adventure.whos_the_pillager_now.title"),
                new TranslatableComponent("advancements.adventure.whos_the_pillager_now.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("kill_pillager", KilledByCrossbowTrigger.TriggerInstance.crossbowKilled(EntityPredicate.Builder.entity().of(EntityType.PILLAGER)))
            .save(param0, "adventure/whos_the_pillager_now");
        Advancement.Builder.advancement()
            .parent(var6)
            .display(
                Items.CROSSBOW,
                new TranslatableComponent("advancements.adventure.two_birds_one_arrow.title"),
                new TranslatableComponent("advancements.adventure.two_birds_one_arrow.description"),
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
            .save(param0, "adventure/two_birds_one_arrow");
        Advancement.Builder.advancement()
            .parent(var6)
            .display(
                Items.CROSSBOW,
                new TranslatableComponent("advancements.adventure.arbalistic.title"),
                new TranslatableComponent("advancements.adventure.arbalistic.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                true
            )
            .rewards(AdvancementRewards.Builder.experience(85))
            .addCriterion("arbalistic", KilledByCrossbowTrigger.TriggerInstance.crossbowKilled(MinMaxBounds.Ints.exactly(5)))
            .save(param0, "adventure/arbalistic");
        Advancement var7 = Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Raid.getLeaderBannerInstance(),
                new TranslatableComponent("advancements.adventure.voluntary_exile.title"),
                new TranslatableComponent("advancements.adventure.voluntary_exile.description"),
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
            .save(param0, "adventure/voluntary_exile");
        Advancement.Builder.advancement()
            .parent(var7)
            .display(
                Raid.getLeaderBannerInstance(),
                new TranslatableComponent("advancements.adventure.hero_of_the_village.title"),
                new TranslatableComponent("advancements.adventure.hero_of_the_village.description"),
                null,
                FrameType.CHALLENGE,
                true,
                true,
                true
            )
            .rewards(AdvancementRewards.Builder.experience(100))
            .addCriterion("hero_of_the_village", LocationTrigger.TriggerInstance.raidWon())
            .save(param0, "adventure/hero_of_the_village");
        Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Blocks.HONEY_BLOCK.asItem(),
                new TranslatableComponent("advancements.adventure.honey_block_slide.title"),
                new TranslatableComponent("advancements.adventure.honey_block_slide.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("honey_block_slide", SlideDownBlockTrigger.TriggerInstance.slidesDownBlock(Blocks.HONEY_BLOCK))
            .save(param0, "adventure/honey_block_slide");
        Advancement.Builder.advancement()
            .parent(var4)
            .display(
                Blocks.TARGET.asItem(),
                new TranslatableComponent("advancements.adventure.bullseye.title"),
                new TranslatableComponent("advancements.adventure.bullseye.description"),
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
                    EntityPredicate.Composite.wrap(
                        EntityPredicate.Builder.entity().distance(DistancePredicate.horizontal(MinMaxBounds.Doubles.atLeast(30.0))).build()
                    )
                )
            )
            .save(param0, "adventure/bullseye");
        Advancement.Builder.advancement()
            .parent(var1)
            .display(
                Items.LEATHER_BOOTS,
                new TranslatableComponent("advancements.adventure.walk_on_powder_snow_with_leather_boots.title"),
                new TranslatableComponent("advancements.adventure.walk_on_powder_snow_with_leather_boots.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "walk_on_powder_snow_with_leather_boots", LocationTrigger.TriggerInstance.walkOnBlockWithEquipment(Blocks.POWDER_SNOW, Items.LEATHER_BOOTS)
            )
            .save(param0, "adventure/walk_on_powder_snow_with_leather_boots");
        Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Items.LIGHTNING_ROD,
                new TranslatableComponent("advancements.adventure.lightning_rod_with_villager_no_fire.title"),
                new TranslatableComponent("advancements.adventure.lightning_rod_with_villager_no_fire.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "lightning_rod_with_villager_no_fire",
                fireCountAndBystander(MinMaxBounds.Ints.exactly(0), EntityPredicate.Builder.entity().of(EntityType.VILLAGER).build())
            )
            .save(param0, "adventure/lightning_rod_with_villager_no_fire");
        Advancement var8 = Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Items.SPYGLASS,
                new TranslatableComponent("advancements.adventure.spyglass_at_parrot.title"),
                new TranslatableComponent("advancements.adventure.spyglass_at_parrot.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("spyglass_at_parrot", lookAtThroughItem(EntityType.PARROT, Items.SPYGLASS))
            .save(param0, "adventure/spyglass_at_parrot");
        Advancement var9 = Advancement.Builder.advancement()
            .parent(var8)
            .display(
                Items.SPYGLASS,
                new TranslatableComponent("advancements.adventure.spyglass_at_ghast.title"),
                new TranslatableComponent("advancements.adventure.spyglass_at_ghast.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("spyglass_at_ghast", lookAtThroughItem(EntityType.GHAST, Items.SPYGLASS))
            .save(param0, "adventure/spyglass_at_ghast");
        Advancement.Builder.advancement()
            .parent(var1)
            .display(
                Items.JUKEBOX,
                new TranslatableComponent("advancements.adventure.play_jukebox_in_meadows.title"),
                new TranslatableComponent("advancements.adventure.play_jukebox_in_meadows.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "play_jukebox_in_meadows",
                ItemUsedOnBlockTrigger.TriggerInstance.itemUsedOnBlock(
                    LocationPredicate.Builder.location().setBiome(Biomes.MEADOW).setBlock(BlockPredicate.Builder.block().of(Blocks.JUKEBOX).build()),
                    ItemPredicate.Builder.item().of(ItemTags.MUSIC_DISCS)
                )
            )
            .save(param0, "adventure/play_jukebox_in_meadows");
        Advancement.Builder.advancement()
            .parent(var9)
            .display(
                Items.SPYGLASS,
                new TranslatableComponent("advancements.adventure.spyglass_at_dragon.title"),
                new TranslatableComponent("advancements.adventure.spyglass_at_dragon.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion("spyglass_at_dragon", lookAtThroughItem(EntityType.ENDER_DRAGON, Items.SPYGLASS))
            .save(param0, "adventure/spyglass_at_dragon");
        Advancement.Builder.advancement()
            .parent(var0)
            .display(
                Items.WATER_BUCKET,
                new TranslatableComponent("advancements.adventure.fall_from_world_height.title"),
                new TranslatableComponent("advancements.adventure.fall_from_world_height.description"),
                null,
                FrameType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "fall_from_world_height",
                DistanceTrigger.TriggerInstance.fallFromHeight(
                    EntityPredicate.Builder.entity().located(LocationPredicate.atYLocation(MinMaxBounds.Doubles.atMost(-59.0))),
                    DistancePredicate.vertical(MinMaxBounds.Doubles.atLeast(379.0)),
                    LocationPredicate.atYLocation(MinMaxBounds.Doubles.atLeast(319.0))
                )
            )
            .save(param0, "adventure/fall_from_world_height");
    }

    private List<ResourceKey<Biome>> getAllOverworldBiomes() {
        return MultiNoiseBiomeSource.Preset.OVERWORLD
            .biomeSource(BuiltinRegistries.BIOME)
            .possibleBiomes()
            .flatMap(param0 -> param0.unwrapKey().stream())
            .collect(Collectors.toList());
    }

    private Advancement.Builder addMobsToKill(Advancement.Builder param0) {
        for(EntityType<?> var0 : MOBS_TO_KILL) {
            param0.addCriterion(
                Registry.ENTITY_TYPE.getKey(var0).toString(), KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(var0))
            );
        }

        return param0;
    }

    protected static Advancement.Builder addBiomes(Advancement.Builder param0, List<ResourceKey<Biome>> param1) {
        for(ResourceKey<Biome> var0 : param1) {
            param0.addCriterion(var0.location().toString(), LocationTrigger.TriggerInstance.located(LocationPredicate.inBiome(var0)));
        }

        return param0;
    }
}
