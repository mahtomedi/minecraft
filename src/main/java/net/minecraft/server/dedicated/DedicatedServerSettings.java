package net.minecraft.server.dedicated;

import java.nio.file.Path;
import java.util.function.UnaryOperator;

public class DedicatedServerSettings {
    private final Path source;
    private DedicatedServerProperties properties;

    public DedicatedServerSettings(Path param0) {
        this.source = param0;
        this.properties = DedicatedServerProperties.fromFile(param0);
    }

    public DedicatedServerProperties getProperties() {
        return this.properties;
    }

    public void forceSave() {
        this.properties.store(this.source);
    }

    public DedicatedServerSettings update(UnaryOperator<DedicatedServerProperties> param0) {
        (this.properties = param0.apply(this.properties)).store(this.source);
        return this;
    }
}
