package net.minecraft.world.level.saveddata.maps;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class MapDecoration {
    private final MapDecoration.Type type;
    private final byte x;
    private final byte y;
    private final byte rot;
    @Nullable
    private final Component name;

    public MapDecoration(MapDecoration.Type param0, byte param1, byte param2, byte param3, @Nullable Component param4) {
        this.type = param0;
        this.x = param1;
        this.y = param2;
        this.rot = param3;
        this.name = param4;
    }

    public byte getImage() {
        return this.type.getIcon();
    }

    public MapDecoration.Type getType() {
        return this.type;
    }

    public byte getX() {
        return this.x;
    }

    public byte getY() {
        return this.y;
    }

    public byte getRot() {
        return this.rot;
    }

    public boolean renderOnFrame() {
        return this.type.isRenderedOnFrame();
    }

    @Nullable
    public Component getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof MapDecoration)) {
            return false;
        } else {
            MapDecoration var0 = (MapDecoration)param0;
            return this.type == var0.type && this.rot == var0.rot && this.x == var0.x && this.y == var0.y && Objects.equals(this.name, var0.name);
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.type.getIcon();
        var0 = 31 * var0 + this.x;
        var0 = 31 * var0 + this.y;
        var0 = 31 * var0 + this.rot;
        return 31 * var0 + Objects.hashCode(this.name);
    }

    public static enum Type {
        PLAYER(false, true),
        FRAME(true, true),
        RED_MARKER(false, true),
        BLUE_MARKER(false, true),
        TARGET_X(true, false),
        TARGET_POINT(true, false),
        PLAYER_OFF_MAP(false, true),
        PLAYER_OFF_LIMITS(false, true),
        MANSION(true, 5393476, false),
        MONUMENT(true, 3830373, false),
        BANNER_WHITE(true, true),
        BANNER_ORANGE(true, true),
        BANNER_MAGENTA(true, true),
        BANNER_LIGHT_BLUE(true, true),
        BANNER_YELLOW(true, true),
        BANNER_LIME(true, true),
        BANNER_PINK(true, true),
        BANNER_GRAY(true, true),
        BANNER_LIGHT_GRAY(true, true),
        BANNER_CYAN(true, true),
        BANNER_PURPLE(true, true),
        BANNER_BLUE(true, true),
        BANNER_BROWN(true, true),
        BANNER_GREEN(true, true),
        BANNER_RED(true, true),
        BANNER_BLACK(true, true),
        RED_X(true, false);

        private final byte icon;
        private final boolean renderedOnFrame;
        private final int mapColor;
        private final boolean trackCount;

        private Type(boolean param0, boolean param1) {
            this(param0, -1, param1);
        }

        private Type(boolean param0, int param1, boolean param2) {
            this.trackCount = param2;
            this.icon = (byte)this.ordinal();
            this.renderedOnFrame = param0;
            this.mapColor = param1;
        }

        public byte getIcon() {
            return this.icon;
        }

        public boolean isRenderedOnFrame() {
            return this.renderedOnFrame;
        }

        public boolean hasMapColor() {
            return this.mapColor >= 0;
        }

        public int getMapColor() {
            return this.mapColor;
        }

        public static MapDecoration.Type byIcon(byte param0) {
            return values()[Mth.clamp(param0, 0, values().length - 1)];
        }

        public boolean shouldTrackCount() {
            return this.trackCount;
        }
    }
}
