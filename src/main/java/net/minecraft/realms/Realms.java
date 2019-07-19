package net.minecraft.realms;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Proxy;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Realms {
    private static final RepeatedNarrator REPEATED_NARRATOR = new RepeatedNarrator(Duration.ofSeconds(5L));

    public static boolean isTouchScreen() {
        return Minecraft.getInstance().options.touchscreen;
    }

    public static Proxy getProxy() {
        return Minecraft.getInstance().getProxy();
    }

    public static String sessionId() {
        User var0 = Minecraft.getInstance().getUser();
        return var0 == null ? null : var0.getSessionId();
    }

    public static String userName() {
        User var0 = Minecraft.getInstance().getUser();
        return var0 == null ? null : var0.getName();
    }

    public static long currentTimeMillis() {
        return Util.getMillis();
    }

    public static String getSessionId() {
        return Minecraft.getInstance().getUser().getSessionId();
    }

    public static String getUUID() {
        return Minecraft.getInstance().getUser().getUuid();
    }

    public static String getName() {
        return Minecraft.getInstance().getUser().getName();
    }

    public static String uuidToName(String param0) {
        return Minecraft.getInstance()
            .getMinecraftSessionService()
            .fillProfileProperties(new GameProfile(UUIDTypeAdapter.fromString(param0), null), false)
            .getName();
    }

    public static <V> CompletableFuture<V> execute(Supplier<V> param0) {
        return Minecraft.getInstance().submit(param0);
    }

    public static void execute(Runnable param0) {
        Minecraft.getInstance().execute(param0);
    }

    public static void setScreen(RealmsScreen param0) {
        execute(() -> {
            setScreenDirect(param0);
            return null;
        });
    }

    public static void setScreenDirect(RealmsScreen param0) {
        Minecraft.getInstance().setScreen(param0.getProxy());
    }

    public static String getGameDirectoryPath() {
        return Minecraft.getInstance().gameDirectory.getAbsolutePath();
    }

    public static int survivalId() {
        return GameType.SURVIVAL.getId();
    }

    public static int creativeId() {
        return GameType.CREATIVE.getId();
    }

    public static int adventureId() {
        return GameType.ADVENTURE.getId();
    }

    public static int spectatorId() {
        return GameType.SPECTATOR.getId();
    }

    public static void setConnectedToRealms(boolean param0) {
        Minecraft.getInstance().setConnectedToRealms(param0);
    }

    public static CompletableFuture<?> downloadResourcePack(String param0, String param1) {
        return Minecraft.getInstance().getClientPackSource().downloadAndSelectResourcePack(param0, param1);
    }

    public static void clearResourcePack() {
        Minecraft.getInstance().getClientPackSource().clearServerPack();
    }

    public static boolean getRealmsNotificationsEnabled() {
        return Minecraft.getInstance().options.realmsNotifications;
    }

    public static boolean inTitleScreen() {
        return Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof TitleScreen;
    }

    public static void deletePlayerTag(File param0) {
        if (param0.exists()) {
            try {
                CompoundTag var0 = NbtIo.readCompressed(new FileInputStream(param0));
                CompoundTag var1 = var0.getCompound("Data");
                var1.remove("Player");
                NbtIo.writeCompressed(var0, new FileOutputStream(param0));
            } catch (Exception var3) {
                var3.printStackTrace();
            }
        }

    }

    public static void openUri(String param0) {
        Util.getPlatform().openUri(param0);
    }

    public static void setClipboard(String param0) {
        Minecraft.getInstance().keyboardHandler.setClipboard(param0);
    }

    public static String getMinecraftVersionString() {
        return SharedConstants.getCurrentVersion().getName();
    }

    public static ResourceLocation resourceLocation(String param0) {
        return new ResourceLocation(param0);
    }

    public static String getLocalizedString(String param0, Object... param1) {
        return I18n.get(param0, param1);
    }

    public static void bind(String param0) {
        ResourceLocation var0 = new ResourceLocation(param0);
        Minecraft.getInstance().getTextureManager().bind(var0);
    }

    public static void narrateNow(String param0) {
        NarratorChatListener var0 = NarratorChatListener.INSTANCE;
        var0.clear();
        var0.handle(ChatType.SYSTEM, new TextComponent(fixNarrationNewlines(param0)));
    }

    private static String fixNarrationNewlines(String param0) {
        return param0.replace("\\n", System.lineSeparator());
    }

    public static void narrateNow(String... param0) {
        narrateNow(Arrays.asList(param0));
    }

    public static void narrateNow(Iterable<String> param0) {
        narrateNow(joinNarrations(param0));
    }

    public static String joinNarrations(Iterable<String> param0) {
        return String.join(System.lineSeparator(), param0);
    }

    public static void narrateRepeatedly(String param0) {
        REPEATED_NARRATOR.narrate(fixNarrationNewlines(param0));
    }
}
