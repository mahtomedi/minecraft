package net.minecraft.world.level.block.entity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.state.BlockState;

public class DaylightDetectorBlockEntity extends BlockEntity implements TickableBlockEntity {
    public DaylightDetectorBlockEntity() {
        super(BlockEntityType.DAYLIGHT_DETECTOR);
    }

    @Override
    public void tick() {
        if (this.level != null && !this.level.isClientSide && this.level.getGameTime() % 20L == 0L) {
            BlockState var0 = this.getBlockState();
            Block var1 = var0.getBlock();
            if (var1 instanceof DaylightDetectorBlock) {
                DaylightDetectorBlock.updateSignalStrength(var0, this.level, this.worldPosition);
            }
        }

    }
}
