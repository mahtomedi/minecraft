package net.minecraft.world.level.dimension.special;

import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G09 extends NormalDimension {
    private static final Vector3f WHITE = new Vector3f(1.0F, 1.0F, 1.0F);
    private static final Vector3f BLACK = new Vector3f(0.0F, 0.0F, 0.0F);

    public G09(Level param0, DimensionType param1) {
        super(param0, param1);
    }

    @OnlyIn(Dist.CLIENT)
    private static Vector3f getColor(BlockPos param0) {
        return ((param0.getX() ^ param0.getY() ^ param0.getZ()) & 1) == 0 ? WHITE : BLACK;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vector3f getExtraTint(BlockState param0, BlockPos param1) {
        return getColor(param1);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <T extends LivingEntity> Vector3f getEntityExtraTint(T param0) {
        return getColor(param0.blockPosition());
    }
}
