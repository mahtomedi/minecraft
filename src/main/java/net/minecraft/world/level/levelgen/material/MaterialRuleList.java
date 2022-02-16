package net.minecraft.world.level.levelgen.material;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;

public record MaterialRuleList(List<NoiseChunk.BlockStateFiller> materialRuleList) implements NoiseChunk.BlockStateFiller {
    @Nullable
    @Override
    public BlockState calculate(DensityFunction.FunctionContext param0) {
        for(NoiseChunk.BlockStateFiller var0 : this.materialRuleList) {
            BlockState var1 = var0.calculate(param0);
            if (var1 != null) {
                return var1;
            }
        }

        return null;
    }
}
