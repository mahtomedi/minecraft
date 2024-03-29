package net.minecraft.world.inventory;

import com.mojang.logging.LogUtils;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class AnvilMenu extends ItemCombinerMenu {
    public static final int INPUT_SLOT = 0;
    public static final int ADDITIONAL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean DEBUG_COST = false;
    public static final int MAX_NAME_LENGTH = 50;
    private int repairItemCountCost;
    @Nullable
    private String itemName;
    private final DataSlot cost = DataSlot.standalone();
    private static final int COST_FAIL = 0;
    private static final int COST_BASE = 1;
    private static final int COST_ADDED_BASE = 1;
    private static final int COST_REPAIR_MATERIAL = 1;
    private static final int COST_REPAIR_SACRIFICE = 2;
    private static final int COST_INCOMPATIBLE_PENALTY = 1;
    private static final int COST_RENAME = 1;
    private static final int INPUT_SLOT_X_PLACEMENT = 27;
    private static final int ADDITIONAL_SLOT_X_PLACEMENT = 76;
    private static final int RESULT_SLOT_X_PLACEMENT = 134;
    private static final int SLOT_Y_PLACEMENT = 47;

    public AnvilMenu(int param0, Inventory param1) {
        this(param0, param1, ContainerLevelAccess.NULL);
    }

    public AnvilMenu(int param0, Inventory param1, ContainerLevelAccess param2) {
        super(MenuType.ANVIL, param0, param1, param2);
        this.addDataSlot(this.cost);
    }

    @Override
    protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create()
            .withSlot(0, 27, 47, param0 -> true)
            .withSlot(1, 76, 47, param0 -> true)
            .withResultSlot(2, 134, 47)
            .build();
    }

    @Override
    protected boolean isValidBlock(BlockState param0) {
        return param0.is(BlockTags.ANVIL);
    }

    @Override
    protected boolean mayPickup(Player param0, boolean param1) {
        return (param0.getAbilities().instabuild || param0.experienceLevel >= this.cost.get()) && this.cost.get() > 0;
    }

    @Override
    protected void onTake(Player param0, ItemStack param1) {
        if (!param0.getAbilities().instabuild) {
            param0.giveExperienceLevels(-this.cost.get());
        }

        this.inputSlots.setItem(0, ItemStack.EMPTY);
        if (this.repairItemCountCost > 0) {
            ItemStack var0 = this.inputSlots.getItem(1);
            if (!var0.isEmpty() && var0.getCount() > this.repairItemCountCost) {
                var0.shrink(this.repairItemCountCost);
                this.inputSlots.setItem(1, var0);
            } else {
                this.inputSlots.setItem(1, ItemStack.EMPTY);
            }
        } else {
            this.inputSlots.setItem(1, ItemStack.EMPTY);
        }

        this.cost.set(0);
        this.access.execute((param1x, param2) -> {
            BlockState var0x = param1x.getBlockState(param2);
            if (!param0.getAbilities().instabuild && var0x.is(BlockTags.ANVIL) && param0.getRandom().nextFloat() < 0.12F) {
                BlockState var1x = AnvilBlock.damage(var0x);
                if (var1x == null) {
                    param1x.removeBlock(param2, false);
                    param1x.levelEvent(1029, param2, 0);
                } else {
                    param1x.setBlock(param2, var1x, 2);
                    param1x.levelEvent(1030, param2, 0);
                }
            } else {
                param1x.levelEvent(1030, param2, 0);
            }

        });
    }

    @Override
    public void createResult() {
        ItemStack var0 = this.inputSlots.getItem(0);
        this.cost.set(1);
        int var1 = 0;
        int var2 = 0;
        int var3 = 0;
        if (var0.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.cost.set(0);
        } else {
            ItemStack var4 = var0.copy();
            ItemStack var5 = this.inputSlots.getItem(1);
            Map<Enchantment, Integer> var6 = EnchantmentHelper.getEnchantments(var4);
            var2 += var0.getBaseRepairCost() + (var5.isEmpty() ? 0 : var5.getBaseRepairCost());
            this.repairItemCountCost = 0;
            if (!var5.isEmpty()) {
                boolean var7 = var5.is(Items.ENCHANTED_BOOK) && !EnchantedBookItem.getEnchantments(var5).isEmpty();
                if (var4.isDamageableItem() && var4.getItem().isValidRepairItem(var0, var5)) {
                    int var8 = Math.min(var4.getDamageValue(), var4.getMaxDamage() / 4);
                    if (var8 <= 0) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.cost.set(0);
                        return;
                    }

                    int var9;
                    for(var9 = 0; var8 > 0 && var9 < var5.getCount(); ++var9) {
                        int var10 = var4.getDamageValue() - var8;
                        var4.setDamageValue(var10);
                        ++var1;
                        var8 = Math.min(var4.getDamageValue(), var4.getMaxDamage() / 4);
                    }

                    this.repairItemCountCost = var9;
                } else {
                    if (!var7 && (!var4.is(var5.getItem()) || !var4.isDamageableItem())) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.cost.set(0);
                        return;
                    }

                    if (var4.isDamageableItem() && !var7) {
                        int var11 = var0.getMaxDamage() - var0.getDamageValue();
                        int var12 = var5.getMaxDamage() - var5.getDamageValue();
                        int var13 = var12 + var4.getMaxDamage() * 12 / 100;
                        int var14 = var11 + var13;
                        int var15 = var4.getMaxDamage() - var14;
                        if (var15 < 0) {
                            var15 = 0;
                        }

                        if (var15 < var4.getDamageValue()) {
                            var4.setDamageValue(var15);
                            var1 += 2;
                        }
                    }

                    Map<Enchantment, Integer> var16 = EnchantmentHelper.getEnchantments(var5);
                    boolean var17 = false;
                    boolean var18 = false;

                    for(Enchantment var19 : var16.keySet()) {
                        if (var19 != null) {
                            int var20 = var6.getOrDefault(var19, 0);
                            int var21 = var16.get(var19);
                            var21 = var20 == var21 ? var21 + 1 : Math.max(var21, var20);
                            boolean var22 = var19.canEnchant(var0);
                            if (this.player.getAbilities().instabuild || var0.is(Items.ENCHANTED_BOOK)) {
                                var22 = true;
                            }

                            for(Enchantment var23 : var6.keySet()) {
                                if (var23 != var19 && !var19.isCompatibleWith(var23)) {
                                    var22 = false;
                                    ++var1;
                                }
                            }

                            if (!var22) {
                                var18 = true;
                            } else {
                                var17 = true;
                                if (var21 > var19.getMaxLevel()) {
                                    var21 = var19.getMaxLevel();
                                }

                                var6.put(var19, var21);
                                int var24 = 0;
                                switch(var19.getRarity()) {
                                    case COMMON:
                                        var24 = 1;
                                        break;
                                    case UNCOMMON:
                                        var24 = 2;
                                        break;
                                    case RARE:
                                        var24 = 4;
                                        break;
                                    case VERY_RARE:
                                        var24 = 8;
                                }

                                if (var7) {
                                    var24 = Math.max(1, var24 / 2);
                                }

                                var1 += var24 * var21;
                                if (var0.getCount() > 1) {
                                    var1 = 40;
                                }
                            }
                        }
                    }

                    if (var18 && !var17) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.cost.set(0);
                        return;
                    }
                }
            }

            if (this.itemName != null && !Util.isBlank(this.itemName)) {
                if (!this.itemName.equals(var0.getHoverName().getString())) {
                    var3 = 1;
                    var1 += var3;
                    var4.setHoverName(Component.literal(this.itemName));
                }
            } else if (var0.hasCustomHoverName()) {
                var3 = 1;
                var1 += var3;
                var4.resetHoverName();
            }

            this.cost.set(var2 + var1);
            if (var1 <= 0) {
                var4 = ItemStack.EMPTY;
            }

            if (var3 == var1 && var3 > 0 && this.cost.get() >= 40) {
                this.cost.set(39);
            }

            if (this.cost.get() >= 40 && !this.player.getAbilities().instabuild) {
                var4 = ItemStack.EMPTY;
            }

            if (!var4.isEmpty()) {
                int var25 = var4.getBaseRepairCost();
                if (!var5.isEmpty() && var25 < var5.getBaseRepairCost()) {
                    var25 = var5.getBaseRepairCost();
                }

                if (var3 != var1 || var3 == 0) {
                    var25 = calculateIncreasedRepairCost(var25);
                }

                var4.setRepairCost(var25);
                EnchantmentHelper.setEnchantments(var6, var4);
            }

            this.resultSlots.setItem(0, var4);
            this.broadcastChanges();
        }
    }

    public static int calculateIncreasedRepairCost(int param0) {
        return param0 * 2 + 1;
    }

    public boolean setItemName(String param0) {
        String var0 = validateName(param0);
        if (var0 != null && !var0.equals(this.itemName)) {
            this.itemName = var0;
            if (this.getSlot(2).hasItem()) {
                ItemStack var1 = this.getSlot(2).getItem();
                if (Util.isBlank(var0)) {
                    var1.resetHoverName();
                } else {
                    var1.setHoverName(Component.literal(var0));
                }
            }

            this.createResult();
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    private static String validateName(String param0) {
        String var0 = SharedConstants.filterText(param0);
        return var0.length() <= 50 ? var0 : null;
    }

    public int getCost() {
        return this.cost.get();
    }
}
