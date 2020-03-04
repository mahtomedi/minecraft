package net.minecraft.world.inventory;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SmithingMenu extends ItemCombinerMenu {
    private static final Map<Item, Item> DIAMOND_TO_NETHERITE = ImmutableMap.<Item, Item>builder()
        .put(Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE)
        .put(Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS)
        .put(Items.DIAMOND_HELMET, Items.NETHERITE_HELMET)
        .put(Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS)
        .put(Items.DIAMOND_SWORD, Items.NETHERITE_SWORD)
        .put(Items.DIAMOND_AXE, Items.NETHERITE_AXE)
        .put(Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE)
        .put(Items.DIAMOND_HOE, Items.NETHERITE_HOE)
        .put(Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL)
        .build();

    public SmithingMenu(int param0, Inventory param1) {
        this(param0, param1, ContainerLevelAccess.NULL);
    }

    public SmithingMenu(int param0, Inventory param1, ContainerLevelAccess param2) {
        super(MenuType.SMITHING, param0, param1, param2);
    }

    @Override
    protected boolean isValidBlock(BlockState param0) {
        return param0.getBlock() == Blocks.SMITHING_TABLE;
    }

    @Override
    protected boolean mayPickup(Player param0, boolean param1) {
        return DIAMOND_TO_NETHERITE.containsKey(this.inputSlots.getItem(0).getItem()) && this.inputSlots.getItem(1).getItem() == Items.NETHERITE_INGOT;
    }

    @Override
    protected ItemStack onTake(Player param0, ItemStack param1) {
        this.inputSlots.setItem(0, ItemStack.EMPTY);
        ItemStack var0 = this.inputSlots.getItem(1);
        var0.shrink(1);
        this.inputSlots.setItem(1, var0);
        this.access.execute((param0x, param1x) -> param0x.levelEvent(1044, param1x, 0));
        return param1;
    }

    @Override
    public void createResult() {
        ItemStack var0 = this.inputSlots.getItem(0);
        ItemStack var1 = this.inputSlots.getItem(1);
        Item var2 = DIAMOND_TO_NETHERITE.get(var0.getItem());
        if (var1.getItem() == Items.NETHERITE_INGOT && var2 != null) {
            ItemStack var3 = new ItemStack(var2);
            CompoundTag var4 = var0.getTag();
            var3.setTag(var4 != null ? var4.copy() : null);
            this.resultSlots.setItem(0, var3);
        } else {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        }

    }
}
