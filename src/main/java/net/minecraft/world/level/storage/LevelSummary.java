package net.minecraft.world.level.storage;

import com.mojang.serialization.Lifecycle;
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
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class LevelSummary implements Comparable<LevelSummary> {
    private final LevelSettings settings;
    private final LevelVersion levelVersion;
    private final String levelId;
    private final boolean requiresConversion;
    private final boolean locked;
    private final File icon;
    private final Lifecycle lifecycle;
    @Nullable
    private Component info;

    public LevelSummary(LevelSettings param0, LevelVersion param1, String param2, boolean param3, boolean param4, File param5, Lifecycle param6) {
        this.settings = param0;
        this.levelVersion = param1;
        this.levelId = param2;
        this.locked = param4;
        this.icon = param5;
        this.requiresConversion = param3;
        this.lifecycle = param6;
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

    public boolean isRequiresConversion() {
        return this.requiresConversion;
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

    public boolean markVersionInList() {
        return this.askToOpenWorld()
            || !SharedConstants.getCurrentVersion().isStable() && !this.levelVersion.snapshot()
            || this.shouldBackup()
            || this.isOldCustomizedWorld()
            || this.experimental();
    }

    public boolean askToOpenWorld() {
        return this.levelVersion.minecraftVersion() > SharedConstants.getCurrentVersion().getWorldVersion();
    }

    public boolean isOldCustomizedWorld() {
        return this.settings.worldGenSettings().isOldCustomizedWorld() && this.levelVersion.minecraftVersion() < 1466;
    }

    protected WorldGenSettings worldGenSettings() {
        return this.settings.worldGenSettings();
    }

    public boolean experimental() {
        return this.lifecycle != Lifecycle.stable();
    }

    public boolean shouldBackup() {
        return this.levelVersion.minecraftVersion() < SharedConstants.getCurrentVersion().getWorldVersion();
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
