package net.minecraft.world.level.storage;

import com.mojang.bridge.game.GameVersion;
import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

public class LevelSummary implements Comparable<LevelSummary> {
    private final LevelSettings settings;
    private final LevelVersion levelVersion;
    private final String levelId;
    private final boolean requiresConversion;
    private final boolean locked;
    private final File icon;
    @Nullable
    @OnlyIn(Dist.CLIENT)
    private Component info;

    public LevelSummary(LevelSettings param0, LevelVersion param1, String param2, boolean param3, boolean param4, File param5) {
        this.settings = param0;
        this.levelVersion = param1;
        this.levelId = param2;
        this.locked = param4;
        this.icon = param5;
        this.requiresConversion = param3;
    }

    @OnlyIn(Dist.CLIENT)
    public String getLevelId() {
        return this.levelId;
    }

    @OnlyIn(Dist.CLIENT)
    public String getLevelName() {
        return StringUtils.isEmpty(this.settings.levelName()) ? this.levelId : this.settings.levelName();
    }

    @OnlyIn(Dist.CLIENT)
    public File getIcon() {
        return this.icon;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isRequiresConversion() {
        return this.requiresConversion;
    }

    @OnlyIn(Dist.CLIENT)
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

    @OnlyIn(Dist.CLIENT)
    public GameType getGameMode() {
        return this.settings.gameType();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isHardcore() {
        return this.settings.hardcore();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasCheats() {
        return this.settings.allowCommands();
    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getWorldVersionName() {
        return (MutableComponent)(StringUtil.isNullOrEmpty(this.levelVersion.minecraftVersionName())
            ? new TranslatableComponent("selectWorld.versionUnknown")
            : new TextComponent(this.levelVersion.minecraftVersionName()));
    }

    public LevelVersion levelVersion() {
        return this.levelVersion;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean markVersionInList() {
        return this.askToOpenWorld() || !SharedConstants.getCurrentVersion().isStable() && !this.levelVersion.snapshot() || this.backupStatus().shouldBackup();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean askToOpenWorld() {
        return this.levelVersion.minecraftVersion() > SharedConstants.getCurrentVersion().getWorldVersion();
    }

    @OnlyIn(Dist.CLIENT)
    public LevelSummary.BackupStatus backupStatus() {
        GameVersion var0 = SharedConstants.getCurrentVersion();
        int var1 = var0.getWorldVersion();
        int var2 = this.levelVersion.minecraftVersion();
        if (!var0.isStable() && var2 < var1) {
            return LevelSummary.BackupStatus.UPGRADE_TO_SNAPSHOT;
        } else {
            return var2 > var1 ? LevelSummary.BackupStatus.DOWNGRADE : LevelSummary.BackupStatus.NONE;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isLocked() {
        return this.locked;
    }

    @OnlyIn(Dist.CLIENT)
    public Component getInfo() {
        if (this.info == null) {
            this.info = this.createInfo();
        }

        return this.info;
    }

    @OnlyIn(Dist.CLIENT)
    private Component createInfo() {
        if (this.isLocked()) {
            return new TranslatableComponent("selectWorld.locked").withStyle(ChatFormatting.RED);
        } else if (this.isRequiresConversion()) {
            return new TranslatableComponent("selectWorld.conversion");
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

    @OnlyIn(Dist.CLIENT)
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
