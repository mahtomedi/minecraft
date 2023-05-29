package net.minecraft.world.level.validation;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ContentValidationException extends Exception {
    private final Path directory;
    private final List<ForbiddenSymlinkInfo> entries;

    public ContentValidationException(Path param0, List<ForbiddenSymlinkInfo> param1) {
        this.directory = param0;
        this.entries = param1;
    }

    @Override
    public String getMessage() {
        return getMessage(this.directory, this.entries);
    }

    public static String getMessage(Path param0, List<ForbiddenSymlinkInfo> param1) {
        return "Failed to validate '"
            + param0
            + "'. Found forbidden symlinks: "
            + (String)param1.stream().map(param0x -> param0x.link() + "->" + param0x.target()).collect(Collectors.joining(", "));
    }
}
