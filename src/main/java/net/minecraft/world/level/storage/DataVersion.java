package net.minecraft.world.level.storage;

public class DataVersion {
    private final int version;
    private final String series;
    public static String MAIN_SERIES = "main";

    public DataVersion(int param0) {
        this(param0, MAIN_SERIES);
    }

    public DataVersion(int param0, String param1) {
        this.version = param0;
        this.series = param1;
    }

    public boolean isSideSeries() {
        return !this.series.equals(MAIN_SERIES);
    }

    public String getSeries() {
        return this.series;
    }

    public int getVersion() {
        return this.version;
    }

    public boolean isCompatible(DataVersion param0) {
        return this.getSeries().equals(param0.getSeries());
    }
}
