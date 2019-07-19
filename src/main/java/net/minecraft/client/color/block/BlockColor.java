package net.minecraft.client.color.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndBiomeGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface BlockColor {
    int getColor(BlockState var1, @Nullable BlockAndBiomeGetter var2, @Nullable BlockPos var3, int var4);
}
