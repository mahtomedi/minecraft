package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;

public class AdmireHeldItem<E extends PathfinderMob> extends RunOne<E> {
    public AdmireHeldItem(float param0) {
        super(ImmutableList.of(Pair.of(new RandomStroll(param0, 1, 0), 1), Pair.of(new DoNothing(10, 20), 1)));
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, E param1) {
        return !param1.getOffhandItem().isEmpty();
    }
}
