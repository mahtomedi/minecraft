package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public class DataPackConfig {
    public static final DataPackConfig DEFAULT = new DataPackConfig(ImmutableList.of("vanilla"), ImmutableList.of());
    public static final Codec<DataPackConfig> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.STRING.listOf().fieldOf("Enabled").forGetter(param0x -> param0x.enabled),
                    Codec.STRING.listOf().fieldOf("Disabled").forGetter(param0x -> param0x.disabled)
                )
                .apply(param0, DataPackConfig::new)
    );
    private final List<String> enabled;
    private final List<String> disabled;

    public DataPackConfig(List<String> param0, List<String> param1) {
        this.enabled = ImmutableList.copyOf(param0);
        this.disabled = ImmutableList.copyOf(param1);
    }

    public List<String> getEnabled() {
        return this.enabled;
    }

    public List<String> getDisabled() {
        return this.disabled;
    }
}
