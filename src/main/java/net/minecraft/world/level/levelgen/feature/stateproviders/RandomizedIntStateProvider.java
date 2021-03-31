package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class RandomizedIntStateProvider extends BlockStateProvider {
    public static final Codec<RandomizedIntStateProvider> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockStateProvider.CODEC.fieldOf("source").forGetter(param0x -> param0x.source),
                    Codec.STRING.fieldOf("property").forGetter(param0x -> param0x.propertyName),
                    IntProvider.CODEC.fieldOf("values").forGetter(param0x -> param0x.values)
                )
                .apply(param0, RandomizedIntStateProvider::new)
    );
    private final BlockStateProvider source;
    private final String propertyName;
    @Nullable
    private IntegerProperty property;
    private final IntProvider values;

    public RandomizedIntStateProvider(BlockStateProvider param0, IntegerProperty param1, IntProvider param2) {
        this.source = param0;
        this.property = param1;
        this.propertyName = param1.getName();
        this.values = param2;
        Collection<Integer> var0 = param1.getPossibleValues();

        for(int var1 = param2.getMinValue(); var1 <= param2.getMaxValue(); ++var1) {
            if (!var0.contains(var1)) {
                throw new IllegalArgumentException("Property value out of range: " + param1.getName() + ": " + var1);
            }
        }

    }

    public RandomizedIntStateProvider(BlockStateProvider param0, String param1, IntProvider param2) {
        this.source = param0;
        this.propertyName = param1;
        this.values = param2;
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.RANDOMIZED_INT_STATE_PROVIDER;
    }

    @Override
    public BlockState getState(Random param0, BlockPos param1) {
        BlockState var0 = this.source.getState(param0, param1);
        if (this.property == null || !var0.hasProperty(this.property)) {
            this.property = findProperty(var0, this.propertyName);
        }

        return var0.setValue(this.property, Integer.valueOf(this.values.sample(param0)));
    }

    private static IntegerProperty findProperty(BlockState param0, String param1) {
        Collection<Property<?>> var0 = param0.getProperties();
        Optional<IntegerProperty> var1 = var0.stream()
            .filter(param1x -> param1x.getName().equals(param1))
            .filter(param0x -> param0x instanceof IntegerProperty)
            .map(param0x -> (IntegerProperty)param0x)
            .findAny();
        return var1.orElseThrow(() -> new IllegalArgumentException("Illegal property: " + param1));
    }
}
