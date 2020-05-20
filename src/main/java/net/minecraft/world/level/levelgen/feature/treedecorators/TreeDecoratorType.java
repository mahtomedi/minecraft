package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public class TreeDecoratorType<P extends TreeDecorator> {
    public static final TreeDecoratorType<TrunkVineDecorator> TRUNK_VINE = register("trunk_vine", TrunkVineDecorator.CODEC);
    public static final TreeDecoratorType<LeaveVineDecorator> LEAVE_VINE = register("leave_vine", LeaveVineDecorator.CODEC);
    public static final TreeDecoratorType<CocoaDecorator> COCOA = register("cocoa", CocoaDecorator.CODEC);
    public static final TreeDecoratorType<BeehiveDecorator> BEEHIVE = register("beehive", BeehiveDecorator.CODEC);
    public static final TreeDecoratorType<AlterGroundDecorator> ALTER_GROUND = register("alter_ground", AlterGroundDecorator.CODEC);
    private final Codec<P> codec;

    private static <P extends TreeDecorator> TreeDecoratorType<P> register(String param0, Codec<P> param1) {
        return Registry.register(Registry.TREE_DECORATOR_TYPES, param0, new TreeDecoratorType<>(param1));
    }

    private TreeDecoratorType(Codec<P> param0) {
        this.codec = param0;
    }

    public Codec<P> codec() {
        return this.codec;
    }
}
