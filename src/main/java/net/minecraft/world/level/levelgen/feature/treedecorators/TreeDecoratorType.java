package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class TreeDecoratorType<P extends TreeDecorator> {
    public static final TreeDecoratorType<TrunkVineDecorator> TRUNK_VINE = register("trunk_vine", TrunkVineDecorator::new);
    public static final TreeDecoratorType<LeaveVineDecorator> LEAVE_VINE = register("leave_vine", LeaveVineDecorator::new);
    public static final TreeDecoratorType<CocoaDecorator> COCOA = register("cocoa", CocoaDecorator::new);
    public static final TreeDecoratorType<BeehiveDecorator> BEEHIVE = register("beehive", BeehiveDecorator::new);
    public static final TreeDecoratorType<AlterGroundDecorator> ALTER_GROUND = register("alter_ground", AlterGroundDecorator::new);
    private final Function<Dynamic<?>, P> deserializer;

    private static <P extends TreeDecorator> TreeDecoratorType<P> register(String param0, Function<Dynamic<?>, P> param1) {
        return Registry.register(Registry.TREE_DECORATOR_TYPES, param0, new TreeDecoratorType<>(param1));
    }

    private TreeDecoratorType(Function<Dynamic<?>, P> param0) {
        this.deserializer = param0;
    }

    public P deserialize(Dynamic<?> param0) {
        return this.deserializer.apply(param0);
    }
}
