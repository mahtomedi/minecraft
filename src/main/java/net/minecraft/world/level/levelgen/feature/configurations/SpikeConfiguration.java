package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;

public class SpikeConfiguration implements FeatureConfiguration {
    private final boolean crystalInvulnerable;
    private final List<SpikeFeature.EndSpike> spikes;
    @Nullable
    private final BlockPos crystalBeamTarget;

    public SpikeConfiguration(boolean param0, List<SpikeFeature.EndSpike> param1, @Nullable BlockPos param2) {
        this.crystalInvulnerable = param0;
        this.spikes = param1;
        this.crystalBeamTarget = param2;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("crystalInvulnerable"),
                    param0.createBoolean(this.crystalInvulnerable),
                    param0.createString("spikes"),
                    param0.createList(this.spikes.stream().map(param1 -> param1.serialize(param0).getValue())),
                    param0.createString("crystalBeamTarget"),
                    (T)(this.crystalBeamTarget == null
                        ? param0.createList(Stream.empty())
                        : param0.createList(
                            IntStream.of(this.crystalBeamTarget.getX(), this.crystalBeamTarget.getY(), this.crystalBeamTarget.getZ())
                                .mapToObj(param0::createInt)
                        ))
                )
            )
        );
    }

    public static <T> SpikeConfiguration deserialize(Dynamic<T> param0) {
        List<SpikeFeature.EndSpike> var0 = param0.get("spikes").asList(SpikeFeature.EndSpike::deserialize);
        List<Integer> var1 = param0.get("crystalBeamTarget").asList(param0x -> param0x.asInt(0));
        BlockPos var2;
        if (var1.size() == 3) {
            var2 = new BlockPos(var1.get(0), var1.get(1), var1.get(2));
        } else {
            var2 = null;
        }

        return new SpikeConfiguration(param0.get("crystalInvulnerable").asBoolean(false), var0, var2);
    }

    public boolean isCrystalInvulnerable() {
        return this.crystalInvulnerable;
    }

    public List<SpikeFeature.EndSpike> getSpikes() {
        return this.spikes;
    }

    @Nullable
    public BlockPos getCrystalBeamTarget() {
        return this.crystalBeamTarget;
    }

    public static SpikeConfiguration random(Random param0) {
        return new SpikeConfiguration(false, ImmutableList.of(), null);
    }
}
