package net.minecraft.world.level;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Abilities;

public enum GameType {
    NOT_SET(-1, ""),
    SURVIVAL(0, "survival"),
    CREATIVE(1, "creative"),
    ADVENTURE(2, "adventure"),
    SPECTATOR(3, "spectator");

    private final int id;
    private final String name;

    private GameType(int param0, String param1) {
        this.id = param0;
        this.name = param1;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Component getDisplayName() {
        return new TranslatableComponent("gameMode." + this.name);
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
        return byId(param0, SURVIVAL);
    }

    public static GameType byId(int param0, GameType param1) {
        for(GameType var0 : values()) {
            if (var0.id == param0) {
                return var0;
            }
        }

        return param1;
    }

    public static GameType byName(String param0) {
        return byName(param0, SURVIVAL);
    }

    public static GameType byName(String param0, GameType param1) {
        for(GameType var0 : values()) {
            if (var0.name.equals(param0)) {
                return var0;
            }
        }

        return param1;
    }
}
