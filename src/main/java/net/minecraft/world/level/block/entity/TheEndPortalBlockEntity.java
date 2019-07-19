package net.minecraft.world.level.block.entity;

import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TheEndPortalBlockEntity extends BlockEntity {
    public TheEndPortalBlockEntity(BlockEntityType<?> param0) {
        super(param0);
    }

    public TheEndPortalBlockEntity() {
        this(BlockEntityType.END_PORTAL);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderFace(Direction param0) {
        return param0 == Direction.UP;
    }
}
