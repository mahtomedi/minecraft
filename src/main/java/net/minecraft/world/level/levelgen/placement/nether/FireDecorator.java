package net.minecraft.world.level.levelgen.placement.nether;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.placement.FrequencyDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

public class FireDecorator extends SimpleFeatureDecorator<FrequencyDecoratorConfiguration> {
    public FireDecorator(Codec<FrequencyDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, FrequencyDecoratorConfiguration param1, BlockPos param2) {
        List<BlockPos> var0 = Lists.newArrayList();

        for(int var1 = 0; var1 < param0.nextInt(param0.nextInt(param1.count) + 1) + 1; ++var1) {
            int var2 = param0.nextInt(16) + param2.getX();
            int var3 = param0.nextInt(16) + param2.getZ();
            int var4 = param0.nextInt(120) + 4;
            var0.add(new BlockPos(var2, var4, var3));
        }

        return var0.stream();
    }
}
