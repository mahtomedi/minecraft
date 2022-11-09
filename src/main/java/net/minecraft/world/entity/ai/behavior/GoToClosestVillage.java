package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class GoToClosestVillage {
    public static BehaviorControl<Villager> create(float param0, int param1) {
        return BehaviorBuilder.create(
            param2 -> param2.<MemoryAccessor>group(param2.absent(MemoryModuleType.WALK_TARGET)).apply(param2, param2x -> (param3, param4, param5) -> {
                        if (param3.isVillage(param4.blockPosition())) {
                            return false;
                        } else {
                            PoiManager var0x = param3.getPoiManager();
                            int var1x = var0x.sectionsToVillage(SectionPos.of(param4.blockPosition()));
                            Vec3 var2x = null;
    
                            for(int var3 = 0; var3 < 5; ++var3) {
                                Vec3 var4 = LandRandomPos.getPos(param4, 15, 7, param1x -> (double)(-var0x.sectionsToVillage(SectionPos.of(param1x))));
                                if (var4 != null) {
                                    int var5 = var0x.sectionsToVillage(SectionPos.of(new BlockPos(var4)));
                                    if (var5 < var1x) {
                                        var2x = var4;
                                        break;
                                    }
    
                                    if (var5 == var1x) {
                                        var2x = var4;
                                    }
                                }
                            }
    
                            if (var2x != null) {
                                param2x.set(new WalkTarget(var2x, param0, param1));
                            }
    
                            return true;
                        }
                    })
        );
    }
}
