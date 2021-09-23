package net.minecraft.client.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.properties.PropertyMap.Serializer;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
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
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Main {
    static final Logger LOGGER = LogManager.getLogger();

    @DontObfuscate
    public static void main(String[] param0) {
        SharedConstants.tryDetectVersion();
        OptionParser var0 = new OptionParser();
        var0.allowsUnrecognizedOptions();
        var0.accepts("demo");
        var0.accepts("disableMultiplayer");
        var0.accepts("disableChat");
        var0.accepts("fullscreen");
        var0.accepts("checkGlErrors");
        OptionSpec<Void> var1 = var0.accepts("jfrProfile");
        OptionSpec<String> var2 = var0.accepts("server").withRequiredArg();
        OptionSpec<Integer> var3 = var0.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(25565);
        OptionSpec<File> var4 = var0.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo(new File("."));
        OptionSpec<File> var5 = var0.accepts("assetsDir").withRequiredArg().ofType(File.class);
        OptionSpec<File> var6 = var0.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
        OptionSpec<String> var7 = var0.accepts("proxyHost").withRequiredArg();
        OptionSpec<Integer> var8 = var0.accepts("proxyPort").withRequiredArg().defaultsTo("8080").ofType(Integer.class);
        OptionSpec<String> var9 = var0.accepts("proxyUser").withRequiredArg();
        OptionSpec<String> var10 = var0.accepts("proxyPass").withRequiredArg();
        OptionSpec<String> var11 = var0.accepts("username").withRequiredArg().defaultsTo("Player" + Util.getMillis() % 1000L);
        OptionSpec<String> var12 = var0.accepts("uuid").withRequiredArg();
        OptionSpec<String> var13 = var0.accepts("xuid").withOptionalArg().defaultsTo("");
        OptionSpec<String> var14 = var0.accepts("clientId").withOptionalArg().defaultsTo("");
        OptionSpec<String> var15 = var0.accepts("accessToken").withRequiredArg().required();
        OptionSpec<String> var16 = var0.accepts("version").withRequiredArg().required();
        OptionSpec<Integer> var17 = var0.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo(854);
        OptionSpec<Integer> var18 = var0.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo(480);
        OptionSpec<Integer> var19 = var0.accepts("fullscreenWidth").withRequiredArg().ofType(Integer.class);
        OptionSpec<Integer> var20 = var0.accepts("fullscreenHeight").withRequiredArg().ofType(Integer.class);
        OptionSpec<String> var21 = var0.accepts("userProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> var22 = var0.accepts("profileProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> var23 = var0.accepts("assetIndex").withRequiredArg();
        OptionSpec<String> var24 = var0.accepts("userType").withRequiredArg().defaultsTo(User.Type.LEGACY.getName());
        OptionSpec<String> var25 = var0.accepts("versionType").withRequiredArg().defaultsTo("release");
        OptionSpec<String> var26 = var0.nonOptions();
        OptionSet var27 = var0.parse(param0);
        List<String> var28 = var27.valuesOf(var26);
        if (!var28.isEmpty()) {
            System.out.println("Completely ignored arguments: " + var28);
        }

        String var29 = parseArgument(var27, var7);
        Proxy var30 = Proxy.NO_PROXY;
        if (var29 != null) {
            try {
                var30 = new Proxy(Type.SOCKS, new InetSocketAddress(var29, parseArgument(var27, var8)));
            } catch (Exception var77) {
            }
        }

        final String var31 = parseArgument(var27, var9);
        final String var32 = parseArgument(var27, var10);
        if (!var30.equals(Proxy.NO_PROXY) && stringHasValue(var31) && stringHasValue(var32)) {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(var31, var32.toCharArray());
                }
            });
        }

        int var33 = parseArgument(var27, var17);
        int var34 = parseArgument(var27, var18);
        OptionalInt var35 = ofNullable(parseArgument(var27, var19));
        OptionalInt var36 = ofNullable(parseArgument(var27, var20));
        boolean var37 = var27.has("fullscreen");
        boolean var38 = var27.has("demo");
        boolean var39 = var27.has("disableMultiplayer");
        boolean var40 = var27.has("disableChat");
        String var41 = parseArgument(var27, var16);
        Gson var42 = new GsonBuilder().registerTypeAdapter(PropertyMap.class, new Serializer()).create();
        PropertyMap var43 = GsonHelper.fromJson(var42, parseArgument(var27, var21), PropertyMap.class);
        PropertyMap var44 = GsonHelper.fromJson(var42, parseArgument(var27, var22), PropertyMap.class);
        String var45 = parseArgument(var27, var25);
        File var46 = parseArgument(var27, var4);
        File var47 = var27.has(var5) ? parseArgument(var27, var5) : new File(var46, "assets/");
        File var48 = var27.has(var6) ? parseArgument(var27, var6) : new File(var46, "resourcepacks/");
        String var49 = var27.has(var12) ? var12.value(var27) : Player.createPlayerUUID(var11.value(var27)).toString();
        String var50 = var27.has(var23) ? var23.value(var27) : null;
        String var51 = var27.valueOf(var13);
        String var52 = var27.valueOf(var14);
        String var53 = parseArgument(var27, var2);
        Integer var54 = parseArgument(var27, var3);
        if (var27.has(var1)) {
            JvmProfiler.INSTANCE.start(Environment.CLIENT);
        }

        JvmProfiler.INSTANCE.initialize();
        CrashReport.preload();
        Bootstrap.bootStrap();
        Bootstrap.validate();
        Util.startTimerHackThread();
        String var55 = var24.value(var27);
        User.Type var56 = User.Type.byName(var55);
        if (var56 == null) {
            LOGGER.warn("Unrecognized user type: {}", var55);
        }

        User var57 = new User(var11.value(var27), var49, var15.value(var27), emptyStringToEmptyOptional(var51), emptyStringToEmptyOptional(var52), var56);
        GameConfig var58 = new GameConfig(
            new GameConfig.UserData(var57, var43, var44, var30),
            new DisplayData(var33, var34, var35, var36, var37),
            new GameConfig.FolderData(var46, var48, var47, var50),
            new GameConfig.GameData(var38, var41, var45, var39, var40),
            new GameConfig.ServerData(var53, var54)
        );
        Thread var59 = new Thread("Client Shutdown Thread") {
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
        var59.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        Runtime.getRuntime().addShutdownHook(var59);

        final Minecraft var60;
        try {
            Thread.currentThread().setName("Render thread");
            RenderSystem.initRenderThread();
            RenderSystem.beginInitialization();
            var60 = new Minecraft(var58);
            RenderSystem.finishInitialization();
        } catch (SilentInitException var75) {
            LOGGER.warn("Failed to create window: ", (Throwable)var75);
            return;
        } catch (Throwable var76) {
            CrashReport var63 = CrashReport.forThrowable(var76, "Initializing game");
            CrashReportCategory var64 = var63.addCategory("Initialization");
            NativeModuleLister.addCrashSection(var64);
            Minecraft.fillReport(null, null, var58.game.launchVersion, null, var63);
            Minecraft.crash(var63);
            return;
        }

        Thread var66;
        if (var60.renderOnThread()) {
            var66 = new Thread("Game thread") {
                @Override
                public void run() {
                    try {
                        RenderSystem.initGameThread(true);
                        var60.run();
                    } catch (Throwable var2) {
                        Main.LOGGER.error("Exception in client thread", var2);
                    }

                }
            };
            var66.start();

            while(var60.isRunning()) {
            }
        } else {
            var66 = null;

            try {
                RenderSystem.initGameThread(false);
                var60.run();
            } catch (Throwable var74) {
                LOGGER.error("Unhandled game exception", var74);
            }
        }

        BufferUploader.reset();

        try {
            var60.stop();
            if (var66 != null) {
                var66.join();
            }
        } catch (InterruptedException var72) {
            LOGGER.error("Exception during client thread shutdown", (Throwable)var72);
        } finally {
            var60.destroy();
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
