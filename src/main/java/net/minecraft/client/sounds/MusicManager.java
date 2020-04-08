package net.minecraft.client.sounds;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MusicManager {
    private final Random random = new Random();
    private final Minecraft minecraft;
    @Nullable
    private SoundInstance currentMusic;
    private int nextSongDelay = 100;

    public MusicManager(Minecraft param0) {
        this.minecraft = param0;
    }

    public void tick() {
        MusicManager.Music var0 = this.minecraft.getSituationalMusic();
        if (this.currentMusic != null) {
            if (!var0.getEvent().getLocation().equals(this.currentMusic.getLocation()) && var0.overridesCurrent) {
                this.minecraft.getSoundManager().stop(this.currentMusic);
                this.nextSongDelay = Mth.nextInt(this.random, 0, var0.getMinDelay() / 2);
            }

            if (!this.minecraft.getSoundManager().isActive(this.currentMusic)) {
                this.currentMusic = null;
                this.nextSongDelay = Math.min(Mth.nextInt(this.random, var0.getMinDelay(), var0.getMaxDelay()), this.nextSongDelay);
            }
        }

        this.nextSongDelay = Math.min(this.nextSongDelay, var0.getMaxDelay());
        if (this.currentMusic == null && this.nextSongDelay-- <= 0) {
            this.startPlaying(var0);
        }

    }

    public void startPlaying(MusicManager.Music param0) {
        this.currentMusic = SimpleSoundInstance.forMusic(param0.getEvent());
        this.minecraft.getSoundManager().play(this.currentMusic);
        this.nextSongDelay = Integer.MAX_VALUE;
    }

    public void stopPlaying() {
        if (this.currentMusic != null) {
            this.minecraft.getSoundManager().stop(this.currentMusic);
            this.currentMusic = null;
            this.nextSongDelay = 0;
        }

    }

    public boolean isPlayingMusic(MusicManager.Music param0) {
        return this.currentMusic == null ? false : param0.getEvent().getLocation().equals(this.currentMusic.getLocation());
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Music {
        MENU(SoundEvents.MUSIC_MENU, 20, 600),
        GAME(SoundEvents.MUSIC_GAME, 12000, 24000),
        CREATIVE(SoundEvents.MUSIC_CREATIVE, 1200, 3600),
        CREDITS(SoundEvents.MUSIC_CREDITS, 0, 0),
        BASALT_DELTAS(SoundEvents.MUSIC_BIOME_BASALT_DELTAS, 1200, 3600, false),
        NETHER_WASTES(SoundEvents.MUSIC_BIOME_NETHER_WASTES, 1200, 3600, false),
        SOUL_SAND_VALLEY(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY, 1200, 3600, false),
        CRIMSON_FOREST(SoundEvents.MUSIC_BIOME_CRIMSON_FOREST, 1200, 3600, false),
        WARPED_FOREST(SoundEvents.MUSIC_BIOME_WARPED_FOREST, 1200, 3600, false),
        END_BOSS(SoundEvents.MUSIC_DRAGON, 0, 0),
        END(SoundEvents.MUSIC_END, 6000, 24000),
        UNDER_WATER(SoundEvents.MUSIC_UNDER_WATER, 12000, 24000);

        private final SoundEvent event;
        private final int minDelay;
        private final int maxDelay;
        private final boolean overridesCurrent;

        private Music(SoundEvent param0, int param1, int param2) {
            this(param0, param1, param2, true);
        }

        private Music(SoundEvent param0, int param1, int param2, boolean param3) {
            this.event = param0;
            this.minDelay = param1;
            this.maxDelay = param2;
            this.overridesCurrent = param3;
        }

        public SoundEvent getEvent() {
            return this.event;
        }

        public int getMinDelay() {
            return this.minDelay;
        }

        public int getMaxDelay() {
            return this.maxDelay;
        }
    }
}
