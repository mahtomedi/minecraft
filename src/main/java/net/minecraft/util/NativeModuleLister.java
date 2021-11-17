package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.Version;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.Tlhelp32.MODULEENTRY32W;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import net.minecraft.CrashReportCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NativeModuleLister {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int LANG_MASK = 65535;
    private static final int DEFAULT_LANG = 1033;
    private static final int CODEPAGE_MASK = -65536;
    private static final int DEFAULT_CODEPAGE = 78643200;

    public static List<NativeModuleLister.NativeModuleInfo> listModules() {
        if (!Platform.isWindows()) {
            return ImmutableList.of();
        } else {
            int var0 = Kernel32.INSTANCE.GetCurrentProcessId();
            Builder<NativeModuleLister.NativeModuleInfo> var1 = ImmutableList.builder();

            for(MODULEENTRY32W var3 : Kernel32Util.getModules(var0)) {
                String var4 = var3.szModule();
                Optional<NativeModuleLister.NativeModuleVersion> var5 = tryGetVersion(var3.szExePath());
                var1.add(new NativeModuleLister.NativeModuleInfo(var4, var5));
            }

            return var1.build();
        }
    }

    private static Optional<NativeModuleLister.NativeModuleVersion> tryGetVersion(String param0) {
        try {
            IntByReference var0 = new IntByReference();
            int var1 = Version.INSTANCE.GetFileVersionInfoSize(param0, var0);
            if (var1 == 0) {
                throw new Win32Exception(Native.getLastError());
            } else {
                Pointer var2 = new Memory((long)var1);
                if (!Version.INSTANCE.GetFileVersionInfo(param0, 0, var1, var2)) {
                    throw new Win32Exception(Native.getLastError());
                } else {
                    IntByReference var3 = new IntByReference();
                    Pointer var4 = queryVersionValue(var2, "\\VarFileInfo\\Translation", var3);
                    int[] var5 = var4.getIntArray(0L, var3.getValue() / 4);
                    OptionalInt var6 = findLangAndCodepage(var5);
                    if (!var6.isPresent()) {
                        return Optional.empty();
                    } else {
                        int var7 = var6.getAsInt();
                        int var8 = var7 & 65535;
                        int var9 = (var7 & -65536) >> 16;
                        String var10 = queryVersionString(var2, langTableKey("FileDescription", var8, var9), var3);
                        String var11 = queryVersionString(var2, langTableKey("CompanyName", var8, var9), var3);
                        String var12 = queryVersionString(var2, langTableKey("FileVersion", var8, var9), var3);
                        return Optional.of(new NativeModuleLister.NativeModuleVersion(var10, var12, var11));
                    }
                }
            }
        } catch (Exception var14) {
            LOGGER.info("Failed to find module info for {}", param0, var14);
            return Optional.empty();
        }
    }

    private static String langTableKey(String param0, int param1, int param2) {
        return String.format("\\StringFileInfo\\%04x%04x\\%s", param1, param2, param0);
    }

    private static OptionalInt findLangAndCodepage(int[] param0) {
        OptionalInt var0 = OptionalInt.empty();

        for(int var1 : param0) {
            if ((var1 & -65536) == 78643200 && (var1 & 65535) == 1033) {
                return OptionalInt.of(var1);
            }

            var0 = OptionalInt.of(var1);
        }

        return var0;
    }

    private static Pointer queryVersionValue(Pointer param0, String param1, IntByReference param2) {
        PointerByReference var0 = new PointerByReference();
        if (!Version.INSTANCE.VerQueryValue(param0, param1, var0, param2)) {
            throw new UnsupportedOperationException("Can't get version value " + param1);
        } else {
            return var0.getValue();
        }
    }

    private static String queryVersionString(Pointer param0, String param1, IntByReference param2) {
        try {
            Pointer var0 = queryVersionValue(param0, param1, param2);
            byte[] var1 = var0.getByteArray(0L, (param2.getValue() - 1) * 2);
            return new String(var1, StandardCharsets.UTF_16LE);
        } catch (Exception var5) {
            return "";
        }
    }

    public static void addCrashSection(CrashReportCategory param0) {
        param0.setDetail(
            "Modules",
            () -> listModules().stream().sorted(Comparator.comparing(param0x -> param0x.name)).map(param0x -> "\n\t\t" + param0x).collect(Collectors.joining())
        );
    }

    public static class NativeModuleInfo {
        public final String name;
        public final Optional<NativeModuleLister.NativeModuleVersion> version;

        public NativeModuleInfo(String param0, Optional<NativeModuleLister.NativeModuleVersion> param1) {
            this.name = param0;
            this.version = param1;
        }

        @Override
        public String toString() {
            return this.version.<String>map(param0 -> this.name + ":" + param0).orElse(this.name);
        }
    }

    public static class NativeModuleVersion {
        public final String description;
        public final String version;
        public final String company;

        public NativeModuleVersion(String param0, String param1, String param2) {
            this.description = param0;
            this.version = param1;
            this.company = param2;
        }

        @Override
        public String toString() {
            return this.description + ":" + this.version + ":" + this.company;
        }
    }
}
