package net.minecraft.client.main;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.properties.PropertyMap.Serializer;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.client.telemetry.events.GameLoadTimesEvent;
import net.minecraft.core.UUIDUtil;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Main {
    static final Logger LOGGER = LogUtils.getLogger();

    @DontObfuscate
    public static void main(String[] param0) {
        Stopwatch var0 = Stopwatch.createStarted(Ticker.systemTicker());
        Stopwatch var1 = Stopwatch.createStarted(Ticker.systemTicker());
        GameLoadTimesEvent.INSTANCE.beginStep(TelemetryProperty.LOAD_TIME_TOTAL_TIME_MS, var0);
        GameLoadTimesEvent.INSTANCE.beginStep(TelemetryProperty.LOAD_TIME_PRE_WINDOW_MS, var1);
        SharedConstants.tryDetectVersion();
        SharedConstants.enableDataFixerOptimizations();
        OptionParser var2 = new OptionParser();
        var2.allowsUnrecognizedOptions();
        var2.accepts("demo");
        var2.accepts("disableMultiplayer");
        var2.accepts("disableChat");
        var2.accepts("fullscreen");
        var2.accepts("checkGlErrors");
        OptionSpec<Void> var3 = var2.accepts("jfrProfile");
        OptionSpec<String> var4 = var2.accepts("quickPlayPath").withRequiredArg();
        OptionSpec<String> var5 = var2.accepts("quickPlaySingleplayer").withRequiredArg();
        OptionSpec<String> var6 = var2.accepts("quickPlayMultiplayer").withRequiredArg();
        OptionSpec<String> var7 = var2.accepts("quickPlayRealms").withRequiredArg();
        OptionSpec<File> var8 = var2.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo(new File("."));
        OptionSpec<File> var9 = var2.accepts("assetsDir").withRequiredArg().ofType(File.class);
        OptionSpec<File> var10 = var2.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
        OptionSpec<String> var11 = var2.accepts("proxyHost").withRequiredArg();
        OptionSpec<Integer> var12 = var2.accepts("proxyPort").withRequiredArg().defaultsTo("8080").ofType(Integer.class);
        OptionSpec<String> var13 = var2.accepts("proxyUser").withRequiredArg();
        OptionSpec<String> var14 = var2.accepts("proxyPass").withRequiredArg();
        OptionSpec<String> var15 = var2.accepts("username").withRequiredArg().defaultsTo("Player" + Util.getMillis() % 1000L);
        OptionSpec<String> var16 = var2.accepts("uuid").withRequiredArg();
        OptionSpec<String> var17 = var2.accepts("xuid").withOptionalArg().defaultsTo("");
        OptionSpec<String> var18 = var2.accepts("clientId").withOptionalArg().defaultsTo("");
        OptionSpec<String> var19 = var2.accepts("accessToken").withRequiredArg().required();
        OptionSpec<String> var20 = var2.accepts("version").withRequiredArg().required();
        OptionSpec<Integer> var21 = var2.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo(854);
        OptionSpec<Integer> var22 = var2.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo(480);
        OptionSpec<Integer> var23 = var2.accepts("fullscreenWidth").withRequiredArg().ofType(Integer.class);
        OptionSpec<Integer> var24 = var2.accepts("fullscreenHeight").withRequiredArg().ofType(Integer.class);
        OptionSpec<String> var25 = var2.accepts("userProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> var26 = var2.accepts("profileProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> var27 = var2.accepts("assetIndex").withRequiredArg();
        OptionSpec<String> var28 = var2.accepts("userType").withRequiredArg().defaultsTo(User.Type.LEGACY.getName());
        OptionSpec<String> var29 = var2.accepts("versionType").withRequiredArg().defaultsTo("release");
        OptionSpec<String> var30 = var2.nonOptions();
        OptionSet var31 = var2.parse(param0);
        List<String> var32 = var31.valuesOf(var30);
        if (!var32.isEmpty()) {
            System.out.println("Completely ignored arguments: " + var32);
        }

        String var33 = parseArgument(var31, var11);
        Proxy var34 = Proxy.NO_PROXY;
        if (var33 != null) {
            try {
                var34 = new Proxy(Type.SOCKS, new InetSocketAddress(var33, parseArgument(var31, var12)));
            } catch (Exception var83) {
            }
        }

        final String var35 = parseArgument(var31, var13);
        final String var36 = parseArgument(var31, var14);
        if (!var34.equals(Proxy.NO_PROXY) && stringHasValue(var35) && stringHasValue(var36)) {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(var35, var36.toCharArray());
                }
            });
        }

        int var37 = parseArgument(var31, var21);
        int var38 = parseArgument(var31, var22);
        OptionalInt var39 = ofNullable(parseArgument(var31, var23));
        OptionalInt var40 = ofNullable(parseArgument(var31, var24));
        boolean var41 = var31.has("fullscreen");
        boolean var42 = var31.has("demo");
        boolean var43 = var31.has("disableMultiplayer");
        boolean var44 = var31.has("disableChat");
        String var45 = parseArgument(var31, var20);
        Gson var46 = new GsonBuilder().registerTypeAdapter(PropertyMap.class, new Serializer()).create();
        PropertyMap var47 = GsonHelper.fromJson(var46, parseArgument(var31, var25), PropertyMap.class);
        PropertyMap var48 = GsonHelper.fromJson(var46, parseArgument(var31, var26), PropertyMap.class);
        String var49 = parseArgument(var31, var29);
        File var50 = parseArgument(var31, var8);
        File var51 = var31.has(var9) ? parseArgument(var31, var9) : new File(var50, "assets/");
        File var52 = var31.has(var10) ? parseArgument(var31, var10) : new File(var50, "resourcepacks/");
        String var53 = var31.has(var16) ? var16.value(var31) : UUIDUtil.createOfflinePlayerUUID(var15.value(var31)).toString();
        String var54 = var31.has(var27) ? var27.value(var31) : null;
        String var55 = var31.valueOf(var17);
        String var56 = var31.valueOf(var18);
        String var57 = parseArgument(var31, var4);
        String var58 = parseArgument(var31, var5);
        String var59 = parseArgument(var31, var6);
        String var60 = parseArgument(var31, var7);
        if (var31.has(var3)) {
            JvmProfiler.INSTANCE.start(Environment.CLIENT);
        }

        CrashReport.preload();
        Bootstrap.bootStrap();
        GameLoadTimesEvent.INSTANCE.setBootstrapTime(Bootstrap.bootstrapDuration.get());
        Bootstrap.validate();
        Util.startTimerHackThread();
        String var61 = var28.value(var31);
        User.Type var62 = User.Type.byName(var61);
        if (var62 == null) {
            LOGGER.warn("Unrecognized user type: {}", var61);
        }

        User var63 = new User(var15.value(var31), var53, var19.value(var31), emptyStringToEmptyOptional(var55), emptyStringToEmptyOptional(var56), var62);
        GameConfig var64 = new GameConfig(
            new GameConfig.UserData(var63, var47, var48, var34),
            new DisplayData(var37, var38, var39, var40, var41),
            new GameConfig.FolderData(var50, var52, var51, var54),
            new GameConfig.GameData(var42, var45, var49, var43, var44),
            new GameConfig.QuickPlayData(var57, var58, var59, var60)
        );
        Thread var65 = new Thread("Client Shutdown Thread") {
            @Override
            public void run() {
                Minecraft var0 = Minecraft.getInstance();
                if (var0 != null) {
                    IntegratedServer var1 = var0.getSingleplayerServer();
                    if (var1 != null) {
                        var1.halt(true);
                    }

                }
            }
        };
        var65.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        Runtime.getRuntime().addShutdownHook(var65);

        final Minecraft var66;
        try {
            Thread.currentThread().setName("Render thread");
            RenderSystem.initRenderThread();
            RenderSystem.beginInitialization();
            var66 = new Minecraft(var64);
            RenderSystem.finishInitialization();
        } catch (SilentInitException var81) {
            LOGGER.warn("Failed to create window: ", (Throwable)var81);
            return;
        } catch (Throwable var82) {
            CrashReport var69 = CrashReport.forThrowable(var82, "Initializing game");
            CrashReportCategory var70 = var69.addCategory("Initialization");
            NativeModuleLister.addCrashSection(var70);
            Minecraft.fillReport(null, null, var64.game.launchVersion, null, var69);
            Minecraft.crash(var69);
            return;
        }

        Thread var72;
        if (var66.renderOnThread()) {
            var72 = new Thread("Game thread") {
                @Override
                public void run() {
                    try {
                        RenderSystem.initGameThread(true);
                        var66.run();
                    } catch (Throwable var2) {
                        Main.LOGGER.error("Exception in client thread", var2);
                    }

                }
            };
            var72.start();

            while(var66.isRunning()) {
            }
        } else {
            var72 = null;

            try {
                RenderSystem.initGameThread(false);
                var66.run();
            } catch (Throwable var80) {
                LOGGER.error("Unhandled game exception", var80);
            }
        }

        BufferUploader.reset();

        try {
            var66.stop();
            if (var72 != null) {
                var72.join();
            }
        } catch (InterruptedException var78) {
            LOGGER.error("Exception during client thread shutdown", (Throwable)var78);
        } finally {
            var66.destroy();
        }

    }

    private static Optional<String> emptyStringToEmptyOptional(String param0) {
        return param0.isEmpty() ? Optional.empty() : Optional.of(param0);
    }

    private static OptionalInt ofNullable(@Nullable Integer param0) {
        return param0 != null ? OptionalInt.of(param0) : OptionalInt.empty();
    }

    @Nullable
    private static <T> T parseArgument(OptionSet param0, OptionSpec<T> param1) {
        try {
            return param0.valueOf(param1);
        } catch (Throwable var5) {
            if (param1 instanceof ArgumentAcceptingOptionSpec var1) {
                List<T> var2 = var1.defaultValues();
                if (!var2.isEmpty()) {
                    return var2.get(0);
                }
            }

            throw var5;
        }
    }

    private static boolean stringHasValue(@Nullable String param0) {
        return param0 != null && !param0.isEmpty();
    }

    static {
        System.setProperty("java.awt.headless", "true");
    }
}
