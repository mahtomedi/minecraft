package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class TreeDecoratorType<P extends TreeDecorator> {
    public static final TreeDecoratorType<TrunkVineDecorator> TRUNK_VINE = register("trunk_vine", TrunkVineDecorator::new, TrunkVineDecorator::random);
    public static final TreeDecoratorType<LeaveVineDecorator> LEAVE_VINE = register("leave_vine", LeaveVineDecorator::new, LeaveVineDecorator::random);
    public static final TreeDecoratorType<CocoaDecorator> COCOA = register("cocoa", CocoaDecorator::new, CocoaDecorator::random);
    public static final TreeDecoratorType<BeehiveDecorator> BEEHIVE = register("beehive", BeehiveDecorator::new, BeehiveDecorator::random);
    public static final TreeDecoratorType<AlterGroundDecorator> ALTER_GROUND = register("alter_ground", AlterGroundDecorator::new, AlterGroundDecorator::random);
    private final Function<Dynamic<?>, P> deserializer;
    private final Function<Random, P> randomProvider;

    private static <P extends TreeDecorator> TreeDecoratorType<P> register(String param0, Function<Dynamic<?>, P> param1, Function<Random, P> param2) {
        return Registry.register(Registry.TREE_DECORATOR_TYPES, param0, new TreeDecoratorType<>(param1, param2));
    }

    public TreeDecoratorType(Function<Dynamic<?>, P> param0, Function<Random, P> param1) {
        this.deserializer = param0;
        this.randomProvider = param1;
    }

    public P deserialize(Dynamic<?> param0) {
        return this.deserializer.apply(param0);
    }

    public P createRandom(Random param0) {
        return this.randomProvider.apply(param0);
    }
}
