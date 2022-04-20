package net.minecraft.client;

import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum NarratorStatus {
    OFF(0, "options.narrator.off"),
    ALL(1, "options.narrator.all"),
    CHAT(2, "options.narrator.chat"),
    SYSTEM(3, "options.narrator.system");

    private static final NarratorStatus[] BY_ID = Arrays.stream(values())
        .sorted(Comparator.comparingInt(NarratorStatus::getId))
        .toArray(param0 -> new NarratorStatus[param0]);
    private final int id;
    private final Component name;

    private NarratorStatus(int param0, String param1) {
        this.id = param0;
        this.name = Component.translatable(param1);
    }

    public int getId() {
        return this.id;
    }

    public Component getName() {
        return this.name;
    }

    public static NarratorStatus byId(int param0) {
        return BY_ID[Mth.positiveModulo(param0, BY_ID.length)];
    }
}
