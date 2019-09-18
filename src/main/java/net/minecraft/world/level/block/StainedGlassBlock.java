package net.minecraft.world.level.block;

import net.minecraft.world.item.DyeColor;

public class StainedGlassBlock extends AbstractGlassBlock implements BeaconBeamBlock {
    private final DyeColor color;

    public StainedGlassBlock(DyeColor param0, Block.Properties param1) {
        super(param1);
        this.color = param0;
    }

    @Override
    public DyeColor getColor() {
        return this.color;
    }
}
