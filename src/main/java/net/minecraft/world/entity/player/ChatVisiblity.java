package net.minecraft.world.entity.player;

import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.util.Mth;

public enum ChatVisiblity {
    FULL(0, "options.chat.visibility.full"),
    SYSTEM(1, "options.chat.visibility.system"),
    HIDDEN(2, "options.chat.visibility.hidden");

    private static final ChatVisiblity[] BY_ID = Arrays.stream(values())
        .sorted(Comparator.comparingInt(ChatVisiblity::getId))
        .toArray(param0 -> new ChatVisiblity[param0]);
    private final int id;
    private final String key;

    private ChatVisiblity(int param0, String param1) {
        this.id = param0;
        this.key = param1;
    }

    public int getId() {
        return this.id;
    }

    public String getKey() {
        return this.key;
    }

    public static ChatVisiblity byId(int param0) {
        return BY_ID[Mth.positiveModulo(param0, BY_ID.length)];
    }
}
