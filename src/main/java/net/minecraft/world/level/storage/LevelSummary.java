package net.minecraft.world.level.storage;

import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import org.apache.commons.lang3.StringUtils;

public class LevelSummary implements Comparable<LevelSummary> {
    public static final Component PLAY_WORLD = Component.translatable("selectWorld.select");
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
        if (this.getLastPlayed() < param0.getLastPlayed()) {
            return 1;
        } else {
            return this.getLastPlayed() > param0.getLastPlayed() ? -1 : this.levelId.compareTo(param0.levelId);
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

    public boolean shouldBackup() {
        return this.backupStatus().shouldBackup();
    }

    public boolean isDowngrade() {
        return this.backupStatus() == LevelSummary.BackupStatus.DOWNGRADE;
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
            return Component.translatable("selectWorld.incompatible.info", this.getWorldVersionName()).withStyle(ChatFormatting.RED);
        } else {
            MutableComponent var0 = this.isHardcore()
                ? Component.empty().append(Component.translatable("gameMode.hardcore").withColor(-65536))
                : Component.translatable("gameMode." + this.getGameMode().getName());
            if (this.hasCheats()) {
                var0.append(", ").append(Component.translatable("selectWorld.cheats"));
            }

            if (this.isExperimental()) {
                var0.append(", ").append(Component.translatable("selectWorld.experimental").withStyle(ChatFormatting.YELLOW));
            }

            MutableComponent var1 = this.getWorldVersionName();
            MutableComponent var2 = Component.literal(", ").append(Component.translatable("selectWorld.version")).append(CommonComponents.SPACE);
            if (this.shouldBackup()) {
                var2.append(var1.withStyle(this.isDowngrade() ? ChatFormatting.RED : ChatFormatting.ITALIC));
            } else {
                var2.append(var1);
            }

            var0.append(var2);
            return var0;
        }
    }

    public Component primaryActionMessage() {
        return PLAY_WORLD;
    }

    public boolean primaryActionActive() {
        return !this.isDisabled();
    }

    public boolean canEdit() {
        return !this.isDisabled();
    }

    public boolean canRecreate() {
        return !this.isDisabled();
    }

    public boolean canDelete() {
        return true;
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

    public static class CorruptedLevelSummary extends LevelSummary {
        private static final Component INFO = Component.translatable("recover_world.warning").withStyle(param0 -> param0.withColor(-65536));
        private static final Component RECOVER = Component.translatable("recover_world.button");
        private final long lastPlayed;

        public CorruptedLevelSummary(String param0, Path param1, long param2) {
            super(null, null, param0, false, false, false, param1);
            this.lastPlayed = param2;
        }

        @Override
        public String getLevelName() {
            return this.getLevelId();
        }

        @Override
        public Component getInfo() {
            return INFO;
        }

        @Override
        public long getLastPlayed() {
            return this.lastPlayed;
        }

        @Override
        public boolean isDisabled() {
            return false;
        }

        @Override
        public Component primaryActionMessage() {
            return RECOVER;
        }

        @Override
        public boolean primaryActionActive() {
            return true;
        }

        @Override
        public boolean canEdit() {
            return false;
        }

        @Override
        public boolean canRecreate() {
            return false;
        }
    }

    public static class SymlinkLevelSummary extends LevelSummary {
        private static final Component MORE_INFO_BUTTON = Component.translatable("symlink_warning.more_info");
        private static final Component INFO = Component.translatable("symlink_warning.title").withColor(-65536);

        public SymlinkLevelSummary(String param0, Path param1) {
            super(null, null, param0, false, false, false, param1);
        }

        @Override
        public String getLevelName() {
            return this.getLevelId();
        }

        @Override
        public Component getInfo() {
            return INFO;
        }

        @Override
        public long getLastPlayed() {
            return -1L;
        }

        @Override
        public boolean isDisabled() {
            return false;
        }

        @Override
        public Component primaryActionMessage() {
            return MORE_INFO_BUTTON;
        }

        @Override
        public boolean primaryActionActive() {
            return true;
        }

        @Override
        public boolean canEdit() {
            return false;
        }

        @Override
        public boolean canRecreate() {
            return false;
        }
    }
}
