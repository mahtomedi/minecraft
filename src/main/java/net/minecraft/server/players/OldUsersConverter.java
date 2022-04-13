package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

public class OldUsersConverter {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final File OLD_IPBANLIST = new File("banned-ips.txt");
    public static final File OLD_USERBANLIST = new File("banned-players.txt");
    public static final File OLD_OPLIST = new File("ops.txt");
    public static final File OLD_WHITELIST = new File("white-list.txt");

    static List<String> readOldListFormat(File param0, Map<String, String[]> param1) throws IOException {
        List<String> var0 = Files.readLines(param0, StandardCharsets.UTF_8);

        for(String var1 : var0) {
            var1 = var1.trim();
            if (!var1.startsWith("#") && var1.length() >= 1) {
                String[] var2 = var1.split("\\|");
                param1.put(var2[0].toLowerCase(Locale.ROOT), var2);
            }
        }

        return var0;
    }

    private static void lookupPlayers(MinecraftServer param0, Collection<String> param1, ProfileLookupCallback param2) {
        String[] var0 = param1.stream().filter(param0x -> !StringUtil.isNullOrEmpty(param0x)).toArray(param0x -> new String[param0x]);
        if (param0.usesAuthentication()) {
            param0.getProfileRepository().findProfilesByNames(var0, Agent.MINECRAFT, param2);
        } else {
            for(String var1 : var0) {
                UUID var2 = UUIDUtil.getOrCreatePlayerUUID(new GameProfile(null, var1));
                GameProfile var3 = new GameProfile(var2, var1);
                param2.onProfileLookupSucceeded(var3);
            }
        }

    }

    public static boolean convertUserBanlist(final MinecraftServer param0) {
        final UserBanList var0 = new UserBanList(PlayerList.USERBANLIST_FILE);
        if (OLD_USERBANLIST.exists() && OLD_USERBANLIST.isFile()) {
            if (var0.getFile().exists()) {
                try {
                    var0.load();
                } catch (IOException var6) {
                    LOGGER.warn("Could not load existing file {}", var0.getFile().getName(), var6);
                }
            }

            try {
                final Map<String, String[]> var2 = Maps.newHashMap();
                readOldListFormat(OLD_USERBANLIST, var2);
                ProfileLookupCallback var3 = new ProfileLookupCallback() {
                    @Override
                    public void onProfileLookupSucceeded(GameProfile param0x) {
                        param0.getProfileCache().add(param0);
                        String[] var0 = var2.get(param0.getName().toLowerCase(Locale.ROOT));
                        if (var0 == null) {
                            OldUsersConverter.LOGGER.warn("Could not convert user banlist entry for {}", param0.getName());
                            throw new OldUsersConverter.ConversionError("Profile not in the conversionlist");
                        } else {
                            Date var1 = var0.length > 1 ? OldUsersConverter.parseDate(var0[1], null) : null;
                            String var2 = var0.length > 2 ? var0[2] : null;
                            Date var3 = var0.length > 3 ? OldUsersConverter.parseDate(var0[3], null) : null;
                            String var4 = var0.length > 4 ? var0[4] : null;
                            var0.add(new UserBanListEntry(param0, var1, var2, var3, var4));
                        }
                    }

                    @Override
                    public void onProfileLookupFailed(GameProfile param0x, Exception param1) {
                        OldUsersConverter.LOGGER.warn("Could not lookup user banlist entry for {}", param0.getName(), param1);
                        if (!(param1 instanceof ProfileNotFoundException)) {
                            throw new OldUsersConverter.ConversionError("Could not request user " + param0.getName() + " from backend systems", param1);
                        }
                    }
                };
                lookupPlayers(param0, var2.keySet(), var3);
                var0.save();
                renameOldFile(OLD_USERBANLIST);
                return true;
            } catch (IOException var4) {
                LOGGER.warn("Could not read old user banlist to convert it!", (Throwable)var4);
                return false;
            } catch (OldUsersConverter.ConversionError var51) {
                LOGGER.error("Conversion failed, please try again later", (Throwable)var51);
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean convertIpBanlist(MinecraftServer param0) {
        IpBanList var0 = new IpBanList(PlayerList.IPBANLIST_FILE);
        if (OLD_IPBANLIST.exists() && OLD_IPBANLIST.isFile()) {
            if (var0.getFile().exists()) {
                try {
                    var0.load();
                } catch (IOException var11) {
                    LOGGER.warn("Could not load existing file {}", var0.getFile().getName(), var11);
                }
            }

            try {
                Map<String, String[]> var2 = Maps.newHashMap();
                readOldListFormat(OLD_IPBANLIST, var2);

                for(String var3 : var2.keySet()) {
                    String[] var4 = var2.get(var3);
                    Date var5 = var4.length > 1 ? parseDate(var4[1], null) : null;
                    String var6 = var4.length > 2 ? var4[2] : null;
                    Date var7 = var4.length > 3 ? parseDate(var4[3], null) : null;
                    String var8 = var4.length > 4 ? var4[4] : null;
                    var0.add(new IpBanListEntry(var3, var5, var6, var7, var8));
                }

                var0.save();
                renameOldFile(OLD_IPBANLIST);
                return true;
            } catch (IOException var10) {
                LOGGER.warn("Could not parse old ip banlist to convert it!", (Throwable)var10);
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean convertOpsList(final MinecraftServer param0) {
        final ServerOpList var0 = new ServerOpList(PlayerList.OPLIST_FILE);
        if (OLD_OPLIST.exists() && OLD_OPLIST.isFile()) {
            if (var0.getFile().exists()) {
                try {
                    var0.load();
                } catch (IOException var6) {
                    LOGGER.warn("Could not load existing file {}", var0.getFile().getName(), var6);
                }
            }

            try {
                List<String> var2 = Files.readLines(OLD_OPLIST, StandardCharsets.UTF_8);
                ProfileLookupCallback var3 = new ProfileLookupCallback() {
                    @Override
                    public void onProfileLookupSucceeded(GameProfile param0x) {
                        param0.getProfileCache().add(param0);
                        var0.add(new ServerOpListEntry(param0, param0.getOperatorUserPermissionLevel(), false));
                    }

                    @Override
                    public void onProfileLookupFailed(GameProfile param0x, Exception param1) {
                        OldUsersConverter.LOGGER.warn("Could not lookup oplist entry for {}", param0.getName(), param1);
                        if (!(param1 instanceof ProfileNotFoundException)) {
                            throw new OldUsersConverter.ConversionError("Could not request user " + param0.getName() + " from backend systems", param1);
                        }
                    }
                };
                lookupPlayers(param0, var2, var3);
                var0.save();
                renameOldFile(OLD_OPLIST);
                return true;
            } catch (IOException var4) {
                LOGGER.warn("Could not read old oplist to convert it!", (Throwable)var4);
                return false;
            } catch (OldUsersConverter.ConversionError var51) {
                LOGGER.error("Conversion failed, please try again later", (Throwable)var51);
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean convertWhiteList(final MinecraftServer param0) {
        final UserWhiteList var0 = new UserWhiteList(PlayerList.WHITELIST_FILE);
        if (OLD_WHITELIST.exists() && OLD_WHITELIST.isFile()) {
            if (var0.getFile().exists()) {
                try {
                    var0.load();
                } catch (IOException var6) {
                    LOGGER.warn("Could not load existing file {}", var0.getFile().getName(), var6);
                }
            }

            try {
                List<String> var2 = Files.readLines(OLD_WHITELIST, StandardCharsets.UTF_8);
                ProfileLookupCallback var3 = new ProfileLookupCallback() {
                    @Override
                    public void onProfileLookupSucceeded(GameProfile param0x) {
                        param0.getProfileCache().add(param0);
                        var0.add(new UserWhiteListEntry(param0));
                    }

                    @Override
                    public void onProfileLookupFailed(GameProfile param0x, Exception param1) {
                        OldUsersConverter.LOGGER.warn("Could not lookup user whitelist entry for {}", param0.getName(), param1);
                        if (!(param1 instanceof ProfileNotFoundException)) {
                            throw new OldUsersConverter.ConversionError("Could not request user " + param0.getName() + " from backend systems", param1);
                        }
                    }
                };
                lookupPlayers(param0, var2, var3);
                var0.save();
                renameOldFile(OLD_WHITELIST);
                return true;
            } catch (IOException var4) {
                LOGGER.warn("Could not read old whitelist to convert it!", (Throwable)var4);
                return false;
            } catch (OldUsersConverter.ConversionError var51) {
                LOGGER.error("Conversion failed, please try again later", (Throwable)var51);
                return false;
            }
        } else {
            return true;
        }
    }

    @Nullable
    public static UUID convertMobOwnerIfNecessary(final MinecraftServer param0, String param1) {
        if (!StringUtil.isNullOrEmpty(param1) && param1.length() <= 16) {
            Optional<UUID> var1 = param0.getProfileCache().get(param1).map(GameProfile::getId);
            if (var1.isPresent()) {
                return var1.get();
            } else if (!param0.isSingleplayer() && param0.usesAuthentication()) {
                final List<GameProfile> var2 = Lists.newArrayList();
                ProfileLookupCallback var3 = new ProfileLookupCallback() {
                    @Override
                    public void onProfileLookupSucceeded(GameProfile param0x) {
                        param0.getProfileCache().add(param0);
                        var2.add(param0);
                    }

                    @Override
                    public void onProfileLookupFailed(GameProfile param0x, Exception param1) {
                        OldUsersConverter.LOGGER.warn("Could not lookup user whitelist entry for {}", param0.getName(), param1);
                    }
                };
                lookupPlayers(param0, Lists.newArrayList(param1), var3);
                return !var2.isEmpty() && var2.get(0).getId() != null ? var2.get(0).getId() : null;
            } else {
                return UUIDUtil.getOrCreatePlayerUUID(new GameProfile(null, param1));
            }
        } else {
            try {
                return UUID.fromString(param1);
            } catch (IllegalArgumentException var5) {
                return null;
            }
        }
    }

    public static boolean convertPlayers(final DedicatedServer param0) {
        final File var0 = getWorldPlayersDirectory(param0);
        final File var1 = new File(var0.getParentFile(), "playerdata");
        final File var2 = new File(var0.getParentFile(), "unknownplayers");
        if (var0.exists() && var0.isDirectory()) {
            File[] var3 = var0.listFiles();
            List<String> var4 = Lists.newArrayList();

            for(File var5 : var3) {
                String var6 = var5.getName();
                if (var6.toLowerCase(Locale.ROOT).endsWith(".dat")) {
                    String var7 = var6.substring(0, var6.length() - ".dat".length());
                    if (!var7.isEmpty()) {
                        var4.add(var7);
                    }
                }
            }

            try {
                final String[] var8 = var4.toArray(new String[var4.size()]);
                ProfileLookupCallback var9 = new ProfileLookupCallback() {
                    @Override
                    public void onProfileLookupSucceeded(GameProfile param0x) {
                        param0.getProfileCache().add(param0);
                        UUID var0 = param0.getId();
                        if (var0 == null) {
                            throw new OldUsersConverter.ConversionError("Missing UUID for user profile " + param0.getName());
                        } else {
                            this.movePlayerFile(var1, this.getFileNameForProfile(param0), var0.toString());
                        }
                    }

                    @Override
                    public void onProfileLookupFailed(GameProfile param0x, Exception param1) {
                        OldUsersConverter.LOGGER.warn("Could not lookup user uuid for {}", param0.getName(), param1);
                        if (param1 instanceof ProfileNotFoundException) {
                            String var0 = this.getFileNameForProfile(param0);
                            this.movePlayerFile(var2, var0, var0);
                        } else {
                            throw new OldUsersConverter.ConversionError("Could not request user " + param0.getName() + " from backend systems", param1);
                        }
                    }

                    private void movePlayerFile(File param0x, String param1, String param2) {
                        File var0 = new File(var0, param1 + ".dat");
                        File var1 = new File(param0, param2 + ".dat");
                        OldUsersConverter.ensureDirectoryExists(param0);
                        if (!var0.renameTo(var1)) {
                            throw new OldUsersConverter.ConversionError("Could not convert file for " + param1);
                        }
                    }

                    private String getFileNameForProfile(GameProfile param0x) {
                        String var0 = null;

                        for(String var1 : var8) {
                            if (var1 != null && var1.equalsIgnoreCase(param0.getName())) {
                                var0 = var1;
                                break;
                            }
                        }

                        if (var0 == null) {
                            throw new OldUsersConverter.ConversionError("Could not find the filename for " + param0.getName() + " anymore");
                        } else {
                            return var0;
                        }
                    }
                };
                lookupPlayers(param0, Lists.newArrayList(var8), var9);
                return true;
            } catch (OldUsersConverter.ConversionError var12) {
                LOGGER.error("Conversion failed, please try again later", (Throwable)var12);
                return false;
            }
        } else {
            return true;
        }
    }

    static void ensureDirectoryExists(File param0) {
        if (param0.exists()) {
            if (!param0.isDirectory()) {
                throw new OldUsersConverter.ConversionError("Can't create directory " + param0.getName() + " in world save directory.");
            }
        } else if (!param0.mkdirs()) {
            throw new OldUsersConverter.ConversionError("Can't create directory " + param0.getName() + " in world save directory.");
        }
    }

    public static boolean serverReadyAfterUserconversion(MinecraftServer param0) {
        boolean var0 = areOldUserlistsRemoved();
        return var0 && areOldPlayersConverted(param0);
    }

    private static boolean areOldUserlistsRemoved() {
        boolean var0 = false;
        if (OLD_USERBANLIST.exists() && OLD_USERBANLIST.isFile()) {
            var0 = true;
        }

        boolean var1 = false;
        if (OLD_IPBANLIST.exists() && OLD_IPBANLIST.isFile()) {
            var1 = true;
        }

        boolean var2 = false;
        if (OLD_OPLIST.exists() && OLD_OPLIST.isFile()) {
            var2 = true;
        }

        boolean var3 = false;
        if (OLD_WHITELIST.exists() && OLD_WHITELIST.isFile()) {
            var3 = true;
        }

        if (!var0 && !var1 && !var2 && !var3) {
            return true;
        } else {
            LOGGER.warn("**** FAILED TO START THE SERVER AFTER ACCOUNT CONVERSION!");
            LOGGER.warn("** please remove the following files and restart the server:");
            if (var0) {
                LOGGER.warn("* {}", OLD_USERBANLIST.getName());
            }

            if (var1) {
                LOGGER.warn("* {}", OLD_IPBANLIST.getName());
            }

            if (var2) {
                LOGGER.warn("* {}", OLD_OPLIST.getName());
            }

            if (var3) {
                LOGGER.warn("* {}", OLD_WHITELIST.getName());
            }

            return false;
        }
    }

    private static boolean areOldPlayersConverted(MinecraftServer param0) {
        File var0 = getWorldPlayersDirectory(param0);
        if (!var0.exists() || !var0.isDirectory() || var0.list().length <= 0 && var0.delete()) {
            return true;
        } else {
            LOGGER.warn("**** DETECTED OLD PLAYER DIRECTORY IN THE WORLD SAVE");
            LOGGER.warn("**** THIS USUALLY HAPPENS WHEN THE AUTOMATIC CONVERSION FAILED IN SOME WAY");
            LOGGER.warn("** please restart the server and if the problem persists, remove the directory '{}'", var0.getPath());
            return false;
        }
    }

    private static File getWorldPlayersDirectory(MinecraftServer param0) {
        return param0.getWorldPath(LevelResource.PLAYER_OLD_DATA_DIR).toFile();
    }

    private static void renameOldFile(File param0) {
        File var0 = new File(param0.getName() + ".converted");
        param0.renameTo(var0);
    }

    static Date parseDate(String param0, Date param1) {
        Date var0;
        try {
            var0 = BanListEntry.DATE_FORMAT.parse(param0);
        } catch (ParseException var4) {
            var0 = param1;
        }

        return var0;
    }

    static class ConversionError extends RuntimeException {
        ConversionError(String param0, Throwable param1) {
            super(param0, param1);
        }

        ConversionError(String param0) {
            super(param0);
        }
    }
}
