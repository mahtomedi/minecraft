package net.minecraft.client.sounds;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MusicManager {
    private static final int STARTING_DELAY = 100;
    private final RandomSource random = RandomSource.create();
    private final Minecraft minecraft;
    @Nullable
    private SoundInstance currentMusic;
    private int nextSongDelay = 100;

    public MusicManager(Minecraft param0) {
        this.minecraft = param0;
    }

    public void tick() {
        Music var0 = this.minecraft.getSituationalMusic();
        if (this.currentMusic != null) {
            if (!var0.getEvent().value().getLocation().equals(this.currentMusic.getLocation()) && var0.replaceCurrentMusic()) {
                this.minecraft.getSoundManager().stop(this.currentMusic);
                this.nextSongDelay = Mth.nextInt(this.random, 0, var0.getMinDelay() / 2);
            }

            if (!this.minecraft.getSoundManager().isActive(this.currentMusic)) {
                this.currentMusic = null;
                this.nextSongDelay = Math.min(this.nextSongDelay, Mth.nextInt(this.random, var0.getMinDelay(), var0.getMaxDelay()));
            }
        }

        this.nextSongDelay = Math.min(this.nextSongDelay, var0.getMaxDelay());
        if (this.currentMusic == null && this.nextSongDelay-- <= 0) {
            this.startPlaying(var0);
        }

    }

    public void startPlaying(Music param0) {
        this.currentMusic = SimpleSoundInstance.forMusic(param0.getEvent().value());
        if (this.currentMusic.getSound() != SoundManager.EMPTY_SOUND) {
            this.minecraft.getSoundManager().play(this.currentMusic);
        }

        this.nextSongDelay = Integer.MAX_VALUE;
    }

    public void stopPlaying(Music param0) {
        if (this.isPlayingMusic(param0)) {
            this.stopPlaying();
        }

    }

    public void stopPlaying() {
        if (this.currentMusic != null) {
            this.minecraft.getSoundManager().stop(this.currentMusic);
            this.currentMusic = null;
        }

        this.nextSongDelay += 100;
    }

    public boolean isPlayingMusic(Music param0) {
        return this.currentMusic == null ? false : param0.getEvent().value().getLocation().equals(this.currentMusic.getLocation());
    }
}
