package net.minecraft.data.loot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
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
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public abstract class BlockLootSubProvider implements LootTableSubProvider {
    protected static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(
        ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1)))
    );
    protected static final LootItemCondition.Builder HAS_NO_SILK_TOUCH = HAS_SILK_TOUCH.invert();
    protected static final LootItemCondition.Builder HAS_SHEARS = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS));
    private static final LootItemCondition.Builder HAS_SHEARS_OR_SILK_TOUCH = HAS_SHEARS.or(HAS_SILK_TOUCH);
    private static final LootItemCondition.Builder HAS_NO_SHEARS_OR_SILK_TOUCH = HAS_SHEARS_OR_SILK_TOUCH.invert();
    private final Set<Item> explosionResistant;
    private final FeatureFlagSet enabledFeatures;
    private final Map<ResourceLocation, LootTable.Builder> map = new HashMap<>();
    protected static final float[] NORMAL_LEAVES_SAPLING_CHANCES = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};
    private static final float[] NORMAL_LEAVES_STICK_CHANCES = new float[]{0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F};

    protected BlockLootSubProvider(Set<Item> param0, FeatureFlagSet param1) {
        this.explosionResistant = param0;
        this.enabledFeatures = param1;
    }

    protected <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike param0, FunctionUserBuilder<T> param1) {
        return (T)(!this.explosionResistant.contains(param0.asItem()) ? param1.apply(ApplyExplosionDecay.explosionDecay()) : param1.unwrap());
    }

    protected <T extends ConditionUserBuilder<T>> T applyExplosionCondition(ItemLike param0, ConditionUserBuilder<T> param1) {
        return (T)(!this.explosionResistant.contains(param0.asItem()) ? param1.when(ExplosionCondition.survivesExplosion()) : param1.unwrap());
    }

    public LootTable.Builder createSingleItemTable(ItemLike param0) {
        return LootTable.lootTable()
            .withPool(this.applyExplosionCondition(param0, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(param0))));
    }

    private static LootTable.Builder createSelfDropDispatchTable(Block param0, LootItemCondition.Builder param1, LootPoolEntryContainer.Builder<?> param2) {
        return LootTable.lootTable()
            .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(param0).when(param1).otherwise(param2)));
    }

    protected static LootTable.Builder createSilkTouchDispatchTable(Block param0, LootPoolEntryContainer.Builder<?> param1) {
        return createSelfDropDispatchTable(param0, HAS_SILK_TOUCH, param1);
    }

    protected static LootTable.Builder createShearsDispatchTable(Block param0, LootPoolEntryContainer.Builder<?> param1) {
        return createSelfDropDispatchTable(param0, HAS_SHEARS, param1);
    }

    protected static LootTable.Builder createSilkTouchOrShearsDispatchTable(Block param0, LootPoolEntryContainer.Builder<?> param1) {
        return createSelfDropDispatchTable(param0, HAS_SHEARS_OR_SILK_TOUCH, param1);
    }

    protected LootTable.Builder createSingleItemTableWithSilkTouch(Block param0, ItemLike param1) {
        return createSilkTouchDispatchTable(param0, this.applyExplosionCondition(param0, LootItem.lootTableItem(param1)));
    }

    protected LootTable.Builder createSingleItemTable(ItemLike param0, NumberProvider param1) {
        return LootTable.lootTable()
            .withPool(
                LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(this.applyExplosionDecay(param0, LootItem.lootTableItem(param0).apply(SetItemCountFunction.setCount(param1))))
            );
    }

    protected LootTable.Builder createSingleItemTableWithSilkTouch(Block param0, ItemLike param1, NumberProvider param2) {
        return createSilkTouchDispatchTable(
            param0, this.applyExplosionDecay(param0, LootItem.lootTableItem(param1).apply(SetItemCountFunction.setCount(param2)))
        );
    }

    private static LootTable.Builder createSilkTouchOnlyTable(ItemLike param0) {
        return LootTable.lootTable()
            .withPool(LootPool.lootPool().when(HAS_SILK_TOUCH).setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(param0)));
    }

    private LootTable.Builder createPotFlowerItemTable(ItemLike param0) {
        return LootTable.lootTable()
            .withPool(
                this.applyExplosionCondition(
                    Blocks.FLOWER_POT, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Blocks.FLOWER_POT))
                )
            )
            .withPool(this.applyExplosionCondition(param0, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(param0))));
    }

    protected LootTable.Builder createSlabItemTable(Block param0) {
        return LootTable.lootTable()
            .withPool(
                LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(
                        this.applyExplosionDecay(
                            param0,
                            LootItem.lootTableItem(param0)
                                .apply(
                                    SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))
                                        .when(
                                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE))
                                        )
                                )
                        )
                    )
            );
    }

    protected <T extends Comparable<T> & StringRepresentable> LootTable.Builder createSinglePropConditionTable(Block param0, Property<T> param1, T param2) {
        return LootTable.lootTable()
            .withPool(
                this.applyExplosionCondition(
                    param0,
                    LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
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

    protected LootTable.Builder createNameableBlockEntityTable(Block param0) {
        return LootTable.lootTable()
            .withPool(
                this.applyExplosionCondition(
                    param0,
                    LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(param0).apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY)))
                )
            );
    }

    protected LootTable.Builder createShulkerBoxDrop(Block param0) {
        return LootTable.lootTable()
            .withPool(
                this.applyExplosionCondition(
                    param0,
                    LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(
                            LootItem.lootTableItem(param0)
                                .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                                .apply(
                                    CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                        .copy("Lock", "BlockEntityTag.Lock")
                                        .copy("LootTable", "BlockEntityTag.LootTable")
                                        .copy("LootTableSeed", "BlockEntityTag.LootTableSeed")
                                )
                                .apply(
                                    SetContainerContents.setContents(BlockEntityType.SHULKER_BOX).withEntry(DynamicLoot.dynamicEntry(ShulkerBoxBlock.CONTENTS))
                                )
                        )
                )
            );
    }

    protected LootTable.Builder createCopperOreDrops(Block param0) {
        return createSilkTouchDispatchTable(
            param0,
            this.applyExplosionDecay(
                param0,
                LootItem.lootTableItem(Items.RAW_COPPER)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F)))
                    .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
            )
        );
    }

    protected LootTable.Builder createLapisOreDrops(Block param0) {
        return createSilkTouchDispatchTable(
            param0,
            this.applyExplosionDecay(
                param0,
                LootItem.lootTableItem(Items.LAPIS_LAZULI)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 9.0F)))
                    .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
            )
        );
    }

    protected LootTable.Builder createRedstoneOreDrops(Block param0) {
        return createSilkTouchDispatchTable(
            param0,
            this.applyExplosionDecay(
                param0,
                LootItem.lootTableItem(Items.REDSTONE)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 5.0F)))
                    .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
            )
        );
    }

    protected LootTable.Builder createBannerDrop(Block param0) {
        return LootTable.lootTable()
            .withPool(
                this.applyExplosionCondition(
                    param0,
                    LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(
                            LootItem.lootTableItem(param0)
                                .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                                .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("Patterns", "BlockEntityTag.Patterns"))
                        )
                )
            );
    }

    protected static LootTable.Builder createBeeNestDrop(Block param0) {
        return LootTable.lootTable()
            .withPool(
                LootPool.lootPool()
                    .when(HAS_SILK_TOUCH)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(
                        LootItem.lootTableItem(param0)
                            .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("Bees", "BlockEntityTag.Bees"))
                            .apply(CopyBlockState.copyState(param0).copy(BeehiveBlock.HONEY_LEVEL))
                    )
            );
    }

    protected static LootTable.Builder createBeeHiveDrop(Block param0) {
        return LootTable.lootTable()
            .withPool(
                LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(
                        LootItem.lootTableItem(param0)
                            .when(HAS_SILK_TOUCH)
                            .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("Bees", "BlockEntityTag.Bees"))
                            .apply(CopyBlockState.copyState(param0).copy(BeehiveBlock.HONEY_LEVEL))
                            .otherwise(LootItem.lootTableItem(param0))
                    )
            );
    }

    protected static LootTable.Builder createCaveVinesDrop(Block param0) {
        return LootTable.lootTable()
            .withPool(
                LootPool.lootPool()
                    .add(LootItem.lootTableItem(Items.GLOW_BERRIES))
                    .when(
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CaveVines.BERRIES, true))
                    )
            );
    }

    protected LootTable.Builder createOreDrop(Block param0, Item param1) {
        return createSilkTouchDispatchTable(
            param0, this.applyExplosionDecay(param0, LootItem.lootTableItem(param1).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)))
        );
    }

    protected LootTable.Builder createMushroomBlockDrop(Block param0, ItemLike param1) {
        return createSilkTouchDispatchTable(
            param0,
            this.applyExplosionDecay(
                param0,
                LootItem.lootTableItem(param1)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(-6.0F, 2.0F)))
                    .apply(LimitCount.limitCount(IntRange.lowerBound(0)))
            )
        );
    }

    protected LootTable.Builder createGrassDrops(Block param0) {
        return createShearsDispatchTable(
            param0,
            this.applyExplosionDecay(
                param0,
                LootItem.lootTableItem(Items.WHEAT_SEEDS)
                    .when(LootItemRandomChanceCondition.randomChance(0.125F))
                    .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, 2))
            )
        );
    }

    public LootTable.Builder createStemDrops(Block param0, Item param1) {
        return LootTable.lootTable()
            .withPool(
                this.applyExplosionDecay(
                    param0,
                    LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(
                            LootItem.lootTableItem(param1)
                                .apply(
                                    StemBlock.AGE.getPossibleValues(),
                                    param1x -> SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, (float)(param1x + 1) / 15.0F))
                                            .when(
                                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, param1x.intValue()))
                                            )
                                )
                        )
                )
            );
    }

    public LootTable.Builder createAttachedStemDrops(Block param0, Item param1) {
        return LootTable.lootTable()
            .withPool(
                this.applyExplosionDecay(
                    param0,
                    LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(param1).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.53333336F))))
                )
            );
    }

    protected static LootTable.Builder createShearsOnlyDrop(ItemLike param0) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(HAS_SHEARS).add(LootItem.lootTableItem(param0)));
    }

    protected LootTable.Builder createMultifaceBlockDrops(Block param0, LootItemCondition.Builder param1) {
        return LootTable.lootTable()
            .withPool(
                LootPool.lootPool()
                    .add(
                        this.applyExplosionDecay(
                            param0,
                            LootItem.lootTableItem(param0)
                                .when(param1)
                                .apply(
                                    Direction.values(),
                                    param1x -> SetItemCountFunction.setCount(ConstantValue.exactly(1.0F), true)
                                            .when(
                                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                                                    .setProperties(
                                                        StatePropertiesPredicate.Builder.properties()
                                                            .hasProperty(MultifaceBlock.getFaceProperty(param1x), true)
                                                    )
                                            )
                                )
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(-1.0F), true))
                        )
                    )
            );
    }

    protected LootTable.Builder createLeavesDrops(Block param0, Block param1, float... param2) {
        return createSilkTouchOrShearsDispatchTable(
                param0,
                this.applyExplosionCondition(param0, LootItem.lootTableItem(param1))
                    .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, param2))
            )
            .withPool(
                LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(HAS_NO_SHEARS_OR_SILK_TOUCH)
                    .add(
                        this.applyExplosionDecay(
                                param0, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                            )
                            .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, NORMAL_LEAVES_STICK_CHANCES))
                    )
            );
    }

    protected LootTable.Builder createOakLeavesDrops(Block param0, Block param1, float... param2) {
        return this.createLeavesDrops(param0, param1, param2)
            .withPool(
                LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(HAS_NO_SHEARS_OR_SILK_TOUCH)
                    .add(
                        this.applyExplosionCondition(param0, LootItem.lootTableItem(Items.APPLE))
                            .when(
                                BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.005F, 0.0055555557F, 0.00625F, 0.008333334F, 0.025F)
                            )
                    )
            );
    }

    protected LootTable.Builder createMangroveLeavesDrops(Block param0) {
        return createSilkTouchOrShearsDispatchTable(
            param0,
            this.applyExplosionDecay(
                    Blocks.MANGROVE_LEAVES, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                )
                .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, NORMAL_LEAVES_STICK_CHANCES))
        );
    }

    protected LootTable.Builder createCropDrops(Block param0, Item param1, Item param2, LootItemCondition.Builder param3) {
        return this.applyExplosionDecay(
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

    protected static LootTable.Builder createDoublePlantShearsDrop(Block param0) {
        return LootTable.lootTable()
            .withPool(
                LootPool.lootPool().when(HAS_SHEARS).add(LootItem.lootTableItem(param0).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))))
            );
    }

    protected LootTable.Builder createDoublePlantWithSeedDrops(Block param0, Block param1) {
        LootPoolEntryContainer.Builder<?> var0 = LootItem.lootTableItem(param1)
            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)))
            .when(HAS_SHEARS)
            .otherwise(this.applyExplosionCondition(param0, LootItem.lootTableItem(Items.WHEAT_SEEDS)).when(LootItemRandomChanceCondition.randomChance(0.125F)));
        return LootTable.lootTable()
            .withPool(
                LootPool.lootPool()
                    .add(var0)
                    .when(
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))
                    )
                    .when(
                        LocationCheck.checkLocation(
                            LocationPredicate.Builder.location()
                                .setBlock(
                                    BlockPredicate.Builder.block()
                                        .of(param0)
                                        .setProperties(
                                            StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER).build()
                                        )
                                        .build()
                                ),
                            new BlockPos(0, 1, 0)
                        )
                    )
            )
            .withPool(
                LootPool.lootPool()
                    .add(var0)
                    .when(
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER))
                    )
                    .when(
                        LocationCheck.checkLocation(
                            LocationPredicate.Builder.location()
                                .setBlock(
                                    BlockPredicate.Builder.block()
                                        .of(param0)
                                        .setProperties(
                                            StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER).build()
                                        )
                                        .build()
                                ),
                            new BlockPos(0, -1, 0)
                        )
                    )
            );
    }

    protected LootTable.Builder createCandleDrops(Block param0) {
        return LootTable.lootTable()
            .withPool(
                LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(
                        this.applyExplosionDecay(
                            param0,
                            LootItem.lootTableItem(param0)
                                .apply(
                                    List.of(2, 3, 4),
                                    param1 -> SetItemCountFunction.setCount(ConstantValue.exactly((float)param1.intValue()))
                                            .when(
                                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(param0)
                                                    .setProperties(
                                                        StatePropertiesPredicate.Builder.properties().hasProperty(CandleBlock.CANDLES, param1.intValue())
                                                    )
                                            )
                                )
                        )
                    )
            );
    }

    protected static LootTable.Builder createCandleCakeDrops(Block param0) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(param0)));
    }

    public static LootTable.Builder noDrop() {
        return LootTable.lootTable();
    }

    protected abstract void generate();

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> param0) {
        this.generate();
        Set<ResourceLocation> var0 = new HashSet<>();

        for(Block var1 : BuiltInRegistries.BLOCK) {
            if (var1.isEnabled(this.enabledFeatures)) {
                ResourceLocation var2 = var1.getLootTable();
                if (var2 != BuiltInLootTables.EMPTY && var0.add(var2)) {
                    LootTable.Builder var3 = this.map.remove(var2);
                    if (var3 == null) {
                        throw new IllegalStateException(
                            String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", var2, BuiltInRegistries.BLOCK.getKey(var1))
                        );
                    }

                    param0.accept(var2, var3);
                }
            }
        }

        if (!this.map.isEmpty()) {
            throw new IllegalStateException("Created block loot tables for non-blocks: " + this.map.keySet());
        }
    }

    protected void addNetherVinesDropTable(Block param0, Block param1) {
        LootTable.Builder var0 = createSilkTouchOrShearsDispatchTable(
            param0, LootItem.lootTableItem(param0).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.33F, 0.55F, 0.77F, 1.0F))
        );
        this.add(param0, var0);
        this.add(param1, var0);
    }

    protected LootTable.Builder createDoorTable(Block param0) {
        return this.createSinglePropConditionTable(param0, DoorBlock.HALF, DoubleBlockHalf.LOWER);
    }

    protected void dropPottedContents(Block param0) {
        this.add(param0, param0x -> this.createPotFlowerItemTable(((FlowerPotBlock)param0x).getContent()));
    }

    protected void otherWhenSilkTouch(Block param0, Block param1) {
        this.add(param0, createSilkTouchOnlyTable(param1));
    }

    protected void dropOther(Block param0, ItemLike param1) {
        this.add(param0, this.createSingleItemTable(param1));
    }

    protected void dropWhenSilkTouch(Block param0) {
        this.otherWhenSilkTouch(param0, param0);
    }

    protected void dropSelf(Block param0) {
        this.dropOther(param0, param0);
    }

    protected void add(Block param0, Function<Block, LootTable.Builder> param1) {
        this.add(param0, param1.apply(param0));
    }

    protected void add(Block param0, LootTable.Builder param1) {
        this.map.put(param0.getLootTable(), param1);
    }
}
