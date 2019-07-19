package net.minecraft.world.level.block.state.predicate;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class BlockMaterialPredicate implements Predicate<BlockState> {
    private static final BlockMaterialPredicate AIR = new BlockMaterialPredicate(Material.AIR) {
        @Override
        public boolean test(@Nullable BlockState param0) {
            return param0 != null && param0.isAir();
        }
    };
    private final Material material;

    private BlockMaterialPredicate(Material param0) {
        this.material = param0;
    }

    public static BlockMaterialPredicate forMaterial(Material param0) {
        return param0 == Material.AIR ? AIR : new BlockMaterialPredicate(param0);
    }

    public boolean test(@Nullable BlockState param0) {
        return param0 != null && param0.getMaterial() == this.material;
    }
}
