package net.minecraft.world.level.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class WoolCarpetBlock extends CarpetBlock {
    private final DyeColor color;

    protected WoolCarpetBlock(DyeColor param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.color = param0;
    }

    public DyeColor getColor() {
        return this.color;
    }
}
