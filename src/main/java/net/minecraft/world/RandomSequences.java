package net.minecraft.world;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class RandomSequences extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final long worldSeed;
    private int salt;
    private boolean includeWorldSeed = true;
    private boolean includeSequenceId = true;
    private final Map<ResourceLocation, RandomSequence> sequences = new Object2ObjectOpenHashMap<>();

    public static SavedData.Factory<RandomSequences> factory(long param0) {
        return new SavedData.Factory<>(() -> new RandomSequences(param0), param1 -> load(param0, param1), DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
    }

    public RandomSequences(long param0) {
        this.worldSeed = param0;
    }

    public RandomSource get(ResourceLocation param0) {
        RandomSource var0 = this.sequences.computeIfAbsent(param0, this::createSequence).random();
        return new RandomSequences.DirtyMarkingRandomSource(var0);
    }

    private RandomSequence createSequence(ResourceLocation param0x) {
        return this.createSequence(param0x, this.salt, this.includeWorldSeed, this.includeSequenceId);
    }

    private RandomSequence createSequence(ResourceLocation param0, int param1, boolean param2, boolean param3) {
        long var0 = (param2 ? this.worldSeed : 0L) ^ (long)param1;
        return new RandomSequence(var0, param3 ? Optional.of(param0) : Optional.empty());
    }

    public void forAllSequences(BiConsumer<ResourceLocation, RandomSequence> param0) {
        this.sequences.forEach(param0);
    }

    public void setSeedDefaults(int param0, boolean param1, boolean param2) {
        this.salt = param0;
        this.includeWorldSeed = param1;
        this.includeSequenceId = param2;
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        param0.putInt("salt", this.salt);
        param0.putBoolean("include_world_seed", this.includeWorldSeed);
        param0.putBoolean("include_sequence_id", this.includeSequenceId);
        CompoundTag var0 = new CompoundTag();
        this.sequences
            .forEach((param1, param2) -> var0.put(param1.toString(), RandomSequence.CODEC.encodeStart(NbtOps.INSTANCE, param2).result().orElseThrow()));
        param0.put("sequences", var0);
        return param0;
    }

    private static boolean getBooleanWithDefault(CompoundTag param0, String param1, boolean param2) {
        return param0.contains(param1, 1) ? param0.getBoolean(param1) : param2;
    }

    public static RandomSequences load(long param0, CompoundTag param1) {
        RandomSequences var0 = new RandomSequences(param0);
        var0.setSeedDefaults(
            param1.getInt("salt"), getBooleanWithDefault(param1, "include_world_seed", true), getBooleanWithDefault(param1, "include_sequence_id", true)
        );
        CompoundTag var1 = param1.getCompound("sequences");

        for(String var3 : var1.getAllKeys()) {
            try {
                RandomSequence var4 = RandomSequence.CODEC.decode(NbtOps.INSTANCE, var1.get(var3)).result().get().getFirst();
                var0.sequences.put(new ResourceLocation(var3), var4);
            } catch (Exception var9) {
                LOGGER.error("Failed to load random sequence {}", var3, var9);
            }
        }

        return var0;
    }

    public int clear() {
        int var0 = this.sequences.size();
        this.sequences.clear();
        return var0;
    }

    public void reset(ResourceLocation param0) {
        this.sequences.put(param0, this.createSequence(param0));
    }

    public void reset(ResourceLocation param0, int param1, boolean param2, boolean param3) {
        this.sequences.put(param0, this.createSequence(param0, param1, param2, param3));
    }

    class DirtyMarkingRandomSource implements RandomSource {
        private final RandomSource random;

        DirtyMarkingRandomSource(RandomSource param0) {
            this.random = param0;
        }

        @Override
        public RandomSource fork() {
            RandomSequences.this.setDirty();
            return this.random.fork();
        }

        @Override
        public PositionalRandomFactory forkPositional() {
            RandomSequences.this.setDirty();
            return this.random.forkPositional();
        }

        @Override
        public void setSeed(long param0) {
            RandomSequences.this.setDirty();
            this.random.setSeed(param0);
        }

        @Override
        public int nextInt() {
            RandomSequences.this.setDirty();
            return this.random.nextInt();
        }

        @Override
        public int nextInt(int param0) {
            RandomSequences.this.setDirty();
            return this.random.nextInt(param0);
        }

        @Override
        public long nextLong() {
            RandomSequences.this.setDirty();
            return this.random.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            RandomSequences.this.setDirty();
            return this.random.nextBoolean();
        }

        @Override
        public float nextFloat() {
            RandomSequences.this.setDirty();
            return this.random.nextFloat();
        }

        @Override
        public double nextDouble() {
            RandomSequences.this.setDirty();
            return this.random.nextDouble();
        }

        @Override
        public double nextGaussian() {
            RandomSequences.this.setDirty();
            return this.random.nextGaussian();
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else {
                return param0 instanceof RandomSequences.DirtyMarkingRandomSource var0 ? this.random.equals(var0.random) : false;
            }
        }
    }
}
