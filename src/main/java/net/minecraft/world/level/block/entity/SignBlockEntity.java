package net.minecraft.world.level.block.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SignBlockEntity extends BlockEntity {
    public final Component[] messages = new Component[]{new TextComponent(""), new TextComponent(""), new TextComponent(""), new TextComponent("")};
    private boolean isEditable = true;
    private Player playerWhoMayEdit;
    private final String[] renderMessages = new String[4];
    private DyeColor color = DyeColor.BLACK;

    public SignBlockEntity() {
        super(BlockEntityType.SIGN);
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);

        for(int var0 = 0; var0 < 4; ++var0) {
            String var1 = Component.Serializer.toJson(this.messages[var0]);
            param0.putString("Text" + (var0 + 1), var1);
        }

        param0.putString("Color", this.color.getName());
        return param0;
    }

    @Override
    public void load(CompoundTag param0) {
        this.isEditable = false;
        super.load(param0);
        this.color = DyeColor.byName(param0.getString("Color"), DyeColor.BLACK);

        for(int var0 = 0; var0 < 4; ++var0) {
            String var1 = param0.getString("Text" + (var0 + 1));
            Component var2 = Component.Serializer.fromJson(var1.isEmpty() ? "\"\"" : var1);
            if (this.level instanceof ServerLevel) {
                try {
                    this.messages[var0] = ComponentUtils.updateForEntity(this.createCommandSourceStack(null), var2, null, 0);
                } catch (CommandSyntaxException var6) {
                    this.messages[var0] = var2;
                }
            } else {
                this.messages[var0] = var2;
            }

            this.renderMessages[var0] = null;
        }

    }

    @OnlyIn(Dist.CLIENT)
    public Component getMessage(int param0) {
        return this.messages[param0];
    }

    public void setMessage(int param0, Component param1) {
        this.messages[param0] = param1;
        this.renderMessages[param0] = null;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public String getRenderMessage(int param0, Function<Component, String> param1) {
        if (this.renderMessages[param0] == null && this.messages[param0] != null) {
            this.renderMessages[param0] = param1.apply(this.messages[param0]);
        }

        return this.renderMessages[param0];
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 9, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public boolean isEditable() {
        return this.isEditable;
    }

    @OnlyIn(Dist.CLIENT)
    public void setEditable(boolean param0) {
        this.isEditable = param0;
        if (!param0) {
            this.playerWhoMayEdit = null;
        }

    }

    public void setAllowedPlayerEditor(Player param0) {
        this.playerWhoMayEdit = param0;
    }

    public Player getPlayerWhoMayEdit() {
        return this.playerWhoMayEdit;
    }

    public boolean executeClickCommands(Player param0) {
        for(Component var0 : this.messages) {
            Style var1 = var0 == null ? null : var0.getStyle();
            if (var1 != null && var1.getClickEvent() != null) {
                ClickEvent var2 = var1.getClickEvent();
                if (var2.getAction() == ClickEvent.Action.RUN_COMMAND) {
                    param0.getServer().getCommands().performCommand(this.createCommandSourceStack((ServerPlayer)param0), var2.getValue());
                }
            }
        }

        return true;
    }

    public CommandSourceStack createCommandSourceStack(@Nullable ServerPlayer param0) {
        String var0 = param0 == null ? "Sign" : param0.getName().getString();
        Component var1 = (Component)(param0 == null ? new TextComponent("Sign") : param0.getDisplayName());
        return new CommandSourceStack(
            CommandSource.NULL, Vec3.atCenterOf(this.worldPosition), Vec2.ZERO, (ServerLevel)this.level, 2, var0, var1, this.level.getServer(), param0
        );
    }

    public DyeColor getColor() {
        return this.color;
    }

    public boolean setColor(DyeColor param0) {
        if (param0 != this.getColor()) {
            this.color = param0;
            this.setChanged();
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
            return true;
        } else {
            return false;
        }
    }
}
