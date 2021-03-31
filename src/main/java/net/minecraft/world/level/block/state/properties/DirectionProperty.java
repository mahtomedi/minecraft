package net.minecraft.world.level.block.state.properties;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.Direction;

public class DirectionProperty extends EnumProperty<Direction> {
    protected DirectionProperty(String param0, Collection<Direction> param1) {
        super(param0, Direction.class, param1);
    }

    public static DirectionProperty create(String param0) {
        return create(param0, Predicates.alwaysTrue());
    }

    public static DirectionProperty create(String param0, Predicate<Direction> param1) {
        return create(param0, Arrays.stream(Direction.values()).filter(param1).collect(Collectors.toList()));
    }

    public static DirectionProperty create(String param0, Direction... param1) {
        return create(param0, Lists.newArrayList(param1));
    }

    public static DirectionProperty create(String param0, Collection<Direction> param1) {
        return new DirectionProperty(param0, param1);
    }
}
