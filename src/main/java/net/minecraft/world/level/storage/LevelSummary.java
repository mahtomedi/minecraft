package net.minecraft.world.level.storage;

import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
    private final File icon;
    @Nullable
    private Component info;

    public LevelSummary(LevelSettings param0, LevelVersion param1, String param2, boolean param3, boolean param4, File param5) {
        this.settings = param0;
        this.levelVersion = param1;
        this.levelId = param2;
        this.locked = param4;
        this.icon = param5;
        this.requiresManualConversion = param3;
    }

    public String getLevelId() {
        return this.levelId;
    }

    public String getLevelName() {
        return StringUtils.isEmpty(this.settings.levelName()) ? this.levelId : this.settings.levelName();
    }

    public File getIcon() {
        return this.icon;
    }

    public boolean requiresManualConversion() {
        return this.requiresManualConversion;
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
        return (MutableComponent)(StringUtil.isNullOrEmpty(this.levelVersion.minecraftVersionName())
            ? new TranslatableComponent("selectWorld.versionUnknown")
            : new TextComponent(this.levelVersion.minecraftVersionName()));
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

    public boolean isIncompatibleWorldHeight() {
        return this.levelVersion.minecraftVersion().isInExtendedWorldHeightSegment();
    }

    public boolean isDisabled() {
        if (!this.isLocked() && !this.requiresManualConversion()) {
            return !this.isCompatible();
        } else {
            return true;
        }
    }

    public boolean isCompatible() {
        return this.levelVersion.minecraftVersion().isCompatible(SharedConstants.getCurrentVersion().getDataVersion());
    }

    public Component getInfo() {
        if (this.info == null) {
            this.info = this.createInfo();
        }

        return this.info;
    }

    private Component createInfo() {
        if (this.isLocked()) {
            return new TranslatableComponent("selectWorld.locked").withStyle(ChatFormatting.RED);
        } else if (this.requiresManualConversion()) {
            return new TranslatableComponent("selectWorld.conversion").withStyle(ChatFormatting.RED);
        } else if (this.isIncompatibleWorldHeight()) {
            return new TranslatableComponent("selectWorld.pre_worldheight").withStyle(ChatFormatting.RED);
        } else if (!this.levelVersion.minecraftVersion().isSameSeries(SharedConstants.getCurrentVersion().getDataVersion())) {
            return new TranslatableComponent("selectWorld.incompatible_series").withStyle(ChatFormatting.RED);
        } else {
            MutableComponent var0 = (MutableComponent)(this.isHardcore()
                ? new TextComponent("").append(new TranslatableComponent("gameMode.hardcore").withStyle(ChatFormatting.DARK_RED))
                : new TranslatableComponent("gameMode." + this.getGameMode().getName()));
            if (this.hasCheats()) {
                var0.append(", ").append(new TranslatableComponent("selectWorld.cheats"));
            }

            MutableComponent var1 = this.getWorldVersionName();
            MutableComponent var2 = new TextComponent(", ").append(new TranslatableComponent("selectWorld.version")).append(" ");
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
