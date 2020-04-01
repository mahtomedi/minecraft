package net.minecraft.world.level.dimension.special;

import com.mojang.math.Vector3f;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G10 extends NormalDimension {
    public G10(Level param0, DimensionType param1) {
        super(param0, param1);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void modifyLightmapColor(int param0, int param1, Vector3f param2) {
        param2.set(1.0F - param2.x(), 1.0F - param2.y(), 1.0F - param2.z());
    }
}
