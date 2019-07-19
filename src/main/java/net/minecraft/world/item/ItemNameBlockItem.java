package net.minecraft.world.item;

import net.minecraft.world.level.block.Block;

public class ItemNameBlockItem extends BlockItem {
    public ItemNameBlockItem(Block param0, Item.Properties param1) {
        super(param0, param1);
    }

    @Override
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }
}
