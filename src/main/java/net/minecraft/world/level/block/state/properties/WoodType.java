package net.minecraft.world.level.block.state.properties;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WoodType {
    private static final Set<WoodType> VALUES = new ObjectArraySet<>();
    public static final WoodType OAK = register(new WoodType("oak"));
    public static final WoodType SPRUCE = register(new WoodType("spruce"));
    public static final WoodType BIRCH = register(new WoodType("birch"));
    public static final WoodType ACACIA = register(new WoodType("acacia"));
    public static final WoodType JUNGLE = register(new WoodType("jungle"));
    public static final WoodType DARK_OAK = register(new WoodType("dark_oak"));
    private final String name;

    protected WoodType(String param0) {
        this.name = param0;
    }

    private static WoodType register(WoodType param0) {
        VALUES.add(param0);
        return param0;
    }

    @OnlyIn(Dist.CLIENT)
    public static Stream<WoodType> values() {
        return VALUES.stream();
    }

    @OnlyIn(Dist.CLIENT)
    public String name() {
        return this.name;
    }
}
