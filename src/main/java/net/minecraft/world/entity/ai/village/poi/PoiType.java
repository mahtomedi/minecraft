package net.minecraft.world.entity.ai.village.poi;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.state.BlockState;

public record PoiType(Set<BlockState> matchingStates, int maxTickets, int validRange) {
    public static final Predicate<Holder<PoiType>> NONE = param0 -> false;

    public PoiType(Set<BlockState> param0, int param1, int param2) {
        param0 = Set.copyOf(param0);
        this.matchingStates = param0;
        this.maxTickets = param1;
        this.validRange = param2;
    }

    public boolean is(BlockState param0) {
        return this.matchingStates.contains(param0);
    }
}
