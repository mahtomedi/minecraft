package net.minecraft.world;

import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public abstract class BossEvent {
    private final UUID id;
    protected Component name;
    protected float progress;
    protected BossEvent.BossBarColor color;
    protected BossEvent.BossBarOverlay overlay;
    protected boolean darkenScreen;
    protected boolean playBossMusic;
    protected boolean createWorldFog;

    public BossEvent(UUID param0, Component param1, BossEvent.BossBarColor param2, BossEvent.BossBarOverlay param3) {
        this.id = param0;
        this.name = param1;
        this.color = param2;
        this.overlay = param3;
        this.progress = 1.0F;
    }

    public UUID getId() {
        return this.id;
    }

    public Component getName() {
        return this.name;
    }

    public void setName(Component param0) {
        this.name = param0;
    }

    public float getProgress() {
        return this.progress;
    }

    public void setProgress(float param0) {
        this.progress = param0;
    }

    public BossEvent.BossBarColor getColor() {
        return this.color;
    }

    public void setColor(BossEvent.BossBarColor param0) {
        this.color = param0;
    }

    public BossEvent.BossBarOverlay getOverlay() {
        return this.overlay;
    }

    public void setOverlay(BossEvent.BossBarOverlay param0) {
        this.overlay = param0;
    }

    public boolean shouldDarkenScreen() {
        return this.darkenScreen;
    }

    public BossEvent setDarkenScreen(boolean param0) {
        this.darkenScreen = param0;
        return this;
    }

    public boolean shouldPlayBossMusic() {
        return this.playBossMusic;
    }

    public BossEvent setPlayBossMusic(boolean param0) {
        this.playBossMusic = param0;
        return this;
    }

    public BossEvent setCreateWorldFog(boolean param0) {
        this.createWorldFog = param0;
        return this;
    }

    public boolean shouldCreateWorldFog() {
        return this.createWorldFog;
    }

    public static enum BossBarColor {
        PINK("pink", ChatFormatting.RED),
        BLUE("blue", ChatFormatting.BLUE),
        RED("red", ChatFormatting.DARK_RED),
        GREEN("green", ChatFormatting.GREEN),
        YELLOW("yellow", ChatFormatting.YELLOW),
        PURPLE("purple", ChatFormatting.DARK_BLUE),
        WHITE("white", ChatFormatting.WHITE);

        private final String name;
        private final ChatFormatting formatting;

        private BossBarColor(String param0, ChatFormatting param1) {
            this.name = param0;
            this.formatting = param1;
        }

        public ChatFormatting getFormatting() {
            return this.formatting;
        }

        public String getName() {
            return this.name;
        }

        public static BossEvent.BossBarColor byName(String param0) {
            for(BossEvent.BossBarColor var0 : values()) {
                if (var0.name.equals(param0)) {
                    return var0;
                }
            }

            return WHITE;
        }
    }

    public static enum BossBarOverlay {
        PROGRESS("progress"),
        NOTCHED_6("notched_6"),
        NOTCHED_10("notched_10"),
        NOTCHED_12("notched_12"),
        NOTCHED_20("notched_20");

        private final String name;

        private BossBarOverlay(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }

        public static BossEvent.BossBarOverlay byName(String param0) {
            for(BossEvent.BossBarOverlay var0 : values()) {
                if (var0.name.equals(param0)) {
                    return var0;
                }
            }

            return PROGRESS;
        }
    }
}
