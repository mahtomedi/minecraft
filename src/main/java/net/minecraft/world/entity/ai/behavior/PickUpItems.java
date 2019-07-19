package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class PickUpItems extends Behavior<Villager> {
    private List<ItemEntity> items = new ArrayList<>();

    public PickUpItems() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        this.items = param0.getEntitiesOfClass(ItemEntity.class, param1.getBoundingBox().inflate(4.0, 2.0, 4.0));
        return !this.items.isEmpty();
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        ItemEntity var0 = this.items.get(param0.random.nextInt(this.items.size()));
        if (param1.wantToPickUp(var0.getItem().getItem())) {
            Vec3 var1 = var0.position();
            param1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(new BlockPos(var1)));
            param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var1, 0.5F, 0));
        }

    }
}
