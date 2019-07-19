package net.minecraft.world.level;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class BaseCommandBlock implements CommandSource {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private long lastExecution = -1L;
    private boolean updateLastExecution = true;
    private int successCount;
    private boolean trackOutput = true;
    private Component lastOutput;
    private String command = "";
    private Component name = new TextComponent("@");

    public int getSuccessCount() {
        return this.successCount;
    }

    public void setSuccessCount(int param0) {
        this.successCount = param0;
    }

    public Component getLastOutput() {
        return (Component)(this.lastOutput == null ? new TextComponent("") : this.lastOutput);
    }

    public CompoundTag save(CompoundTag param0) {
        param0.putString("Command", this.command);
        param0.putInt("SuccessCount", this.successCount);
        param0.putString("CustomName", Component.Serializer.toJson(this.name));
        param0.putBoolean("TrackOutput", this.trackOutput);
        if (this.lastOutput != null && this.trackOutput) {
            param0.putString("LastOutput", Component.Serializer.toJson(this.lastOutput));
        }

        param0.putBoolean("UpdateLastExecution", this.updateLastExecution);
        if (this.updateLastExecution && this.lastExecution > 0L) {
            param0.putLong("LastExecution", this.lastExecution);
        }

        return param0;
    }

    public void load(CompoundTag param0) {
        this.command = param0.getString("Command");
        this.successCount = param0.getInt("SuccessCount");
        if (param0.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(param0.getString("CustomName"));
        }

        if (param0.contains("TrackOutput", 1)) {
            this.trackOutput = param0.getBoolean("TrackOutput");
        }

        if (param0.contains("LastOutput", 8) && this.trackOutput) {
            try {
                this.lastOutput = Component.Serializer.fromJson(param0.getString("LastOutput"));
            } catch (Throwable var3) {
                this.lastOutput = new TextComponent(var3.getMessage());
            }
        } else {
            this.lastOutput = null;
        }

        if (param0.contains("UpdateLastExecution")) {
            this.updateLastExecution = param0.getBoolean("UpdateLastExecution");
        }

        if (this.updateLastExecution && param0.contains("LastExecution")) {
            this.lastExecution = param0.getLong("LastExecution");
        } else {
            this.lastExecution = -1L;
        }

    }

    public void setCommand(String param0) {
        this.command = param0;
        this.successCount = 0;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean performCommand(Level param0) {
        if (param0.isClientSide || param0.getGameTime() == this.lastExecution) {
            return false;
        } else if ("Searge".equalsIgnoreCase(this.command)) {
            this.lastOutput = new TextComponent("#itzlipofutzli");
            this.successCount = 1;
            return true;
        } else {
            this.successCount = 0;
            MinecraftServer var0 = this.getLevel().getServer();
            if (var0 != null && var0.isInitialized() && var0.isCommandBlockEnabled() && !StringUtil.isNullOrEmpty(this.command)) {
                try {
                    this.lastOutput = null;
                    CommandSourceStack var1 = this.createCommandSourceStack().withCallback((param0x, param1, param2) -> {
                        if (param1) {
                            ++this.successCount;
                        }

                    });
                    var0.getCommands().performCommand(var1, this.command);
                } catch (Throwable var6) {
                    CrashReport var3 = CrashReport.forThrowable(var6, "Executing command block");
                    CrashReportCategory var4 = var3.addCategory("Command to be executed");
                    var4.setDetail("Command", this::getCommand);
                    var4.setDetail("Name", () -> this.getName().getString());
                    throw new ReportedException(var3);
                }
            }

            if (this.updateLastExecution) {
                this.lastExecution = param0.getGameTime();
            } else {
                this.lastExecution = -1L;
            }

            return true;
        }
    }

    public Component getName() {
        return this.name;
    }

    public void setName(Component param0) {
        this.name = param0;
    }

    @Override
    public void sendMessage(Component param0) {
        if (this.trackOutput) {
            this.lastOutput = new TextComponent("[" + TIME_FORMAT.format(new Date()) + "] ").append(param0);
            this.onUpdated();
        }

    }

    public abstract ServerLevel getLevel();

    public abstract void onUpdated();

    public void setLastOutput(@Nullable Component param0) {
        this.lastOutput = param0;
    }

    public void setTrackOutput(boolean param0) {
        this.trackOutput = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isTrackOutput() {
        return this.trackOutput;
    }

    public boolean usedBy(Player param0) {
        if (!param0.canUseGameMasterBlocks()) {
            return false;
        } else {
            if (param0.getCommandSenderWorld().isClientSide) {
                param0.openMinecartCommandBlock(this);
            }

            return true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public abstract Vec3 getPosition();

    public abstract CommandSourceStack createCommandSourceStack();

    @Override
    public boolean acceptsSuccess() {
        return this.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK) && this.trackOutput;
    }

    @Override
    public boolean acceptsFailure() {
        return this.trackOutput;
    }

    @Override
    public boolean shouldInformAdmins() {
        return this.getLevel().getGameRules().getBoolean(GameRules.RULE_COMMANDBLOCKOUTPUT);
    }
}
