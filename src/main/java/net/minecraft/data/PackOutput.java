package net.minecraft.data;

import java.nio.file.Path;
import net.minecraft.resources.ResourceLocation;

public class PackOutput {
    private final Path outputFolder;

    public PackOutput(Path param0) {
        this.outputFolder = param0;
    }

    public Path getOutputFolder() {
        return this.outputFolder;
    }

    public Path getOutputFolder(PackOutput.Target param0) {
        return this.getOutputFolder().resolve(param0.directory);
    }

    public PackOutput.PathProvider createPathProvider(PackOutput.Target param0, String param1) {
        return new PackOutput.PathProvider(this, param0, param1);
    }

    public static class PathProvider {
        private final Path root;
        private final String kind;

        PathProvider(PackOutput param0, PackOutput.Target param1, String param2) {
            this.root = param0.getOutputFolder(param1);
            this.kind = param2;
        }

        public Path file(ResourceLocation param0, String param1) {
            return this.root.resolve(param0.getNamespace()).resolve(this.kind).resolve(param0.getPath() + "." + param1);
        }

        public Path json(ResourceLocation param0) {
            return this.root.resolve(param0.getNamespace()).resolve(this.kind).resolve(param0.getPath() + ".json");
        }
    }

    public static enum Target {
        DATA_PACK("data"),
        RESOURCE_PACK("assets"),
        REPORTS("reports");

        final String directory;

        private Target(String param0) {
            this.directory = param0;
        }
    }
}
