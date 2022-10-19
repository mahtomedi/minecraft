package net.minecraft.world.level.storage;

import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import org.apache.commons.lang3.StringUtils;

public class LevelSummary implements Comparable<LevelSummary> {
    private final LevelSettings settings;
    private final LevelVersion levelVersion;
    private final String levelId;
    private final boolean requiresManualConversion;
    private final boolean locked;
    private final boolean experimental;
    private final Path icon;
    @Nullable
    private Component info;

    public LevelSummary(LevelSettings param0, LevelVersion param1, String param2, boolean param3, boolean param4, boolean param5, Path param6) {
        this.settings = param0;
        this.levelVersion = param1;
        this.levelId = param2;
        this.locked = param4;
        this.experimental = param5;
        this.icon = param6;
        this.requiresManualConversion = param3;
    }

    public String getLevelId() {
        return this.levelId;
    }

    public String getLevelName() {
        return StringUtils.isEmpty(this.settings.levelName()) ? this.levelId : this.settings.levelName();
    }

    public Path getIcon() {
        return this.icon;
    }

    public boolean requiresManualConversion() {
        return this.requiresManualConversion;
    }

    public boolean isExperimental() {
        return this.experimental;
    }

    public long getLastPlayed() {
        return this.levelVersion.lastPlayed();
    }

    public int compareTo(LevelSummary param0) {
        if (this.levelVersion.lastPlayed() < param0.levelVersion.lastPlayed()) {
            return 1;
        } else {
            return this.levelVersion.lastPlayed() > param0.levelVersion.lastPlayed() ? -1 : this.levelId.compareTo(param0.levelId);
        }
    }

    public LevelSettings getSettings() {
        return this.settings;
    }

    public GameType getGameMode() {
        return this.settings.gameType();
    }

    public boolean isHardcore() {
        return this.settings.hardcore();
    }

    public boolean hasCheats() {
        return this.settings.allowCommands();
    }

    public MutableComponent getWorldVersionName() {
        return StringUtil.isNullOrEmpty(this.levelVersion.minecraftVersionName())
            ? Component.translatable("selectWorld.versionUnknown")
            : Component.literal(this.levelVersion.minecraftVersionName());
    }

    public LevelVersion levelVersion() {
        return this.levelVersion;
    }

    public boolean markVersionInList() {
        return this.askToOpenWorld() || !SharedConstants.getCurrentVersion().isStable() && !this.levelVersion.snapshot() || this.backupStatus().shouldBackup();
    }

    public boolean askToOpenWorld() {
        return this.levelVersion.minecraftVersion().getVersion() > SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }

    public LevelSummary.BackupStatus backupStatus() {
        WorldVersion var0 = SharedConstants.getCurrentVersion();
        int var1 = var0.getDataVersion().getVersion();
        int var2 = this.levelVersion.minecraftVersion().getVersion();
        if (!var0.isStable() && var2 < var1) {
            return LevelSummary.BackupStatus.UPGRADE_TO_SNAPSHOT;
        } else {
            return var2 > var1 ? LevelSummary.BackupStatus.DOWNGRADE : LevelSummary.BackupStatus.NONE;
        }
    }

    public boolean isLocked() {
        return this.locked;
    }

    public boolean isDisabled() {
        if (!this.isLocked() && !this.requiresManualConversion()) {
            return !this.isCompatible();
        } else {
            return true;
        }
    }

    public boolean isCompatible() {
        return SharedConstants.getCurrentVersion().getDataVersion().isCompatible(this.levelVersion.minecraftVersion());
    }

    public Component getInfo() {
        if (this.info == null) {
            this.info = this.createInfo();
        }

        return this.info;
    }

    private Component createInfo() {
        if (this.isLocked()) {
            return Component.translatable("selectWorld.locked").withStyle(ChatFormatting.RED);
        } else if (this.requiresManualConversion()) {
            return Component.translatable("selectWorld.conversion").withStyle(ChatFormatting.RED);
        } else if (!this.isCompatible()) {
            return Component.translatable("selectWorld.incompatible_series").withStyle(ChatFormatting.RED);
        } else {
            MutableComponent var0 = this.isHardcore()
                ? Component.empty().append(Component.translatable("gameMode.hardcore").withStyle(ChatFormatting.DARK_RED))
                : Component.translatable("gameMode." + this.getGameMode().getName());
            if (this.hasCheats()) {
                var0.append(", ").append(Component.translatable("selectWorld.cheats"));
            }

            if (this.isExperimental()) {
                var0.append(", ").append(Component.translatable("selectWorld.experimental").withStyle(ChatFormatting.YELLOW));
            }

            MutableComponent var1 = this.getWorldVersionName();
            MutableComponent var2 = Component.literal(", ").append(Component.translatable("selectWorld.version")).append(" ");
            if (this.markVersionInList()) {
                var2.append(var1.withStyle(this.askToOpenWorld() ? ChatFormatting.RED : ChatFormatting.ITALIC));
            } else {
                var2.append(var1);
            }

            var0.append(var2);
            return var0;
        }
    }

    public static enum BackupStatus {
        NONE(false, false, ""),
        DOWNGRADE(true, true, "downgrade"),
        UPGRADE_TO_SNAPSHOT(true, false, "snapshot");

        private final boolean shouldBackup;
        private final boolean severe;
        private final String translationKey;

        private BackupStatus(boolean param0, boolean param1, String param2) {
            this.shouldBackup = param0;
            this.severe = param1;
            this.translationKey = param2;
        }

        public boolean shouldBackup() {
            return this.shouldBackup;
        }

        public boolean isSevere() {
            return this.severe;
        }

        public String getTranslationKey() {
            return this.translationKey;
        }
    }
}
