package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.level.Level;

public class SmithingTemplateItem extends Item {
    private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;
    private static final String DESCRIPTION_ID = Util.makeDescriptionId("item", new ResourceLocation("smithing_template"));
    private static final Component INGREDIENTS_TITLE = Component.translatable(
            Util.makeDescriptionId("item", new ResourceLocation("smithing_template.ingredients"))
        )
        .withStyle(TITLE_FORMAT);
    private static final Component APPLIES_TO_TITLE = Component.translatable(
            Util.makeDescriptionId("item", new ResourceLocation("smithing_template.applies_to"))
        )
        .withStyle(TITLE_FORMAT);
    private static final Component NETHERITE_UPGRADE = Component.translatable(Util.makeDescriptionId("upgrade", new ResourceLocation("netherite_upgrade")))
        .withStyle(TITLE_FORMAT);
    private static final Component ARMOR_TRIM_APPLIES_TO = Component.translatable(
            Util.makeDescriptionId("item", new ResourceLocation("smithing_template.armor_trim.applies_to"))
        )
        .withStyle(DESCRIPTION_FORMAT);
    private static final Component ARMOR_TRIM_INGREDIENTS = Component.translatable(
            Util.makeDescriptionId("item", new ResourceLocation("smithing_template.armor_trim.ingredients"))
        )
        .withStyle(DESCRIPTION_FORMAT);
    private static final Component ARMOR_TRIM_BASE_SLOT_DESCRIPTION = Component.translatable(
        Util.makeDescriptionId("item", new ResourceLocation("smithing_template.armor_trim.base_slot_description"))
    );
    private static final Component ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(
        Util.makeDescriptionId("item", new ResourceLocation("smithing_template.armor_trim.additions_slot_description"))
    );
    private static final Component NETHERITE_UPGRADE_APPLIES_TO = Component.translatable(
            Util.makeDescriptionId("item", new ResourceLocation("smithing_template.netherite_upgrade.applies_to"))
        )
        .withStyle(DESCRIPTION_FORMAT);
    private static final Component NETHERITE_UPGRADE_INGREDIENTS = Component.translatable(
            Util.makeDescriptionId("item", new ResourceLocation("smithing_template.netherite_upgrade.ingredients"))
        )
        .withStyle(DESCRIPTION_FORMAT);
    private static final Component NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable(
        Util.makeDescriptionId("item", new ResourceLocation("smithing_template.netherite_upgrade.base_slot_description"))
    );
    private static final Component NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(
        Util.makeDescriptionId("item", new ResourceLocation("smithing_template.netherite_upgrade.additions_slot_description"))
    );
    private static final ResourceLocation EMPTY_SLOT_HELMET = new ResourceLocation("item/empty_armor_slot_helmet");
    private static final ResourceLocation EMPTY_SLOT_CHESTPLATE = new ResourceLocation("item/empty_armor_slot_chestplate");
    private static final ResourceLocation EMPTY_SLOT_LEGGINGS = new ResourceLocation("item/empty_armor_slot_leggings");
    private static final ResourceLocation EMPTY_SLOT_BOOTS = new ResourceLocation("item/empty_armor_slot_boots");
    private static final ResourceLocation EMPTY_SLOT_HOE = new ResourceLocation("item/empty_slot_hoe");
    private static final ResourceLocation EMPTY_SLOT_AXE = new ResourceLocation("item/empty_slot_axe");
    private static final ResourceLocation EMPTY_SLOT_SWORD = new ResourceLocation("item/empty_slot_sword");
    private static final ResourceLocation EMPTY_SLOT_SHOVEL = new ResourceLocation("item/empty_slot_shovel");
    private static final ResourceLocation EMPTY_SLOT_PICKAXE = new ResourceLocation("item/empty_slot_pickaxe");
    private static final ResourceLocation EMPTY_SLOT_INGOT = new ResourceLocation("item/empty_slot_ingot");
    private static final ResourceLocation EMPTY_SLOT_REDSTONE_DUST = new ResourceLocation("item/empty_slot_redstone_dust");
    private static final ResourceLocation EMPTY_SLOT_QUARTZ = new ResourceLocation("item/empty_slot_quartz");
    private static final ResourceLocation EMPTY_SLOT_EMERALD = new ResourceLocation("item/empty_slot_emerald");
    private static final ResourceLocation EMPTY_SLOT_DIAMOND = new ResourceLocation("item/empty_slot_diamond");
    private static final ResourceLocation EMPTY_SLOT_LAPIS_LAZULI = new ResourceLocation("item/empty_slot_lapis_lazuli");
    private final Component appliesTo;
    private final Component ingredients;
    private final Component upgradeDescription;
    private final Component baseSlotDescription;
    private final Component additionsSlotDescription;
    private final List<ResourceLocation> baseSlotEmptyIcons;
    private final List<ResourceLocation> additionalSlotEmptyIcons;

    public SmithingTemplateItem(
        Component param0, Component param1, Component param2, Component param3, Component param4, List<ResourceLocation> param5, List<ResourceLocation> param6
    ) {
        super(new Item.Properties().requiredFeatures(FeatureFlags.UPDATE_1_20));
        this.appliesTo = param0;
        this.ingredients = param1;
        this.upgradeDescription = param2;
        this.baseSlotDescription = param3;
        this.additionsSlotDescription = param4;
        this.baseSlotEmptyIcons = param5;
        this.additionalSlotEmptyIcons = param6;
    }

    public static SmithingTemplateItem createArmorTrimTemplate(ResourceKey<TrimPattern> param0) {
        return createArmorTrimTemplate(param0.location());
    }

    public static SmithingTemplateItem createArmorTrimTemplate(ResourceLocation param0) {
        return new SmithingTemplateItem(
            ARMOR_TRIM_APPLIES_TO,
            ARMOR_TRIM_INGREDIENTS,
            Component.translatable(Util.makeDescriptionId("trim_pattern", param0)).withStyle(TITLE_FORMAT),
            ARMOR_TRIM_BASE_SLOT_DESCRIPTION,
            ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION,
            createTrimmableArmorIconList(),
            createTrimmableMaterialIconList()
        );
    }

    public static SmithingTemplateItem createNetheriteUpgradeTemplate() {
        return new SmithingTemplateItem(
            NETHERITE_UPGRADE_APPLIES_TO,
            NETHERITE_UPGRADE_INGREDIENTS,
            NETHERITE_UPGRADE,
            NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION,
            NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION,
            createNetheriteUpgradeIconList(),
            createNetheriteUpgradeMaterialList()
        );
    }

    private static List<ResourceLocation> createTrimmableArmorIconList() {
        return List.of(EMPTY_SLOT_HELMET, EMPTY_SLOT_CHESTPLATE, EMPTY_SLOT_LEGGINGS, EMPTY_SLOT_BOOTS);
    }

    private static List<ResourceLocation> createTrimmableMaterialIconList() {
        return List.of(EMPTY_SLOT_INGOT, EMPTY_SLOT_REDSTONE_DUST, EMPTY_SLOT_LAPIS_LAZULI, EMPTY_SLOT_QUARTZ, EMPTY_SLOT_DIAMOND, EMPTY_SLOT_EMERALD);
    }

    private static List<ResourceLocation> createNetheriteUpgradeIconList() {
        return List.of(
            EMPTY_SLOT_HELMET,
            EMPTY_SLOT_SWORD,
            EMPTY_SLOT_CHESTPLATE,
            EMPTY_SLOT_PICKAXE,
            EMPTY_SLOT_LEGGINGS,
            EMPTY_SLOT_AXE,
            EMPTY_SLOT_BOOTS,
            EMPTY_SLOT_HOE,
            EMPTY_SLOT_SHOVEL
        );
    }

    private static List<ResourceLocation> createNetheriteUpgradeMaterialList() {
        return List.of(EMPTY_SLOT_INGOT);
    }

    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        super.appendHoverText(param0, param1, param2, param3);
        param2.add(this.upgradeDescription);
        param2.add(CommonComponents.EMPTY);
        param2.add(APPLIES_TO_TITLE);
        param2.add(CommonComponents.space().append(this.appliesTo));
        param2.add(INGREDIENTS_TITLE);
        param2.add(CommonComponents.space().append(this.ingredients));
    }

    public Component getBaseSlotDescription() {
        return this.baseSlotDescription;
    }

    public Component getAdditionSlotDescription() {
        return this.additionsSlotDescription;
    }

    public List<ResourceLocation> getBaseSlotEmptyIcons() {
        return this.baseSlotEmptyIcons;
    }

    public List<ResourceLocation> getAdditionalSlotEmptyIcons() {
        return this.additionalSlotEmptyIcons;
    }

    @Override
    public String getDescriptionId() {
        return DESCRIPTION_ID;
    }
}
