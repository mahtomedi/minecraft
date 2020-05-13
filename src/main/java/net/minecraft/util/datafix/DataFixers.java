package net.minecraft.util.datafix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.Objects;
import java.util.function.BiFunction;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.AddNewChoices;
import net.minecraft.util.datafix.fixes.AdvancementsFix;
import net.minecraft.util.datafix.fixes.AdvancementsRenameFix;
import net.minecraft.util.datafix.fixes.AttributesRename;
import net.minecraft.util.datafix.fixes.BedBlockEntityInjecter;
import net.minecraft.util.datafix.fixes.BedItemColorFix;
import net.minecraft.util.datafix.fixes.BeehivePoiRenameFix;
import net.minecraft.util.datafix.fixes.BiomeFix;
import net.minecraft.util.datafix.fixes.BitStorageAlignFix;
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
import net.minecraft.util.datafix.fixes.BlockStateStructureTemplateFix;
import net.minecraft.util.datafix.fixes.CatTypeFix;
import net.minecraft.util.datafix.fixes.ChunkBiomeFix;
import net.minecraft.util.datafix.fixes.ChunkLightRemoveFix;
import net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix;
import net.minecraft.util.datafix.fixes.ChunkStatusFix;
import net.minecraft.util.datafix.fixes.ChunkStatusFix2;
import net.minecraft.util.datafix.fixes.ChunkStructuresTemplateRenameFix;
import net.minecraft.util.datafix.fixes.ChunkToProtochunkFix;
import net.minecraft.util.datafix.fixes.ColorlessShulkerEntityFix;
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
import net.minecraft.util.datafix.fixes.EntityWolfColorFix;
import net.minecraft.util.datafix.fixes.EntityZombieSplitFix;
import net.minecraft.util.datafix.fixes.EntityZombieVillagerTypeFix;
import net.minecraft.util.datafix.fixes.EntityZombifiedPiglinRenameFix;
import net.minecraft.util.datafix.fixes.ForcePoiRebuild;
import net.minecraft.util.datafix.fixes.FurnaceRecipeFix;
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
import net.minecraft.util.datafix.fixes.MobSpawnerEntityIdentifiersFix;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.NewVillageFix;
import net.minecraft.util.datafix.fixes.ObjectiveDisplayNameFix;
import net.minecraft.util.datafix.fixes.ObjectiveRenderTypeFix;
import net.minecraft.util.datafix.fixes.OptionsAddTextBackgroundFix;
import net.minecraft.util.datafix.fixes.OptionsForceVBOFix;
import net.minecraft.util.datafix.fixes.OptionsKeyLwjgl3Fix;
import net.minecraft.util.datafix.fixes.OptionsKeyTranslationFix;
import net.minecraft.util.datafix.fixes.OptionsLowerCaseLanguageFix;
import net.minecraft.util.datafix.fixes.PlayerUUIDFix;
import net.minecraft.util.datafix.fixes.RecipesFix;
import net.minecraft.util.datafix.fixes.RecipesRenameFix;
import net.minecraft.util.datafix.fixes.RecipesRenameningFix;
import net.minecraft.util.datafix.fixes.RedstoneWireConnectionsFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.fixes.RenameBiomesFix;
import net.minecraft.util.datafix.fixes.RenamedCoralFansFix;
import net.minecraft.util.datafix.fixes.RenamedCoralFix;
import net.minecraft.util.datafix.fixes.ReorganizePoi;
import net.minecraft.util.datafix.fixes.SavedDataUUIDFix;
import net.minecraft.util.datafix.fixes.SavedDataVillageCropFix;
import net.minecraft.util.datafix.fixes.StatsCounterFix;
import net.minecraft.util.datafix.fixes.StriderGravityFix;
import net.minecraft.util.datafix.fixes.StructureReferenceCountFix;
import net.minecraft.util.datafix.fixes.SwimStatsRenameFix;
import net.minecraft.util.datafix.fixes.TeamDisplayNameFix;
import net.minecraft.util.datafix.fixes.TrappedChestBlockEntityFix;
import net.minecraft.util.datafix.fixes.VillagerDataFix;
import net.minecraft.util.datafix.fixes.VillagerFollowRangeFix;
import net.minecraft.util.datafix.fixes.VillagerRebuildLevelAndXpFix;
import net.minecraft.util.datafix.fixes.VillagerTradeFix;
import net.minecraft.util.datafix.fixes.WallPropertyFix;
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
import net.minecraft.util.datafix.schemas.V501;
import net.minecraft.util.datafix.schemas.V700;
import net.minecraft.util.datafix.schemas.V701;
import net.minecraft.util.datafix.schemas.V702;
import net.minecraft.util.datafix.schemas.V703;
import net.minecraft.util.datafix.schemas.V704;
import net.minecraft.util.datafix.schemas.V705;
import net.minecraft.util.datafix.schemas.V808;
import net.minecraft.util.datafix.schemas.V99;

public class DataFixers {
    private static final BiFunction<Integer, Schema, Schema> SAME = Schema::new;
    private static final BiFunction<Integer, Schema, Schema> SAME_NAMESPACED = NamespacedSchema::new;
    private static final DataFixer DATA_FIXER = createFixerUpper();

    private static DataFixer createFixerUpper() {
        DataFixerBuilder var0 = new DataFixerBuilder(SharedConstants.getCurrentVersion().getWorldVersion());
        addFixers(var0);
        return var0.build(Util.bootstrapExecutor());
    }

    public static DataFixer getDataFixer() {
        return DATA_FIXER;
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
        param0.addFixer(
            ItemRenameFix.create(var31, "totem item renamer", param0x -> Objects.equals(param0x, "minecraft:totem") ? "minecraft:totem_of_undying" : param0x)
        );
        Schema var32 = param0.addSchema(1022, V1022::new);
        param0.addFixer(new WriteAndReadFix(var32, "added shoulder entities to players", References.PLAYER));
        Schema var33 = param0.addSchema(1125, V1125::new);
        param0.addFixer(new BedBlockEntityInjecter(var33, true));
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
                param0x -> ImmutableMap.of("minecraft:flowing_water", "minecraft:water", "minecraft:flowing_lava", "minecraft:lava")
                        .getOrDefault(param0x, param0x)
            )
        );
        Schema var53 = param0.addSchema(1480, SAME_NAMESPACED);
        param0.addFixer(BlockRenameFix.create(var53, "Rename coral blocks", param0x -> RenamedCoralFix.RENAMED_IDS.getOrDefault(param0x, param0x)));
        param0.addFixer(ItemRenameFix.create(var53, "Rename coral items", param0x -> RenamedCoralFix.RENAMED_IDS.getOrDefault(param0x, param0x)));
        Schema var54 = param0.addSchema(1481, V1481::new);
        param0.addFixer(new AddNewChoices(var54, "Add conduit", References.BLOCK_ENTITY));
        Schema var55 = param0.addSchema(1483, V1483::new);
        param0.addFixer(new EntityPufferfishRenameFix(var55, true));
        param0.addFixer(
            ItemRenameFix.create(var55, "Rename pufferfish egg item", param0x -> EntityPufferfishRenameFix.RENAMED_IDS.getOrDefault(param0x, param0x))
        );
        Schema var56 = param0.addSchema(1484, SAME_NAMESPACED);
        param0.addFixer(
            ItemRenameFix.create(
                var56,
                "Rename seagrass items",
                param0x -> ImmutableMap.of("minecraft:sea_grass", "minecraft:seagrass", "minecraft:tall_sea_grass", "minecraft:tall_seagrass")
                        .getOrDefault(param0x, param0x)
            )
        );
        param0.addFixer(
            BlockRenameFix.create(
                var56,
                "Rename seagrass blocks",
                param0x -> ImmutableMap.of("minecraft:sea_grass", "minecraft:seagrass", "minecraft:tall_sea_grass", "minecraft:tall_seagrass")
                        .getOrDefault(param0x, param0x)
            )
        );
        param0.addFixer(new HeightmapRenamingFix(var56, false));
        Schema var57 = param0.addSchema(1486, V1486::new);
        param0.addFixer(new EntityCodSalmonFix(var57, true));
        param0.addFixer(
            ItemRenameFix.create(var57, "Rename cod/salmon egg items", param0x -> EntityCodSalmonFix.RENAMED_EGG_IDS.getOrDefault(param0x, param0x))
        );
        Schema var58 = param0.addSchema(1487, SAME_NAMESPACED);
        param0.addFixer(
            ItemRenameFix.create(
                var58,
                "Rename prismarine_brick(s)_* blocks",
                param0x -> ImmutableMap.of(
                            "minecraft:prismarine_bricks_slab",
                            "minecraft:prismarine_brick_slab",
                            "minecraft:prismarine_bricks_stairs",
                            "minecraft:prismarine_brick_stairs"
                        )
                        .getOrDefault(param0x, param0x)
            )
        );
        param0.addFixer(
            BlockRenameFix.create(
                var58,
                "Rename prismarine_brick(s)_* items",
                param0x -> ImmutableMap.of(
                            "minecraft:prismarine_bricks_slab",
                            "minecraft:prismarine_brick_slab",
                            "minecraft:prismarine_bricks_stairs",
                            "minecraft:prismarine_brick_stairs"
                        )
                        .getOrDefault(param0x, param0x)
            )
        );
        Schema var59 = param0.addSchema(1488, SAME_NAMESPACED);
        param0.addFixer(
            BlockRenameFix.create(
                var59,
                "Rename kelp/kelptop",
                param0x -> ImmutableMap.of("minecraft:kelp_top", "minecraft:kelp", "minecraft:kelp", "minecraft:kelp_plant").getOrDefault(param0x, param0x)
            )
        );
        param0.addFixer(ItemRenameFix.create(var59, "Rename kelptop", param0x -> Objects.equals(param0x, "minecraft:kelp_top") ? "minecraft:kelp" : param0x));
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
        param0.addFixer(
            BlockRenameFix.create(var60, "Rename melon_block", param0x -> Objects.equals(param0x, "minecraft:melon_block") ? "minecraft:melon" : param0x)
        );
        param0.addFixer(
            ItemRenameFix.create(
                var60,
                "Rename melon_block/melon/speckled_melon",
                param0x -> ImmutableMap.of(
                            "minecraft:melon_block",
                            "minecraft:melon",
                            "minecraft:melon",
                            "minecraft:melon_slice",
                            "minecraft:speckled_melon",
                            "minecraft:glistering_melon_slice"
                        )
                        .getOrDefault(param0x, param0x)
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
        Schema var68 = param0.addSchema(1508, SAME_NAMESPACED);
        param0.addFixer(new BiomeFix(var68, false));
        Schema var69 = param0.addSchema(1510, V1510::new);
        param0.addFixer(BlockRenameFix.create(var69, "Block renamening fix", param0x -> EntityTheRenameningFix.RENAMED_BLOCKS.getOrDefault(param0x, param0x)));
        param0.addFixer(ItemRenameFix.create(var69, "Item renamening fix", param0x -> EntityTheRenameningFix.RENAMED_ITEMS.getOrDefault(param0x, param0x)));
        param0.addFixer(new RecipesRenameningFix(var69, false));
        param0.addFixer(new EntityTheRenameningFix(var69, true));
        param0.addFixer(new SwimStatsRenameFix(var69, false));
        Schema var70 = param0.addSchema(1514, SAME_NAMESPACED);
        param0.addFixer(new ObjectiveDisplayNameFix(var70, false));
        param0.addFixer(new TeamDisplayNameFix(var70, false));
        param0.addFixer(new ObjectiveRenderTypeFix(var70, false));
        Schema var71 = param0.addSchema(1515, SAME_NAMESPACED);
        param0.addFixer(BlockRenameFix.create(var71, "Rename coral fan blocks", param0x -> RenamedCoralFansFix.RENAMED_IDS.getOrDefault(param0x, param0x)));
        Schema var72 = param0.addSchema(1624, SAME_NAMESPACED);
        param0.addFixer(new TrappedChestBlockEntityFix(var72, false));
        Schema var73 = param0.addSchema(1800, V1800::new);
        param0.addFixer(new AddNewChoices(var73, "Added 1.14 mobs fix", References.ENTITY));
        param0.addFixer(ItemRenameFix.create(var73, "Rename dye items", param0x -> DyeItemRenameFix.RENAMED_IDS.getOrDefault(param0x, param0x)));
        Schema var74 = param0.addSchema(1801, V1801::new);
        param0.addFixer(new AddNewChoices(var74, "Added Illager Beast", References.ENTITY));
        Schema var75 = param0.addSchema(1802, SAME_NAMESPACED);
        param0.addFixer(
            BlockRenameFix.create(
                var75,
                "Rename sign blocks & stone slabs",
                param0x -> ImmutableMap.of(
                            "minecraft:stone_slab",
                            "minecraft:smooth_stone_slab",
                            "minecraft:sign",
                            "minecraft:oak_sign",
                            "minecraft:wall_sign",
                            "minecraft:oak_wall_sign"
                        )
                        .getOrDefault(param0x, param0x)
            )
        );
        param0.addFixer(
            ItemRenameFix.create(
                var75,
                "Rename sign item & stone slabs",
                param0x -> ImmutableMap.of("minecraft:stone_slab", "minecraft:smooth_stone_slab", "minecraft:sign", "minecraft:oak_sign")
                        .getOrDefault(param0x, param0x)
            )
        );
        Schema var76 = param0.addSchema(1803, SAME_NAMESPACED);
        param0.addFixer(new ItemLoreFix(var76, false));
        Schema var77 = param0.addSchema(1904, V1904::new);
        param0.addFixer(new AddNewChoices(var77, "Added Cats", References.ENTITY));
        param0.addFixer(new EntityCatSplitFix(var77, false));
        Schema var78 = param0.addSchema(1905, SAME_NAMESPACED);
        param0.addFixer(new ChunkStatusFix(var78, false));
        Schema var79 = param0.addSchema(1906, V1906::new);
        param0.addFixer(new AddNewChoices(var79, "Add POI Blocks", References.BLOCK_ENTITY));
        Schema var80 = param0.addSchema(1909, V1909::new);
        param0.addFixer(new AddNewChoices(var80, "Add jigsaw", References.BLOCK_ENTITY));
        Schema var81 = param0.addSchema(1911, SAME_NAMESPACED);
        param0.addFixer(new ChunkStatusFix2(var81, false));
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
        param0.addFixer(ItemRenameFix.create(var86, "Rename ravager egg item", param0x -> EntityRavagerRenameFix.RENAMED_IDS.getOrDefault(param0x, param0x)));
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
        Schema var95 = param0.addSchema(2100, V2100::new);
        param0.addFixer(new AddNewChoices(var95, "Added Bee and Bee Stinger", References.ENTITY));
        param0.addFixer(new AddNewChoices(var95, "Add beehive", References.BLOCK_ENTITY));
        param0.addFixer(
            new RecipesRenameFix(var95, false, "Rename sugar recipe", param0x -> "minecraft:sugar".equals(param0x) ? "sugar_from_sugar_cane" : param0x)
        );
        param0.addFixer(
            new AdvancementsRenameFix(
                var95,
                false,
                "Rename sugar recipe advancement",
                param0x -> "minecraft:recipes/misc/sugar".equals(param0x) ? "minecraft:recipes/misc/sugar_from_sugar_cane" : param0x
            )
        );
        Schema var96 = param0.addSchema(2202, SAME_NAMESPACED);
        param0.addFixer(new ChunkBiomeFix(var96, false));
        Schema var97 = param0.addSchema(2209, SAME_NAMESPACED);
        param0.addFixer(
            ItemRenameFix.create(
                var97, "Rename bee_hive item to beehive", param0x -> Objects.equals(param0x, "minecraft:bee_hive") ? "minecraft:beehive" : param0x
            )
        );
        param0.addFixer(new BeehivePoiRenameFix(var97));
        param0.addFixer(
            BlockRenameFix.create(
                var97, "Rename bee_hive block to beehive", param0x -> ImmutableMap.of("minecraft:bee_hive", "minecraft:beehive").getOrDefault(param0x, param0x)
            )
        );
        Schema var98 = param0.addSchema(2211, SAME_NAMESPACED);
        param0.addFixer(new StructureReferenceCountFix(var98, false));
        Schema var99 = param0.addSchema(2218, SAME_NAMESPACED);
        param0.addFixer(new ForcePoiRebuild(var99, false));
        Schema var100 = param0.addSchema(2501, V2501::new);
        param0.addFixer(new FurnaceRecipeFix(var100, true));
        Schema var101 = param0.addSchema(2502, V2502::new);
        param0.addFixer(new AddNewChoices(var101, "Added Hoglin", References.ENTITY));
        param0.addFixer(new RenameBiomesFix(var101, false, "Nether biome rename", ImmutableMap.of("minecraft:nether", "minecraft:nether_wastes")));
        Schema var102 = param0.addSchema(2503, SAME_NAMESPACED);
        param0.addFixer(new WallPropertyFix(var102, false));
        Schema var103 = param0.addSchema(2505, V2505::new);
        param0.addFixer(new AddNewChoices(var103, "Added Piglin", References.ENTITY));
        param0.addFixer(new MemoryExpiryDataFix(var103, "minecraft:villager"));
        Schema var104 = param0.addSchema(2508, SAME_NAMESPACED);
        param0.addFixer(
            ItemRenameFix.create(
                var104,
                "Renamed fungi items to fungus",
                param0x -> ImmutableMap.of("minecraft:warped_fungi", "minecraft:warped_fungus", "minecraft:crimson_fungi", "minecraft:crimson_fungus")
                        .getOrDefault(param0x, param0x)
            )
        );
        param0.addFixer(
            BlockRenameFix.create(
                var104,
                "Renamed fungi blocks to fungus",
                param0x -> ImmutableMap.of("minecraft:warped_fungi", "minecraft:warped_fungus", "minecraft:crimson_fungi", "minecraft:crimson_fungus")
                        .getOrDefault(param0x, param0x)
            )
        );
        Schema var105 = param0.addSchema(2509, V2509::new);
        param0.addFixer(new EntityZombifiedPiglinRenameFix(var105));
        param0.addFixer(
            ItemRenameFix.create(var105, "Rename zombie pigman egg item", param0x -> EntityZombifiedPiglinRenameFix.RENAMED_IDS.getOrDefault(param0x, param0x))
        );
        Schema var106 = param0.addSchema(2511, SAME_NAMESPACED);
        param0.addFixer(new EntityProjectileOwnerFix(var106));
        Schema var107 = param0.addSchema(2514, SAME_NAMESPACED);
        param0.addFixer(new EntityUUIDFix(var107));
        param0.addFixer(new BlockEntityUUIDFix(var107));
        param0.addFixer(new PlayerUUIDFix(var107));
        param0.addFixer(new LevelUUIDFix(var107));
        param0.addFixer(new SavedDataUUIDFix(var107));
        param0.addFixer(new ItemStackUUIDFix(var107));
        Schema var108 = param0.addSchema(2516, SAME_NAMESPACED);
        param0.addFixer(new GossipUUIDFix(var108, "minecraft:villager"));
        param0.addFixer(new GossipUUIDFix(var108, "minecraft:zombie_villager"));
        Schema var109 = param0.addSchema(2518, SAME_NAMESPACED);
        param0.addFixer(new JigsawPropertiesFix(var109, false));
        param0.addFixer(new JigsawRotationFix(var109, false));
        Schema var110 = param0.addSchema(2519, V2519::new);
        param0.addFixer(new AddNewChoices(var110, "Added Strider", References.ENTITY));
        Schema var111 = param0.addSchema(2522, V2522::new);
        param0.addFixer(new AddNewChoices(var111, "Added Zoglin", References.ENTITY));
        Schema var112 = param0.addSchema(2523, SAME_NAMESPACED);
        param0.addFixer(new AttributesRename(var112));
        Schema var113 = param0.addSchema(2527, SAME_NAMESPACED);
        param0.addFixer(new BitStorageAlignFix(var113));
        Schema var114 = param0.addSchema(2528, SAME_NAMESPACED);
        param0.addFixer(
            ItemRenameFix.create(
                var114,
                "Rename soul fire torch and soul fire lantern",
                param0x -> ImmutableMap.of("minecraft:soul_fire_torch", "minecraft:soul_torch", "minecraft:soul_fire_lantern", "minecraft:soul_lantern")
                        .getOrDefault(param0x, param0x)
            )
        );
        param0.addFixer(
            BlockRenameFix.create(
                var114,
                "Rename soul fire torch and soul fire lantern",
                param0x -> ImmutableMap.of(
                            "minecraft:soul_fire_torch",
                            "minecraft:soul_torch",
                            "minecraft:soul_fire_wall_torch",
                            "minecraft:soul_wall_torch",
                            "minecraft:soul_fire_lantern",
                            "minecraft:soul_lantern"
                        )
                        .getOrDefault(param0x, param0x)
            )
        );
        Schema var115 = param0.addSchema(2529, SAME_NAMESPACED);
        param0.addFixer(new StriderGravityFix(var115, false));
        Schema var116 = param0.addSchema(2531, SAME_NAMESPACED);
        param0.addFixer(new RedstoneWireConnectionsFix(var116));
        Schema var117 = param0.addSchema(2533, SAME_NAMESPACED);
        param0.addFixer(new VillagerFollowRangeFix(var117));
        Schema var118 = param0.addSchema(2535, SAME_NAMESPACED);
        param0.addFixer(new EntityShulkerRotationFix(var118));
    }
}
