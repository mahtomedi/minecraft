package net.minecraft.world.level.storage;

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
import net.minecraft.world.level.LevelType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LevelSummary implements Comparable<LevelSummary> {
    private final String levelId;
    private final String levelName;
    private final long lastPlayed;
    private final long sizeOnDisk;
    private final boolean requiresConversion;
    private final GameType gameMode;
    private final boolean hardcore;
    private final boolean hasCheats;
    private final String worldVersionName;
    private final int worldVersion;
    private final boolean snapshot;
    private final LevelType generatorType;
    private final boolean locked;
    private final File icon;
    @Nullable
    private Component info;

    public LevelSummary(WorldData param0, String param1, String param2, long param3, boolean param4, boolean param5, File param6) {
        this.levelId = param1;
        this.levelName = param2;
        this.locked = param5;
        this.icon = param6;
        this.lastPlayed = param0.getLastPlayed();
        this.sizeOnDisk = param3;
        this.gameMode = param0.getGameType();
        this.requiresConversion = param4;
        this.hardcore = param0.isHardcore();
        this.hasCheats = param0.getAllowCommands();
        this.worldVersionName = param0.getMinecraftVersionName();
        this.worldVersion = param0.getMinecraftVersion();
        this.snapshot = param0.isSnapshot();
        this.generatorType = param0.overworldData().getGeneratorType();
    }

    public String getLevelId() {
        return this.levelId;
    }

    public String getLevelName() {
        return this.levelName;
    }

    public File getIcon() {
        return this.icon;
    }

    public boolean isRequiresConversion() {
        return this.requiresConversion;
    }

    public long getLastPlayed() {
        return this.lastPlayed;
    }

    public int compareTo(LevelSummary param0) {
        if (this.lastPlayed < param0.lastPlayed) {
            return 1;
        } else {
            return this.lastPlayed > param0.lastPlayed ? -1 : this.levelId.compareTo(param0.levelId);
        }
    }

    public GameType getGameMode() {
        return this.gameMode;
    }

    public boolean isHardcore() {
        return this.hardcore;
    }

    public boolean hasCheats() {
        return this.hasCheats;
    }

    public MutableComponent getWorldVersionName() {
        return (MutableComponent)(StringUtil.isNullOrEmpty(this.worldVersionName)
            ? new TranslatableComponent("selectWorld.versionUnknown")
            : new TextComponent(this.worldVersionName));
    }

    public boolean markVersionInList() {
        return this.askToOpenWorld() || !SharedConstants.getCurrentVersion().isStable() && !this.snapshot || this.shouldBackup() || this.isOldCustomizedWorld();
    }

    public boolean askToOpenWorld() {
        return this.worldVersion > SharedConstants.getCurrentVersion().getWorldVersion();
    }

    public boolean isOldCustomizedWorld() {
        return this.generatorType == LevelType.CUSTOMIZED && this.worldVersion < 1466;
    }

    public boolean shouldBackup() {
        return this.worldVersion < SharedConstants.getCurrentVersion().getWorldVersion();
    }

    public boolean isLocked() {
        return this.locked;
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
}
