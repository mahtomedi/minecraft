package net.minecraft.world.level.block.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class SignBlockEntity extends BlockEntity {
    public static final int LINES = 4;
    private static final int MAX_TEXT_LINE_WIDTH = 90;
    private static final int TEXT_LINE_HEIGHT = 10;
    private static final String[] RAW_TEXT_FIELD_NAMES = new String[]{"Text1", "Text2", "Text3", "Text4"};
    private static final String[] FILTERED_TEXT_FIELD_NAMES = new String[]{"FilteredText1", "FilteredText2", "FilteredText3", "FilteredText4"};
    private final Component[] messages = new Component[]{CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY};
    private final Component[] filteredMessages = new Component[]{
        CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY
    };
    private boolean isEditable = true;
    @Nullable
    private UUID playerWhoMayEdit;
    @Nullable
    private FormattedCharSequence[] renderMessages;
    private boolean renderMessagedFiltered;
    private DyeColor color = DyeColor.BLACK;
    private boolean hasGlowingText;

    public SignBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.SIGN, param0, param1);
    }

    public SignBlockEntity(BlockEntityType param0, BlockPos param1, BlockState param2) {
        super(param0, param1, param2);
    }

    public int getTextLineHeight() {
        return 10;
    }

    public int getMaxTextLineWidth() {
        return 90;
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);

        for(int var0 = 0; var0 < 4; ++var0) {
            Component var1 = this.messages[var0];
            String var2 = Component.Serializer.toJson(var1);
            param0.putString(RAW_TEXT_FIELD_NAMES[var0], var2);
            Component var3 = this.filteredMessages[var0];
            if (!var3.equals(var1)) {
                param0.putString(FILTERED_TEXT_FIELD_NAMES[var0], Component.Serializer.toJson(var3));
            }
        }

        param0.putString("Color", this.color.getName());
        param0.putBoolean("GlowingText", this.hasGlowingText);
    }

    @Override
    public void load(CompoundTag param0) {
        this.isEditable = false;
        super.load(param0);
        this.color = DyeColor.byName(param0.getString("Color"), DyeColor.BLACK);

        for(int var0 = 0; var0 < 4; ++var0) {
            String var1 = param0.getString(RAW_TEXT_FIELD_NAMES[var0]);
            Component var2 = this.loadLine(var1);
            this.messages[var0] = var2;
            String var3 = FILTERED_TEXT_FIELD_NAMES[var0];
            if (param0.contains(var3, 8)) {
                this.filteredMessages[var0] = this.loadLine(param0.getString(var3));
            } else {
                this.filteredMessages[var0] = var2;
            }
        }

        this.renderMessages = null;
        this.hasGlowingText = param0.getBoolean("GlowingText");
    }

    private Component loadLine(String param0) {
        Component var0 = this.deserializeTextSafe(param0);
        if (this.level instanceof ServerLevel) {
            try {
                return ComponentUtils.updateForEntity(this.createCommandSourceStack(null), var0, null, 0);
            } catch (CommandSyntaxException var4) {
            }
        }

        return var0;
    }

    private Component deserializeTextSafe(String param0) {
        try {
            Component var0 = Component.Serializer.fromJson(param0);
            if (var0 != null) {
                return var0;
            }
        } catch (Exception var3) {
        }

        return CommonComponents.EMPTY;
    }

    public Component getMessage(int param0, boolean param1) {
        return this.getMessages(param1)[param0];
    }

    public void setMessage(int param0, Component param1) {
        this.setMessage(param0, param1, param1);
    }

    public void setMessage(int param0, Component param1, Component param2) {
        this.messages[param0] = param1;
        this.filteredMessages[param0] = param2;
        this.renderMessages = null;
    }

    public FormattedCharSequence[] getRenderMessages(boolean param0, Function<Component, FormattedCharSequence> param1) {
        if (this.renderMessages == null || this.renderMessagedFiltered != param0) {
            this.renderMessagedFiltered = param0;
            this.renderMessages = new FormattedCharSequence[4];

            for(int var0 = 0; var0 < 4; ++var0) {
                this.renderMessages[var0] = param1.apply(this.getMessage(var0, param0));
            }
        }

        return this.renderMessages;
    }

    private Component[] getMessages(boolean param0) {
        return param0 ? this.filteredMessages : this.messages;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public boolean isEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean param0) {
        this.isEditable = param0;
        if (!param0) {
            this.playerWhoMayEdit = null;
        }

    }

    public void setAllowedPlayerEditor(UUID param0) {
        this.playerWhoMayEdit = param0;
    }

    @Nullable
    public UUID getPlayerWhoMayEdit() {
        return this.playerWhoMayEdit;
    }

    public boolean hasAnyClickCommands(Player param0) {
        for(Component var0 : this.getMessages(param0.isTextFilteringEnabled())) {
            Style var1 = var0.getStyle();
            ClickEvent var2 = var1.getClickEvent();
            if (var2 != null && var2.getAction() == ClickEvent.Action.RUN_COMMAND) {
                return true;
            }
        }

        return false;
    }

    public boolean executeClickCommands(ServerPlayer param0) {
        for(Component var0 : this.getMessages(param0.isTextFilteringEnabled())) {
            Style var1 = var0.getStyle();
            ClickEvent var2 = var1.getClickEvent();
            if (var2 != null && var2.getAction() == ClickEvent.Action.RUN_COMMAND) {
                param0.getServer().getCommands().performPrefixedCommand(this.createCommandSourceStack(param0), var2.getValue());
            }
        }

        return true;
    }

    public CommandSourceStack createCommandSourceStack(@Nullable ServerPlayer param0) {
        String var0 = param0 == null ? "Sign" : param0.getName().getString();
        Component var1 = (Component)(param0 == null ? Component.literal("Sign") : param0.getDisplayName());
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
            this.markUpdated();
            return true;
        } else {
            return false;
        }
    }

    public boolean hasGlowingText() {
        return this.hasGlowingText;
    }

    public boolean setHasGlowingText(boolean param0) {
        if (this.hasGlowingText != param0) {
            this.hasGlowingText = param0;
            this.markUpdated();
            return true;
        } else {
            return false;
        }
    }

    private void markUpdated() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }
}
