package net.minecraft.client.resources.sounds;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomeAmbientSoundsHandler implements AmbientSoundHandler {
    private static final int LOOP_SOUND_CROSS_FADE_TIME = 40;
    private static final float SKY_MOOD_RECOVERY_RATE = 0.001F;
    private final LocalPlayer player;
    private final SoundManager soundManager;
    private final BiomeManager biomeManager;
    private final RandomSource random;
    private final Object2ObjectArrayMap<Biome, BiomeAmbientSoundsHandler.LoopSoundInstance> loopSounds = new Object2ObjectArrayMap<>();
    private Optional<AmbientMoodSettings> moodSettings = Optional.empty();
    private Optional<AmbientAdditionsSettings> additionsSettings = Optional.empty();
    private float moodiness;
    @Nullable
    private Biome previousBiome;

    public BiomeAmbientSoundsHandler(LocalPlayer param0, SoundManager param1, BiomeManager param2) {
        this.random = param0.level.getRandom();
        this.player = param0;
        this.soundManager = param1;
        this.biomeManager = param2;
    }

    public float getMoodiness() {
        return this.moodiness;
    }

    @Override
    public void tick() {
        this.loopSounds.values().removeIf(AbstractTickableSoundInstance::isStopped);
        Biome var0 = this.biomeManager.getNoiseBiomeAtPosition(this.player.getX(), this.player.getY(), this.player.getZ()).value();
        if (var0 != this.previousBiome) {
            this.previousBiome = var0;
            this.moodSettings = var0.getAmbientMood();
            this.additionsSettings = var0.getAmbientAdditions();
            this.loopSounds.values().forEach(BiomeAmbientSoundsHandler.LoopSoundInstance::fadeOut);
            var0.getAmbientLoop().ifPresent(param1 -> this.loopSounds.compute(var0, (param1x, param2) -> {
                    if (param2 == null) {
                        param2 = new BiomeAmbientSoundsHandler.LoopSoundInstance(param1);
                        this.soundManager.play(param2);
                    }

                    param2.fadeIn();
                    return param2;
                }));
        }

        this.additionsSettings.ifPresent(param0 -> {
            if (this.random.nextDouble() < param0.getTickChance()) {
                this.soundManager.play(SimpleSoundInstance.forAmbientAddition(param0.getSoundEvent()));
            }

        });
        this.moodSettings
            .ifPresent(
                param0 -> {
                    Level var0x = this.player.level;
                    int var1x = param0.getBlockSearchExtent() * 2 + 1;
                    BlockPos var2 = new BlockPos(
                        this.player.getX() + (double)this.random.nextInt(var1x) - (double)param0.getBlockSearchExtent(),
                        this.player.getEyeY() + (double)this.random.nextInt(var1x) - (double)param0.getBlockSearchExtent(),
                        this.player.getZ() + (double)this.random.nextInt(var1x) - (double)param0.getBlockSearchExtent()
                    );
                    int var3 = var0x.getBrightness(LightLayer.SKY, var2);
                    if (var3 > 0) {
                        this.moodiness -= (float)var3 / (float)var0x.getMaxLightLevel() * 0.001F;
                    } else {
                        this.moodiness -= (float)(var0x.getBrightness(LightLayer.BLOCK, var2) - 1) / (float)param0.getTickDelay();
                    }
        
                    if (this.moodiness >= 1.0F) {
                        double var4 = (double)var2.getX() + 0.5;
                        double var5 = (double)var2.getY() + 0.5;
                        double var6 = (double)var2.getZ() + 0.5;
                        double var7 = var4 - this.player.getX();
                        double var8 = var5 - this.player.getEyeY();
                        double var9 = var6 - this.player.getZ();
                        double var10 = Math.sqrt(var7 * var7 + var8 * var8 + var9 * var9);
                        double var11 = var10 + param0.getSoundPositionOffset();
                        SimpleSoundInstance var12 = SimpleSoundInstance.forAmbientMood(
                            param0.getSoundEvent(),
                            this.random,
                            this.player.getX() + var7 / var10 * var11,
                            this.player.getEyeY() + var8 / var10 * var11,
                            this.player.getZ() + var9 / var10 * var11
                        );
                        this.soundManager.play(var12);
                        this.moodiness = 0.0F;
                    } else {
                        this.moodiness = Math.max(this.moodiness, 0.0F);
                    }
        
                }
            );
    }

    @OnlyIn(Dist.CLIENT)
    public static class LoopSoundInstance extends AbstractTickableSoundInstance {
        private int fadeDirection;
        private int fade;

        public LoopSoundInstance(SoundEvent param0) {
            super(param0, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
            this.looping = true;
            this.delay = 0;
            this.volume = 1.0F;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (this.fade < 0) {
                this.stop();
            }

            this.fade += this.fadeDirection;
            this.volume = Mth.clamp((float)this.fade / 40.0F, 0.0F, 1.0F);
        }

        public void fadeOut() {
            this.fade = Math.min(this.fade, 40);
            this.fadeDirection = -1;
        }

        public void fadeIn() {
            this.fade = Math.max(0, this.fade);
            this.fadeDirection = 1;
        }
    }
}
