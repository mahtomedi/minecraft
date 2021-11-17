package net.minecraft.world.level.levelgen.material;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseChunk;

public class MaterialRuleList implements WorldGenMaterialRule {
    private final List<WorldGenMaterialRule> materialRuleList;

    public MaterialRuleList(List<WorldGenMaterialRule> param0) {
        this.materialRuleList = param0;
    }

    @Nullable
    @Override
    public BlockState apply(NoiseChunk param0, int param1, int param2, int param3) {
        for(WorldGenMaterialRule var0 : this.materialRuleList) {
            BlockState var1 = var0.apply(param0, param1, param2, param3);
            if (var1 != null) {
                return var1;
            }
        }

        return null;
    }
}
