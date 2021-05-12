package net.minecraft.util.profiling.registry;

public class Metric {
    private final String name;

    public Metric(String param0) {
        this.name = param0;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "Metric{name='" + this.name + "'}";
    }
}
