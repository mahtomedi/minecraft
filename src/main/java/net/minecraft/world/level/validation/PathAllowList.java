package net.minecraft.world.level.validation;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

public class PathAllowList implements PathMatcher {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String COMMENT_PREFIX = "#";
    private final List<PathAllowList.ConfigEntry> entries;
    private final Map<String, PathMatcher> compiledPaths = new ConcurrentHashMap<>();

    public PathAllowList(List<PathAllowList.ConfigEntry> param0) {
        this.entries = param0;
    }

    public PathMatcher getForFileSystem(FileSystem param0) {
        return this.compiledPaths.computeIfAbsent(param0.provider().getScheme(), param1 -> {
            List<PathMatcher> var0;
            try {
                var0 = this.entries.stream().map(param1x -> param1x.compile(param0)).toList();
            } catch (Exception var5) {
                LOGGER.error("Failed to compile file pattern list", (Throwable)var5);
                return param0x -> false;
            }

            return switch(var0.size()) {
                case 0 -> param0x -> false;
                case 1 -> (PathMatcher)var0.get(0);
                default -> param1x -> {
                for(PathMatcher var0x : var0) {
                    if (var0x.matches(param1x)) {
                        return true;
                    }
                }

                return false;
            };
            };
        });
    }

    @Override
    public boolean matches(Path param0) {
        return this.getForFileSystem(param0.getFileSystem()).matches(param0);
    }

    public static PathAllowList readPlain(BufferedReader param0) {
        return new PathAllowList(param0.lines().flatMap(param0x -> PathAllowList.ConfigEntry.parse(param0x).stream()).toList());
    }

    public static record ConfigEntry(PathAllowList.EntryType type, String pattern) {
        public PathMatcher compile(FileSystem param0) {
            return this.type().compile(param0, this.pattern);
        }

        static Optional<PathAllowList.ConfigEntry> parse(String param0) {
            if (param0.isBlank() || param0.startsWith("#")) {
                return Optional.empty();
            } else if (!param0.startsWith("[")) {
                return Optional.of(new PathAllowList.ConfigEntry(PathAllowList.EntryType.PREFIX, param0));
            } else {
                int var0 = param0.indexOf(93, 1);
                if (var0 == -1) {
                    throw new IllegalArgumentException("Unterminated type in line '" + param0 + "'");
                } else {
                    String var1 = param0.substring(1, var0);
                    String var2 = param0.substring(var0 + 1);

                    return switch(var1) {
                        case "glob", "regex" -> Optional.of(new PathAllowList.ConfigEntry(PathAllowList.EntryType.FILESYSTEM, var1 + ":" + var2));
                        case "prefix" -> Optional.of(new PathAllowList.ConfigEntry(PathAllowList.EntryType.PREFIX, var2));
                        default -> throw new IllegalArgumentException("Unsupported definition type in line '" + param0 + "'");
                    };
                }
            }
        }

        static PathAllowList.ConfigEntry glob(String param0) {
            return new PathAllowList.ConfigEntry(PathAllowList.EntryType.FILESYSTEM, "glob:" + param0);
        }

        static PathAllowList.ConfigEntry regex(String param0) {
            return new PathAllowList.ConfigEntry(PathAllowList.EntryType.FILESYSTEM, "regex:" + param0);
        }

        static PathAllowList.ConfigEntry prefix(String param0) {
            return new PathAllowList.ConfigEntry(PathAllowList.EntryType.PREFIX, param0);
        }
    }

    @FunctionalInterface
    public interface EntryType {
        PathAllowList.EntryType FILESYSTEM = FileSystem::getPathMatcher;
        PathAllowList.EntryType PREFIX = (param0, param1) -> param1x -> param1x.toString().startsWith(param1);

        PathMatcher compile(FileSystem var1, String var2);
    }
}
