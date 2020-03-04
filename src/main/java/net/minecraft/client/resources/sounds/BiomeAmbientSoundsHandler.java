package net.minecraft.client.resources.sounds;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomeAmbientSoundsHandler implements AmbientSoundHandler {
    private final LocalPlayer player;
    private final SoundManager soundManager;
    private final BiomeManager biomeManager;
    private final Random random;
    private Object2ObjectArrayMap<Biome, BiomeAmbientSoundsHandler.LoopSoundInstance> loopSounds;
    private Optional<SoundEvent> moodSound;
    private Optional<SoundEvent> additionsSound;
    private int ticksUntilNextMoodSound;
    private Biome previousBiome;

    public BiomeAmbientSoundsHandler(LocalPlayer param0, SoundManager param1, BiomeManager param2) {
        this.random = param0.level.getRandom();
        this.player = param0;
        this.soundManager = param1;
        this.biomeManager = param2;
        this.loopSounds = new Object2ObjectArrayMap<>();
        this.moodSound = Optional.empty();
        this.additionsSound = Optional.empty();
        this.ticksUntilNextMoodSound = calculateTicksUntilNextMoodSound(this.random);
    }

    @Override
    public void tick() {
        this.loopSounds.values().removeIf(AbstractTickableSoundInstance::isStopped);
        Biome var0 = this.biomeManager.getNoiseBiomeAtPosition(this.player.getX(), this.player.getY(), this.player.getZ());
        if (var0 != this.previousBiome) {
            this.previousBiome = var0;
            this.moodSound = var0.getAmbientMoodSoundEvent();
            this.additionsSound = var0.getAmbientAdditionsSoundEvent();
            this.loopSounds.values().forEach(BiomeAmbientSoundsHandler.LoopSoundInstance::fadeOut);
            var0.getAmbientLoopSoundEvent().ifPresent(param1 -> {
            });
        }

        this.additionsSound.ifPresent(param0 -> {
            if (this.random.nextDouble() < 0.0111F) {
                this.soundManager.play(SimpleSoundInstance.forAmbientAddition(param0));
            }

        });
        if (this.ticksUntilNextMoodSound > 0) {
            --this.ticksUntilNextMoodSound;
        } else {
            this.moodSound.ifPresent(param0 -> {
                BlockPos var0x = this.findMoodyBlock();
                if (var0x != null) {
                    this.soundManager.play(SimpleSoundInstance.forAmbientMood(param0, (float)var0x.getX(), (float)var0x.getY(), (float)var0x.getZ()));
                    this.ticksUntilNextMoodSound = calculateTicksUntilNextMoodSound(this.random);
                }

            });
        }

    }

    @Nullable
    private BlockPos findMoodyBlock() {
        BlockPos var0 = this.player.blockPosition();
        Level var1 = this.player.level;
        int var2 = 9;
        BlockPos var3 = var0.offset(this.random.nextInt(9) - 4, this.random.nextInt(9) - 4, this.random.nextInt(9) - 4);
        double var4 = var0.distSqr(var3);
        if (var4 >= 4.0 && var4 <= 256.0) {
            BlockState var5 = var1.getBlockState(var3);
            if (var5.isAir() && var1.getRawBrightness(var3, 0) <= this.random.nextInt(8) && var1.getBrightness(LightLayer.SKY, var3) <= 0) {
                return var3;
            }
        }

        return null;
    }

    private static int calculateTicksUntilNextMoodSound(Random param0) {
        return param0.nextInt(12000) + 6000;
    }

    @OnlyIn(Dist.CLIENT)
    public static class LoopSoundInstance extends AbstractTickableSoundInstance {
        private int fadeDirection;
        private int fade;

        public LoopSoundInstance(SoundEvent param0) {
            super(param0, SoundSource.AMBIENT);
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
