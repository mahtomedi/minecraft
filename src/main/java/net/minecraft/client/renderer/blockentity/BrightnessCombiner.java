package net.minecraft.client.renderer.blockentity;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BrightnessCombiner<S extends BlockEntity> implements DoubleBlockCombiner.Combiner<S, Int2IntFunction> {
    public Int2IntFunction acceptDouble(S param0, S param1) {
        return param2 -> {
            int var0 = LevelRenderer.getLightColor(param0.getLevel(), param0.getBlockPos());
            int var1x = LevelRenderer.getLightColor(param1.getLevel(), param1.getBlockPos());
            int var2x = LightTexture.block(var0);
            int var3 = LightTexture.block(var1x);
            int var4 = LightTexture.sky(var0);
            int var5 = LightTexture.sky(var1x);
            return LightTexture.pack(Math.max(var2x, var3), Math.max(var4, var5));
        };
    }

    public Int2IntFunction acceptSingle(S param0) {
        return param0x -> param0x;
    }

    public Int2IntFunction acceptNone() {
        return param0 -> param0;
    }
}
