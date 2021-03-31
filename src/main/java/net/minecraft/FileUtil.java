package net.minecraft;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;

public class FileUtil {
    private static final Pattern COPY_COUNTER_PATTERN = Pattern.compile("(<name>.*) \\((<count>\\d*)\\)", 66);
    private static final int MAX_FILE_NAME = 255;
    private static final Pattern RESERVED_WINDOWS_FILENAMES = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?", 2);

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
}
