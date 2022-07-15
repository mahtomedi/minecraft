package net.minecraft.client.multiplayer.chat;

import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum ChatPreviewStatus implements OptionEnum {
    OFF(0, "options.off"),
    LIVE(1, "options.chatPreview.live"),
    CONFIRM(2, "options.chatPreview.confirm");

    private static final ChatPreviewStatus[] BY_ID = Arrays.stream(values())
        .sorted(Comparator.comparingInt(ChatPreviewStatus::getId))
        .toArray(param0 -> new ChatPreviewStatus[param0]);
    private final int id;
    private final String key;

    private ChatPreviewStatus(int param0, String param1) {
        this.id = param0;
        this.key = param1;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public int getId() {
        return this.id;
    }

    public static ChatPreviewStatus byId(int param0) {
        return BY_ID[Mth.positiveModulo(param0, BY_ID.length)];
    }
}
