package net.minecraft.data.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.PotatoBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.ConstantIntValue;
import net.minecraft.world.level.storage.loot.IntLimiter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.RandomIntGenerator;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

public class BlockLoot implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
    private static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(
        ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1)))
    );
    private static final LootItemCondition.Builder HAS_NO_SILK_TOUCH = HAS_SILK_TOUCH.invert();
    private static final LootItemCondition.Builder HAS_SHEARS = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS));
    private static final LootItemCondition.Builder HAS_SHEARS_OR_SILK_TOUCH = HAS_SHEARS.or(HAS_SILK_TOUCH);
    private static final LootItemCondition.Builder HAS_NO_SHEARS_OR_SILK_TOUCH = HAS_SHEARS_OR_SILK_TOUCH.invert();
    private static final Set<Item> EXPLOSION_RESISTANT = Stream.of(
            Blocks.DRAGON_EGG,
            Blocks.BEACON,
            Blocks.CONDUIT,
            Blocks.SKELETON_SKULL,
            Blocks.WITHER_SKELETON_SKULL,
            Blocks.PLAYER_HEAD,
            Blocks.ZOMBIE_HEAD,
            Blocks.CREEPER_HEAD,
            Blocks.DRAGON_HEAD,
            Blocks.SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX,
            Blocks.LIGHT_GRAY_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX,
            Blocks.ORANGE_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.PURPLE_SHULKER_BOX,
            Blocks.RED_SHULKER_BOX,
            Blocks.WHITE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX
        )
        .map(ItemLike::asItem)
        .collect(ImmutableSet.toImmutableSet());
    private static final float[] NORMAL_LEAVES_SAPLING_CHANCES = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};
    private static final float[] JUNGLE_LEAVES_SAPLING_CHANGES = new float[]{0.025F, 0.027777778F, 0.03125F, 0.041666668F, 0.1F};
    private final Map<ResourceLocation, LootTable.Builder> map = Maps.newHashMap();

    private static <T> T applyExplosionDecay(ItemLike param0, FunctionUserBuilder<T> param1) {
        return (T)(!EXPLOSION_RESISTANT.contains(param0.asItem()) ? param1.apply(ApplyExplosionDecay.explosionDecay()) : param1.unwrap());
    }

    private static <T> T applyExplosionCondition(ItemLike param0, ConditionUserBuilder<T> param1) {
        return (T)(!EXPLOSION_RESISTANT.contains(param0.asItem()) ? param1.when(ExplosionCondition.survivesExplosion()) : param1.unwrap());
    }

    private static LootTable.Builder createSingleItemTable(ItemLike param0) {
        return LootTable.lootTable()
            .withPool(applyExplosionCondition(param0, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(param0))));
    }

    private static LootTable.Builder createSelfDropDispatchTable(Block param0, LootItemCondition.Builder param1, LootPoolEntryContainer.Builder<?> param2) {
        return LootTable.lootTable()
            .withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(param0).when(param1).otherwise(param2)));
    }

    private static LootTable.Builder createSilkTouchDispatchTable(Block param0, LootPoolEntryContainer.Builder<?> param1) {
        return createSelfDropDispatchTable(param0, HAS_SILK_TOUCH, param1);
    }

    private static LootTable.Builder createShearsDispatchTable(Block param0, LootPoolEntryContainer.Builder<?> param1) {
        return createSelfDropDispatchTable(param0, HAS_SHEARS, param1);
    }

    private static LootTable.Builder createSilkTouchOrShearsDispatchTable(Block param0, LootPoolEntryContainer.Builder<?> param1) {
        return createSelfDropDispatchTable(param0, HAS_SHEARS_OR_SILK_TOUCH, param1);
    }

    private static LootTable.Builder createSingleItemTableWithSilkTouch(Block param0, ItemLike param1) {
        return createSilkTouchDispatchTable(param0, applyExplosionCondition(param0, LootItem.lootTableItem(param1)));
    }

    private static LootTable.Builder createSingleItemTable(ItemLike param0, RandomIntGenerator param1) {
        return LootTable.lootTable()
            .withPool(
                LootPool.lootPool()
                    .setRolls(ConstantIntValue.exactly(1))
                    .add(applyExplosionDecay(param0, LootItem.lootTableItem(param0).apply(SetItemCountFunction.setCount(param1))))
            );
    }

    private static LootTable.Builder createSingleItemTableWithSilkTouch(Block param0, ItemLike param1, RandomIntGenerator param2) {
        return createSilkTouchDispatchTable(param0, applyExplosionDecay(param0, LootItem.lootTableItem(param1).apply(SetItemCountFunction.setCount(param2))));
    }

    private static LootTable.Builder createSilkTouchOnlyTable(ItemLike param0) {
        return LootTable.lootTable()
            .withPool(LootPool.lootPool().when(HAS_SILK_TOUCH).setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(param0)));
    }

    private static LootTable.Builder createPotFlowerItemTable(ItemLike param0) {
        return LootTable.lootTable()
            .withPool(
                applyExplosionCondition(
                    Blocks.FLOWER_POT, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(Blocks.FLOWER_POT))
                )
            )
            .withPool(applyExplosionCondition(param0, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(param0))));
    }

    private static LootTable.Builder createSlabItemTable(Block param0) {
        return LootTable.lootTable()
            .withPool(
                LootPool.lootPool()
                    .setRolls(ConstantIntValue.exactly(1))
                    .add(
                        applyExplosionDecay(
                            param0,
                            LootItem.lootTableItem(param0)
                                .apply(
                                    SetItemCountFunction.setCount(ConstantIntValue.exactly(2))
                                        .when(
                                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE))
                                        )
                                )
                        )
                    )
            );
    }

    private static <T extends Comparable<T> & StringRepresentable> LootTable.Builder createSinglePropConditionTable(Block param0, Property<T> param1, T param2) {
        return LootTable.lootTable()
            .withPool(
                applyExplosionCondition(
                    param0,
                    LootPool.lootPool()
                        .setRolls(ConstantIntValue.exactly(1))
                        .add(
                            LootItem.lootTableItem(param0)
                                .when(
                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(param1, param2))
                                )
                        )
                )
            );
    }

    private static LootTable.Builder createNameableBlockEntityTable(Block param0) {
        return LootTable.lootTable()
            .withPool(
                applyExplosionCondition(
                    param0,
                    LootPool.lootPool()
                        .setRolls(ConstantIntValue.exactly(1))
                        .add(LootItem.lootTableItem(param0).apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY)))
                )
            );
    }

    private static LootTable.Builder createShulkerBoxDrop(Block param0) {
        return LootTable.lootTable()
            .withPool(
                applyExplosionCondition(
                    param0,
                    LootPool.lootPool()
                        .setRolls(ConstantIntValue.exactly(1))
                        .add(
                            LootItem.lootTableItem(param0)
                                .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                                .apply(
                                    CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY)
                                        .copy("Lock", "BlockEntityTag.Lock")
                                        .copy("LootTable", "BlockEntityTag.LootTable")
                                        .copy("LootTableSeed", "BlockEntityTag.LootTableSeed")
                                )
                                .apply(SetContainerContents.setContents().withEntry(DynamicLoot.dynamicEntry(ShulkerBoxBlock.CONTENTS)))
                        )
                )
            );
    }

    private static LootTable.Builder createBannerDrop(Block param0) {
        return LootTable.lootTable()
            .withPool(
                applyExplosionCondition(
                    param0,
                    LootPool.lootPool()
                        .setRolls(ConstantIntValue.exactly(1))
                        .add(
                            LootItem.lootTableItem(param0)
                                .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                                .apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("Patterns", "BlockEntityTag.Patterns"))
                        )
                )
            );
    }

    private static LootTable.Builder createBeeNestDrop(Block param0) {
        return LootTable.lootTable()
            .withPool(
                LootPool.lootPool()
                    .when(HAS_SILK_TOUCH)
                    .setRolls(ConstantIntValue.exactly(1))
                    .add(
                        LootItem.lootTableItem(param0)
                            .apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("Bees", "BlockEntityTag.Bees"))
                            .apply(CopyBlockState.copyState(param0).copy(BeehiveBlock.HONEY_LEVEL))
                    )
            );
    }

    private static LootTable.Builder createBeeHiveDrop(Block param0) {
        return LootTable.lootTable()
            .withPool(
                LootPool.lootPool()
                    .setRolls(ConstantIntValue.exactly(1))
                    .add(
                        LootItem.lootTableItem(param0)
                            .when(HAS_SILK_TOUCH)
                            .apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("Bees", "BlockEntityTag.Bees"))
                            .apply(CopyBlockState.copyState(param0).copy(BeehiveBlock.HONEY_LEVEL))
                            .otherwise(LootItem.lootTableItem(param0))
                    )
            );
    }

    private static LootTable.Builder createOreDrop(Block param0, Item param1) {
        return createSilkTouchDispatchTable(
            param0, applyExplosionDecay(param0, LootItem.lootTableItem(param1).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)))
        );
    }

    private static LootTable.Builder createMushroomBlockDrop(Block param0, ItemLike param1) {
        return createSilkTouchDispatchTable(
            param0,
            applyExplosionDecay(
                param0,
                LootItem.lootTableItem(param1)
                    .apply(SetItemCountFunction.setCount(RandomValueBounds.between(-6.0F, 2.0F)))
                    .apply(LimitCount.limitCount(IntLimiter.lowerBound(0)))
            )
        );
    }

    private static LootTable.Builder createGrassDrops(Block param0) {
        return createShearsDispatchTable(
            param0,
            applyExplosionDecay(
                param0,
                LootItem.lootTableItem(Items.WHEAT_SEEDS)
                    .when(LootItemRandomChanceCondition.randomChance(0.125F))
                    .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, 2))
            )
        );
    }

    private static LootTable.Builder createStemDrops(Block param0, Item param1) {
        return LootTable.lootTable()
            .withPool(
                applyExplosionDecay(
                    param0,
                    LootPool.lootPool()
                        .setRolls(ConstantIntValue.exactly(1))
                        .add(
                            LootItem.lootTableItem(param1)
                                .apply(
                                    SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.06666667F))
                                        .when(
                                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 0))
                                        )
                                )
                                .apply(
                                    SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.13333334F))
                                        .when(
                                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 1))
                                        )
                                )
                                .apply(
                                    SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.2F))
                                        .when(
                                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 2))
                                        )
                                )
                                .apply(
                                    SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.26666668F))
                                        .when(
                                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 3))
                                        )
                                )
                                .apply(
                                    SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.33333334F))
                                        .when(
                                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 4))
                                        )
                                )
                                .apply(
                                    SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.4F))
                                        .when(
                                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 5))
                                        )
                                )
                                .apply(
                                    SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.46666667F))
                                        .when(
                                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 6))
                                        )
                                )
                                .apply(
                                    SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.53333336F))
                                        .when(
                                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 7))
                                        )
                                )
                        )
                )
            );
    }

    private static LootTable.Builder createAttachedStemDrops(Block param0, Item param1) {
        return LootTable.lootTable()
            .withPool(
                applyExplosionDecay(
                    param0,
                    LootPool.lootPool()
                        .setRolls(ConstantIntValue.exactly(1))
                        .add(LootItem.lootTableItem(param1).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.53333336F))))
                )
            );
    }

    private static LootTable.Builder createShearsOnlyDrop(ItemLike param0) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).when(HAS_SHEARS).add(LootItem.lootTableItem(param0)));
    }

    private static LootTable.Builder createLeavesDrops(Block param0, Block param1, float... param2) {
        return createSilkTouchOrShearsDispatchTable(
                param0,
                applyExplosionCondition(param0, LootItem.lootTableItem(param1))
                    .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, param2))
            )
            .withPool(
                LootPool.lootPool()
                    .setRolls(ConstantIntValue.exactly(1))
                    .when(HAS_NO_SHEARS_OR_SILK_TOUCH)
                    .add(
                        applyExplosionDecay(
                                param0, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F)))
                            )
                            .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F))
                    )
            );
    }

    private static LootTable.Builder createOakLeavesDrops(Block param0, Block param1, float... param2) {
        return createLeavesDrops(param0, param1, param2)
            .withPool(
                LootPool.lootPool()
                    .setRolls(ConstantIntValue.exactly(1))
                    .when(HAS_NO_SHEARS_OR_SILK_TOUCH)
                    .add(
                        applyExplosionCondition(param0, LootItem.lootTableItem(Items.APPLE))
                            .when(
                                BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.005F, 0.0055555557F, 0.00625F, 0.008333334F, 0.025F)
                            )
                    )
            );
    }

    private static LootTable.Builder createCropDrops(Block param0, Item param1, Item param2, LootItemCondition.Builder param3) {
        return applyExplosionDecay(
            param0,
            LootTable.lootTable()
                .withPool(LootPool.lootPool().add(LootItem.lootTableItem(param1).when(param3).otherwise(LootItem.lootTableItem(param2))))
                .withPool(
                    LootPool.lootPool()
                        .when(param3)
                        .add(LootItem.lootTableItem(param2).apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286F, 3)))
                )
        );
    }

    public static LootTable.Builder noDrop() {
        return LootTable.lootTable();
    }

    public void accept(BiConsumer<ResourceLocation, LootTable.Builder> param0) {
        this.dropSelf(Blocks.GRANITE);
        this.dropSelf(Blocks.POLISHED_GRANITE);
        this.dropSelf(Blocks.DIORITE);
        this.dropSelf(Blocks.POLISHED_DIORITE);
        this.dropSelf(Blocks.ANDESITE);
        this.dropSelf(Blocks.POLISHED_ANDESITE);
        this.dropSelf(Blocks.DIRT);
        this.dropSelf(Blocks.COARSE_DIRT);
        this.dropSelf(Blocks.COBBLESTONE);
        this.dropSelf(Blocks.OAK_PLANKS);
        this.dropSelf(Blocks.SPRUCE_PLANKS);
        this.dropSelf(Blocks.BIRCH_PLANKS);
        this.dropSelf(Blocks.JUNGLE_PLANKS);
        this.dropSelf(Blocks.ACACIA_PLANKS);
        this.dropSelf(Blocks.DARK_OAK_PLANKS);
        this.dropSelf(Blocks.OAK_SAPLING);
        this.dropSelf(Blocks.SPRUCE_SAPLING);
        this.dropSelf(Blocks.BIRCH_SAPLING);
        this.dropSelf(Blocks.JUNGLE_SAPLING);
        this.dropSelf(Blocks.ACACIA_SAPLING);
        this.dropSelf(Blocks.DARK_OAK_SAPLING);
        this.dropSelf(Blocks.SAND);
        this.dropSelf(Blocks.RED_SAND);
        this.dropSelf(Blocks.GOLD_ORE);
        this.dropSelf(Blocks.IRON_ORE);
        this.dropSelf(Blocks.OAK_LOG);
        this.dropSelf(Blocks.SPRUCE_LOG);
        this.dropSelf(Blocks.BIRCH_LOG);
        this.dropSelf(Blocks.JUNGLE_LOG);
        this.dropSelf(Blocks.ACACIA_LOG);
        this.dropSelf(Blocks.DARK_OAK_LOG);
        this.dropSelf(Blocks.STRIPPED_SPRUCE_LOG);
        this.dropSelf(Blocks.STRIPPED_BIRCH_LOG);
        this.dropSelf(Blocks.STRIPPED_JUNGLE_LOG);
        this.dropSelf(Blocks.STRIPPED_ACACIA_LOG);
        this.dropSelf(Blocks.STRIPPED_DARK_OAK_LOG);
        this.dropSelf(Blocks.STRIPPED_OAK_LOG);
        this.dropSelf(Blocks.STRIPPED_WARPED_STEM);
        this.dropSelf(Blocks.STRIPPED_CRIMSON_STEM);
        this.dropSelf(Blocks.OAK_WOOD);
        this.dropSelf(Blocks.SPRUCE_WOOD);
        this.dropSelf(Blocks.BIRCH_WOOD);
        this.dropSelf(Blocks.JUNGLE_WOOD);
        this.dropSelf(Blocks.ACACIA_WOOD);
        this.dropSelf(Blocks.DARK_OAK_WOOD);
        this.dropSelf(Blocks.STRIPPED_OAK_WOOD);
        this.dropSelf(Blocks.STRIPPED_SPRUCE_WOOD);
        this.dropSelf(Blocks.STRIPPED_BIRCH_WOOD);
        this.dropSelf(Blocks.STRIPPED_JUNGLE_WOOD);
        this.dropSelf(Blocks.STRIPPED_ACACIA_WOOD);
        this.dropSelf(Blocks.STRIPPED_DARK_OAK_WOOD);
        this.dropSelf(Blocks.SPONGE);
        this.dropSelf(Blocks.WET_SPONGE);
        this.dropSelf(Blocks.LAPIS_BLOCK);
        this.dropSelf(Blocks.SANDSTONE);
        this.dropSelf(Blocks.CHISELED_SANDSTONE);
        this.dropSelf(Blocks.CUT_SANDSTONE);
        this.dropSelf(Blocks.NOTE_BLOCK);
        this.dropSelf(Blocks.POWERED_RAIL);
        this.dropSelf(Blocks.DETECTOR_RAIL);
        this.dropSelf(Blocks.STICKY_PISTON);
        this.dropSelf(Blocks.PISTON);
        this.dropSelf(Blocks.WHITE_WOOL);
        this.dropSelf(Blocks.ORANGE_WOOL);
        this.dropSelf(Blocks.MAGENTA_WOOL);
        this.dropSelf(Blocks.LIGHT_BLUE_WOOL);
        this.dropSelf(Blocks.YELLOW_WOOL);
        this.dropSelf(Blocks.LIME_WOOL);
        this.dropSelf(Blocks.PINK_WOOL);
        this.dropSelf(Blocks.GRAY_WOOL);
        this.dropSelf(Blocks.LIGHT_GRAY_WOOL);
        this.dropSelf(Blocks.CYAN_WOOL);
        this.dropSelf(Blocks.PURPLE_WOOL);
        this.dropSelf(Blocks.BLUE_WOOL);
        this.dropSelf(Blocks.BROWN_WOOL);
        this.dropSelf(Blocks.GREEN_WOOL);
        this.dropSelf(Blocks.RED_WOOL);
        this.dropSelf(Blocks.BLACK_WOOL);
        this.dropSelf(Blocks.DANDELION);
        this.dropSelf(Blocks.POPPY);
        this.dropSelf(Blocks.BLUE_ORCHID);
        this.dropSelf(Blocks.ALLIUM);
        this.dropSelf(Blocks.AZURE_BLUET);
        this.dropSelf(Blocks.RED_TULIP);
        this.dropSelf(Blocks.ORANGE_TULIP);
        this.dropSelf(Blocks.WHITE_TULIP);
        this.dropSelf(Blocks.PINK_TULIP);
        this.dropSelf(Blocks.OXEYE_DAISY);
        this.dropSelf(Blocks.CORNFLOWER);
        this.dropSelf(Blocks.WITHER_ROSE);
        this.dropSelf(Blocks.LILY_OF_THE_VALLEY);
        this.dropSelf(Blocks.BROWN_MUSHROOM);
        this.dropSelf(Blocks.RED_MUSHROOM);
        this.dropSelf(Blocks.GOLD_BLOCK);
        this.dropSelf(Blocks.IRON_BLOCK);
        this.dropSelf(Blocks.BRICKS);
        this.dropSelf(Blocks.MOSSY_COBBLESTONE);
        this.dropSelf(Blocks.OBSIDIAN);
        this.dropSelf(Blocks.CRYING_OBSIDIAN);
        this.dropSelf(Blocks.TORCH);
        this.dropSelf(Blocks.OAK_STAIRS);
        this.dropSelf(Blocks.REDSTONE_WIRE);
        this.dropSelf(Blocks.DIAMOND_BLOCK);
        this.dropSelf(Blocks.CRAFTING_TABLE);
        this.dropSelf(Blocks.OAK_SIGN);
        this.dropSelf(Blocks.SPRUCE_SIGN);
        this.dropSelf(Blocks.BIRCH_SIGN);
        this.dropSelf(Blocks.ACACIA_SIGN);
        this.dropSelf(Blocks.JUNGLE_SIGN);
        this.dropSelf(Blocks.DARK_OAK_SIGN);
        this.dropSelf(Blocks.LADDER);
        this.dropSelf(Blocks.RAIL);
        this.dropSelf(Blocks.COBBLESTONE_STAIRS);
        this.dropSelf(Blocks.LEVER);
        this.dropSelf(Blocks.STONE_PRESSURE_PLATE);
        this.dropSelf(Blocks.OAK_PRESSURE_PLATE);
        this.dropSelf(Blocks.SPRUCE_PRESSURE_PLATE);
        this.dropSelf(Blocks.BIRCH_PRESSURE_PLATE);
        this.dropSelf(Blocks.JUNGLE_PRESSURE_PLATE);
        this.dropSelf(Blocks.ACACIA_PRESSURE_PLATE);
        this.dropSelf(Blocks.DARK_OAK_PRESSURE_PLATE);
        this.dropSelf(Blocks.REDSTONE_TORCH);
        this.dropSelf(Blocks.STONE_BUTTON);
        this.dropSelf(Blocks.CACTUS);
        this.dropSelf(Blocks.SUGAR_CANE);
        this.dropSelf(Blocks.JUKEBOX);
        this.dropSelf(Blocks.OAK_FENCE);
        this.dropSelf(Blocks.PUMPKIN);
        this.dropSelf(Blocks.NETHERRACK);
        this.dropSelf(Blocks.SOUL_SAND);
        this.dropSelf(Blocks.SOUL_SOIL);
        this.dropSelf(Blocks.BASALT);
        this.dropSelf(Blocks.SOUL_FIRE_TORCH);
        this.dropSelf(Blocks.CARVED_PUMPKIN);
        this.dropSelf(Blocks.JACK_O_LANTERN);
        this.dropSelf(Blocks.REPEATER);
        this.dropSelf(Blocks.OAK_TRAPDOOR);
        this.dropSelf(Blocks.SPRUCE_TRAPDOOR);
        this.dropSelf(Blocks.BIRCH_TRAPDOOR);
        this.dropSelf(Blocks.JUNGLE_TRAPDOOR);
        this.dropSelf(Blocks.ACACIA_TRAPDOOR);
        this.dropSelf(Blocks.DARK_OAK_TRAPDOOR);
        this.dropSelf(Blocks.STONE_BRICKS);
        this.dropSelf(Blocks.MOSSY_STONE_BRICKS);
        this.dropSelf(Blocks.CRACKED_STONE_BRICKS);
        this.dropSelf(Blocks.CHISELED_STONE_BRICKS);
        this.dropSelf(Blocks.IRON_BARS);
        this.dropSelf(Blocks.OAK_FENCE_GATE);
        this.dropSelf(Blocks.BRICK_STAIRS);
        this.dropSelf(Blocks.STONE_BRICK_STAIRS);
        this.dropSelf(Blocks.LILY_PAD);
        this.dropSelf(Blocks.NETHER_BRICKS);
        this.dropSelf(Blocks.NETHER_BRICK_FENCE);
        this.dropSelf(Blocks.NETHER_BRICK_STAIRS);
        this.dropSelf(Blocks.CAULDRON);
        this.dropSelf(Blocks.END_STONE);
        this.dropSelf(Blocks.REDSTONE_LAMP);
        this.dropSelf(Blocks.SANDSTONE_STAIRS);
        this.dropSelf(Blocks.TRIPWIRE_HOOK);
        this.dropSelf(Blocks.EMERALD_BLOCK);
        this.dropSelf(Blocks.SPRUCE_STAIRS);
        this.dropSelf(Blocks.BIRCH_STAIRS);
        this.dropSelf(Blocks.JUNGLE_STAIRS);
        this.dropSelf(Blocks.COBBLESTONE_WALL);
        this.dropSelf(Blocks.MOSSY_COBBLESTONE_WALL);
        this.dropSelf(Blocks.FLOWER_POT);
        this.dropSelf(Blocks.OAK_BUTTON);
        this.dropSelf(Blocks.SPRUCE_BUTTON);
        this.dropSelf(Blocks.BIRCH_BUTTON);
        this.dropSelf(Blocks.JUNGLE_BUTTON);
        this.dropSelf(Blocks.ACACIA_BUTTON);
        this.dropSelf(Blocks.DARK_OAK_BUTTON);
        this.dropSelf(Blocks.SKELETON_SKULL);
        this.dropSelf(Blocks.WITHER_SKELETON_SKULL);
        this.dropSelf(Blocks.ZOMBIE_HEAD);
        this.dropSelf(Blocks.CREEPER_HEAD);
        this.dropSelf(Blocks.DRAGON_HEAD);
        this.dropSelf(Blocks.ANVIL);
        this.dropSelf(Blocks.CHIPPED_ANVIL);
        this.dropSelf(Blocks.DAMAGED_ANVIL);
        this.dropSelf(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
        this.dropSelf(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
        this.dropSelf(Blocks.COMPARATOR);
        this.dropSelf(Blocks.DAYLIGHT_DETECTOR);
        this.dropSelf(Blocks.REDSTONE_BLOCK);
        this.dropSelf(Blocks.QUARTZ_BLOCK);
        this.dropSelf(Blocks.CHISELED_QUARTZ_BLOCK);
        this.dropSelf(Blocks.QUARTZ_PILLAR);
        this.dropSelf(Blocks.QUARTZ_STAIRS);
        this.dropSelf(Blocks.ACTIVATOR_RAIL);
        this.dropSelf(Blocks.WHITE_TERRACOTTA);
        this.dropSelf(Blocks.ORANGE_TERRACOTTA);
        this.dropSelf(Blocks.MAGENTA_TERRACOTTA);
        this.dropSelf(Blocks.LIGHT_BLUE_TERRACOTTA);
        this.dropSelf(Blocks.YELLOW_TERRACOTTA);
        this.dropSelf(Blocks.LIME_TERRACOTTA);
        this.dropSelf(Blocks.PINK_TERRACOTTA);
        this.dropSelf(Blocks.GRAY_TERRACOTTA);
        this.dropSelf(Blocks.LIGHT_GRAY_TERRACOTTA);
        this.dropSelf(Blocks.CYAN_TERRACOTTA);
        this.dropSelf(Blocks.PURPLE_TERRACOTTA);
        this.dropSelf(Blocks.BLUE_TERRACOTTA);
        this.dropSelf(Blocks.BROWN_TERRACOTTA);
        this.dropSelf(Blocks.GREEN_TERRACOTTA);
        this.dropSelf(Blocks.RED_TERRACOTTA);
        this.dropSelf(Blocks.BLACK_TERRACOTTA);
        this.dropSelf(Blocks.ACACIA_STAIRS);
        this.dropSelf(Blocks.DARK_OAK_STAIRS);
        this.dropSelf(Blocks.SLIME_BLOCK);
        this.dropSelf(Blocks.IRON_TRAPDOOR);
        this.dropSelf(Blocks.PRISMARINE);
        this.dropSelf(Blocks.PRISMARINE_BRICKS);
        this.dropSelf(Blocks.DARK_PRISMARINE);
        this.dropSelf(Blocks.PRISMARINE_STAIRS);
        this.dropSelf(Blocks.PRISMARINE_BRICK_STAIRS);
        this.dropSelf(Blocks.DARK_PRISMARINE_STAIRS);
        this.dropSelf(Blocks.HAY_BLOCK);
        this.dropSelf(Blocks.WHITE_CARPET);
        this.dropSelf(Blocks.ORANGE_CARPET);
        this.dropSelf(Blocks.MAGENTA_CARPET);
        this.dropSelf(Blocks.LIGHT_BLUE_CARPET);
        this.dropSelf(Blocks.YELLOW_CARPET);
        this.dropSelf(Blocks.LIME_CARPET);
        this.dropSelf(Blocks.PINK_CARPET);
        this.dropSelf(Blocks.GRAY_CARPET);
        this.dropSelf(Blocks.LIGHT_GRAY_CARPET);
        this.dropSelf(Blocks.CYAN_CARPET);
        this.dropSelf(Blocks.PURPLE_CARPET);
        this.dropSelf(Blocks.BLUE_CARPET);
        this.dropSelf(Blocks.BROWN_CARPET);
        this.dropSelf(Blocks.GREEN_CARPET);
        this.dropSelf(Blocks.RED_CARPET);
        this.dropSelf(Blocks.BLACK_CARPET);
        this.dropSelf(Blocks.TERRACOTTA);
        this.dropSelf(Blocks.COAL_BLOCK);
        this.dropSelf(Blocks.RED_SANDSTONE);
        this.dropSelf(Blocks.CHISELED_RED_SANDSTONE);
        this.dropSelf(Blocks.CUT_RED_SANDSTONE);
        this.dropSelf(Blocks.RED_SANDSTONE_STAIRS);
        this.dropSelf(Blocks.SMOOTH_STONE);
        this.dropSelf(Blocks.SMOOTH_SANDSTONE);
        this.dropSelf(Blocks.SMOOTH_QUARTZ);
        this.dropSelf(Blocks.SMOOTH_RED_SANDSTONE);
        this.dropSelf(Blocks.SPRUCE_FENCE_GATE);
        this.dropSelf(Blocks.BIRCH_FENCE_GATE);
        this.dropSelf(Blocks.JUNGLE_FENCE_GATE);
        this.dropSelf(Blocks.ACACIA_FENCE_GATE);
        this.dropSelf(Blocks.DARK_OAK_FENCE_GATE);
        this.dropSelf(Blocks.SPRUCE_FENCE);
        this.dropSelf(Blocks.BIRCH_FENCE);
        this.dropSelf(Blocks.JUNGLE_FENCE);
        this.dropSelf(Blocks.ACACIA_FENCE);
        this.dropSelf(Blocks.DARK_OAK_FENCE);
        this.dropSelf(Blocks.END_ROD);
        this.dropSelf(Blocks.PURPUR_BLOCK);
        this.dropSelf(Blocks.PURPUR_PILLAR);
        this.dropSelf(Blocks.PURPUR_STAIRS);
        this.dropSelf(Blocks.END_STONE_BRICKS);
        this.dropSelf(Blocks.MAGMA_BLOCK);
        this.dropSelf(Blocks.NETHER_WART_BLOCK);
        this.dropSelf(Blocks.RED_NETHER_BRICKS);
        this.dropSelf(Blocks.BONE_BLOCK);
        this.dropSelf(Blocks.OBSERVER);
        this.dropSelf(Blocks.TARGET);
        this.dropSelf(Blocks.WHITE_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.ORANGE_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.MAGENTA_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.YELLOW_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.LIME_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.PINK_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.GRAY_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.CYAN_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.PURPLE_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.BLUE_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.BROWN_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.GREEN_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.RED_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.BLACK_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.WHITE_CONCRETE);
        this.dropSelf(Blocks.ORANGE_CONCRETE);
        this.dropSelf(Blocks.MAGENTA_CONCRETE);
        this.dropSelf(Blocks.LIGHT_BLUE_CONCRETE);
        this.dropSelf(Blocks.YELLOW_CONCRETE);
        this.dropSelf(Blocks.LIME_CONCRETE);
        this.dropSelf(Blocks.PINK_CONCRETE);
        this.dropSelf(Blocks.GRAY_CONCRETE);
        this.dropSelf(Blocks.LIGHT_GRAY_CONCRETE);
        this.dropSelf(Blocks.CYAN_CONCRETE);
        this.dropSelf(Blocks.PURPLE_CONCRETE);
        this.dropSelf(Blocks.BLUE_CONCRETE);
        this.dropSelf(Blocks.BROWN_CONCRETE);
        this.dropSelf(Blocks.GREEN_CONCRETE);
        this.dropSelf(Blocks.RED_CONCRETE);
        this.dropSelf(Blocks.BLACK_CONCRETE);
        this.dropSelf(Blocks.WHITE_CONCRETE_POWDER);
        this.dropSelf(Blocks.ORANGE_CONCRETE_POWDER);
        this.dropSelf(Blocks.MAGENTA_CONCRETE_POWDER);
        this.dropSelf(Blocks.LIGHT_BLUE_CONCRETE_POWDER);
        this.dropSelf(Blocks.YELLOW_CONCRETE_POWDER);
        this.dropSelf(Blocks.LIME_CONCRETE_POWDER);
        this.dropSelf(Blocks.PINK_CONCRETE_POWDER);
        this.dropSelf(Blocks.GRAY_CONCRETE_POWDER);
        this.dropSelf(Blocks.LIGHT_GRAY_CONCRETE_POWDER);
        this.dropSelf(Blocks.CYAN_CONCRETE_POWDER);
        this.dropSelf(Blocks.PURPLE_CONCRETE_POWDER);
        this.dropSelf(Blocks.BLUE_CONCRETE_POWDER);
        this.dropSelf(Blocks.BROWN_CONCRETE_POWDER);
        this.dropSelf(Blocks.GREEN_CONCRETE_POWDER);
        this.dropSelf(Blocks.RED_CONCRETE_POWDER);
        this.dropSelf(Blocks.BLACK_CONCRETE_POWDER);
        this.dropSelf(Blocks.KELP);
        this.dropSelf(Blocks.DRIED_KELP_BLOCK);
        this.dropSelf(Blocks.DEAD_TUBE_CORAL_BLOCK);
        this.dropSelf(Blocks.DEAD_BRAIN_CORAL_BLOCK);
        this.dropSelf(Blocks.DEAD_BUBBLE_CORAL_BLOCK);
        this.dropSelf(Blocks.DEAD_FIRE_CORAL_BLOCK);
        this.dropSelf(Blocks.DEAD_HORN_CORAL_BLOCK);
        this.dropSelf(Blocks.CONDUIT);
        this.dropSelf(Blocks.DRAGON_EGG);
        this.dropSelf(Blocks.BAMBOO);
        this.dropSelf(Blocks.POLISHED_GRANITE_STAIRS);
        this.dropSelf(Blocks.SMOOTH_RED_SANDSTONE_STAIRS);
        this.dropSelf(Blocks.MOSSY_STONE_BRICK_STAIRS);
        this.dropSelf(Blocks.POLISHED_DIORITE_STAIRS);
        this.dropSelf(Blocks.MOSSY_COBBLESTONE_STAIRS);
        this.dropSelf(Blocks.END_STONE_BRICK_STAIRS);
        this.dropSelf(Blocks.STONE_STAIRS);
        this.dropSelf(Blocks.SMOOTH_SANDSTONE_STAIRS);
        this.dropSelf(Blocks.SMOOTH_QUARTZ_STAIRS);
        this.dropSelf(Blocks.GRANITE_STAIRS);
        this.dropSelf(Blocks.ANDESITE_STAIRS);
        this.dropSelf(Blocks.RED_NETHER_BRICK_STAIRS);
        this.dropSelf(Blocks.POLISHED_ANDESITE_STAIRS);
        this.dropSelf(Blocks.DIORITE_STAIRS);
        this.dropSelf(Blocks.BRICK_WALL);
        this.dropSelf(Blocks.PRISMARINE_WALL);
        this.dropSelf(Blocks.RED_SANDSTONE_WALL);
        this.dropSelf(Blocks.MOSSY_STONE_BRICK_WALL);
        this.dropSelf(Blocks.GRANITE_WALL);
        this.dropSelf(Blocks.STONE_BRICK_WALL);
        this.dropSelf(Blocks.NETHER_BRICK_WALL);
        this.dropSelf(Blocks.ANDESITE_WALL);
        this.dropSelf(Blocks.RED_NETHER_BRICK_WALL);
        this.dropSelf(Blocks.SANDSTONE_WALL);
        this.dropSelf(Blocks.END_STONE_BRICK_WALL);
        this.dropSelf(Blocks.DIORITE_WALL);
        this.dropSelf(Blocks.LOOM);
        this.dropSelf(Blocks.SCAFFOLDING);
        this.dropSelf(Blocks.HONEY_BLOCK);
        this.dropSelf(Blocks.HONEYCOMB_BLOCK);
        this.dropSelf(Blocks.WARPED_STEM);
        this.dropSelf(Blocks.WARPED_NYLIUM);
        this.dropSelf(Blocks.WARPED_FUNGUS);
        this.dropSelf(Blocks.WARPED_WART_BLOCK);
        this.dropSelf(Blocks.WARPED_ROOTS);
        this.dropSelf(Blocks.CRIMSON_STEM);
        this.dropSelf(Blocks.CRIMSON_NYLIUM);
        this.dropSelf(Blocks.CRIMSON_FUNGUS);
        this.dropSelf(Blocks.SHROOMLIGHT);
        this.dropSelf(Blocks.CRIMSON_ROOTS);
        this.dropSelf(Blocks.WEEPING_VINES);
        this.dropSelf(Blocks.CRIMSON_PLANKS);
        this.dropSelf(Blocks.WARPED_PLANKS);
        this.dropSelf(Blocks.WARPED_PRESSURE_PLATE);
        this.dropSelf(Blocks.WARPED_FENCE);
        this.dropSelf(Blocks.WARPED_TRAPDOOR);
        this.dropSelf(Blocks.WARPED_FENCE_GATE);
        this.dropSelf(Blocks.WARPED_STAIRS);
        this.dropSelf(Blocks.WARPED_BUTTON);
        this.dropSelf(Blocks.WARPED_SIGN);
        this.dropSelf(Blocks.CRIMSON_PRESSURE_PLATE);
        this.dropSelf(Blocks.CRIMSON_FENCE);
        this.dropSelf(Blocks.CRIMSON_TRAPDOOR);
        this.dropSelf(Blocks.CRIMSON_FENCE_GATE);
        this.dropSelf(Blocks.CRIMSON_STAIRS);
        this.dropSelf(Blocks.CRIMSON_BUTTON);
        this.dropSelf(Blocks.CRIMSON_SIGN);
        this.dropSelf(Blocks.NETHERITE_BLOCK);
        this.dropSelf(Blocks.ANCIENT_DEBRIS);
        this.dropOther(Blocks.FARMLAND, Blocks.DIRT);
        this.dropOther(Blocks.TRIPWIRE, Items.STRING);
        this.dropOther(Blocks.GRASS_PATH, Blocks.DIRT);
        this.dropOther(Blocks.KELP_PLANT, Blocks.KELP);
        this.dropOther(Blocks.BAMBOO_SAPLING, Blocks.BAMBOO);
        this.dropOther(Blocks.WEEPING_VINES_PLANT, Blocks.WEEPING_VINES);
        this.add(Blocks.STONE, param0x -> createSingleItemTableWithSilkTouch(param0x, Blocks.COBBLESTONE));
        this.add(Blocks.GRASS_BLOCK, param0x -> createSingleItemTableWithSilkTouch(param0x, Blocks.DIRT));
        this.add(Blocks.PODZOL, param0x -> createSingleItemTableWithSilkTouch(param0x, Blocks.DIRT));
        this.add(Blocks.MYCELIUM, param0x -> createSingleItemTableWithSilkTouch(param0x, Blocks.DIRT));
        this.add(Blocks.TUBE_CORAL_BLOCK, param0x -> createSingleItemTableWithSilkTouch(param0x, Blocks.DEAD_TUBE_CORAL_BLOCK));
        this.add(Blocks.BRAIN_CORAL_BLOCK, param0x -> createSingleItemTableWithSilkTouch(param0x, Blocks.DEAD_BRAIN_CORAL_BLOCK));
        this.add(Blocks.BUBBLE_CORAL_BLOCK, param0x -> createSingleItemTableWithSilkTouch(param0x, Blocks.DEAD_BUBBLE_CORAL_BLOCK));
        this.add(Blocks.FIRE_CORAL_BLOCK, param0x -> createSingleItemTableWithSilkTouch(param0x, Blocks.DEAD_FIRE_CORAL_BLOCK));
        this.add(Blocks.HORN_CORAL_BLOCK, param0x -> createSingleItemTableWithSilkTouch(param0x, Blocks.DEAD_HORN_CORAL_BLOCK));
        this.add(Blocks.BOOKSHELF, param0x -> createSingleItemTableWithSilkTouch(param0x, Items.BOOK, ConstantIntValue.exactly(3)));
        this.add(Blocks.CLAY, param0x -> createSingleItemTableWithSilkTouch(param0x, Items.CLAY_BALL, ConstantIntValue.exactly(4)));
        this.add(Blocks.ENDER_CHEST, param0x -> createSingleItemTableWithSilkTouch(param0x, Blocks.OBSIDIAN, ConstantIntValue.exactly(8)));
        this.add(Blocks.SNOW_BLOCK, param0x -> createSingleItemTableWithSilkTouch(param0x, Items.SNOWBALL, ConstantIntValue.exactly(4)));
        this.add(Blocks.CHORUS_PLANT, createSingleItemTable(Items.CHORUS_FRUIT, RandomValueBounds.between(0.0F, 1.0F)));
        this.dropPottedContents(Blocks.POTTED_OAK_SAPLING);
        this.dropPottedContents(Blocks.POTTED_SPRUCE_SAPLING);
        this.dropPottedContents(Blocks.POTTED_BIRCH_SAPLING);
        this.dropPottedContents(Blocks.POTTED_JUNGLE_SAPLING);
        this.dropPottedContents(Blocks.POTTED_ACACIA_SAPLING);
        this.dropPottedContents(Blocks.POTTED_DARK_OAK_SAPLING);
        this.dropPottedContents(Blocks.POTTED_FERN);
        this.dropPottedContents(Blocks.POTTED_DANDELION);
        this.dropPottedContents(Blocks.POTTED_POPPY);
        this.dropPottedContents(Blocks.POTTED_BLUE_ORCHID);
        this.dropPottedContents(Blocks.POTTED_ALLIUM);
        this.dropPottedContents(Blocks.POTTED_AZURE_BLUET);
        this.dropPottedContents(Blocks.POTTED_RED_TULIP);
        this.dropPottedContents(Blocks.POTTED_ORANGE_TULIP);
        this.dropPottedContents(Blocks.POTTED_WHITE_TULIP);
        this.dropPottedContents(Blocks.POTTED_PINK_TULIP);
        this.dropPottedContents(Blocks.POTTED_OXEYE_DAISY);
        this.dropPottedContents(Blocks.POTTED_CORNFLOWER);
        this.dropPottedContents(Blocks.POTTED_LILY_OF_THE_VALLEY);
        this.dropPottedContents(Blocks.POTTED_WITHER_ROSE);
        this.dropPottedContents(Blocks.POTTED_RED_MUSHROOM);
        this.dropPottedContents(Blocks.POTTED_BROWN_MUSHROOM);
        this.dropPottedContents(Blocks.POTTED_DEAD_BUSH);
        this.dropPottedContents(Blocks.POTTED_CACTUS);
        this.dropPottedContents(Blocks.POTTED_BAMBOO);
        this.dropPottedContents(Blocks.POTTED_CRIMSON_FUNGUS);
        this.dropPottedContents(Blocks.POTTED_WARPED_FUNGUS);
        this.dropPottedContents(Blocks.POTTED_CRIMSON_ROOTS);
        this.dropPottedContents(Blocks.POTTED_WARPED_ROOTS);
        this.add(Blocks.ACACIA_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.BIRCH_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.BRICK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.COBBLESTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.DARK_OAK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.DARK_PRISMARINE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.JUNGLE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.NETHER_BRICK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.OAK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.PETRIFIED_OAK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.PRISMARINE_BRICK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.PRISMARINE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.PURPUR_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.QUARTZ_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.RED_SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.CUT_RED_SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.CUT_SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.SPRUCE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.STONE_BRICK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.STONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.SMOOTH_STONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.POLISHED_GRANITE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.SMOOTH_RED_SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.MOSSY_STONE_BRICK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.POLISHED_DIORITE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.MOSSY_COBBLESTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.END_STONE_BRICK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.SMOOTH_SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.SMOOTH_QUARTZ_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.GRANITE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.ANDESITE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.RED_NETHER_BRICK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.POLISHED_ANDESITE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.DIORITE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.CRIMSON_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.WARPED_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.ACACIA_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.BIRCH_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.DARK_OAK_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.IRON_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.JUNGLE_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.OAK_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.SPRUCE_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.WARPED_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.CRIMSON_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.BLACK_BED, param0x -> createSinglePropConditionTable(param0x, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.BLUE_BED, param0x -> createSinglePropConditionTable(param0x, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.BROWN_BED, param0x -> createSinglePropConditionTable(param0x, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.CYAN_BED, param0x -> createSinglePropConditionTable(param0x, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.GRAY_BED, param0x -> createSinglePropConditionTable(param0x, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.GREEN_BED, param0x -> createSinglePropConditionTable(param0x, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.LIGHT_BLUE_BED, param0x -> createSinglePropConditionTable(param0x, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.LIGHT_GRAY_BED, param0x -> createSinglePropConditionTable(param0x, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.LIME_BED, param0x -> createSinglePropConditionTable(param0x, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.MAGENTA_BED, param0x -> createSinglePropConditionTable(param0x, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.PURPLE_BED, param0x -> createSinglePropConditionTable(param0x, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.ORANGE_BED, param0x -> createSinglePropConditionTable(param0x, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.PINK_BED, param0x -> createSinglePropConditionTable(param0x, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.RED_BED, param0x -> createSinglePropConditionTable(param0x, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.WHITE_BED, param0x -> createSinglePropConditionTable(param0x, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.YELLOW_BED, param0x -> createSinglePropConditionTable(param0x, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.LILAC, param0x -> createSinglePropConditionTable(param0x, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        this.add(Blocks.SUNFLOWER, param0x -> createSinglePropConditionTable(param0x, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        this.add(Blocks.PEONY, param0x -> createSinglePropConditionTable(param0x, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        this.add(Blocks.ROSE_BUSH, param0x -> createSinglePropConditionTable(param0x, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        this.add(
            Blocks.TNT,
            LootTable.lootTable()
                .withPool(
                    applyExplosionCondition(
                        Blocks.TNT,
                        LootPool.lootPool()
                            .setRolls(ConstantIntValue.exactly(1))
                            .add(
                                LootItem.lootTableItem(Blocks.TNT)
                                    .when(
                                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.TNT)
                                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(TntBlock.UNSTABLE, false))
                                    )
                            )
                    )
                )
        );
        this.add(
            Blocks.COCOA,
            param0x -> LootTable.lootTable()
                    .withPool(
                        LootPool.lootPool()
                            .setRolls(ConstantIntValue.exactly(1))
                            .add(
                                applyExplosionDecay(
                                    param0x,
                                    LootItem.lootTableItem(Items.COCOA_BEANS)
                                        .apply(
                                            SetItemCountFunction.setCount(ConstantIntValue.exactly(3))
                                                .when(
                                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CocoaBlock.AGE, 2))
                                                )
                                        )
                                )
                            )
                    )
        );
        this.add(
            Blocks.SEA_PICKLE,
            param0x -> LootTable.lootTable()
                    .withPool(
                        LootPool.lootPool()
                            .setRolls(ConstantIntValue.exactly(1))
                            .add(
                                applyExplosionDecay(
                                    Blocks.SEA_PICKLE,
                                    LootItem.lootTableItem(param0x)
                                        .apply(
                                            SetItemCountFunction.setCount(ConstantIntValue.exactly(2))
                                                .when(
                                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SeaPickleBlock.PICKLES, 2))
                                                )
                                        )
                                        .apply(
                                            SetItemCountFunction.setCount(ConstantIntValue.exactly(3))
                                                .when(
                                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SeaPickleBlock.PICKLES, 3))
                                                )
                                        )
                                        .apply(
                                            SetItemCountFunction.setCount(ConstantIntValue.exactly(4))
                                                .when(
                                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SeaPickleBlock.PICKLES, 4))
                                                )
                                        )
                                )
                            )
                    )
        );
        this.add(
            Blocks.COMPOSTER,
            param0x -> LootTable.lootTable()
                    .withPool(LootPool.lootPool().add(applyExplosionDecay(param0x, LootItem.lootTableItem(Items.COMPOSTER))))
                    .withPool(
                        LootPool.lootPool()
                            .add(LootItem.lootTableItem(Items.BONE_MEAL))
                            .when(
                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(ComposterBlock.LEVEL, 8))
                            )
                    )
        );
        this.add(Blocks.BEACON, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.BREWING_STAND, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.DISPENSER, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.DROPPER, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.ENCHANTING_TABLE, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.FURNACE, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.HOPPER, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.TRAPPED_CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.SMOKER, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.BLAST_FURNACE, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.BARREL, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.CARTOGRAPHY_TABLE, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.FLETCHING_TABLE, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.GRINDSTONE, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.LECTERN, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.SMITHING_TABLE, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.STONECUTTER, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.BELL, BlockLoot::createSingleItemTable);
        this.add(Blocks.LANTERN, BlockLoot::createSingleItemTable);
        this.add(Blocks.SOUL_FIRE_LANTERN, BlockLoot::createSingleItemTable);
        this.add(Blocks.SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.BLACK_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.BLUE_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.BROWN_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.CYAN_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.GRAY_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.GREEN_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.LIGHT_BLUE_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.LIGHT_GRAY_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.LIME_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.MAGENTA_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.ORANGE_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.PINK_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.PURPLE_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.RED_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.WHITE_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.YELLOW_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.BLACK_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.BLUE_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.BROWN_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.CYAN_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.GRAY_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.GREEN_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.LIGHT_BLUE_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.LIGHT_GRAY_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.LIME_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.MAGENTA_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.ORANGE_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.PINK_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.PURPLE_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.RED_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.WHITE_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.YELLOW_BANNER, BlockLoot::createBannerDrop);
        this.add(
            Blocks.PLAYER_HEAD,
            param0x -> LootTable.lootTable()
                    .withPool(
                        applyExplosionCondition(
                            param0x,
                            LootPool.lootPool()
                                .setRolls(ConstantIntValue.exactly(1))
                                .add(
                                    LootItem.lootTableItem(param0x)
                                        .apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("Owner", "SkullOwner"))
                                )
                        )
                    )
        );
        this.add(Blocks.BEE_NEST, BlockLoot::createBeeNestDrop);
        this.add(Blocks.BEEHIVE, BlockLoot::createBeeHiveDrop);
        this.add(Blocks.BIRCH_LEAVES, param0x -> createLeavesDrops(param0x, Blocks.BIRCH_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
        this.add(Blocks.ACACIA_LEAVES, param0x -> createLeavesDrops(param0x, Blocks.ACACIA_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
        this.add(Blocks.JUNGLE_LEAVES, param0x -> createLeavesDrops(param0x, Blocks.JUNGLE_SAPLING, JUNGLE_LEAVES_SAPLING_CHANGES));
        this.add(Blocks.SPRUCE_LEAVES, param0x -> createLeavesDrops(param0x, Blocks.SPRUCE_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
        this.add(Blocks.OAK_LEAVES, param0x -> createOakLeavesDrops(param0x, Blocks.OAK_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
        this.add(Blocks.DARK_OAK_LEAVES, param0x -> createOakLeavesDrops(param0x, Blocks.DARK_OAK_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
        LootItemCondition.Builder var0 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.BEETROOTS)
            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BeetrootBlock.AGE, 3));
        this.add(Blocks.BEETROOTS, createCropDrops(Blocks.BEETROOTS, Items.BEETROOT, Items.BEETROOT_SEEDS, var0));
        LootItemCondition.Builder var1 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.WHEAT)
            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, 7));
        this.add(Blocks.WHEAT, createCropDrops(Blocks.WHEAT, Items.WHEAT, Items.WHEAT_SEEDS, var1));
        LootItemCondition.Builder var2 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.CARROTS)
            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CarrotBlock.AGE, 7));
        this.add(
            Blocks.CARROTS,
            applyExplosionDecay(
                Blocks.CARROTS,
                LootTable.lootTable()
                    .withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.CARROT)))
                    .withPool(
                        LootPool.lootPool()
                            .when(var2)
                            .add(
                                LootItem.lootTableItem(Items.CARROT)
                                    .apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286F, 3))
                            )
                    )
            )
        );
        LootItemCondition.Builder var3 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.POTATOES)
            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PotatoBlock.AGE, 7));
        this.add(
            Blocks.POTATOES,
            applyExplosionDecay(
                Blocks.POTATOES,
                LootTable.lootTable()
                    .withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.POTATO)))
                    .withPool(
                        LootPool.lootPool()
                            .when(var3)
                            .add(
                                LootItem.lootTableItem(Items.POTATO)
                                    .apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286F, 3))
                            )
                    )
                    .withPool(
                        LootPool.lootPool()
                            .when(var3)
                            .add(LootItem.lootTableItem(Items.POISONOUS_POTATO).when(LootItemRandomChanceCondition.randomChance(0.02F)))
                    )
            )
        );
        this.add(
            Blocks.SWEET_BERRY_BUSH,
            param0x -> applyExplosionDecay(
                    param0x,
                    LootTable.lootTable()
                        .withPool(
                            LootPool.lootPool()
                                .when(
                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SWEET_BERRY_BUSH)
                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SweetBerryBushBlock.AGE, 3))
                                )
                                .add(LootItem.lootTableItem(Items.SWEET_BERRIES))
                                .apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 3.0F)))
                                .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
                        )
                        .withPool(
                            LootPool.lootPool()
                                .when(
                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SWEET_BERRY_BUSH)
                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SweetBerryBushBlock.AGE, 2))
                                )
                                .add(LootItem.lootTableItem(Items.SWEET_BERRIES))
                                .apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F)))
                                .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
                        )
                )
        );
        this.add(Blocks.BROWN_MUSHROOM_BLOCK, param0x -> createMushroomBlockDrop(param0x, Blocks.BROWN_MUSHROOM));
        this.add(Blocks.RED_MUSHROOM_BLOCK, param0x -> createMushroomBlockDrop(param0x, Blocks.RED_MUSHROOM));
        this.add(Blocks.COAL_ORE, param0x -> createOreDrop(param0x, Items.COAL));
        this.add(Blocks.EMERALD_ORE, param0x -> createOreDrop(param0x, Items.EMERALD));
        this.add(Blocks.NETHER_QUARTZ_ORE, param0x -> createOreDrop(param0x, Items.QUARTZ));
        this.add(Blocks.DIAMOND_ORE, param0x -> createOreDrop(param0x, Items.DIAMOND));
        this.add(
            Blocks.LAPIS_ORE,
            param0x -> createSilkTouchDispatchTable(
                    param0x,
                    applyExplosionDecay(
                        param0x,
                        LootItem.lootTableItem(Items.LAPIS_LAZULI)
                            .apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 9.0F)))
                            .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
                    )
                )
        );
        this.add(
            Blocks.COBWEB, param0x -> createSilkTouchOrShearsDispatchTable(param0x, applyExplosionCondition(param0x, LootItem.lootTableItem(Items.STRING)))
        );
        this.add(
            Blocks.DEAD_BUSH,
            param0x -> createShearsDispatchTable(
                    param0x,
                    applyExplosionDecay(
                        param0x, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
                    )
                )
        );
        this.add(
            Blocks.NETHER_SPROUTS,
            param0x -> createSilkTouchOrShearsDispatchTable(param0x, applyExplosionCondition(param0x, LootItem.lootTableItem(Blocks.NETHER_SPROUTS)))
        );
        this.add(Blocks.SEAGRASS, BlockLoot::createShearsOnlyDrop);
        this.add(Blocks.VINE, BlockLoot::createShearsOnlyDrop);
        this.add(Blocks.TALL_SEAGRASS, createShearsOnlyDrop(Blocks.SEAGRASS));
        this.add(
            Blocks.LARGE_FERN,
            param0x -> createShearsDispatchTable(
                    Blocks.FERN,
                    applyExplosionCondition(param0x, LootItem.lootTableItem(Items.WHEAT_SEEDS))
                        .when(
                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))
                        )
                        .when(LootItemRandomChanceCondition.randomChance(0.125F))
                )
        );
        this.add(
            Blocks.TALL_GRASS,
            createShearsDispatchTable(
                Blocks.GRASS,
                applyExplosionCondition(Blocks.TALL_GRASS, LootItem.lootTableItem(Items.WHEAT_SEEDS))
                    .when(
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.TALL_GRASS)
                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))
                    )
                    .when(LootItemRandomChanceCondition.randomChance(0.125F))
            )
        );
        this.add(Blocks.MELON_STEM, param0x -> createStemDrops(param0x, Items.MELON_SEEDS));
        this.add(Blocks.ATTACHED_MELON_STEM, param0x -> createAttachedStemDrops(param0x, Items.MELON_SEEDS));
        this.add(Blocks.PUMPKIN_STEM, param0x -> createStemDrops(param0x, Items.PUMPKIN_SEEDS));
        this.add(Blocks.ATTACHED_PUMPKIN_STEM, param0x -> createAttachedStemDrops(param0x, Items.PUMPKIN_SEEDS));
        this.add(
            Blocks.CHORUS_FLOWER,
            param0x -> LootTable.lootTable()
                    .withPool(
                        LootPool.lootPool()
                            .setRolls(ConstantIntValue.exactly(1))
                            .add(
                                applyExplosionCondition(param0x, LootItem.lootTableItem(param0x))
                                    .when(LootItemEntityPropertyCondition.entityPresent(LootContext.EntityTarget.THIS))
                            )
                    )
        );
        this.add(Blocks.FERN, BlockLoot::createGrassDrops);
        this.add(Blocks.GRASS, BlockLoot::createGrassDrops);
        this.add(
            Blocks.GLOWSTONE,
            param0x -> createSilkTouchDispatchTable(
                    param0x,
                    applyExplosionDecay(
                        param0x,
                        LootItem.lootTableItem(Items.GLOWSTONE_DUST)
                            .apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F)))
                            .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
                            .apply(LimitCount.limitCount(IntLimiter.clamp(1, 4)))
                    )
                )
        );
        this.add(
            Blocks.MELON,
            param0x -> createSilkTouchDispatchTable(
                    param0x,
                    applyExplosionDecay(
                        param0x,
                        LootItem.lootTableItem(Items.MELON_SLICE)
                            .apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 7.0F)))
                            .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
                            .apply(LimitCount.limitCount(IntLimiter.upperBound(9)))
                    )
                )
        );
        this.add(
            Blocks.REDSTONE_ORE,
            param0x -> createSilkTouchDispatchTable(
                    param0x,
                    applyExplosionDecay(
                        param0x,
                        LootItem.lootTableItem(Items.REDSTONE)
                            .apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 5.0F)))
                            .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
                    )
                )
        );
        this.add(
            Blocks.SEA_LANTERN,
            param0x -> createSilkTouchDispatchTable(
                    param0x,
                    applyExplosionDecay(
                        param0x,
                        LootItem.lootTableItem(Items.PRISMARINE_CRYSTALS)
                            .apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 3.0F)))
                            .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
                            .apply(LimitCount.limitCount(IntLimiter.clamp(1, 5)))
                    )
                )
        );
        this.add(
            Blocks.NETHER_WART,
            param0x -> LootTable.lootTable()
                    .withPool(
                        applyExplosionDecay(
                            param0x,
                            LootPool.lootPool()
                                .setRolls(ConstantIntValue.exactly(1))
                                .add(
                                    LootItem.lootTableItem(Items.NETHER_WART)
                                        .apply(
                                            SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))
                                                .when(
                                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(NetherWartBlock.AGE, 3))
                                                )
                                        )
                                        .apply(
                                            ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE)
                                                .when(
                                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(NetherWartBlock.AGE, 3))
                                                )
                                        )
                                )
                        )
                    )
        );
        this.add(
            Blocks.SNOW,
            param0x -> LootTable.lootTable()
                    .withPool(
                        LootPool.lootPool()
                            .when(LootItemEntityPropertyCondition.entityPresent(LootContext.EntityTarget.THIS))
                            .add(
                                AlternativesEntry.alternatives(
                                    AlternativesEntry.alternatives(
                                            LootItem.lootTableItem(Items.SNOWBALL)
                                                .when(
                                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 1))
                                                ),
                                            LootItem.lootTableItem(Items.SNOWBALL)
                                                .when(
                                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 2))
                                                )
                                                .apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(2))),
                                            LootItem.lootTableItem(Items.SNOWBALL)
                                                .when(
                                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 3))
                                                )
                                                .apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(3))),
                                            LootItem.lootTableItem(Items.SNOWBALL)
                                                .when(
                                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 4))
                                                )
                                                .apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(4))),
                                            LootItem.lootTableItem(Items.SNOWBALL)
                                                .when(
                                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 5))
                                                )
                                                .apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(5))),
                                            LootItem.lootTableItem(Items.SNOWBALL)
                                                .when(
                                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 6))
                                                )
                                                .apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(6))),
                                            LootItem.lootTableItem(Items.SNOWBALL)
                                                .when(
                                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 7))
                                                )
                                                .apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(7))),
                                            LootItem.lootTableItem(Items.SNOWBALL).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(8)))
                                        )
                                        .when(HAS_NO_SILK_TOUCH),
                                    AlternativesEntry.alternatives(
                                        LootItem.lootTableItem(Blocks.SNOW)
                                            .when(
                                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 1))
                                            ),
                                        LootItem.lootTableItem(Blocks.SNOW)
                                            .apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(2)))
                                            .when(
                                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 2))
                                            ),
                                        LootItem.lootTableItem(Blocks.SNOW)
                                            .apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(3)))
                                            .when(
                                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 3))
                                            ),
                                        LootItem.lootTableItem(Blocks.SNOW)
                                            .apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(4)))
                                            .when(
                                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 4))
                                            ),
                                        LootItem.lootTableItem(Blocks.SNOW)
                                            .apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(5)))
                                            .when(
                                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 5))
                                            ),
                                        LootItem.lootTableItem(Blocks.SNOW)
                                            .apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(6)))
                                            .when(
                                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 6))
                                            ),
                                        LootItem.lootTableItem(Blocks.SNOW)
                                            .apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(7)))
                                            .when(
                                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0x)
                                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 7))
                                            ),
                                        LootItem.lootTableItem(Blocks.SNOW_BLOCK)
                                    )
                                )
                            )
                    )
        );
        this.add(
            Blocks.GRAVEL,
            param0x -> createSilkTouchDispatchTable(
                    param0x,
                    applyExplosionCondition(
                        param0x,
                        LootItem.lootTableItem(Items.FLINT)
                            .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.1F, 0.14285715F, 0.25F, 1.0F))
                            .otherwise(LootItem.lootTableItem(param0x))
                    )
                )
        );
        this.add(
            Blocks.CAMPFIRE,
            param0x -> createSilkTouchDispatchTable(
                    param0x,
                    applyExplosionCondition(param0x, LootItem.lootTableItem(Items.CHARCOAL).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(2))))
                )
        );
        this.dropWhenSilkTouch(Blocks.GLASS);
        this.dropWhenSilkTouch(Blocks.WHITE_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.ORANGE_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.MAGENTA_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.LIGHT_BLUE_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.YELLOW_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.LIME_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.PINK_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.GRAY_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.LIGHT_GRAY_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.CYAN_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.PURPLE_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.BLUE_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.BROWN_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.GREEN_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.RED_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.BLACK_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.WHITE_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.ORANGE_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.MAGENTA_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.YELLOW_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.LIME_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.PINK_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.GRAY_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.CYAN_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.PURPLE_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.BLUE_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.BROWN_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.GREEN_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.RED_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.BLACK_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.ICE);
        this.dropWhenSilkTouch(Blocks.PACKED_ICE);
        this.dropWhenSilkTouch(Blocks.BLUE_ICE);
        this.dropWhenSilkTouch(Blocks.TURTLE_EGG);
        this.dropWhenSilkTouch(Blocks.MUSHROOM_STEM);
        this.dropWhenSilkTouch(Blocks.DEAD_TUBE_CORAL);
        this.dropWhenSilkTouch(Blocks.DEAD_BRAIN_CORAL);
        this.dropWhenSilkTouch(Blocks.DEAD_BUBBLE_CORAL);
        this.dropWhenSilkTouch(Blocks.DEAD_FIRE_CORAL);
        this.dropWhenSilkTouch(Blocks.DEAD_HORN_CORAL);
        this.dropWhenSilkTouch(Blocks.TUBE_CORAL);
        this.dropWhenSilkTouch(Blocks.BRAIN_CORAL);
        this.dropWhenSilkTouch(Blocks.BUBBLE_CORAL);
        this.dropWhenSilkTouch(Blocks.FIRE_CORAL);
        this.dropWhenSilkTouch(Blocks.HORN_CORAL);
        this.dropWhenSilkTouch(Blocks.DEAD_TUBE_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.DEAD_BRAIN_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.DEAD_BUBBLE_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.DEAD_FIRE_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.DEAD_HORN_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.TUBE_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.BRAIN_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.BUBBLE_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.FIRE_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.HORN_CORAL_FAN);
        this.otherWhenSilkTouch(Blocks.INFESTED_STONE, Blocks.STONE);
        this.otherWhenSilkTouch(Blocks.INFESTED_COBBLESTONE, Blocks.COBBLESTONE);
        this.otherWhenSilkTouch(Blocks.INFESTED_STONE_BRICKS, Blocks.STONE_BRICKS);
        this.otherWhenSilkTouch(Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS);
        this.otherWhenSilkTouch(Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS);
        this.otherWhenSilkTouch(Blocks.INFESTED_CHISELED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS);
        this.add(Blocks.CAKE, noDrop());
        this.add(Blocks.FROSTED_ICE, noDrop());
        this.add(Blocks.SPAWNER, noDrop());
        Set<ResourceLocation> var4 = Sets.newHashSet();

        for(Block var5 : Registry.BLOCK) {
            ResourceLocation var6 = var5.getLootTable();
            if (var6 != BuiltInLootTables.EMPTY && var4.add(var6)) {
                LootTable.Builder var7 = this.map.remove(var6);
                if (var7 == null) {
                    throw new IllegalStateException(String.format("Missing loottable '%s' for '%s'", var6, Registry.BLOCK.getKey(var5)));
                }

                param0.accept(var6, var7);
            }
        }

        if (!this.map.isEmpty()) {
            throw new IllegalStateException("Created block loot tables for non-blocks: " + this.map.keySet());
        }
    }

    public static LootTable.Builder createDoorTable(Block param0x) {
        return createSinglePropConditionTable(param0x, DoorBlock.HALF, DoubleBlockHalf.LOWER);
    }

    public void dropPottedContents(Block param0) {
        this.add(param0, param0x -> createPotFlowerItemTable(((FlowerPotBlock)param0x).getContent()));
    }

    public void otherWhenSilkTouch(Block param0, Block param1) {
        this.add(param0, createSilkTouchOnlyTable(param1));
    }

    public void dropOther(Block param0, ItemLike param1) {
        this.add(param0, createSingleItemTable(param1));
    }

    public void dropWhenSilkTouch(Block param0) {
        this.otherWhenSilkTouch(param0, param0);
    }

    public void dropSelf(Block param0) {
        this.dropOther(param0, param0);
    }

    private void add(Block param0, Function<Block, LootTable.Builder> param1) {
        this.add(param0, param1.apply(param0));
    }

    private void add(Block param0, LootTable.Builder param1) {
        this.map.put(param0.getLootTable(), param1);
    }
}
