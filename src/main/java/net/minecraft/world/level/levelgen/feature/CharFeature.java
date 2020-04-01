package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.CharConfiguration;

public class CharFeature extends Feature<CharConfiguration> {
    public CharFeature(Function<Dynamic<?>, ? extends CharConfiguration> param0, Function<Random, ? extends CharConfiguration> param1) {
        super(param0, param1);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, CharConfiguration param4
    ) {
        return place(param3, param4, param3x -> this.setBlock(param0, param3x, param4.material.getState(param2, param3x)));
    }

    private static void addPixel(Consumer<BlockPos> param0, BlockPos.MutableBlockPos param1, Direction param2, int param3, byte param4) {
        if ((param3 & param4) != 0) {
            param0.accept(param1);
        }

        param1.move(param2);
    }

    public static boolean place(BlockPos param0, CharConfiguration param1, Consumer<BlockPos> param2) {
        Direction var0 = param1.orientation.rotate(Direction.EAST);
        Direction var1 = param1.orientation.rotate(Direction.DOWN);
        byte[] var2 = param1.getBytes();
        if (var2 == null) {
            return false;
        } else {
            BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();

            for(int var4 = 0; var4 < 8; ++var4) {
                var3.set(param0).move(var1, var4);
                byte var5 = var2[var4];
                addPixel(param2, var3, var0, 128, var5);
                addPixel(param2, var3, var0, 64, var5);
                addPixel(param2, var3, var0, 32, var5);
                addPixel(param2, var3, var0, 16, var5);
                addPixel(param2, var3, var0, 8, var5);
                addPixel(param2, var3, var0, 4, var5);
                addPixel(param2, var3, var0, 2, var5);
                addPixel(param2, var3, var0, 1, var5);
            }

            return true;
        }
    }
}
