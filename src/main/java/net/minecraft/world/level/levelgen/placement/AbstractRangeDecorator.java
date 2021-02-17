package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractRangeDecorator extends VerticalDecorator<RangeDecoratorConfiguration> {
    private static final Logger LOGGER = LogManager.getLogger();

    public AbstractRangeDecorator(Codec<RangeDecoratorConfiguration> param0) {
        super(param0);
    }

    protected int y(DecorationContext param0, Random param1, RangeDecoratorConfiguration param2, int param3) {
        int var0 = param2.bottomInclusive().resolveY(param0);
        int var1 = param2.topInclusive().resolveY(param0);
        if (var0 >= var1) {
            LOGGER.warn("Empty range decorator: {} [{}-{}]", this, var0, var1);
            return var0;
        } else {
            return this.y(param1, var0, var1);
        }
    }

    protected abstract int y(Random var1, int var2, int var3);
}
