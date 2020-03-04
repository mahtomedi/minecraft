package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class GoToClosestVillage extends Behavior<Villager> {
    private final float speed;
    private final int closeEnoughDistance;

    public GoToClosestVillage(float param0, int param1) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.speed = param0;
        this.closeEnoughDistance = param1;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        return !param0.isVillage(param1.blockPosition());
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        PoiManager var0 = param0.getPoiManager();
        int var1 = var0.sectionsToVillage(SectionPos.of(param1.blockPosition()));
        Vec3 var2 = null;

        for(int var3 = 0; var3 < 5; ++var3) {
            Vec3 var4 = RandomPos.getLandPos(param1, 15, 7, param1x -> (double)(-param0.sectionsToVillage(SectionPos.of(param1x))));
            if (var4 != null) {
                int var5 = var0.sectionsToVillage(SectionPos.of(new BlockPos(var4)));
                if (var5 < var1) {
                    var2 = var4;
                    break;
                }

                if (var5 == var1) {
                    var2 = var4;
                }
            }
        }

        if (var2 != null) {
            param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(var2, this.speed, this.closeEnoughDistance));
        }

    }
}
