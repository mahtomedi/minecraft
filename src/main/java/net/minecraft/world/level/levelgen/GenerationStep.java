package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerationStep {
    public static enum Carving {
        AIR("air"),
        LIQUID("liquid");

        private static final Map<String, GenerationStep.Carving> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(GenerationStep.Carving::getName, param0 -> param0));
        private final String name;

        private Carving(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }
    }

    public static enum Decoration {
        RAW_GENERATION("raw_generation"),
        LOCAL_MODIFICATIONS("local_modifications"),
        UNDERGROUND_STRUCTURES("underground_structures"),
        SURFACE_STRUCTURES("surface_structures"),
        UNDERGROUND_ORES("underground_ores"),
        UNDERGROUND_DECORATION("underground_decoration"),
        VEGETAL_DECORATION("vegetal_decoration"),
        TOP_LAYER_MODIFICATION("top_layer_modification");

        private static final Map<String, GenerationStep.Decoration> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(GenerationStep.Decoration::getName, param0 -> param0));
        private final String name;

        private Decoration(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }
    }
}
