package net.minecraft.client.main;

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
        SharedConstants.tryDetectVersion();
        SharedConstants.enableDataFixerOptimizations();
        OptionParser var0 = new OptionParser();
        var0.allowsUnrecognizedOptions();
        var0.accepts("demo");
        var0.accepts("disableMultiplayer");
        var0.accepts("disableChat");
        var0.accepts("fullscreen");
        var0.accepts("checkGlErrors");
        OptionSpec<Void> var1 = var0.accepts("jfrProfile");
        OptionSpec<String> var2 = var0.accepts("quickPlayPath").withRequiredArg();
        OptionSpec<String> var3 = var0.accepts("quickPlaySingleplayer").withRequiredArg();
        OptionSpec<String> var4 = var0.accepts("quickPlayMultiplayer").withRequiredArg();
        OptionSpec<String> var5 = var0.accepts("quickPlayRealms").withRequiredArg();
        OptionSpec<File> var6 = var0.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo(new File("."));
        OptionSpec<File> var7 = var0.accepts("assetsDir").withRequiredArg().ofType(File.class);
        OptionSpec<File> var8 = var0.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
        OptionSpec<String> var9 = var0.accepts("proxyHost").withRequiredArg();
        OptionSpec<Integer> var10 = var0.accepts("proxyPort").withRequiredArg().defaultsTo("8080").ofType(Integer.class);
        OptionSpec<String> var11 = var0.accepts("proxyUser").withRequiredArg();
        OptionSpec<String> var12 = var0.accepts("proxyPass").withRequiredArg();
        OptionSpec<String> var13 = var0.accepts("username").withRequiredArg().defaultsTo("Player" + Util.getMillis() % 1000L);
        OptionSpec<String> var14 = var0.accepts("uuid").withRequiredArg();
        OptionSpec<String> var15 = var0.accepts("xuid").withOptionalArg().defaultsTo("");
        OptionSpec<String> var16 = var0.accepts("clientId").withOptionalArg().defaultsTo("");
        OptionSpec<String> var17 = var0.accepts("accessToken").withRequiredArg().required();
        OptionSpec<String> var18 = var0.accepts("version").withRequiredArg().required();
        OptionSpec<Integer> var19 = var0.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo(854);
        OptionSpec<Integer> var20 = var0.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo(480);
        OptionSpec<Integer> var21 = var0.accepts("fullscreenWidth").withRequiredArg().ofType(Integer.class);
        OptionSpec<Integer> var22 = var0.accepts("fullscreenHeight").withRequiredArg().ofType(Integer.class);
        OptionSpec<String> var23 = var0.accepts("userProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> var24 = var0.accepts("profileProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> var25 = var0.accepts("assetIndex").withRequiredArg();
        OptionSpec<String> var26 = var0.accepts("userType").withRequiredArg().defaultsTo(User.Type.LEGACY.getName());
        OptionSpec<String> var27 = var0.accepts("versionType").withRequiredArg().defaultsTo("release");
        OptionSpec<String> var28 = var0.nonOptions();
        OptionSet var29 = var0.parse(param0);
        List<String> var30 = var29.valuesOf(var28);
        if (!var30.isEmpty()) {
            System.out.println("Completely ignored arguments: " + var30);
        }

        String var31 = parseArgument(var29, var9);
        Proxy var32 = Proxy.NO_PROXY;
        if (var31 != null) {
            try {
                var32 = new Proxy(Type.SOCKS, new InetSocketAddress(var31, parseArgument(var29, var10)));
            } catch (Exception var81) {
            }
        }

        final String var33 = parseArgument(var29, var11);
        final String var34 = parseArgument(var29, var12);
        if (!var32.equals(Proxy.NO_PROXY) && stringHasValue(var33) && stringHasValue(var34)) {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(var33, var34.toCharArray());
                }
            });
        }

        int var35 = parseArgument(var29, var19);
        int var36 = parseArgument(var29, var20);
        OptionalInt var37 = ofNullable(parseArgument(var29, var21));
        OptionalInt var38 = ofNullable(parseArgument(var29, var22));
        boolean var39 = var29.has("fullscreen");
        boolean var40 = var29.has("demo");
        boolean var41 = var29.has("disableMultiplayer");
        boolean var42 = var29.has("disableChat");
        String var43 = parseArgument(var29, var18);
        Gson var44 = new GsonBuilder().registerTypeAdapter(PropertyMap.class, new Serializer()).create();
        PropertyMap var45 = GsonHelper.fromJson(var44, parseArgument(var29, var23), PropertyMap.class);
        PropertyMap var46 = GsonHelper.fromJson(var44, parseArgument(var29, var24), PropertyMap.class);
        String var47 = parseArgument(var29, var27);
        File var48 = parseArgument(var29, var6);
        File var49 = var29.has(var7) ? parseArgument(var29, var7) : new File(var48, "assets/");
        File var50 = var29.has(var8) ? parseArgument(var29, var8) : new File(var48, "resourcepacks/");
        String var51 = var29.has(var14) ? var14.value(var29) : UUIDUtil.createOfflinePlayerUUID(var13.value(var29)).toString();
        String var52 = var29.has(var25) ? var25.value(var29) : null;
        String var53 = var29.valueOf(var15);
        String var54 = var29.valueOf(var16);
        String var55 = parseArgument(var29, var2);
        String var56 = parseArgument(var29, var3);
        String var57 = parseArgument(var29, var4);
        String var58 = parseArgument(var29, var5);
        if (var29.has(var1)) {
            JvmProfiler.INSTANCE.start(Environment.CLIENT);
        }

        CrashReport.preload();
        Bootstrap.bootStrap();
        Bootstrap.validate();
        Util.startTimerHackThread();
        String var59 = var26.value(var29);
        User.Type var60 = User.Type.byName(var59);
        if (var60 == null) {
            LOGGER.warn("Unrecognized user type: {}", var59);
        }

        User var61 = new User(var13.value(var29), var51, var17.value(var29), emptyStringToEmptyOptional(var53), emptyStringToEmptyOptional(var54), var60);
        GameConfig var62 = new GameConfig(
            new GameConfig.UserData(var61, var45, var46, var32),
            new DisplayData(var35, var36, var37, var38, var39),
            new GameConfig.FolderData(var48, var50, var49, var52),
            new GameConfig.GameData(var40, var43, var47, var41, var42),
            new GameConfig.QuickPlayData(var55, var56, var57, var58)
        );
        Thread var63 = new Thread("Client Shutdown Thread") {
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
        var63.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        Runtime.getRuntime().addShutdownHook(var63);

        final Minecraft var64;
        try {
            Thread.currentThread().setName("Render thread");
            RenderSystem.initRenderThread();
            RenderSystem.beginInitialization();
            var64 = new Minecraft(var62);
            RenderSystem.finishInitialization();
        } catch (SilentInitException var79) {
            LOGGER.warn("Failed to create window: ", (Throwable)var79);
            return;
        } catch (Throwable var80) {
            CrashReport var67 = CrashReport.forThrowable(var80, "Initializing game");
            CrashReportCategory var68 = var67.addCategory("Initialization");
            NativeModuleLister.addCrashSection(var68);
            Minecraft.fillReport(null, null, var62.game.launchVersion, null, var67);
            Minecraft.crash(var67);
            return;
        }

        Thread var70;
        if (var64.renderOnThread()) {
            var70 = new Thread("Game thread") {
                @Override
                public void run() {
                    try {
                        RenderSystem.initGameThread(true);
                        var64.run();
                    } catch (Throwable var2) {
                        Main.LOGGER.error("Exception in client thread", var2);
                    }

                }
            };
            var70.start();

            while(var64.isRunning()) {
            }
        } else {
            var70 = null;

            try {
                RenderSystem.initGameThread(false);
                var64.run();
            } catch (Throwable var78) {
                LOGGER.error("Unhandled game exception", var78);
            }
        }

        BufferUploader.reset();

        try {
            var64.stop();
            if (var70 != null) {
                var70.join();
            }
        } catch (InterruptedException var76) {
            LOGGER.error("Exception during client thread shutdown", (Throwable)var76);
        } finally {
            var64.destroy();
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
