package net.minecraft.world.level;

import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Abilities;
import org.jetbrains.annotations.Contract;

public enum GameType implements StringRepresentable {
    SURVIVAL(0, "survival"),
    CREATIVE(1, "creative"),
    ADVENTURE(2, "adventure"),
    SPECTATOR(3, "spectator");

    public static final GameType DEFAULT_MODE = SURVIVAL;
    public static final StringRepresentable.EnumCodec<GameType> CODEC = StringRepresentable.fromEnum(GameType::values);
    private static final IntFunction<GameType> BY_ID = ByIdMap.continuous(GameType::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    private static final int NOT_SET = -1;
    private final int id;
    private final String name;
    private final Component shortName;
    private final Component longName;

    private GameType(int param0, String param1) {
        this.id = param0;
        this.name = param1;
        this.shortName = Component.translatable("selectWorld.gameMode." + param1);
        this.longName = Component.translatable("gameMode." + param1);
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public Component getLongDisplayName() {
        return this.longName;
    }

    public Component getShortDisplayName() {
        return this.shortName;
    }

    public void updatePlayerAbilities(Abilities param0) {
        if (this == CREATIVE) {
            param0.mayfly = true;
            param0.instabuild = true;
            param0.invulnerable = true;
        } else if (this == SPECTATOR) {
            param0.mayfly = true;
            param0.instabuild = false;
            param0.invulnerable = true;
            param0.flying = true;
        } else {
            param0.mayfly = false;
            param0.instabuild = false;
            param0.invulnerable = false;
            param0.flying = false;
        }

        param0.mayBuild = !this.isBlockPlacingRestricted();
    }

    public boolean isBlockPlacingRestricted() {
        return this == ADVENTURE || this == SPECTATOR;
    }

    public boolean isCreative() {
        return this == CREATIVE;
    }

    public boolean isSurvival() {
        return this == SURVIVAL || this == ADVENTURE;
    }

    public static GameType byId(int param0) {
        return BY_ID.apply(param0);
    }

    public static GameType byName(String param0) {
        return byName(param0, SURVIVAL);
    }

    @Nullable
    @Contract("_,!null->!null;_,null->_")
    public static GameType byName(String param0, @Nullable GameType param1) {
        GameType var0 = CODEC.byName(param0);
        return var0 != null ? var0 : param1;
    }

    public static int getNullableId(@Nullable GameType param0) {
        return param0 != null ? param0.id : -1;
    }

    @Nullable
    public static GameType byNullableId(int param0) {
        return param0 == -1 ? null : byId(param0);
    }
}
