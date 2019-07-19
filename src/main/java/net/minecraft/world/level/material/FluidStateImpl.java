package net.minecraft.world.level.material;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.level.block.state.AbstractStateHolder;
import net.minecraft.world.level.block.state.properties.Property;

public class FluidStateImpl extends AbstractStateHolder<Fluid, FluidState> implements FluidState {
    public FluidStateImpl(Fluid param0, ImmutableMap<Property<?>, Comparable<?>> param1) {
        super(param0, param1);
    }

    @Override
    public Fluid getType() {
        return this.owner;
    }
}
