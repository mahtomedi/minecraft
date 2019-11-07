package net.minecraft.world.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum DyeColor implements StringRepresentable {
    WHITE(0, "white", 16383998, MaterialColor.SNOW, 15790320, 16777215),
    ORANGE(1, "orange", 16351261, MaterialColor.COLOR_ORANGE, 15435844, 16738335),
    MAGENTA(2, "magenta", 13061821, MaterialColor.COLOR_MAGENTA, 12801229, 16711935),
    LIGHT_BLUE(3, "light_blue", 3847130, MaterialColor.COLOR_LIGHT_BLUE, 6719955, 10141901),
    YELLOW(4, "yellow", 16701501, MaterialColor.COLOR_YELLOW, 14602026, 16776960),
    LIME(5, "lime", 8439583, MaterialColor.COLOR_LIGHT_GREEN, 4312372, 12582656),
    PINK(6, "pink", 15961002, MaterialColor.COLOR_PINK, 14188952, 16738740),
    GRAY(7, "gray", 4673362, MaterialColor.COLOR_GRAY, 4408131, 8421504),
    LIGHT_GRAY(8, "light_gray", 10329495, MaterialColor.COLOR_LIGHT_GRAY, 11250603, 13882323),
    CYAN(9, "cyan", 1481884, MaterialColor.COLOR_CYAN, 2651799, 65535),
    PURPLE(10, "purple", 8991416, MaterialColor.COLOR_PURPLE, 8073150, 10494192),
    BLUE(11, "blue", 3949738, MaterialColor.COLOR_BLUE, 2437522, 255),
    BROWN(12, "brown", 8606770, MaterialColor.COLOR_BROWN, 5320730, 9127187),
    GREEN(13, "green", 6192150, MaterialColor.COLOR_GREEN, 3887386, 65280),
    RED(14, "red", 11546150, MaterialColor.COLOR_RED, 11743532, 16711680),
    BLACK(15, "black", 1908001, MaterialColor.COLOR_BLACK, 1973019, 0);

    private static final DyeColor[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(DyeColor::getId)).toArray(param0 -> new DyeColor[param0]);
    private static final Int2ObjectOpenHashMap<DyeColor> BY_FIREWORK_COLOR = new Int2ObjectOpenHashMap<>(
        Arrays.stream(values()).collect(Collectors.toMap(param0 -> param0.fireworkColor, param0 -> param0))
    );
    private final int id;
    private final String name;
    private final MaterialColor color;
    private final int textureDiffuseColor;
    private final int textureDiffuseColorBGR;
    private final float[] textureDiffuseColors;
    private final int fireworkColor;
    private final int textColor;

    private DyeColor(int param0, String param1, int param2, MaterialColor param3, int param4, int param5) {
        this.id = param0;
        this.name = param1;
        this.textureDiffuseColor = param2;
        this.color = param3;
        this.textColor = param5;
        int param6 = (param2 & 0xFF0000) >> 16;
        int param7 = (param2 & 0xFF00) >> 8;
        int var0 = (param2 & 0xFF) >> 0;
        this.textureDiffuseColorBGR = var0 << 16 | param7 << 8 | param6 << 0;
        this.textureDiffuseColors = new float[]{(float)param6 / 255.0F, (float)param7 / 255.0F, (float)var0 / 255.0F};
        this.fireworkColor = param4;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public float[] getTextureDiffuseColors() {
        return this.textureDiffuseColors;
    }

    public MaterialColor getMaterialColor() {
        return this.color;
    }

    public int getFireworkColor() {
        return this.fireworkColor;
    }

    @OnlyIn(Dist.CLIENT)
    public int getTextColor() {
        return this.textColor;
    }

    public static DyeColor byId(int param0) {
        if (param0 < 0 || param0 >= BY_ID.length) {
            param0 = 0;
        }

        return BY_ID[param0];
    }

    public static DyeColor byName(String param0, DyeColor param1) {
        for(DyeColor var0 : values()) {
            if (var0.name.equals(param0)) {
                return var0;
            }
        }

        return param1;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static DyeColor byFireworkColor(int param0) {
        return BY_FIREWORK_COLOR.get(param0);
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
