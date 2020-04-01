package net.minecraft.world.level.dimension.special;

import com.mojang.math.Vector3f;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G14 extends NormalDimension {
    private final Vector3f selector;
    private final Vec3 fog;

    public G14(Level param0, DimensionType param1, Vector3f param2) {
        super(param0, param1);
        this.selector = param2;
        this.fog = new Vec3(param2);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vector3f getExtraTint(BlockState param0, BlockPos param1) {
        return this.selector;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <T extends LivingEntity> Vector3f getEntityExtraTint(T param0) {
        return this.selector;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 param0, float param1) {
        return this.fog;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void modifyLightmapColor(int param0, int param1, Vector3f param2) {
        param2.mul(this.selector);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vector3f getSunTint() {
        return this.selector;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vector3f getMoonTint() {
        return this.selector;
    }

    public static BiFunction<Level, DimensionType, ? extends Dimension> create(Vector3f param0) {
        return (param1, param2) -> new G14(param1, param2, param0);
    }
}
