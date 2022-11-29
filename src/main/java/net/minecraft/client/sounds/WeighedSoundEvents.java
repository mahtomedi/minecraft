package net.minecraft.client.sounds;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WeighedSoundEvents implements Weighted<Sound> {
    private final List<Weighted<Sound>> list = Lists.newArrayList();
    @Nullable
    private final Component subtitle;

    public WeighedSoundEvents(ResourceLocation param0, @Nullable String param1) {
        this.subtitle = param1 == null ? null : Component.translatable(param1);
    }

    @Override
    public int getWeight() {
        int var0 = 0;

        for(Weighted<Sound> var1 : this.list) {
            var0 += var1.getWeight();
        }

        return var0;
    }

    public Sound getSound(RandomSource param0) {
        int var0 = this.getWeight();
        if (!this.list.isEmpty() && var0 != 0) {
            int var1 = param0.nextInt(var0);

            for(Weighted<Sound> var2 : this.list) {
                var1 -= var2.getWeight();
                if (var1 < 0) {
                    return var2.getSound(param0);
                }
            }

            return SoundManager.EMPTY_SOUND;
        } else {
            return SoundManager.EMPTY_SOUND;
        }
    }

    public void addSound(Weighted<Sound> param0) {
        this.list.add(param0);
    }

    @Nullable
    public Component getSubtitle() {
        return this.subtitle;
    }

    @Override
    public void preloadIfRequired(SoundEngine param0) {
        for(Weighted<Sound> var0 : this.list) {
            var0.preloadIfRequired(param0);
        }

    }
}
