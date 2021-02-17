package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.BiasedRangeDecoratorConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractBiasedRangeDecorator extends VerticalDecorator<BiasedRangeDecoratorConfiguration> {
    private static final Logger LOGGER = LogManager.getLogger();

    public AbstractBiasedRangeDecorator(Codec<BiasedRangeDecoratorConfiguration> param0) {
        super(param0);
    }

    protected int y(DecorationContext param0, Random param1, BiasedRangeDecoratorConfiguration param2, int param3) {
        int var0 = param2.bottomInclusive().resolveY(param0);
        int var1 = param2.topInclusive().resolveY(param0);
        if (var0 >= var1) {
            LOGGER.warn("Empty range decorator: {} [{}-{}]", this, var0, var1);
            return var0;
        } else {
            return this.y(param1, var0, var1, param2.cutoff());
        }
    }

    protected abstract int y(Random var1, int var2, int var3, int var4);
}
