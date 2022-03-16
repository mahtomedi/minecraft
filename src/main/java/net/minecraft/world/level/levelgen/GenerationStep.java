package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.StringRepresentable;

public class GenerationStep {
    public static enum Carving implements StringRepresentable {
        AIR("air"),
        LIQUID("liquid");

        public static final Codec<GenerationStep.Carving> CODEC = StringRepresentable.fromEnum(GenerationStep.Carving::values, GenerationStep.Carving::byName);
        private static final Map<String, GenerationStep.Carving> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(GenerationStep.Carving::getName, param0 -> param0));
        private final String name;

        private Carving(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }

        @Nullable
        public static GenerationStep.Carving byName(String param0) {
            return BY_NAME.get(param0);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static enum Decoration implements StringRepresentable {
        RAW_GENERATION("raw_generation"),
        LAKES("lakes"),
        LOCAL_MODIFICATIONS("local_modifications"),
        UNDERGROUND_STRUCTURES("underground_structures"),
        SURFACE_STRUCTURES("surface_structures"),
        STRONGHOLDS("strongholds"),
        UNDERGROUND_ORES("underground_ores"),
        UNDERGROUND_DECORATION("underground_decoration"),
        FLUID_SPRINGS("fluid_springs"),
        VEGETAL_DECORATION("vegetal_decoration"),
        TOP_LAYER_MODIFICATION("top_layer_modification");

        public static final Codec<GenerationStep.Decoration> CODEC = StringRepresentable.fromEnum(
            GenerationStep.Decoration::values, GenerationStep.Decoration::byName
        );
        private static final Map<String, GenerationStep.Decoration> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(GenerationStep.Decoration::getName, param0 -> param0));
        private final String name;

        private Decoration(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }

        @Nullable
        public static GenerationStep.Decoration byName(String param0) {
            return BY_NAME.get(param0);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
