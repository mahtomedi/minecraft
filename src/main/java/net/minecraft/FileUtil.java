package net.minecraft;

import com.mojang.serialization.DataResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;

public class FileUtil {
    private static final Pattern COPY_COUNTER_PATTERN = Pattern.compile("(<name>.*) \\((<count>\\d*)\\)", 66);
    private static final int MAX_FILE_NAME = 255;
    private static final Pattern RESERVED_WINDOWS_FILENAMES = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?", 2);
    private static final Pattern STRICT_PATH_SEGMENT_CHECK = Pattern.compile("[._a-z0-9]+");

    public static String findAvailableName(Path param0, String param1, String param2) throws IOException {
        for(char var0 : SharedConstants.ILLEGAL_FILE_CHARACTERS) {
            param1 = param1.replace(var0, '_');
        }

        param1 = param1.replaceAll("[./\"]", "_");
        if (RESERVED_WINDOWS_FILENAMES.matcher(param1).matches()) {
            param1 = "_" + param1 + "_";
        }

        Matcher var1 = COPY_COUNTER_PATTERN.matcher(param1);
        int var2 = 0;
        if (var1.matches()) {
            param1 = var1.group("name");
            var2 = Integer.parseInt(var1.group("count"));
        }

        if (param1.length() > 255 - param2.length()) {
            param1 = param1.substring(0, 255 - param2.length());
        }

        while(true) {
            String var3 = param1;
            if (var2 != 0) {
                String var4 = " (" + var2 + ")";
                int var5 = 255 - var4.length();
                if (param1.length() > var5) {
                    var3 = param1.substring(0, var5);
                }

                var3 = var3 + var4;
            }

            var3 = var3 + param2;
            Path var6 = param0.resolve(var3);

            try {
                Path var7 = Files.createDirectory(var6);
                Files.deleteIfExists(var7);
                return param0.relativize(var7).toString();
            } catch (FileAlreadyExistsException var81) {
                ++var2;
            }
        }
    }

    public static boolean isPathNormalized(Path param0) {
        Path var0 = param0.normalize();
        return var0.equals(param0);
    }

    public static boolean isPathPortable(Path param0) {
        for(Path var0 : param0) {
            if (RESERVED_WINDOWS_FILENAMES.matcher(var0.toString()).matches()) {
                return false;
            }
        }

        return true;
    }

    public static Path createPathToResource(Path param0, String param1, String param2) {
        String var0 = param1 + param2;
        Path var1 = Paths.get(var0);
        if (var1.endsWith(param2)) {
            throw new InvalidPathException(var0, "empty resource name");
        } else {
            return param0.resolve(var1);
        }
    }

    public static String getFullResourcePath(String param0) {
        return FilenameUtils.getFullPath(param0).replace(File.separator, "/");
    }

    public static String normalizeResourcePath(String param0) {
        return FilenameUtils.normalize(param0).replace(File.separator, "/");
    }

    public static DataResult<List<String>> decomposePath(String param0) {
        int var0 = param0.indexOf(47);
        if (var0 == -1) {
            return switch(param0) {
                case "", ".", ".." -> DataResult.error("Invalid path '" + param0 + "'");
                default -> DataResult.success(List.of(param0));
            };
        } else {
            List<String> var1 = new ArrayList<>();
            int var2 = 0;
            boolean var3 = false;

            while(true) {
                String var4 = param0.substring(var2, var0);
                switch(var4) {
                    case "":
                    case ".":
                    case "..":
                        return DataResult.error("Invalid segment '" + var4 + "' in path '" + param0 + "'");
                }
            }
        }
    }

    public static Path resolvePath(Path param0, List<String> param1) {
        int var0 = param1.size();

        return switch(var0) {
            case 0 -> param0;
            case 1 -> param0.resolve(param1.get(0));
            default -> {
                String[] var1 = new String[var0 - 1];

                for(int var2 = 1; var2 < var0; ++var2) {
                    var1[var2 - 1] = param1.get(var2);
                }

                yield param0.resolve(param0.getFileSystem().getPath(param1.get(0), var1));
            }
        };
    }

    public static boolean isValidStrictPathSegment(String param0) {
        return STRICT_PATH_SEGMENT_CHECK.matcher(param0).matches();
    }

    public static void validatePath(String... param0) {
        if (param0.length == 0) {
            throw new IllegalArgumentException("Path must have at least one element");
        } else {
            for(String var0 : param0) {
                if (var0.equals("..") || var0.equals(".") || !isValidStrictPathSegment(var0)) {
                    throw new IllegalArgumentException("Illegal segment " + var0 + " in path " + Arrays.toString((Object[])param0));
                }
            }

        }
    }
}
