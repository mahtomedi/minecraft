package net.minecraft.client;

import java.util.Arrays;
import java.util.Comparator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum CloudStatus {
    OFF(0, "options.off"),
    FAST(1, "options.clouds.fast"),
    FANCY(2, "options.clouds.fancy");

    private static final CloudStatus[] BY_ID = Arrays.stream(values())
        .sorted(Comparator.comparingInt(CloudStatus::getId))
        .toArray(param0 -> new CloudStatus[param0]);
    private final int id;
    private final String key;

    private CloudStatus(int param0, String param1) {
        this.id = param0;
        this.key = param1;
    }

    public int getId() {
        return this.id;
    }

    public String getKey() {
        return this.key;
    }
}
