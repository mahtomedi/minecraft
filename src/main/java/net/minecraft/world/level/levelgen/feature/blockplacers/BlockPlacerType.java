package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class BlockPlacerType<P extends BlockPlacer> {
    public static final BlockPlacerType<SimpleBlockPlacer> SIMPLE_BLOCK_PLACER = register("simple_block_placer", SimpleBlockPlacer::new);
    public static final BlockPlacerType<DoublePlantPlacer> DOUBLE_PLANT_PLACER = register("double_plant_placer", DoublePlantPlacer::new);
    public static final BlockPlacerType<ColumnPlacer> COLUMN_PLACER = register("column_placer", ColumnPlacer::new);
    private final Function<Dynamic<?>, P> deserializer;

    private static <P extends BlockPlacer> BlockPlacerType<P> register(String param0, Function<Dynamic<?>, P> param1) {
        return Registry.register(Registry.BLOCK_PLACER_TYPES, param0, new BlockPlacerType<>(param1));
    }

    private BlockPlacerType(Function<Dynamic<?>, P> param0) {
        this.deserializer = param0;
    }

    public P deserialize(Dynamic<?> param0) {
        return this.deserializer.apply(param0);
    }
}
