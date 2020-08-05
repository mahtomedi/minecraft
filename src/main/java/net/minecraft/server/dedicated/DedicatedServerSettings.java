package net.minecraft.server.dedicated;

import java.nio.file.Path;
import java.util.function.UnaryOperator;
import net.minecraft.core.RegistryAccess;

public class DedicatedServerSettings {
    private final Path source;
    private DedicatedServerProperties properties;

    public DedicatedServerSettings(RegistryAccess param0, Path param1) {
        this.source = param1;
        this.properties = DedicatedServerProperties.fromFile(param0, param1);
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
