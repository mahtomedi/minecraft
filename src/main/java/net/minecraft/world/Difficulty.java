package net.minecraft.world;

import java.util.Arrays;
import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum Difficulty {
    PEACEFUL(0, "peaceful"),
    EASY(1, "easy"),
    NORMAL(2, "normal"),
    HARD(3, "hard");

    private static final Difficulty[] BY_ID = Arrays.stream(values())
        .sorted(Comparator.comparingInt(Difficulty::getId))
        .toArray(param0 -> new Difficulty[param0]);
    private final int id;
    private final String key;

    private Difficulty(int param0, String param1) {
        this.id = param0;
        this.key = param1;
    }

    public int getId() {
        return this.id;
    }

    public Component getDisplayName() {
        return new TranslatableComponent("options.difficulty." + this.key);
    }

    public static Difficulty byId(int param0) {
        return BY_ID[param0 % BY_ID.length];
    }

    @Nullable
    public static Difficulty byName(String param0) {
        for(Difficulty var0 : values()) {
            if (var0.key.equals(param0)) {
                return var0;
            }
        }

        return null;
    }

    public String getKey() {
        return this.key;
    }

    @OnlyIn(Dist.CLIENT)
    public Difficulty nextById() {
        return BY_ID[(this.id + 1) % BY_ID.length];
    }
}
