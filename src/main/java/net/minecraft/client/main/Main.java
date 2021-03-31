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
        OptionSpec<File> var6 = var0.accepts("dataPackDir").withRequiredArg().ofType(File.class);
        OptionSpec<String> var7 = var0.accepts("proxyHost").withRequiredArg();
        OptionSpec<Integer> var8 = var0.accepts("proxyPort").withRequiredArg().defaultsTo("8080").ofType(Integer.class);
        OptionSpec<String> var9 = var0.accepts("proxyUser").withRequiredArg();
        OptionSpec<String> var10 = var0.accepts("proxyPass").withRequiredArg();
        OptionSpec<String> var11 = var0.accepts("username").withRequiredArg().defaultsTo("Player" + Util.getMillis() % 1000L);
        OptionSpec<String> var12 = var0.accepts("uuid").withRequiredArg();
        OptionSpec<String> var13 = var0.accepts("accessToken").withRequiredArg().required();
        OptionSpec<String> var14 = var0.accepts("version").withRequiredArg().required();
        OptionSpec<Integer> var15 = var0.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo(854);
        OptionSpec<Integer> var16 = var0.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo(480);
        OptionSpec<Integer> var17 = var0.accepts("fullscreenWidth").withRequiredArg().ofType(Integer.class);
        OptionSpec<Integer> var18 = var0.accepts("fullscreenHeight").withRequiredArg().ofType(Integer.class);
        OptionSpec<String> var19 = var0.accepts("userProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> var20 = var0.accepts("profileProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> var21 = var0.accepts("assetIndex").withRequiredArg();
        OptionSpec<String> var22 = var0.accepts("userType").withRequiredArg().defaultsTo("legacy");
        OptionSpec<String> var23 = var0.accepts("versionType").withRequiredArg().defaultsTo("release");
        OptionSpec<String> var24 = var0.nonOptions();
        OptionSet var25 = var0.parse(param0);
        List<String> var26 = var25.valuesOf(var24);
        if (!var26.isEmpty()) {
            System.out.println("Completely ignored arguments: " + var26);
        }

        String var27 = parseArgument(var25, var7);
        Proxy var28 = Proxy.NO_PROXY;
        if (var27 != null) {
            try {
                var28 = new Proxy(Type.SOCKS, new InetSocketAddress(var27, parseArgument(var25, var8)));
            } catch (Exception var71) {
            }
        }

        final String var29 = parseArgument(var25, var9);
        final String var30 = parseArgument(var25, var10);
        if (!var28.equals(Proxy.NO_PROXY) && stringHasValue(var29) && stringHasValue(var30)) {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(var29, var30.toCharArray());
                }
            });
        }

        int var31 = parseArgument(var25, var15);
        int var32 = parseArgument(var25, var16);
        OptionalInt var33 = ofNullable(parseArgument(var25, var17));
        OptionalInt var34 = ofNullable(parseArgument(var25, var18));
        boolean var35 = var25.has("fullscreen");
        boolean var36 = var25.has("demo");
        boolean var37 = var25.has("disableMultiplayer");
        boolean var38 = var25.has("disableChat");
        String var39 = parseArgument(var25, var14);
        Gson var40 = new GsonBuilder().registerTypeAdapter(PropertyMap.class, new Serializer()).create();
        PropertyMap var41 = GsonHelper.fromJson(var40, parseArgument(var25, var19), PropertyMap.class);
        PropertyMap var42 = GsonHelper.fromJson(var40, parseArgument(var25, var20), PropertyMap.class);
        String var43 = parseArgument(var25, var23);
        File var44 = parseArgument(var25, var3);
        File var45 = var25.has(var4) ? parseArgument(var25, var4) : new File(var44, "assets/");
        File var46 = var25.has(var5) ? parseArgument(var25, var5) : new File(var44, "resourcepacks/");
        String var47 = var25.has(var12) ? var12.value(var25) : Player.createPlayerUUID(var11.value(var25)).toString();
        String var48 = var25.has(var21) ? var21.value(var25) : null;
        String var49 = parseArgument(var25, var1);
        Integer var50 = parseArgument(var25, var2);
        CrashReport.preload();
        Bootstrap.bootStrap();
        Bootstrap.validate();
        Util.startTimerHackThread();
        User var51 = new User(var11.value(var25), var47, var13.value(var25), var22.value(var25));
        GameConfig var52 = new GameConfig(
            new GameConfig.UserData(var51, var41, var42, var28),
            new DisplayData(var31, var32, var33, var34, var35),
            new GameConfig.FolderData(var44, var46, var45, var48),
            new GameConfig.GameData(var36, var39, var43, var37, var38),
            new GameConfig.ServerData(var49, var50)
        );
        Thread var53 = new Thread("Client Shutdown Thread") {
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
        var53.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        Runtime.getRuntime().addShutdownHook(var53);
        new RenderPipeline();

        final Minecraft var55;
        try {
            Thread.currentThread().setName("Render thread");
            RenderSystem.initRenderThread();
            RenderSystem.beginInitialization();
            var55 = new Minecraft(var52);
            RenderSystem.finishInitialization();
        } catch (SilentInitException var69) {
            LOGGER.warn("Failed to create window: ", (Throwable)var69);
            return;
        } catch (Throwable var70) {
            CrashReport var58 = CrashReport.forThrowable(var70, "Initializing game");
            var58.addCategory("Initialization");
            Minecraft.fillReport(null, var52.game.launchVersion, null, var58);
            Minecraft.crash(var58);
            return;
        }

        Thread var60;
        if (var55.renderOnThread()) {
            var60 = new Thread("Game thread") {
                @Override
                public void run() {
                    try {
                        RenderSystem.initGameThread(true);
                        var55.run();
                    } catch (Throwable var2) {
                        Main.LOGGER.error("Exception in client thread", var2);
                    }

                }
            };
            var60.start();

            while(var55.isRunning()) {
            }
        } else {
            var60 = null;

            try {
                RenderSystem.initGameThread(false);
                var55.run();
            } catch (Throwable var68) {
                LOGGER.error("Unhandled game exception", var68);
            }
        }

        BufferUploader.reset();

        try {
            var55.stop();
            if (var60 != null) {
                var60.join();
            }
        } catch (InterruptedException var66) {
            LOGGER.error("Exception during client thread shutdown", (Throwable)var66);
        } finally {
            var55.destroy();
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
