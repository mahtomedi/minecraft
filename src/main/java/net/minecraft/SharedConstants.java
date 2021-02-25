package net.minecraft;

import com.mojang.bridge.game.GameVersion;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import java.time.Duration;
import net.minecraft.commands.BrigadierExceptions;

public class SharedConstants {
    public static final Level NETTY_LEAK_DETECTION = Level.DISABLED;
    public static final long MAXIMUM_TICK_TIME_NANOS = Duration.ofMillis(300L).toNanos();
    public static boolean CHECK_DATA_FIXER_SCHEMA = true;
    public static boolean IS_RUNNING_IN_IDE;
    public static final char[] ILLEGAL_FILE_CHARACTERS = new char[]{'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '"', ':'};
    private static GameVersion CURRENT_VERSION;

    public static boolean isAllowedChatCharacter(char param0) {
        return param0 != 167 && param0 >= ' ' && param0 != 127;
    }

    public static String filterText(String param0) {
        StringBuilder var0 = new StringBuilder();

        for(char var1 : param0.toCharArray()) {
            if (isAllowedChatCharacter(var1)) {
                var0.append(var1);
            }
        }

        return var0.toString();
    }

    public static GameVersion getCurrentVersion() {
        if (CURRENT_VERSION == null) {
            CURRENT_VERSION = DetectedVersion.tryDetectVersion();
        }

        return CURRENT_VERSION;
    }

    public static int getProtocolVersion() {
        return 1073741841;
    }

    static {
        ResourceLeakDetector.setLevel(NETTY_LEAK_DETECTION);
        CommandSyntaxException.ENABLE_COMMAND_STACK_TRACES = false;
        CommandSyntaxException.BUILT_IN_EXCEPTIONS = new BrigadierExceptions();
    }
}
