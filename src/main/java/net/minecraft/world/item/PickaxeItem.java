package net.minecraft.world.item;

import net.minecraft.tags.BlockTags;

public class PickaxeItem extends DiggerItem {
    protected PickaxeItem(Tier param0, int param1, float param2, Item.Properties param3) {
        super((float)param1, param2, param0, BlockTags.MINEABLE_WITH_PICKAXE, param3);
    }
}
