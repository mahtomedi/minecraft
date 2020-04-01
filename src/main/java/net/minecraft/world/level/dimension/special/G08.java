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

public class G08 extends NormalDimension {
    private static final Vector3f GREEN = new Vector3f(s(129.0F), s(185.0F), s(0.0F));
    private static final Vector3f YELLOW = new Vector3f(s(255.0F), s(185.0F), s(2.0F));
    private static final Vector3f BLUE = new Vector3f(s(1.0F), s(164.0F), s(239.0F));
    private static final Vector3f RED = new Vector3f(s(244.0F), s(78.0F), s(36.0F));

    private static float s(float param0) {
        return param0 / 255.0F;
    }

    public G08(Level param0, DimensionType param1) {
        super(param0, param1);
    }

    @OnlyIn(Dist.CLIENT)
    private static Vector3f getQuadrantColor(BlockPos param0) {
        if (param0.getX() < 0) {
            return param0.getZ() > 0 ? GREEN : YELLOW;
        } else {
            return param0.getZ() > 0 ? RED : BLUE;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vector3f getExtraTint(BlockState param0, BlockPos param1) {
        return getQuadrantColor(param1);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <T extends LivingEntity> Vector3f getEntityExtraTint(T param0) {
        return getQuadrantColor(param0.blockPosition());
    }
}
