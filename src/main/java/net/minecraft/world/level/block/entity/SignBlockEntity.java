package net.minecraft.world.level.block.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.FilteredText;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class SignBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_TEXT_LINE_WIDTH = 90;
    private static final int TEXT_LINE_HEIGHT = 10;
    @Nullable
    private UUID playerWhoMayEdit;
    private SignText frontText = this.createDefaultSignText();
    private SignText backText = this.createDefaultSignText();
    private boolean isWaxed;

    public SignBlockEntity(BlockPos param0, BlockState param1) {
        this(BlockEntityType.SIGN, param0, param1);
    }

    public SignBlockEntity(BlockEntityType param0, BlockPos param1, BlockState param2) {
        super(param0, param1, param2);
    }

    protected SignText createDefaultSignText() {
        return new SignText();
    }

    public boolean isFacingFrontText(Player param0) {
        Block var1 = this.getBlockState().getBlock();
        if (var1 instanceof SignBlock var0) {
            Vec3 var1x = var0.getSignHitboxCenterPosition(this.getBlockState());
            double var2 = param0.getX() - ((double)this.getBlockPos().getX() + var1x.x);
            double var3x = param0.getZ() - ((double)this.getBlockPos().getZ() + var1x.z);
            float var4 = var0.getYRotationDegrees(this.getBlockState());
            float var5 = (float)(Mth.atan2(var3x, var2) * 180.0F / (float)Math.PI) - 90.0F;
            return Mth.degreesDifferenceAbs(var4, var5) <= 90.0F;
        } else {
            return false;
        }
    }

    public SignText getText(boolean param0) {
        return param0 ? this.frontText : this.backText;
    }

    public SignText getFrontText() {
        return this.frontText;
    }

    public SignText getBackText() {
        return this.backText;
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
        SignText.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE, this.frontText).resultOrPartial(LOGGER::error).ifPresent(param1 -> param0.put("front_text", param1));
        SignText.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE, this.backText).resultOrPartial(LOGGER::error).ifPresent(param1 -> param0.put("back_text", param1));
        param0.putBoolean("is_waxed", this.isWaxed);
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        if (param0.contains("front_text")) {
            SignText.DIRECT_CODEC
                .parse(NbtOps.INSTANCE, param0.getCompound("front_text"))
                .resultOrPartial(LOGGER::error)
                .ifPresent(param0x -> this.frontText = this.loadLines(param0x));
        }

        if (param0.contains("back_text")) {
            SignText.DIRECT_CODEC
                .parse(NbtOps.INSTANCE, param0.getCompound("back_text"))
                .resultOrPartial(LOGGER::error)
                .ifPresent(param0x -> this.backText = this.loadLines(param0x));
        }

        this.isWaxed = param0.getBoolean("is_waxed");
    }

    private SignText loadLines(SignText param0) {
        for(int var0 = 0; var0 < 4; ++var0) {
            Component var1 = this.loadLine(param0.getMessage(var0, false));
            Component var2 = this.loadLine(param0.getMessage(var0, true));
            param0 = param0.setMessage(var0, var1, var2);
        }

        return param0;
    }

    private Component loadLine(Component param0) {
        Level var3 = this.level;
        if (var3 instanceof ServerLevel var0) {
            try {
                return ComponentUtils.updateForEntity(createCommandSourceStack(null, var0, this.worldPosition), param0, null, 0);
            } catch (CommandSyntaxException var4) {
            }
        }

        return param0;
    }

    public void updateSignText(Player param0, boolean param1, List<FilteredText> param2) {
        if (!this.isWaxed() && param0.getUUID().equals(this.getPlayerWhoMayEdit()) && this.level != null) {
            this.updateText(param2x -> this.setMessages(param0, param2, param2x), param1);
            this.setAllowedPlayerEditor(null);
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        } else {
            LOGGER.warn("Player {} just tried to change non-editable sign", param0.getName().getString());
        }
    }

    public boolean updateText(UnaryOperator<SignText> param0, boolean param1) {
        SignText var0 = this.getText(param1);
        return this.setText(param0.apply(var0), param1);
    }

    private SignText setMessages(Player param0, List<FilteredText> param1, SignText param2) {
        for(int var0 = 0; var0 < param1.size(); ++var0) {
            FilteredText var1 = param1.get(var0);
            Style var2 = param2.getMessage(var0, param0.isTextFilteringEnabled()).getStyle();
            if (param0.isTextFilteringEnabled()) {
                param2 = param2.setMessage(var0, Component.literal(var1.filteredOrEmpty()).setStyle(var2));
            } else {
                param2 = param2.setMessage(var0, Component.literal(var1.raw()).setStyle(var2), Component.literal(var1.filteredOrEmpty()).setStyle(var2));
            }
        }

        return param2;
    }

    public boolean setText(SignText param0, boolean param1) {
        return param1 ? this.setFrontText(param0) : this.setBackText(param0);
    }

    private boolean setBackText(SignText param0) {
        if (param0 != this.backText) {
            this.backText = param0;
            this.markUpdated();
            return true;
        } else {
            return false;
        }
    }

    private boolean setFrontText(SignText param0) {
        if (param0 != this.frontText) {
            this.frontText = param0;
            this.markUpdated();
            return true;
        } else {
            return false;
        }
    }

    public boolean canExecuteClickCommands(boolean param0, Player param1) {
        return this.isWaxed() && this.getText(param0).hasAnyClickCommands(param1);
    }

    public boolean executeClickCommandsIfPresent(Player param0, Level param1, BlockPos param2, boolean param3) {
        boolean var0 = false;

        for(Component var1 : this.getText(param3).getMessages(param0.isTextFilteringEnabled())) {
            Style var2 = var1.getStyle();
            ClickEvent var3 = var2.getClickEvent();
            if (var3 != null && var3.getAction() == ClickEvent.Action.RUN_COMMAND) {
                param0.getServer().getCommands().performPrefixedCommand(createCommandSourceStack(param0, param1, param2), var3.getValue());
                var0 = true;
            }
        }

        return var0;
    }

    private static CommandSourceStack createCommandSourceStack(@Nullable Player param0, Level param1, BlockPos param2) {
        String var0 = param0 == null ? "Sign" : param0.getName().getString();
        Component var1 = (Component)(param0 == null ? Component.literal("Sign") : param0.getDisplayName());
        return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(param2), Vec2.ZERO, (ServerLevel)param1, 2, var0, var1, param1.getServer(), param0);
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

    public void setAllowedPlayerEditor(@Nullable UUID param0) {
        this.playerWhoMayEdit = param0;
    }

    @Nullable
    public UUID getPlayerWhoMayEdit() {
        return this.playerWhoMayEdit;
    }

    private void markUpdated() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public boolean isWaxed() {
        return this.isWaxed;
    }

    public boolean setWaxed(boolean param0) {
        if (this.isWaxed != param0) {
            this.isWaxed = param0;
            this.markUpdated();
            return true;
        } else {
            return false;
        }
    }

    public boolean playerIsTooFarAwayToEdit(UUID param0) {
        Player var0 = this.level.getPlayerByUUID(param0);
        return var0 == null
            || var0.distanceToSqr((double)this.getBlockPos().getX(), (double)this.getBlockPos().getY(), (double)this.getBlockPos().getZ()) > 64.0;
    }

    public static void tick(Level param0, BlockPos param1, BlockState param2, SignBlockEntity param3) {
        UUID var0 = param3.getPlayerWhoMayEdit();
        if (var0 != null) {
            param3.clearInvalidPlayerWhoMayEdit(param3, param0, var0);
        }

    }

    private void clearInvalidPlayerWhoMayEdit(SignBlockEntity param0, Level param1, UUID param2) {
        if (param0.playerIsTooFarAwayToEdit(param2)) {
            param0.setAllowedPlayerEditor(null);
        }

    }

    public SoundEvent getSignInteractionFailedSoundEvent() {
        return SoundEvents.WAXED_SIGN_INTERACT_FAIL;
    }
}
