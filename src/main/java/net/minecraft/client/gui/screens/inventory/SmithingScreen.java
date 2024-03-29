package net.minecraft.client.gui.screens.inventory;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class SmithingScreen extends ItemCombinerScreen<SmithingMenu> {
    private static final ResourceLocation ERROR_SPRITE = new ResourceLocation("container/smithing/error");
    private static final ResourceLocation EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM = new ResourceLocation("item/empty_slot_smithing_template_armor_trim");
    private static final ResourceLocation EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE = new ResourceLocation(
        "item/empty_slot_smithing_template_netherite_upgrade"
    );
    private static final Component MISSING_TEMPLATE_TOOLTIP = Component.translatable("container.upgrade.missing_template_tooltip");
    private static final Component ERROR_TOOLTIP = Component.translatable("container.upgrade.error_tooltip");
    private static final List<ResourceLocation> EMPTY_SLOT_SMITHING_TEMPLATES = List.of(
        EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM, EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE
    );
    private static final int TITLE_LABEL_X = 44;
    private static final int TITLE_LABEL_Y = 15;
    private static final int ERROR_ICON_WIDTH = 28;
    private static final int ERROR_ICON_HEIGHT = 21;
    private static final int ERROR_ICON_X = 65;
    private static final int ERROR_ICON_Y = 46;
    private static final int TOOLTIP_WIDTH = 115;
    private static final int ARMOR_STAND_Y_ROT = 210;
    private static final int ARMOR_STAND_X_ROT = 25;
    private static final Vector3f ARMOR_STAND_TRANSLATION = new Vector3f();
    private static final Quaternionf ARMOR_STAND_ANGLE = new Quaternionf().rotationXYZ(0.43633232F, 0.0F, (float) Math.PI);
    private static final int ARMOR_STAND_SCALE = 25;
    private static final int ARMOR_STAND_OFFSET_Y = 75;
    private static final int ARMOR_STAND_OFFSET_X = 141;
    private final CyclingSlotBackground templateIcon = new CyclingSlotBackground(0);
    private final CyclingSlotBackground baseIcon = new CyclingSlotBackground(1);
    private final CyclingSlotBackground additionalIcon = new CyclingSlotBackground(2);
    @Nullable
    private ArmorStand armorStandPreview;

    public SmithingScreen(SmithingMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2, new ResourceLocation("textures/gui/container/smithing.png"));
        this.titleLabelX = 44;
        this.titleLabelY = 15;
    }

    @Override
    protected void subInit() {
        this.armorStandPreview = new ArmorStand(this.minecraft.level, 0.0, 0.0, 0.0);
        this.armorStandPreview.setNoBasePlate(true);
        this.armorStandPreview.setShowArms(true);
        this.armorStandPreview.yBodyRot = 210.0F;
        this.armorStandPreview.setXRot(25.0F);
        this.armorStandPreview.yHeadRot = this.armorStandPreview.getYRot();
        this.armorStandPreview.yHeadRotO = this.armorStandPreview.getYRot();
        this.updateArmorStandPreview(this.menu.getSlot(3).getItem());
    }

    @Override
    public void containerTick() {
        super.containerTick();
        Optional<SmithingTemplateItem> var0 = this.getTemplateItem();
        this.templateIcon.tick(EMPTY_SLOT_SMITHING_TEMPLATES);
        this.baseIcon.tick(var0.map(SmithingTemplateItem::getBaseSlotEmptyIcons).orElse(List.of()));
        this.additionalIcon.tick(var0.map(SmithingTemplateItem::getAdditionalSlotEmptyIcons).orElse(List.of()));
    }

    private Optional<SmithingTemplateItem> getTemplateItem() {
        ItemStack var0 = this.menu.getSlot(0).getItem();
        if (!var0.isEmpty()) {
            Item var3 = var0.getItem();
            if (var3 instanceof SmithingTemplateItem var1) {
                return Optional.of(var1);
            }
        }

        return Optional.empty();
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.renderOnboardingTooltips(param0, param1, param2);
    }

    @Override
    protected void renderBg(GuiGraphics param0, float param1, int param2, int param3) {
        super.renderBg(param0, param1, param2, param3);
        this.templateIcon.render(this.menu, param0, param1, this.leftPos, this.topPos);
        this.baseIcon.render(this.menu, param0, param1, this.leftPos, this.topPos);
        this.additionalIcon.render(this.menu, param0, param1, this.leftPos, this.topPos);
        InventoryScreen.renderEntityInInventory(
            param0, (float)(this.leftPos + 141), (float)(this.topPos + 75), 25, ARMOR_STAND_TRANSLATION, ARMOR_STAND_ANGLE, null, this.armorStandPreview
        );
    }

    @Override
    public void slotChanged(AbstractContainerMenu param0, int param1, ItemStack param2) {
        if (param1 == 3) {
            this.updateArmorStandPreview(param2);
        }

    }

    private void updateArmorStandPreview(ItemStack param0) {
        if (this.armorStandPreview != null) {
            for(EquipmentSlot var0 : EquipmentSlot.values()) {
                this.armorStandPreview.setItemSlot(var0, ItemStack.EMPTY);
            }

            if (!param0.isEmpty()) {
                ItemStack var1 = param0.copy();
                Item var8 = param0.getItem();
                if (var8 instanceof ArmorItem var2) {
                    this.armorStandPreview.setItemSlot(var2.getEquipmentSlot(), var1);
                } else {
                    this.armorStandPreview.setItemSlot(EquipmentSlot.OFFHAND, var1);
                }
            }

        }
    }

    @Override
    protected void renderErrorIcon(GuiGraphics param0, int param1, int param2) {
        if (this.hasRecipeError()) {
            param0.blitSprite(ERROR_SPRITE, param1 + 65, param2 + 46, 28, 21);
        }

    }

    private void renderOnboardingTooltips(GuiGraphics param0, int param1, int param2) {
        Optional<Component> var0 = Optional.empty();
        if (this.hasRecipeError() && this.isHovering(65, 46, 28, 21, (double)param1, (double)param2)) {
            var0 = Optional.of(ERROR_TOOLTIP);
        }

        if (this.hoveredSlot != null) {
            ItemStack var1 = this.menu.getSlot(0).getItem();
            ItemStack var2 = this.hoveredSlot.getItem();
            if (var1.isEmpty()) {
                if (this.hoveredSlot.index == 0) {
                    var0 = Optional.of(MISSING_TEMPLATE_TOOLTIP);
                }
            } else {
                Item var8 = var1.getItem();
                if (var8 instanceof SmithingTemplateItem var3 && var2.isEmpty()) {
                    if (this.hoveredSlot.index == 1) {
                        var0 = Optional.of(var3.getBaseSlotDescription());
                    } else if (this.hoveredSlot.index == 2) {
                        var0 = Optional.of(var3.getAdditionSlotDescription());
                    }
                }
            }
        }

        var0.ifPresent(param3 -> param0.renderTooltip(this.font, this.font.split(param3, 115), param1, param2));
    }

    private boolean hasRecipeError() {
        return this.menu.getSlot(0).hasItem()
            && this.menu.getSlot(1).hasItem()
            && this.menu.getSlot(2).hasItem()
            && !this.menu.getSlot(this.menu.getResultSlot()).hasItem();
    }
}
