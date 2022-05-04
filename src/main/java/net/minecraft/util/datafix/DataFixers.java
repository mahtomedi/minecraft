package net.minecraft.util.datafix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.AbstractArrowPickupFix;
import net.minecraft.util.datafix.fixes.AddFlagIfNotPresentFix;
import net.minecraft.util.datafix.fixes.AddNewChoices;
import net.minecraft.util.datafix.fixes.AdvancementsFix;
import net.minecraft.util.datafix.fixes.AdvancementsRenameFix;
import net.minecraft.util.datafix.fixes.AttributesRename;
import net.minecraft.util.datafix.fixes.BedItemColorFix;
import net.minecraft.util.datafix.fixes.BeehivePoiRenameFix;
import net.minecraft.util.datafix.fixes.BiomeFix;
import net.minecraft.util.datafix.fixes.BitStorageAlignFix;
import net.minecraft.util.datafix.fixes.BlendingDataFix;
import net.minecraft.util.datafix.fixes.BlockEntityBannerColorFix;
import net.minecraft.util.datafix.fixes.BlockEntityBlockStateFix;
import net.minecraft.util.datafix.fixes.BlockEntityCustomNameToComponentFix;
import net.minecraft.util.datafix.fixes.BlockEntityIdFix;
import net.minecraft.util.datafix.fixes.BlockEntityJukeboxFix;
import net.minecraft.util.datafix.fixes.BlockEntityKeepPacked;
import net.minecraft.util.datafix.fixes.BlockEntityShulkerBoxColorFix;
import net.minecraft.util.datafix.fixes.BlockEntitySignTextStrictJsonFix;
import net.minecraft.util.datafix.fixes.BlockEntityUUIDFix;
import net.minecraft.util.datafix.fixes.BlockNameFlatteningFix;
import net.minecraft.util.datafix.fixes.BlockRenameFix;
import net.minecraft.util.datafix.fixes.BlockRenameFixWithJigsaw;
import net.minecraft.util.datafix.fixes.BlockStateStructureTemplateFix;
import net.minecraft.util.datafix.fixes.CatTypeFix;
import net.minecraft.util.datafix.fixes.CauldronRenameFix;
import net.minecraft.util.datafix.fixes.CavesAndCliffsRenames;
import net.minecraft.util.datafix.fixes.ChunkBedBlockEntityInjecterFix;
import net.minecraft.util.datafix.fixes.ChunkBiomeFix;
import net.minecraft.util.datafix.fixes.ChunkDeleteIgnoredLightDataFix;
import net.minecraft.util.datafix.fixes.ChunkHeightAndBiomeFix;
import net.minecraft.util.datafix.fixes.ChunkLightRemoveFix;
import net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix;
import net.minecraft.util.datafix.fixes.ChunkProtoTickListFix;
import net.minecraft.util.datafix.fixes.ChunkRenamesFix;
import net.minecraft.util.datafix.fixes.ChunkStatusFix;
import net.minecraft.util.datafix.fixes.ChunkStatusFix2;
import net.minecraft.util.datafix.fixes.ChunkStructuresTemplateRenameFix;
import net.minecraft.util.datafix.fixes.ChunkToProtochunkFix;
import net.minecraft.util.datafix.fixes.ColorlessShulkerEntityFix;
import net.minecraft.util.datafix.fixes.CriteriaRenameFix;
import net.minecraft.util.datafix.fixes.DyeItemRenameFix;
import net.minecraft.util.datafix.fixes.EntityArmorStandSilentFix;
import net.minecraft.util.datafix.fixes.EntityBlockStateFix;
import net.minecraft.util.datafix.fixes.EntityCatSplitFix;
import net.minecraft.util.datafix.fixes.EntityCodSalmonFix;
import net.minecraft.util.datafix.fixes.EntityCustomNameToComponentFix;
import net.minecraft.util.datafix.fixes.EntityElderGuardianSplitFix;
import net.minecraft.util.datafix.fixes.EntityEquipmentToArmorAndHandFix;
import net.minecraft.util.datafix.fixes.EntityHealthFix;
import net.minecraft.util.datafix.fixes.EntityHorseSaddleFix;
import net.minecraft.util.datafix.fixes.EntityHorseSplitFix;
import net.minecraft.util.datafix.fixes.EntityIdFix;
import net.minecraft.util.datafix.fixes.EntityItemFrameDirectionFix;
import net.minecraft.util.datafix.fixes.EntityMinecartIdentifiersFix;
import net.minecraft.util.datafix.fixes.EntityPaintingFieldsRenameFix;
import net.minecraft.util.datafix.fixes.EntityPaintingItemFrameDirectionFix;
import net.minecraft.util.datafix.fixes.EntityPaintingMotiveFix;
import net.minecraft.util.datafix.fixes.EntityProjectileOwnerFix;
import net.minecraft.util.datafix.fixes.EntityPufferfishRenameFix;
import net.minecraft.util.datafix.fixes.EntityRavagerRenameFix;
import net.minecraft.util.datafix.fixes.EntityRedundantChanceTagsFix;
import net.minecraft.util.datafix.fixes.EntityRidingToPassengersFix;
import net.minecraft.util.datafix.fixes.EntityShulkerColorFix;
import net.minecraft.util.datafix.fixes.EntityShulkerRotationFix;
import net.minecraft.util.datafix.fixes.EntitySkeletonSplitFix;
import net.minecraft.util.datafix.fixes.EntityStringUuidFix;
import net.minecraft.util.datafix.fixes.EntityTheRenameningFix;
import net.minecraft.util.datafix.fixes.EntityTippedArrowFix;
import net.minecraft.util.datafix.fixes.EntityUUIDFix;
import net.minecraft.util.datafix.fixes.EntityVariantFix;
import net.minecraft.util.datafix.fixes.EntityWolfColorFix;
import net.minecraft.util.datafix.fixes.EntityZombieSplitFix;
import net.minecraft.util.datafix.fixes.EntityZombieVillagerTypeFix;
import net.minecraft.util.datafix.fixes.EntityZombifiedPiglinRenameFix;
import net.minecraft.util.datafix.fixes.ForcePoiRebuild;
import net.minecraft.util.datafix.fixes.FurnaceRecipeFix;
import net.minecraft.util.datafix.fixes.GoatHornIdFix;
import net.minecraft.util.datafix.fixes.GossipUUIDFix;
import net.minecraft.util.datafix.fixes.HeightmapRenamingFix;
import net.minecraft.util.datafix.fixes.IglooMetadataRemovalFix;
import net.minecraft.util.datafix.fixes.ItemBannerColorFix;
import net.minecraft.util.datafix.fixes.ItemCustomNameToComponentFix;
import net.minecraft.util.datafix.fixes.ItemIdFix;
import net.minecraft.util.datafix.fixes.ItemLoreFix;
import net.minecraft.util.datafix.fixes.ItemPotionFix;
import net.minecraft.util.datafix.fixes.ItemRenameFix;
import net.minecraft.util.datafix.fixes.ItemShulkerBoxColorFix;
import net.minecraft.util.datafix.fixes.ItemSpawnEggFix;
import net.minecraft.util.datafix.fixes.ItemStackEnchantmentNamesFix;
import net.minecraft.util.datafix.fixes.ItemStackMapIdFix;
import net.minecraft.util.datafix.fixes.ItemStackSpawnEggFix;
import net.minecraft.util.datafix.fixes.ItemStackTheFlatteningFix;
import net.minecraft.util.datafix.fixes.ItemStackUUIDFix;
import net.minecraft.util.datafix.fixes.ItemWaterPotionFix;
import net.minecraft.util.datafix.fixes.ItemWrittenBookPagesStrictJsonFix;
import net.minecraft.util.datafix.fixes.JigsawPropertiesFix;
import net.minecraft.util.datafix.fixes.JigsawRotationFix;
import net.minecraft.util.datafix.fixes.LeavesFix;
import net.minecraft.util.datafix.fixes.LevelDataGeneratorOptionsFix;
import net.minecraft.util.datafix.fixes.LevelFlatGeneratorInfoFix;
import net.minecraft.util.datafix.fixes.LevelUUIDFix;
import net.minecraft.util.datafix.fixes.MapIdFix;
import net.minecraft.util.datafix.fixes.MemoryExpiryDataFix;
import net.minecraft.util.datafix.fixes.MissingDimensionFix;
import net.minecraft.util.datafix.fixes.MobSpawnerEntityIdentifiersFix;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.NewVillageFix;
import net.minecraft.util.datafix.fixes.ObjectiveDisplayNameFix;
import net.minecraft.util.datafix.fixes.ObjectiveRenderTypeFix;
import net.minecraft.util.datafix.fixes.OminousBannerBlockEntityRenameFix;
import net.minecraft.util.datafix.fixes.OminousBannerRenameFix;
import net.minecraft.util.datafix.fixes.OptionsAddTextBackgroundFix;
import net.minecraft.util.datafix.fixes.OptionsForceVBOFix;
import net.minecraft.util.datafix.fixes.OptionsKeyLwjgl3Fix;
import net.minecraft.util.datafix.fixes.OptionsKeyTranslationFix;
import net.minecraft.util.datafix.fixes.OptionsLowerCaseLanguageFix;
import net.minecraft.util.datafix.fixes.OptionsRenameFieldFix;
import net.minecraft.util.datafix.fixes.OverreachingTickFix;
import net.minecraft.util.datafix.fixes.PlayerUUIDFix;
import net.minecraft.util.datafix.fixes.RecipesFix;
import net.minecraft.util.datafix.fixes.RecipesRenameFix;
import net.minecraft.util.datafix.fixes.RecipesRenameningFix;
import net.minecraft.util.datafix.fixes.RedstoneWireConnectionsFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.fixes.RemoveGolemGossipFix;
import net.minecraft.util.datafix.fixes.RenameBiomesFix;
import net.minecraft.util.datafix.fixes.RenamedCoralFansFix;
import net.minecraft.util.datafix.fixes.RenamedCoralFix;
import net.minecraft.util.datafix.fixes.ReorganizePoi;
import net.minecraft.util.datafix.fixes.SavedDataFeaturePoolElementFix;
import net.minecraft.util.datafix.fixes.SavedDataUUIDFix;
import net.minecraft.util.datafix.fixes.SavedDataVillageCropFix;
import net.minecraft.util.datafix.fixes.SimpleRenameFix;
import net.minecraft.util.datafix.fixes.SpawnerDataFix;
import net.minecraft.util.datafix.fixes.StatsCounterFix;
import net.minecraft.util.datafix.fixes.StatsRenameFix;
import net.minecraft.util.datafix.fixes.StriderGravityFix;
import net.minecraft.util.datafix.fixes.StructureReferenceCountFix;
import net.minecraft.util.datafix.fixes.StructureSettingsFlattenFix;
import net.minecraft.util.datafix.fixes.StructuresBecomeConfiguredFix;
import net.minecraft.util.datafix.fixes.TeamDisplayNameFix;
import net.minecraft.util.datafix.fixes.TrappedChestBlockEntityFix;
import net.minecraft.util.datafix.fixes.VillagerDataFix;
import net.minecraft.util.datafix.fixes.VillagerFollowRangeFix;
import net.minecraft.util.datafix.fixes.VillagerRebuildLevelAndXpFix;
import net.minecraft.util.datafix.fixes.VillagerTradeFix;
import net.minecraft.util.datafix.fixes.WallPropertyFix;
import net.minecraft.util.datafix.fixes.WeaponSmithChestLootTableFix;
import net.minecraft.util.datafix.fixes.WorldGenSettingsDisallowOldCustomWorldsFix;
import net.minecraft.util.datafix.fixes.WorldGenSettingsFix;
import net.minecraft.util.datafix.fixes.WorldGenSettingsHeightAndBiomeFix;
import net.minecraft.util.datafix.fixes.WriteAndReadFix;
import net.minecraft.util.datafix.fixes.ZombieVillagerRebuildXpFix;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.util.datafix.schemas.V100;
import net.minecraft.util.datafix.schemas.V102;
import net.minecraft.util.datafix.schemas.V1022;
import net.minecraft.util.datafix.schemas.V106;
import net.minecraft.util.datafix.schemas.V107;
import net.minecraft.util.datafix.schemas.V1125;
import net.minecraft.util.datafix.schemas.V135;
import net.minecraft.util.datafix.schemas.V143;
import net.minecraft.util.datafix.schemas.V1451;
import net.minecraft.util.datafix.schemas.V1451_1;
import net.minecraft.util.datafix.schemas.V1451_2;
import net.minecraft.util.datafix.schemas.V1451_3;
import net.minecraft.util.datafix.schemas.V1451_4;
import net.minecraft.util.datafix.schemas.V1451_5;
import net.minecraft.util.datafix.schemas.V1451_6;
import net.minecraft.util.datafix.schemas.V1451_7;
import net.minecraft.util.datafix.schemas.V1460;
import net.minecraft.util.datafix.schemas.V1466;
import net.minecraft.util.datafix.schemas.V1470;
import net.minecraft.util.datafix.schemas.V1481;
import net.minecraft.util.datafix.schemas.V1483;
import net.minecraft.util.datafix.schemas.V1486;
import net.minecraft.util.datafix.schemas.V1510;
import net.minecraft.util.datafix.schemas.V1800;
import net.minecraft.util.datafix.schemas.V1801;
import net.minecraft.util.datafix.schemas.V1904;
import net.minecraft.util.datafix.schemas.V1906;
import net.minecraft.util.datafix.schemas.V1909;
import net.minecraft.util.datafix.schemas.V1920;
import net.minecraft.util.datafix.schemas.V1928;
import net.minecraft.util.datafix.schemas.V1929;
import net.minecraft.util.datafix.schemas.V1931;
import net.minecraft.util.datafix.schemas.V2100;
import net.minecraft.util.datafix.schemas.V2501;
import net.minecraft.util.datafix.schemas.V2502;
import net.minecraft.util.datafix.schemas.V2505;
import net.minecraft.util.datafix.schemas.V2509;
import net.minecraft.util.datafix.schemas.V2519;
import net.minecraft.util.datafix.schemas.V2522;
import net.minecraft.util.datafix.schemas.V2551;
import net.minecraft.util.datafix.schemas.V2568;
import net.minecraft.util.datafix.schemas.V2571;
import net.minecraft.util.datafix.schemas.V2684;
import net.minecraft.util.datafix.schemas.V2686;
import net.minecraft.util.datafix.schemas.V2688;
import net.minecraft.util.datafix.schemas.V2704;
import net.minecraft.util.datafix.schemas.V2707;
import net.minecraft.util.datafix.schemas.V2831;
import net.minecraft.util.datafix.schemas.V2832;
import net.minecraft.util.datafix.schemas.V2842;
import net.minecraft.util.datafix.schemas.V3076;
import net.minecraft.util.datafix.schemas.V3078;
import net.minecraft.util.datafix.schemas.V3081;
import net.minecraft.util.datafix.schemas.V3082;
import net.minecraft.util.datafix.schemas.V3083;
import net.minecraft.util.datafix.schemas.V501;
import net.minecraft.util.datafix.schemas.V700;
import net.minecraft.util.datafix.schemas.V701;
import net.minecraft.util.datafix.schemas.V702;
import net.minecraft.util.datafix.schemas.V703;
import net.minecraft.util.datafix.schemas.V704;
import net.minecraft.util.datafix.schemas.V705;
import net.minecraft.util.datafix.schemas.V808;
import net.minecraft.util.datafix.schemas.V99;
import org.slf4j.Logger;

public class DataFixers {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final BiFunction<Integer, Schema, Schema> SAME = Schema::new;
    private static final BiFunction<Integer, Schema, Schema> SAME_NAMESPACED = NamespacedSchema::new;
    private static final DataFixer dataFixer = createFixerUpper();
    public static final int BLENDING_VERSION = 3088;

    private DataFixers() {
    }

    public static DataFixer getDataFixer() {
        return dataFixer;
    }

    private static synchronized DataFixer createFixerUpper() {
        DataFixerBuilder var0 = new DataFixerBuilder(SharedConstants.getCurrentVersion().getWorldVersion());
        addFixers(var0);

        boolean var1 = switch(SharedConstants.DATAFIXER_OPTIMIZATION_OPTION) {
            case UNINITIALIZED_OPTIMIZED -> true;
            case UNINITIALIZED_UNOPTIMIZED -> false;
            default -> throw new IllegalStateException("Already loaded");
        };
        SharedConstants.DATAFIXER_OPTIMIZATION_OPTION = var1
            ? DataFixerOptimizationOption.INITIALIZED_OPTIMIZED
            : DataFixerOptimizationOption.INITIALIZED_UNOPTIMIZED;
        LOGGER.info("Building {} datafixer", var1 ? "optimized" : "unoptimized");
        return var1 ? var0.buildOptimized(Util.bootstrapExecutor()) : var0.buildUnoptimized();
    }

    private static void addFixers(DataFixerBuilder param0) {
        Schema var0 = param0.addSchema(99, V99::new);
        Schema var1 = param0.addSchema(100, V100::new);
        param0.addFixer(new EntityEquipmentToArmorAndHandFix(var1, true));
        Schema var2 = param0.addSchema(101, SAME);
        param0.addFixer(new BlockEntitySignTextStrictJsonFix(var2, false));
        Schema var3 = param0.addSchema(102, V102::new);
        param0.addFixer(new ItemIdFix(var3, true));
        param0.addFixer(new ItemPotionFix(var3, false));
        Schema var4 = param0.addSchema(105, SAME);
        param0.addFixer(new ItemSpawnEggFix(var4, true));
        Schema var5 = param0.addSchema(106, V106::new);
        param0.addFixer(new MobSpawnerEntityIdentifiersFix(var5, true));
        Schema var6 = param0.addSchema(107, V107::new);
        param0.addFixer(new EntityMinecartIdentifiersFix(var6, true));
        Schema var7 = param0.addSchema(108, SAME);
        param0.addFixer(new EntityStringUuidFix(var7, true));
        Schema var8 = param0.addSchema(109, SAME);
        param0.addFixer(new EntityHealthFix(var8, true));
        Schema var9 = param0.addSchema(110, SAME);
        param0.addFixer(new EntityHorseSaddleFix(var9, true));
        Schema var10 = param0.addSchema(111, SAME);
        param0.addFixer(new EntityPaintingItemFrameDirectionFix(var10, true));
        Schema var11 = param0.addSchema(113, SAME);
        param0.addFixer(new EntityRedundantChanceTagsFix(var11, true));
        Schema var12 = param0.addSchema(135, V135::new);
        param0.addFixer(new EntityRidingToPassengersFix(var12, true));
        Schema var13 = param0.addSchema(143, V143::new);
        param0.addFixer(new EntityTippedArrowFix(var13, true));
        Schema var14 = param0.addSchema(147, SAME);
        param0.addFixer(new EntityArmorStandSilentFix(var14, true));
        Schema var15 = param0.addSchema(165, SAME);
        param0.addFixer(new ItemWrittenBookPagesStrictJsonFix(var15, true));
        Schema var16 = param0.addSchema(501, V501::new);
        param0.addFixer(new AddNewChoices(var16, "Add 1.10 entities fix", References.ENTITY));
        Schema var17 = param0.addSchema(502, SAME);
        param0.addFixer(
            ItemRenameFix.create(
                var17,
                "cooked_fished item renamer",
                param0x -> Objects.equals(NamespacedSchema.ensureNamespaced(param0x), "minecraft:cooked_fished") ? "minecraft:cooked_fish" : param0x
            )
        );
        param0.addFixer(new EntityZombieVillagerTypeFix(var17, false));
        Schema var18 = param0.addSchema(505, SAME);
        param0.addFixer(new OptionsForceVBOFix(var18, false));
        Schema var19 = param0.addSchema(700, V700::new);
        param0.addFixer(new EntityElderGuardianSplitFix(var19, true));
        Schema var20 = param0.addSchema(701, V701::new);
        param0.addFixer(new EntitySkeletonSplitFix(var20, true));
        Schema var21 = param0.addSchema(702, V702::new);
        param0.addFixer(new EntityZombieSplitFix(var21, true));
        Schema var22 = param0.addSchema(703, V703::new);
        param0.addFixer(new EntityHorseSplitFix(var22, true));
        Schema var23 = param0.addSchema(704, V704::new);
        param0.addFixer(new BlockEntityIdFix(var23, true));
        Schema var24 = param0.addSchema(705, V705::new);
        param0.addFixer(new EntityIdFix(var24, true));
        Schema var25 = param0.addSchema(804, SAME_NAMESPACED);
        param0.addFixer(new ItemBannerColorFix(var25, true));
        Schema var26 = param0.addSchema(806, SAME_NAMESPACED);
        param0.addFixer(new ItemWaterPotionFix(var26, false));
        Schema var27 = param0.addSchema(808, V808::new);
        param0.addFixer(new AddNewChoices(var27, "added shulker box", References.BLOCK_ENTITY));
        Schema var28 = param0.addSchema(808, 1, SAME_NAMESPACED);
        param0.addFixer(new EntityShulkerColorFix(var28, false));
        Schema var29 = param0.addSchema(813, SAME_NAMESPACED);
        param0.addFixer(new ItemShulkerBoxColorFix(var29, false));
        param0.addFixer(new BlockEntityShulkerBoxColorFix(var29, false));
        Schema var30 = param0.addSchema(816, SAME_NAMESPACED);
        param0.addFixer(new OptionsLowerCaseLanguageFix(var30, false));
        Schema var31 = param0.addSchema(820, SAME_NAMESPACED);
        param0.addFixer(ItemRenameFix.create(var31, "totem item renamer", createRenamer("minecraft:totem", "minecraft:totem_of_undying")));
        Schema var32 = param0.addSchema(1022, V1022::new);
        param0.addFixer(new WriteAndReadFix(var32, "added shoulder entities to players", References.PLAYER));
        Schema var33 = param0.addSchema(1125, V1125::new);
        param0.addFixer(new ChunkBedBlockEntityInjecterFix(var33, true));
        param0.addFixer(new BedItemColorFix(var33, false));
        Schema var34 = param0.addSchema(1344, SAME_NAMESPACED);
        param0.addFixer(new OptionsKeyLwjgl3Fix(var34, false));
        Schema var35 = param0.addSchema(1446, SAME_NAMESPACED);
        param0.addFixer(new OptionsKeyTranslationFix(var35, false));
        Schema var36 = param0.addSchema(1450, SAME_NAMESPACED);
        param0.addFixer(new BlockStateStructureTemplateFix(var36, false));
        Schema var37 = param0.addSchema(1451, V1451::new);
        param0.addFixer(new AddNewChoices(var37, "AddTrappedChestFix", References.BLOCK_ENTITY));
        Schema var38 = param0.addSchema(1451, 1, V1451_1::new);
        param0.addFixer(new ChunkPalettedStorageFix(var38, true));
        Schema var39 = param0.addSchema(1451, 2, V1451_2::new);
        param0.addFixer(new BlockEntityBlockStateFix(var39, true));
        Schema var40 = param0.addSchema(1451, 3, V1451_3::new);
        param0.addFixer(new EntityBlockStateFix(var40, true));
        param0.addFixer(new ItemStackMapIdFix(var40, false));
        Schema var41 = param0.addSchema(1451, 4, V1451_4::new);
        param0.addFixer(new BlockNameFlatteningFix(var41, true));
        param0.addFixer(new ItemStackTheFlatteningFix(var41, false));
        Schema var42 = param0.addSchema(1451, 5, V1451_5::new);
        param0.addFixer(new AddNewChoices(var42, "RemoveNoteBlockFlowerPotFix", References.BLOCK_ENTITY));
        param0.addFixer(new ItemStackSpawnEggFix(var42, false));
        param0.addFixer(new EntityWolfColorFix(var42, false));
        param0.addFixer(new BlockEntityBannerColorFix(var42, false));
        param0.addFixer(new LevelFlatGeneratorInfoFix(var42, false));
        Schema var43 = param0.addSchema(1451, 6, V1451_6::new);
        param0.addFixer(new StatsCounterFix(var43, true));
        param0.addFixer(new WriteAndReadFix(var43, "Rewrite objectives", References.OBJECTIVE));
        param0.addFixer(new BlockEntityJukeboxFix(var43, false));
        Schema var44 = param0.addSchema(1451, 7, V1451_7::new);
        param0.addFixer(new SavedDataVillageCropFix(var44, true));
        Schema var45 = param0.addSchema(1451, 7, SAME_NAMESPACED);
        param0.addFixer(new VillagerTradeFix(var45, false));
        Schema var46 = param0.addSchema(1456, SAME_NAMESPACED);
        param0.addFixer(new EntityItemFrameDirectionFix(var46, false));
        Schema var47 = param0.addSchema(1458, SAME_NAMESPACED);
        param0.addFixer(new EntityCustomNameToComponentFix(var47, false));
        param0.addFixer(new ItemCustomNameToComponentFix(var47, false));
        param0.addFixer(new BlockEntityCustomNameToComponentFix(var47, false));
        Schema var48 = param0.addSchema(1460, V1460::new);
        param0.addFixer(new EntityPaintingMotiveFix(var48, false));
        Schema var49 = param0.addSchema(1466, V1466::new);
        param0.addFixer(new ChunkToProtochunkFix(var49, true));
        Schema var50 = param0.addSchema(1470, V1470::new);
        param0.addFixer(new AddNewChoices(var50, "Add 1.13 entities fix", References.ENTITY));
        Schema var51 = param0.addSchema(1474, SAME_NAMESPACED);
        param0.addFixer(new ColorlessShulkerEntityFix(var51, false));
        param0.addFixer(
            BlockRenameFix.create(
                var51,
                "Colorless shulker block fixer",
                param0x -> Objects.equals(NamespacedSchema.ensureNamespaced(param0x), "minecraft:purple_shulker_box") ? "minecraft:shulker_box" : param0x
            )
        );
        param0.addFixer(
            ItemRenameFix.create(
                var51,
                "Colorless shulker item fixer",
                param0x -> Objects.equals(NamespacedSchema.ensureNamespaced(param0x), "minecraft:purple_shulker_box") ? "minecraft:shulker_box" : param0x
            )
        );
        Schema var52 = param0.addSchema(1475, SAME_NAMESPACED);
        param0.addFixer(
            BlockRenameFix.create(
                var52,
                "Flowing fixer",
                createRenamer(ImmutableMap.of("minecraft:flowing_water", "minecraft:water", "minecraft:flowing_lava", "minecraft:lava"))
            )
        );
        Schema var53 = param0.addSchema(1480, SAME_NAMESPACED);
        param0.addFixer(BlockRenameFix.create(var53, "Rename coral blocks", createRenamer(RenamedCoralFix.RENAMED_IDS)));
        param0.addFixer(ItemRenameFix.create(var53, "Rename coral items", createRenamer(RenamedCoralFix.RENAMED_IDS)));
        Schema var54 = param0.addSchema(1481, V1481::new);
        param0.addFixer(new AddNewChoices(var54, "Add conduit", References.BLOCK_ENTITY));
        Schema var55 = param0.addSchema(1483, V1483::new);
        param0.addFixer(new EntityPufferfishRenameFix(var55, true));
        param0.addFixer(ItemRenameFix.create(var55, "Rename pufferfish egg item", createRenamer(EntityPufferfishRenameFix.RENAMED_IDS)));
        Schema var56 = param0.addSchema(1484, SAME_NAMESPACED);
        param0.addFixer(
            ItemRenameFix.create(
                var56,
                "Rename seagrass items",
                createRenamer(ImmutableMap.of("minecraft:sea_grass", "minecraft:seagrass", "minecraft:tall_sea_grass", "minecraft:tall_seagrass"))
            )
        );
        param0.addFixer(
            BlockRenameFix.create(
                var56,
                "Rename seagrass blocks",
                createRenamer(ImmutableMap.of("minecraft:sea_grass", "minecraft:seagrass", "minecraft:tall_sea_grass", "minecraft:tall_seagrass"))
            )
        );
        param0.addFixer(new HeightmapRenamingFix(var56, false));
        Schema var57 = param0.addSchema(1486, V1486::new);
        param0.addFixer(new EntityCodSalmonFix(var57, true));
        param0.addFixer(ItemRenameFix.create(var57, "Rename cod/salmon egg items", createRenamer(EntityCodSalmonFix.RENAMED_EGG_IDS)));
        Schema var58 = param0.addSchema(1487, SAME_NAMESPACED);
        param0.addFixer(
            ItemRenameFix.create(
                var58,
                "Rename prismarine_brick(s)_* blocks",
                createRenamer(
                    ImmutableMap.of(
                        "minecraft:prismarine_bricks_slab",
                        "minecraft:prismarine_brick_slab",
                        "minecraft:prismarine_bricks_stairs",
                        "minecraft:prismarine_brick_stairs"
                    )
                )
            )
        );
        param0.addFixer(
            BlockRenameFix.create(
                var58,
                "Rename prismarine_brick(s)_* items",
                createRenamer(
                    ImmutableMap.of(
                        "minecraft:prismarine_bricks_slab",
                        "minecraft:prismarine_brick_slab",
                        "minecraft:prismarine_bricks_stairs",
                        "minecraft:prismarine_brick_stairs"
                    )
                )
            )
        );
        Schema var59 = param0.addSchema(1488, SAME_NAMESPACED);
        param0.addFixer(
            BlockRenameFix.create(
                var59, "Rename kelp/kelptop", createRenamer(ImmutableMap.of("minecraft:kelp_top", "minecraft:kelp", "minecraft:kelp", "minecraft:kelp_plant"))
            )
        );
        param0.addFixer(ItemRenameFix.create(var59, "Rename kelptop", createRenamer("minecraft:kelp_top", "minecraft:kelp")));
        param0.addFixer(new NamedEntityFix(var59, false, "Command block block entity custom name fix", References.BLOCK_ENTITY, "minecraft:command_block") {
            @Override
            protected Typed<?> fix(Typed<?> param0) {
                return param0.update(DSL.remainderFinder(), EntityCustomNameToComponentFix::fixTagCustomName);
            }
        });
        param0.addFixer(new NamedEntityFix(var59, false, "Command block minecart custom name fix", References.ENTITY, "minecraft:commandblock_minecart") {
            @Override
            protected Typed<?> fix(Typed<?> param0) {
                return param0.update(DSL.remainderFinder(), EntityCustomNameToComponentFix::fixTagCustomName);
            }
        });
        param0.addFixer(new IglooMetadataRemovalFix(var59, false));
        Schema var60 = param0.addSchema(1490, SAME_NAMESPACED);
        param0.addFixer(BlockRenameFix.create(var60, "Rename melon_block", createRenamer("minecraft:melon_block", "minecraft:melon")));
        param0.addFixer(
            ItemRenameFix.create(
                var60,
                "Rename melon_block/melon/speckled_melon",
                createRenamer(
                    ImmutableMap.of(
                        "minecraft:melon_block",
                        "minecraft:melon",
                        "minecraft:melon",
                        "minecraft:melon_slice",
                        "minecraft:speckled_melon",
                        "minecraft:glistering_melon_slice"
                    )
                )
            )
        );
        Schema var61 = param0.addSchema(1492, SAME_NAMESPACED);
        param0.addFixer(new ChunkStructuresTemplateRenameFix(var61, false));
        Schema var62 = param0.addSchema(1494, SAME_NAMESPACED);
        param0.addFixer(new ItemStackEnchantmentNamesFix(var62, false));
        Schema var63 = param0.addSchema(1496, SAME_NAMESPACED);
        param0.addFixer(new LeavesFix(var63, false));
        Schema var64 = param0.addSchema(1500, SAME_NAMESPACED);
        param0.addFixer(new BlockEntityKeepPacked(var64, false));
        Schema var65 = param0.addSchema(1501, SAME_NAMESPACED);
        param0.addFixer(new AdvancementsFix(var65, false));
        Schema var66 = param0.addSchema(1502, SAME_NAMESPACED);
        param0.addFixer(new RecipesFix(var66, false));
        Schema var67 = param0.addSchema(1506, SAME_NAMESPACED);
        param0.addFixer(new LevelDataGeneratorOptionsFix(var67, false));
        Schema var68 = param0.addSchema(1510, V1510::new);
        param0.addFixer(BlockRenameFix.create(var68, "Block renamening fix", createRenamer(EntityTheRenameningFix.RENAMED_BLOCKS)));
        param0.addFixer(ItemRenameFix.create(var68, "Item renamening fix", createRenamer(EntityTheRenameningFix.RENAMED_ITEMS)));
        param0.addFixer(new RecipesRenameningFix(var68, false));
        param0.addFixer(new EntityTheRenameningFix(var68, true));
        param0.addFixer(
            new StatsRenameFix(
                var68,
                "SwimStatsRenameFix",
                ImmutableMap.of("minecraft:swim_one_cm", "minecraft:walk_on_water_one_cm", "minecraft:dive_one_cm", "minecraft:walk_under_water_one_cm")
            )
        );
        Schema var69 = param0.addSchema(1514, SAME_NAMESPACED);
        param0.addFixer(new ObjectiveDisplayNameFix(var69, false));
        param0.addFixer(new TeamDisplayNameFix(var69, false));
        param0.addFixer(new ObjectiveRenderTypeFix(var69, false));
        Schema var70 = param0.addSchema(1515, SAME_NAMESPACED);
        param0.addFixer(BlockRenameFix.create(var70, "Rename coral fan blocks", createRenamer(RenamedCoralFansFix.RENAMED_IDS)));
        Schema var71 = param0.addSchema(1624, SAME_NAMESPACED);
        param0.addFixer(new TrappedChestBlockEntityFix(var71, false));
        Schema var72 = param0.addSchema(1800, V1800::new);
        param0.addFixer(new AddNewChoices(var72, "Added 1.14 mobs fix", References.ENTITY));
        param0.addFixer(ItemRenameFix.create(var72, "Rename dye items", createRenamer(DyeItemRenameFix.RENAMED_IDS)));
        Schema var73 = param0.addSchema(1801, V1801::new);
        param0.addFixer(new AddNewChoices(var73, "Added Illager Beast", References.ENTITY));
        Schema var74 = param0.addSchema(1802, SAME_NAMESPACED);
        param0.addFixer(
            BlockRenameFix.create(
                var74,
                "Rename sign blocks & stone slabs",
                createRenamer(
                    ImmutableMap.of(
                        "minecraft:stone_slab",
                        "minecraft:smooth_stone_slab",
                        "minecraft:sign",
                        "minecraft:oak_sign",
                        "minecraft:wall_sign",
                        "minecraft:oak_wall_sign"
                    )
                )
            )
        );
        param0.addFixer(
            ItemRenameFix.create(
                var74,
                "Rename sign item & stone slabs",
                createRenamer(ImmutableMap.of("minecraft:stone_slab", "minecraft:smooth_stone_slab", "minecraft:sign", "minecraft:oak_sign"))
            )
        );
        Schema var75 = param0.addSchema(1803, SAME_NAMESPACED);
        param0.addFixer(new ItemLoreFix(var75, false));
        Schema var76 = param0.addSchema(1904, V1904::new);
        param0.addFixer(new AddNewChoices(var76, "Added Cats", References.ENTITY));
        param0.addFixer(new EntityCatSplitFix(var76, false));
        Schema var77 = param0.addSchema(1905, SAME_NAMESPACED);
        param0.addFixer(new ChunkStatusFix(var77, false));
        Schema var78 = param0.addSchema(1906, V1906::new);
        param0.addFixer(new AddNewChoices(var78, "Add POI Blocks", References.BLOCK_ENTITY));
        Schema var79 = param0.addSchema(1909, V1909::new);
        param0.addFixer(new AddNewChoices(var79, "Add jigsaw", References.BLOCK_ENTITY));
        Schema var80 = param0.addSchema(1911, SAME_NAMESPACED);
        param0.addFixer(new ChunkStatusFix2(var80, false));
        Schema var81 = param0.addSchema(1914, SAME_NAMESPACED);
        param0.addFixer(new WeaponSmithChestLootTableFix(var81, false));
        Schema var82 = param0.addSchema(1917, SAME_NAMESPACED);
        param0.addFixer(new CatTypeFix(var82, false));
        Schema var83 = param0.addSchema(1918, SAME_NAMESPACED);
        param0.addFixer(new VillagerDataFix(var83, "minecraft:villager"));
        param0.addFixer(new VillagerDataFix(var83, "minecraft:zombie_villager"));
        Schema var84 = param0.addSchema(1920, V1920::new);
        param0.addFixer(new NewVillageFix(var84, false));
        param0.addFixer(new AddNewChoices(var84, "Add campfire", References.BLOCK_ENTITY));
        Schema var85 = param0.addSchema(1925, SAME_NAMESPACED);
        param0.addFixer(new MapIdFix(var85, false));
        Schema var86 = param0.addSchema(1928, V1928::new);
        param0.addFixer(new EntityRavagerRenameFix(var86, true));
        param0.addFixer(ItemRenameFix.create(var86, "Rename ravager egg item", createRenamer(EntityRavagerRenameFix.RENAMED_IDS)));
        Schema var87 = param0.addSchema(1929, V1929::new);
        param0.addFixer(new AddNewChoices(var87, "Add Wandering Trader and Trader Llama", References.ENTITY));
        Schema var88 = param0.addSchema(1931, V1931::new);
        param0.addFixer(new AddNewChoices(var88, "Added Fox", References.ENTITY));
        Schema var89 = param0.addSchema(1936, SAME_NAMESPACED);
        param0.addFixer(new OptionsAddTextBackgroundFix(var89, false));
        Schema var90 = param0.addSchema(1946, SAME_NAMESPACED);
        param0.addFixer(new ReorganizePoi(var90, false));
        Schema var91 = param0.addSchema(1948, SAME_NAMESPACED);
        param0.addFixer(new OminousBannerRenameFix(var91, false));
        Schema var92 = param0.addSchema(1953, SAME_NAMESPACED);
        param0.addFixer(new OminousBannerBlockEntityRenameFix(var92, false));
        Schema var93 = param0.addSchema(1955, SAME_NAMESPACED);
        param0.addFixer(new VillagerRebuildLevelAndXpFix(var93, false));
        param0.addFixer(new ZombieVillagerRebuildXpFix(var93, false));
        Schema var94 = param0.addSchema(1961, SAME_NAMESPACED);
        param0.addFixer(new ChunkLightRemoveFix(var94, false));
        Schema var95 = param0.addSchema(1963, SAME_NAMESPACED);
        param0.addFixer(new RemoveGolemGossipFix(var95, false));
        Schema var96 = param0.addSchema(2100, V2100::new);
        param0.addFixer(new AddNewChoices(var96, "Added Bee and Bee Stinger", References.ENTITY));
        param0.addFixer(new AddNewChoices(var96, "Add beehive", References.BLOCK_ENTITY));
        param0.addFixer(new RecipesRenameFix(var96, false, "Rename sugar recipe", createRenamer("minecraft:sugar", "sugar_from_sugar_cane")));
        param0.addFixer(
            new AdvancementsRenameFix(
                var96, false, "Rename sugar recipe advancement", createRenamer("minecraft:recipes/misc/sugar", "minecraft:recipes/misc/sugar_from_sugar_cane")
            )
        );
        Schema var97 = param0.addSchema(2202, SAME_NAMESPACED);
        param0.addFixer(new ChunkBiomeFix(var97, false));
        Schema var98 = param0.addSchema(2209, SAME_NAMESPACED);
        param0.addFixer(ItemRenameFix.create(var98, "Rename bee_hive item to beehive", createRenamer("minecraft:bee_hive", "minecraft:beehive")));
        param0.addFixer(new BeehivePoiRenameFix(var98));
        param0.addFixer(BlockRenameFix.create(var98, "Rename bee_hive block to beehive", createRenamer("minecraft:bee_hive", "minecraft:beehive")));
        Schema var99 = param0.addSchema(2211, SAME_NAMESPACED);
        param0.addFixer(new StructureReferenceCountFix(var99, false));
        Schema var100 = param0.addSchema(2218, SAME_NAMESPACED);
        param0.addFixer(new ForcePoiRebuild(var100, false));
        Schema var101 = param0.addSchema(2501, V2501::new);
        param0.addFixer(new FurnaceRecipeFix(var101, true));
        Schema var102 = param0.addSchema(2502, V2502::new);
        param0.addFixer(new AddNewChoices(var102, "Added Hoglin", References.ENTITY));
        Schema var103 = param0.addSchema(2503, SAME_NAMESPACED);
        param0.addFixer(new WallPropertyFix(var103, false));
        param0.addFixer(
            new AdvancementsRenameFix(
                var103, false, "Composter category change", createRenamer("minecraft:recipes/misc/composter", "minecraft:recipes/decorations/composter")
            )
        );
        Schema var104 = param0.addSchema(2505, V2505::new);
        param0.addFixer(new AddNewChoices(var104, "Added Piglin", References.ENTITY));
        param0.addFixer(new MemoryExpiryDataFix(var104, "minecraft:villager"));
        Schema var105 = param0.addSchema(2508, SAME_NAMESPACED);
        param0.addFixer(
            ItemRenameFix.create(
                var105,
                "Renamed fungi items to fungus",
                createRenamer(ImmutableMap.of("minecraft:warped_fungi", "minecraft:warped_fungus", "minecraft:crimson_fungi", "minecraft:crimson_fungus"))
            )
        );
        param0.addFixer(
            BlockRenameFix.create(
                var105,
                "Renamed fungi blocks to fungus",
                createRenamer(ImmutableMap.of("minecraft:warped_fungi", "minecraft:warped_fungus", "minecraft:crimson_fungi", "minecraft:crimson_fungus"))
            )
        );
        Schema var106 = param0.addSchema(2509, V2509::new);
        param0.addFixer(new EntityZombifiedPiglinRenameFix(var106));
        param0.addFixer(ItemRenameFix.create(var106, "Rename zombie pigman egg item", createRenamer(EntityZombifiedPiglinRenameFix.RENAMED_IDS)));
        Schema var107 = param0.addSchema(2511, SAME_NAMESPACED);
        param0.addFixer(new EntityProjectileOwnerFix(var107));
        Schema var108 = param0.addSchema(2514, SAME_NAMESPACED);
        param0.addFixer(new EntityUUIDFix(var108));
        param0.addFixer(new BlockEntityUUIDFix(var108));
        param0.addFixer(new PlayerUUIDFix(var108));
        param0.addFixer(new LevelUUIDFix(var108));
        param0.addFixer(new SavedDataUUIDFix(var108));
        param0.addFixer(new ItemStackUUIDFix(var108));
        Schema var109 = param0.addSchema(2516, SAME_NAMESPACED);
        param0.addFixer(new GossipUUIDFix(var109, "minecraft:villager"));
        param0.addFixer(new GossipUUIDFix(var109, "minecraft:zombie_villager"));
        Schema var110 = param0.addSchema(2518, SAME_NAMESPACED);
        param0.addFixer(new JigsawPropertiesFix(var110, false));
        param0.addFixer(new JigsawRotationFix(var110, false));
        Schema var111 = param0.addSchema(2519, V2519::new);
        param0.addFixer(new AddNewChoices(var111, "Added Strider", References.ENTITY));
        Schema var112 = param0.addSchema(2522, V2522::new);
        param0.addFixer(new AddNewChoices(var112, "Added Zoglin", References.ENTITY));
        Schema var113 = param0.addSchema(2523, SAME_NAMESPACED);
        param0.addFixer(new AttributesRename(var113));
        Schema var114 = param0.addSchema(2527, SAME_NAMESPACED);
        param0.addFixer(new BitStorageAlignFix(var114));
        Schema var115 = param0.addSchema(2528, SAME_NAMESPACED);
        param0.addFixer(
            ItemRenameFix.create(
                var115,
                "Rename soul fire torch and soul fire lantern",
                createRenamer(ImmutableMap.of("minecraft:soul_fire_torch", "minecraft:soul_torch", "minecraft:soul_fire_lantern", "minecraft:soul_lantern"))
            )
        );
        param0.addFixer(
            BlockRenameFix.create(
                var115,
                "Rename soul fire torch and soul fire lantern",
                createRenamer(
                    ImmutableMap.of(
                        "minecraft:soul_fire_torch",
                        "minecraft:soul_torch",
                        "minecraft:soul_fire_wall_torch",
                        "minecraft:soul_wall_torch",
                        "minecraft:soul_fire_lantern",
                        "minecraft:soul_lantern"
                    )
                )
            )
        );
        Schema var116 = param0.addSchema(2529, SAME_NAMESPACED);
        param0.addFixer(new StriderGravityFix(var116, false));
        Schema var117 = param0.addSchema(2531, SAME_NAMESPACED);
        param0.addFixer(new RedstoneWireConnectionsFix(var117));
        Schema var118 = param0.addSchema(2533, SAME_NAMESPACED);
        param0.addFixer(new VillagerFollowRangeFix(var118));
        Schema var119 = param0.addSchema(2535, SAME_NAMESPACED);
        param0.addFixer(new EntityShulkerRotationFix(var119));
        Schema var120 = param0.addSchema(2550, SAME_NAMESPACED);
        param0.addFixer(new WorldGenSettingsFix(var120));
        Schema var121 = param0.addSchema(2551, V2551::new);
        param0.addFixer(new WriteAndReadFix(var121, "add types to WorldGenData", References.WORLD_GEN_SETTINGS));
        Schema var122 = param0.addSchema(2552, SAME_NAMESPACED);
        param0.addFixer(new RenameBiomesFix(var122, false, "Nether biome rename", ImmutableMap.of("minecraft:nether", "minecraft:nether_wastes")));
        Schema var123 = param0.addSchema(2553, SAME_NAMESPACED);
        param0.addFixer(new BiomeFix(var123, false));
        Schema var124 = param0.addSchema(2558, SAME_NAMESPACED);
        param0.addFixer(new MissingDimensionFix(var124, false));
        param0.addFixer(new OptionsRenameFieldFix(var124, false, "Rename swapHands setting", "key_key.swapHands", "key_key.swapOffhand"));
        Schema var125 = param0.addSchema(2568, V2568::new);
        param0.addFixer(new AddNewChoices(var125, "Added Piglin Brute", References.ENTITY));
        Schema var126 = param0.addSchema(2571, V2571::new);
        param0.addFixer(new AddNewChoices(var126, "Added Goat", References.ENTITY));
        Schema var127 = param0.addSchema(2679, SAME_NAMESPACED);
        param0.addFixer(new CauldronRenameFix(var127, false));
        Schema var128 = param0.addSchema(2680, SAME_NAMESPACED);
        param0.addFixer(ItemRenameFix.create(var128, "Renamed grass path item to dirt path", createRenamer("minecraft:grass_path", "minecraft:dirt_path")));
        param0.addFixer(
            BlockRenameFixWithJigsaw.create(var128, "Renamed grass path block to dirt path", createRenamer("minecraft:grass_path", "minecraft:dirt_path"))
        );
        Schema var129 = param0.addSchema(2684, V2684::new);
        param0.addFixer(new AddNewChoices(var129, "Added Sculk Sensor", References.BLOCK_ENTITY));
        Schema var130 = param0.addSchema(2686, V2686::new);
        param0.addFixer(new AddNewChoices(var130, "Added Axolotl", References.ENTITY));
        Schema var131 = param0.addSchema(2688, V2688::new);
        param0.addFixer(new AddNewChoices(var131, "Added Glow Squid", References.ENTITY));
        param0.addFixer(new AddNewChoices(var131, "Added Glow Item Frame", References.ENTITY));
        Schema var132 = param0.addSchema(2690, SAME_NAMESPACED);
        ImmutableMap<String, String> var133 = ImmutableMap.<String, String>builder()
            .put("minecraft:weathered_copper_block", "minecraft:oxidized_copper_block")
            .put("minecraft:semi_weathered_copper_block", "minecraft:weathered_copper_block")
            .put("minecraft:lightly_weathered_copper_block", "minecraft:exposed_copper_block")
            .put("minecraft:weathered_cut_copper", "minecraft:oxidized_cut_copper")
            .put("minecraft:semi_weathered_cut_copper", "minecraft:weathered_cut_copper")
            .put("minecraft:lightly_weathered_cut_copper", "minecraft:exposed_cut_copper")
            .put("minecraft:weathered_cut_copper_stairs", "minecraft:oxidized_cut_copper_stairs")
            .put("minecraft:semi_weathered_cut_copper_stairs", "minecraft:weathered_cut_copper_stairs")
            .put("minecraft:lightly_weathered_cut_copper_stairs", "minecraft:exposed_cut_copper_stairs")
            .put("minecraft:weathered_cut_copper_slab", "minecraft:oxidized_cut_copper_slab")
            .put("minecraft:semi_weathered_cut_copper_slab", "minecraft:weathered_cut_copper_slab")
            .put("minecraft:lightly_weathered_cut_copper_slab", "minecraft:exposed_cut_copper_slab")
            .put("minecraft:waxed_semi_weathered_copper", "minecraft:waxed_weathered_copper")
            .put("minecraft:waxed_lightly_weathered_copper", "minecraft:waxed_exposed_copper")
            .put("minecraft:waxed_semi_weathered_cut_copper", "minecraft:waxed_weathered_cut_copper")
            .put("minecraft:waxed_lightly_weathered_cut_copper", "minecraft:waxed_exposed_cut_copper")
            .put("minecraft:waxed_semi_weathered_cut_copper_stairs", "minecraft:waxed_weathered_cut_copper_stairs")
            .put("minecraft:waxed_lightly_weathered_cut_copper_stairs", "minecraft:waxed_exposed_cut_copper_stairs")
            .put("minecraft:waxed_semi_weathered_cut_copper_slab", "minecraft:waxed_weathered_cut_copper_slab")
            .put("minecraft:waxed_lightly_weathered_cut_copper_slab", "minecraft:waxed_exposed_cut_copper_slab")
            .build();
        param0.addFixer(ItemRenameFix.create(var132, "Renamed copper block items to new oxidized terms", createRenamer(var133)));
        param0.addFixer(BlockRenameFixWithJigsaw.create(var132, "Renamed copper blocks to new oxidized terms", createRenamer(var133)));
        Schema var134 = param0.addSchema(2691, SAME_NAMESPACED);
        ImmutableMap<String, String> var135 = ImmutableMap.<String, String>builder()
            .put("minecraft:waxed_copper", "minecraft:waxed_copper_block")
            .put("minecraft:oxidized_copper_block", "minecraft:oxidized_copper")
            .put("minecraft:weathered_copper_block", "minecraft:weathered_copper")
            .put("minecraft:exposed_copper_block", "minecraft:exposed_copper")
            .build();
        param0.addFixer(ItemRenameFix.create(var134, "Rename copper item suffixes", createRenamer(var135)));
        param0.addFixer(BlockRenameFixWithJigsaw.create(var134, "Rename copper blocks suffixes", createRenamer(var135)));
        Schema var136 = param0.addSchema(2693, SAME_NAMESPACED);
        param0.addFixer(new AddFlagIfNotPresentFix(var136, References.WORLD_GEN_SETTINGS, "has_increased_height_already", false));
        Schema var137 = param0.addSchema(2696, SAME_NAMESPACED);
        ImmutableMap<String, String> var138 = ImmutableMap.<String, String>builder()
            .put("minecraft:grimstone", "minecraft:deepslate")
            .put("minecraft:grimstone_slab", "minecraft:cobbled_deepslate_slab")
            .put("minecraft:grimstone_stairs", "minecraft:cobbled_deepslate_stairs")
            .put("minecraft:grimstone_wall", "minecraft:cobbled_deepslate_wall")
            .put("minecraft:polished_grimstone", "minecraft:polished_deepslate")
            .put("minecraft:polished_grimstone_slab", "minecraft:polished_deepslate_slab")
            .put("minecraft:polished_grimstone_stairs", "minecraft:polished_deepslate_stairs")
            .put("minecraft:polished_grimstone_wall", "minecraft:polished_deepslate_wall")
            .put("minecraft:grimstone_tiles", "minecraft:deepslate_tiles")
            .put("minecraft:grimstone_tile_slab", "minecraft:deepslate_tile_slab")
            .put("minecraft:grimstone_tile_stairs", "minecraft:deepslate_tile_stairs")
            .put("minecraft:grimstone_tile_wall", "minecraft:deepslate_tile_wall")
            .put("minecraft:grimstone_bricks", "minecraft:deepslate_bricks")
            .put("minecraft:grimstone_brick_slab", "minecraft:deepslate_brick_slab")
            .put("minecraft:grimstone_brick_stairs", "minecraft:deepslate_brick_stairs")
            .put("minecraft:grimstone_brick_wall", "minecraft:deepslate_brick_wall")
            .put("minecraft:chiseled_grimstone", "minecraft:chiseled_deepslate")
            .build();
        param0.addFixer(ItemRenameFix.create(var137, "Renamed grimstone block items to deepslate", createRenamer(var138)));
        param0.addFixer(BlockRenameFixWithJigsaw.create(var137, "Renamed grimstone blocks to deepslate", createRenamer(var138)));
        Schema var139 = param0.addSchema(2700, SAME_NAMESPACED);
        param0.addFixer(
            BlockRenameFixWithJigsaw.create(
                var139,
                "Renamed cave vines blocks",
                createRenamer(ImmutableMap.of("minecraft:cave_vines_head", "minecraft:cave_vines", "minecraft:cave_vines_body", "minecraft:cave_vines_plant"))
            )
        );
        Schema var140 = param0.addSchema(2701, SAME_NAMESPACED);
        param0.addFixer(new SavedDataFeaturePoolElementFix(var140));
        Schema var141 = param0.addSchema(2702, SAME_NAMESPACED);
        param0.addFixer(new AbstractArrowPickupFix(var141));
        Schema var142 = param0.addSchema(2704, V2704::new);
        param0.addFixer(new AddNewChoices(var142, "Added Goat", References.ENTITY));
        Schema var143 = param0.addSchema(2707, V2707::new);
        param0.addFixer(new AddNewChoices(var143, "Added Marker", References.ENTITY));
        param0.addFixer(new AddFlagIfNotPresentFix(var143, References.WORLD_GEN_SETTINGS, "has_increased_height_already", true));
        Schema var144 = param0.addSchema(2710, SAME_NAMESPACED);
        param0.addFixer(
            new StatsRenameFix(var144, "Renamed play_one_minute stat to play_time", ImmutableMap.of("minecraft:play_one_minute", "minecraft:play_time"))
        );
        Schema var145 = param0.addSchema(2717, SAME_NAMESPACED);
        param0.addFixer(
            ItemRenameFix.create(
                var145, "Rename azalea_leaves_flowers", createRenamer(ImmutableMap.of("minecraft:azalea_leaves_flowers", "minecraft:flowering_azalea_leaves"))
            )
        );
        param0.addFixer(
            BlockRenameFix.create(
                var145,
                "Rename azalea_leaves_flowers items",
                createRenamer(ImmutableMap.of("minecraft:azalea_leaves_flowers", "minecraft:flowering_azalea_leaves"))
            )
        );
        Schema var146 = param0.addSchema(2825, SAME_NAMESPACED);
        param0.addFixer(new AddFlagIfNotPresentFix(var146, References.WORLD_GEN_SETTINGS, "has_increased_height_already", false));
        Schema var147 = param0.addSchema(2831, V2831::new);
        param0.addFixer(new SpawnerDataFix(var147));
        Schema var148 = param0.addSchema(2832, V2832::new);
        param0.addFixer(new WorldGenSettingsHeightAndBiomeFix(var148));
        param0.addFixer(new ChunkHeightAndBiomeFix(var148));
        Schema var149 = param0.addSchema(2833, SAME_NAMESPACED);
        param0.addFixer(new WorldGenSettingsDisallowOldCustomWorldsFix(var149));
        Schema var150 = param0.addSchema(2838, SAME_NAMESPACED);
        param0.addFixer(new RenameBiomesFix(var150, false, "Caves and Cliffs biome renames", CavesAndCliffsRenames.RENAMES));
        Schema var151 = param0.addSchema(2841, SAME_NAMESPACED);
        param0.addFixer(new ChunkProtoTickListFix(var151));
        Schema var152 = param0.addSchema(2842, V2842::new);
        param0.addFixer(new ChunkRenamesFix(var152));
        Schema var153 = param0.addSchema(2843, SAME_NAMESPACED);
        param0.addFixer(new OverreachingTickFix(var153));
        param0.addFixer(new RenameBiomesFix(var153, false, "Remove Deep Warm Ocean", Map.of("minecraft:deep_warm_ocean", "minecraft:warm_ocean")));
        Schema var154 = param0.addSchema(2846, SAME_NAMESPACED);
        param0.addFixer(
            new AdvancementsRenameFix(
                var154,
                false,
                "Rename some C&C part 2 advancements",
                createRenamer(
                    ImmutableMap.of(
                        "minecraft:husbandry/play_jukebox_in_meadows",
                        "minecraft:adventure/play_jukebox_in_meadows",
                        "minecraft:adventure/caves_and_cliff",
                        "minecraft:adventure/fall_from_world_height",
                        "minecraft:adventure/ride_strider_in_overworld_lava",
                        "minecraft:nether/ride_strider_in_overworld_lava"
                    )
                )
            )
        );
        Schema var155 = param0.addSchema(2852, SAME_NAMESPACED);
        param0.addFixer(new WorldGenSettingsDisallowOldCustomWorldsFix(var155));
        Schema var156 = param0.addSchema(2967, SAME_NAMESPACED);
        param0.addFixer(new StructureSettingsFlattenFix(var156));
        Schema var157 = param0.addSchema(2970, SAME_NAMESPACED);
        param0.addFixer(new StructuresBecomeConfiguredFix(var157));
        Schema var158 = param0.addSchema(3076, V3076::new);
        param0.addFixer(new AddNewChoices(var158, "Added Sculk Catalyst", References.BLOCK_ENTITY));
        Schema var159 = param0.addSchema(3077, SAME_NAMESPACED);
        param0.addFixer(new ChunkDeleteIgnoredLightDataFix(var159));
        Schema var160 = param0.addSchema(3078, V3078::new);
        param0.addFixer(new AddNewChoices(var160, "Added Frog", References.ENTITY));
        param0.addFixer(new AddNewChoices(var160, "Added Tadpole", References.ENTITY));
        param0.addFixer(new AddNewChoices(var160, "Added Sculk Shrieker", References.BLOCK_ENTITY));
        Schema var161 = param0.addSchema(3081, V3081::new);
        param0.addFixer(new AddNewChoices(var161, "Added Warden", References.ENTITY));
        Schema var162 = param0.addSchema(3082, V3082::new);
        param0.addFixer(new AddNewChoices(var162, "Added Chest Boat", References.ENTITY));
        Schema var163 = param0.addSchema(3083, V3083::new);
        param0.addFixer(new AddNewChoices(var163, "Added Allay", References.ENTITY));
        Schema var164 = param0.addSchema(3084, SAME_NAMESPACED);
        param0.addFixer(
            new SimpleRenameFix(
                var164,
                References.GAME_EVENT_NAME,
                ImmutableMap.<String, String>builder()
                    .put("minecraft:block_press", "minecraft:block_activate")
                    .put("minecraft:block_switch", "minecraft:block_activate")
                    .put("minecraft:block_unpress", "minecraft:block_deactivate")
                    .put("minecraft:block_unswitch", "minecraft:block_deactivate")
                    .put("minecraft:drinking_finish", "minecraft:drink")
                    .put("minecraft:elytra_free_fall", "minecraft:elytra_glide")
                    .put("minecraft:entity_damaged", "minecraft:entity_damage")
                    .put("minecraft:entity_dying", "minecraft:entity_die")
                    .put("minecraft:entity_killed", "minecraft:entity_die")
                    .put("minecraft:mob_interact", "minecraft:entity_interact")
                    .put("minecraft:ravager_roar", "minecraft:entity_roar")
                    .put("minecraft:ring_bell", "minecraft:block_change")
                    .put("minecraft:shulker_close", "minecraft:container_close")
                    .put("minecraft:shulker_open", "minecraft:container_open")
                    .put("minecraft:wolf_shaking", "minecraft:entity_shake")
                    .build()
            )
        );
        Schema var165 = param0.addSchema(3086, SAME_NAMESPACED);
        param0.addFixer(
            new EntityVariantFix(
                var165, "Change cat variant type", References.ENTITY, "minecraft:cat", "CatType", Util.make(new Int2ObjectOpenHashMap(), param0x -> {
                    param0x.defaultReturnValue("minecraft:tabby");
                    param0x.put(0, "minecraft:tabby");
                    param0x.put(1, "minecraft:black");
                    param0x.put(2, "minecraft:red");
                    param0x.put(3, "minecraft:siamese");
                    param0x.put(4, "minecraft:british");
                    param0x.put(5, "minecraft:calico");
                    param0x.put(6, "minecraft:persian");
                    param0x.put(7, "minecraft:ragdoll");
                    param0x.put(8, "minecraft:white");
                    param0x.put(9, "minecraft:jellie");
                    param0x.put(10, "minecraft:all_black");
                })::get
            )
        );
        ImmutableMap<String, String> var166 = ImmutableMap.<String, String>builder()
            .put("textures/entity/cat/tabby.png", "minecraft:tabby")
            .put("textures/entity/cat/black.png", "minecraft:black")
            .put("textures/entity/cat/red.png", "minecraft:red")
            .put("textures/entity/cat/siamese.png", "minecraft:siamese")
            .put("textures/entity/cat/british_shorthair.png", "minecraft:british")
            .put("textures/entity/cat/calico.png", "minecraft:calico")
            .put("textures/entity/cat/persian.png", "minecraft:persian")
            .put("textures/entity/cat/ragdoll.png", "minecraft:ragdoll")
            .put("textures/entity/cat/white.png", "minecraft:white")
            .put("textures/entity/cat/jellie.png", "minecraft:jellie")
            .put("textures/entity/cat/all_black.png", "minecraft:all_black")
            .build();
        param0.addFixer(
            new CriteriaRenameFix(
                var165, "Migrate cat variant advancement", "minecraft:husbandry/complete_catalogue", param1 -> var166.getOrDefault(param1, param1)
            )
        );
        Schema var167 = param0.addSchema(3087, SAME_NAMESPACED);
        param0.addFixer(
            new EntityVariantFix(
                var167, "Change frog variant type", References.ENTITY, "minecraft:frog", "Variant", Util.make(new Int2ObjectOpenHashMap(), param0x -> {
                    param0x.put(0, "minecraft:temperate");
                    param0x.put(1, "minecraft:warm");
                    param0x.put(2, "minecraft:cold");
                })::get
            )
        );
        Schema var168 = param0.addSchema(3088, SAME_NAMESPACED);
        param0.addFixer(new BlendingDataFix(var168));
        Schema var169 = param0.addSchema(3090, SAME_NAMESPACED);
        param0.addFixer(new EntityPaintingFieldsRenameFix(var169));
        Schema var170 = param0.addSchema(3094, SAME_NAMESPACED);
        param0.addFixer(new GoatHornIdFix(var170));
    }

    private static UnaryOperator<String> createRenamer(Map<String, String> param0) {
        return param1 -> param0.getOrDefault(param1, param1);
    }

    private static UnaryOperator<String> createRenamer(String param0, String param1) {
        return param2 -> Objects.equals(param2, param0) ? param1 : param2;
    }
}
