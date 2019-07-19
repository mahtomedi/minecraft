package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface StateHolder<C> {
    Logger LOGGER = LogManager.getLogger();

    <T extends Comparable<T>> T getValue(Property<T> var1);

    <T extends Comparable<T>, V extends T> C setValue(Property<T> var1, V var2);

    ImmutableMap<Property<?>, Comparable<?>> getValues();

    static <T extends Comparable<T>> String getName(Property<T> param0, Comparable<?> param1) {
        return param0.getName((T)param1);
    }

    static <S extends StateHolder<S>, T extends Comparable<T>> S setValueHelper(S param0, Property<T> param1, String param2, String param3, String param4) {
        Optional<T> var0 = param1.getValue(param4);
        if (var0.isPresent()) {
            return param0.setValue(param1, var0.get());
        } else {
            LOGGER.warn("Unable to read property: {} with value: {} for input: {}", param2, param4, param3);
            return param0;
        }
    }
}
