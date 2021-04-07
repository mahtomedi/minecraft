package net.minecraft.client.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.properties.PropertyMap.Serializer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
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
import java.util.OptionalInt;
import javax.annotation.Nullable;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

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
        OptionSpec<String> var1 = var0.accepts("server").withRequiredArg();
        OptionSpec<Integer> var2 = var0.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(25565);
        OptionSpec<File> var3 = var0.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo(new File("."));
        OptionSpec<File> var4 = var0.accepts("assetsDir").withRequiredArg().ofType(File.class);
        OptionSpec<File> var5 = var0.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
        OptionSpec<String> var6 = var0.accepts("proxyHost").withRequiredArg();
        OptionSpec<Integer> var7 = var0.accepts("proxyPort").withRequiredArg().defaultsTo("8080").ofType(Integer.class);
        OptionSpec<String> var8 = var0.accepts("proxyUser").withRequiredArg();
        OptionSpec<String> var9 = var0.accepts("proxyPass").withRequiredArg();
        OptionSpec<String> var10 = var0.accepts("username").withRequiredArg().defaultsTo("Player" + Util.getMillis() % 1000L);
        OptionSpec<String> var11 = var0.accepts("uuid").withRequiredArg();
        OptionSpec<String> var12 = var0.accepts("accessToken").withRequiredArg().required();
        OptionSpec<String> var13 = var0.accepts("version").withRequiredArg().required();
        OptionSpec<Integer> var14 = var0.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo(854);
        OptionSpec<Integer> var15 = var0.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo(480);
        OptionSpec<Integer> var16 = var0.accepts("fullscreenWidth").withRequiredArg().ofType(Integer.class);
        OptionSpec<Integer> var17 = var0.accepts("fullscreenHeight").withRequiredArg().ofType(Integer.class);
        OptionSpec<String> var18 = var0.accepts("userProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> var19 = var0.accepts("profileProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> var20 = var0.accepts("assetIndex").withRequiredArg();
        OptionSpec<String> var21 = var0.accepts("userType").withRequiredArg().defaultsTo("legacy");
        OptionSpec<String> var22 = var0.accepts("versionType").withRequiredArg().defaultsTo("release");
        OptionSpec<String> var23 = var0.nonOptions();
        OptionSet var24 = var0.parse(param0);
        List<String> var25 = var24.valuesOf(var23);
        if (!var25.isEmpty()) {
            System.out.println("Completely ignored arguments: " + var25);
        }

        String var26 = parseArgument(var24, var6);
        Proxy var27 = Proxy.NO_PROXY;
        if (var26 != null) {
            try {
                var27 = new Proxy(Type.SOCKS, new InetSocketAddress(var26, parseArgument(var24, var7)));
            } catch (Exception var70) {
            }
        }

        final String var28 = parseArgument(var24, var8);
        final String var29 = parseArgument(var24, var9);
        if (!var27.equals(Proxy.NO_PROXY) && stringHasValue(var28) && stringHasValue(var29)) {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(var28, var29.toCharArray());
                }
            });
        }

        int var30 = parseArgument(var24, var14);
        int var31 = parseArgument(var24, var15);
        OptionalInt var32 = ofNullable(parseArgument(var24, var16));
        OptionalInt var33 = ofNullable(parseArgument(var24, var17));
        boolean var34 = var24.has("fullscreen");
        boolean var35 = var24.has("demo");
        boolean var36 = var24.has("disableMultiplayer");
        boolean var37 = var24.has("disableChat");
        String var38 = parseArgument(var24, var13);
        Gson var39 = new GsonBuilder().registerTypeAdapter(PropertyMap.class, new Serializer()).create();
        PropertyMap var40 = GsonHelper.fromJson(var39, parseArgument(var24, var18), PropertyMap.class);
        PropertyMap var41 = GsonHelper.fromJson(var39, parseArgument(var24, var19), PropertyMap.class);
        String var42 = parseArgument(var24, var22);
        File var43 = parseArgument(var24, var3);
        File var44 = var24.has(var4) ? parseArgument(var24, var4) : new File(var43, "assets/");
        File var45 = var24.has(var5) ? parseArgument(var24, var5) : new File(var43, "resourcepacks/");
        String var46 = var24.has(var11) ? var11.value(var24) : Player.createPlayerUUID(var10.value(var24)).toString();
        String var47 = var24.has(var20) ? var20.value(var24) : null;
        String var48 = parseArgument(var24, var1);
        Integer var49 = parseArgument(var24, var2);
        CrashReport.preload();
        Bootstrap.bootStrap();
        Bootstrap.validate();
        Util.startTimerHackThread();
        User var50 = new User(var10.value(var24), var46, var12.value(var24), var21.value(var24));
        GameConfig var51 = new GameConfig(
            new GameConfig.UserData(var50, var40, var41, var27),
            new DisplayData(var30, var31, var32, var33, var34),
            new GameConfig.FolderData(var43, var45, var44, var47),
            new GameConfig.GameData(var35, var38, var42, var36, var37),
            new GameConfig.ServerData(var48, var49)
        );
        Thread var52 = new Thread("Client Shutdown Thread") {
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
        var52.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        Runtime.getRuntime().addShutdownHook(var52);
        new RenderPipeline();

        final Minecraft var54;
        try {
            Thread.currentThread().setName("Render thread");
            RenderSystem.initRenderThread();
            RenderSystem.beginInitialization();
            var54 = new Minecraft(var51);
            RenderSystem.finishInitialization();
        } catch (SilentInitException var68) {
            LOGGER.warn("Failed to create window: ", (Throwable)var68);
            return;
        } catch (Throwable var69) {
            CrashReport var57 = CrashReport.forThrowable(var69, "Initializing game");
            var57.addCategory("Initialization");
            Minecraft.fillReport(null, var51.game.launchVersion, null, var57);
            Minecraft.crash(var57);
            return;
        }

        Thread var59;
        if (var54.renderOnThread()) {
            var59 = new Thread("Game thread") {
                @Override
                public void run() {
                    try {
                        RenderSystem.initGameThread(true);
                        var54.run();
                    } catch (Throwable var2) {
                        Main.LOGGER.error("Exception in client thread", var2);
                    }

                }
            };
            var59.start();

            while(var54.isRunning()) {
            }
        } else {
            var59 = null;

            try {
                RenderSystem.initGameThread(false);
                var54.run();
            } catch (Throwable var67) {
                LOGGER.error("Unhandled game exception", var67);
            }
        }

        BufferUploader.reset();

        try {
            var54.stop();
            if (var59 != null) {
                var59.join();
            }
        } catch (InterruptedException var65) {
            LOGGER.error("Exception during client thread shutdown", (Throwable)var65);
        } finally {
            var54.destroy();
        }

    }

    private static OptionalInt ofNullable(@Nullable Integer param0) {
        return param0 != null ? OptionalInt.of(param0) : OptionalInt.empty();
    }

    @Nullable
    private static <T> T parseArgument(OptionSet param0, OptionSpec<T> param1) {
        try {
            return param0.valueOf(param1);
        } catch (Throwable var5) {
            if (param1 instanceof ArgumentAcceptingOptionSpec) {
                ArgumentAcceptingOptionSpec<T> var1 = (ArgumentAcceptingOptionSpec)param1;
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
