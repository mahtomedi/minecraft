package net.minecraft.world.level.block.state.predicate;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockStatePredicate implements Predicate<BlockState> {
    public static final Predicate<BlockState> ANY = param0 -> true;
    private final StateDefinition<Block, BlockState> definition;
    private final Map<Property<?>, Predicate<Object>> properties = Maps.newHashMap();

    private BlockStatePredicate(StateDefinition<Block, BlockState> param0) {
        this.definition = param0;
    }

    public static BlockStatePredicate forBlock(Block param0) {
        return new BlockStatePredicate(param0.getStateDefinition());
    }

    public boolean test(@Nullable BlockState param0) {
        if (param0 != null && param0.getBlock().equals(this.definition.getOwner())) {
            if (this.properties.isEmpty()) {
                return true;
            } else {
                for(Entry<Property<?>, Predicate<Object>> var0 : this.properties.entrySet()) {
                    if (!this.applies(param0, var0.getKey(), var0.getValue())) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    protected <T extends Comparable<T>> boolean applies(BlockState param0, Property<T> param1, Predicate<Object> param2) {
        T var0 = param0.getValue(param1);
        return param2.test(var0);
    }

    public <V extends Comparable<V>> BlockStatePredicate where(Property<V> param0, Predicate<Object> param1) {
        if (!this.definition.getProperties().contains(param0)) {
            throw new IllegalArgumentException(this.definition + " cannot support property " + param0);
        } else {
            this.properties.put(param0, param1);
            return this;
        }
    }
}
