package net.minecraft.client.resources.sounds;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundEventRegistration {
    private final List<Sound> sounds;
    private final boolean replace;
    private final String subtitle;

    public SoundEventRegistration(List<Sound> param0, boolean param1, String param2) {
        this.sounds = param0;
        this.replace = param1;
        this.subtitle = param2;
    }

    public List<Sound> getSounds() {
        return this.sounds;
    }

    public boolean isReplace() {
        return this.replace;
    }

    @Nullable
    public String getSubtitle() {
        return this.subtitle;
    }
}
