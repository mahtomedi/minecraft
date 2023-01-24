package net.minecraft.data.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public abstract class EntityLootSubProvider implements LootTableSubProvider {
    protected static final EntityPredicate.Builder ENTITY_ON_FIRE = EntityPredicate.Builder.entity()
        .flags(EntityFlagsPredicate.Builder.flags().setOnFire(true).build());
    private static final Set<EntityType<?>> SPECIAL_LOOT_TABLE_TYPES = ImmutableSet.of(
        EntityType.PLAYER, EntityType.ARMOR_STAND, EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM, EntityType.VILLAGER
    );
    private final FeatureFlagSet allowed;
    private final FeatureFlagSet required;
    private final Map<EntityType<?>, Map<ResourceLocation, LootTable.Builder>> map = Maps.newHashMap();

    protected EntityLootSubProvider(FeatureFlagSet param0) {
        this(param0, param0);
    }

    protected EntityLootSubProvider(FeatureFlagSet param0, FeatureFlagSet param1) {
        this.allowed = param0;
        this.required = param1;
    }

    protected static LootTable.Builder createSheepTable(ItemLike param0) {
        return LootTable.lootTable()
            .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(param0)))
            .withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootTableReference.lootTableReference(EntityType.SHEEP.getDefaultLootTable()))
            );
    }

    public abstract void generate();

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> param0) {
        this.generate();
        Set<ResourceLocation> var0 = Sets.newHashSet();
        BuiltInRegistries.ENTITY_TYPE
            .holders()
            .forEach(
                param2 -> {
                    EntityType<?> var0x = param2.value();
                    if (var0x.isEnabled(this.allowed)) {
                        if (canHaveLootTable(var0x)) {
                            Map<ResourceLocation, LootTable.Builder> var1 = this.map.remove(var0x);
                            ResourceLocation var2x = var0x.getDefaultLootTable();
                            if (!var2x.equals(BuiltInLootTables.EMPTY) && var0x.isEnabled(this.required) && (var1 == null || !var1.containsKey(var2x))) {
                                throw new IllegalStateException(String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", var2x, param2.key().location()));
                            }
        
                            if (var1 != null) {
                                var1.forEach(
                                    (param3, param4) -> {
                                        if (!var0.add(param3)) {
                                            throw new IllegalStateException(
                                                String.format(Locale.ROOT, "Duplicate loottable '%s' for '%s'", param3, param2.key().location())
                                            );
                                        } else {
                                            param0.accept(param3, param4);
                                        }
                                    }
                                );
                            }
                        } else {
                            Map<ResourceLocation, LootTable.Builder> var3 = this.map.remove(var0x);
                            if (var3 != null) {
                                throw new IllegalStateException(
                                    String.format(
                                        Locale.ROOT,
                                        "Weird loottables '%s' for '%s', not a LivingEntity so should not have loot",
                                        var3.keySet().stream().map(ResourceLocation::toString).collect(Collectors.joining(",")),
                                        param2.key().location()
                                    )
                                );
                            }
                        }
        
                    }
                }
            );
        if (!this.map.isEmpty()) {
            throw new IllegalStateException("Created loot tables for entities not supported by datapack: " + this.map.keySet());
        }
    }

    private static boolean canHaveLootTable(EntityType<?> param0) {
        return SPECIAL_LOOT_TABLE_TYPES.contains(param0) || param0.getCategory() != MobCategory.MISC;
    }

    protected LootItemCondition.Builder killedByFrog() {
        return DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().source(EntityPredicate.Builder.entity().of(EntityType.FROG)));
    }

    protected LootItemCondition.Builder killedByFrogVariant(FrogVariant param0) {
        return DamageSourceCondition.hasDamageSource(
            DamageSourcePredicate.Builder.damageType()
                .source(EntityPredicate.Builder.entity().of(EntityType.FROG).subPredicate(EntitySubPredicate.variant(param0)))
        );
    }

    protected void add(EntityType<?> param0, LootTable.Builder param1) {
        this.add(param0, param0.getDefaultLootTable(), param1);
    }

    protected void add(EntityType<?> param0, ResourceLocation param1, LootTable.Builder param2) {
        this.map.computeIfAbsent(param0, param0x -> new HashMap()).put(param1, param2);
    }
}
