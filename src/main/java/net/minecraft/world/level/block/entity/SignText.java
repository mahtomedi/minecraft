package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class SignText {
    private static final Codec<Component[]> LINES_CODEC = ExtraCodecs.FLAT_COMPONENT
        .listOf()
        .comapFlatMap(
            param0 -> Util.fixedSize(param0, 4).map(param0x -> new Component[]{param0x.get(0), param0x.get(1), param0x.get(2), param0x.get(3)}),
            param0 -> List.of(param0[0], param0[1], param0[2], param0[3])
        );
    public static final Codec<SignText> DIRECT_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    LINES_CODEC.fieldOf("messages").forGetter(param0x -> param0x.messages),
                    LINES_CODEC.optionalFieldOf("filtered_messages").forGetter(SignText::getOnlyFilteredMessages),
                    DyeColor.CODEC.fieldOf("color").orElse(DyeColor.BLACK).forGetter(param0x -> param0x.color),
                    Codec.BOOL.fieldOf("has_glowing_text").orElse(false).forGetter(param0x -> param0x.hasGlowingText)
                )
                .apply(param0, SignText::load)
    );
    public static final int LINES = 4;
    private final Component[] messages;
    private final Component[] filteredMessages;
    private final DyeColor color;
    private final boolean hasGlowingText;
    @Nullable
    private FormattedCharSequence[] renderMessages;
    private boolean renderMessagedFiltered;

    public SignText() {
        this(emptyMessages(), emptyMessages(), DyeColor.BLACK, false);
    }

    public SignText(Component[] param0, Component[] param1, DyeColor param2, boolean param3) {
        this.messages = param0;
        this.filteredMessages = param1;
        this.color = param2;
        this.hasGlowingText = param3;
    }

    private static Component[] emptyMessages() {
        return new Component[]{CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY};
    }

    private static SignText load(Component[] param0, Optional<Component[]> param1, DyeColor param2, boolean param3) {
        Component[] var0 = param1.orElseGet(SignText::emptyMessages);
        populateFilteredMessagesWithRawMessages(param0, var0);
        return new SignText(param0, var0, param2, param3);
    }

    private static void populateFilteredMessagesWithRawMessages(Component[] param0, Component[] param1) {
        for(int var0 = 0; var0 < 4; ++var0) {
            if (param1[var0].equals(CommonComponents.EMPTY)) {
                param1[var0] = param0[var0];
            }
        }

    }

    public boolean hasGlowingText() {
        return this.hasGlowingText;
    }

    public SignText setHasGlowingText(boolean param0) {
        return param0 == this.hasGlowingText ? this : new SignText(this.messages, this.filteredMessages, this.color, param0);
    }

    public DyeColor getColor() {
        return this.color;
    }

    public SignText setColor(DyeColor param0) {
        return param0 == this.getColor() ? this : new SignText(this.messages, this.filteredMessages, param0, this.hasGlowingText);
    }

    public Component getMessage(int param0, boolean param1) {
        return this.getMessages(param1)[param0];
    }

    public SignText setMessage(int param0, Component param1) {
        return this.setMessage(param0, param1, param1);
    }

    public SignText setMessage(int param0, Component param1, Component param2) {
        Component[] var0 = Arrays.copyOf(this.messages, this.messages.length);
        Component[] var1 = Arrays.copyOf(this.filteredMessages, this.filteredMessages.length);
        var0[param0] = param1;
        var1[param0] = param2;
        return new SignText(var0, var1, this.color, this.hasGlowingText);
    }

    public boolean hasMessage(Player param0) {
        return Arrays.stream(this.getMessages(param0.isTextFilteringEnabled())).anyMatch(param0x -> !param0x.getString().isEmpty());
    }

    private Component[] getMessages(boolean param0) {
        return param0 ? this.filteredMessages : this.messages;
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

    private Optional<Component[]> getOnlyFilteredMessages() {
        Component[] var0 = new Component[4];
        boolean var1 = false;

        for(int var2 = 0; var2 < 4; ++var2) {
            Component var3 = this.filteredMessages[var2];
            if (!var3.equals(this.messages[var2])) {
                var0[var2] = var3;
                var1 = true;
            } else {
                var0[var2] = CommonComponents.EMPTY;
            }
        }

        return var1 ? Optional.of(var0) : Optional.empty();
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

    public boolean executeClickCommandsIfPresent(ServerPlayer param0, ServerLevel param1, BlockPos param2) {
        boolean var0 = false;

        for(Component var1 : this.getMessages(param0.isTextFilteringEnabled())) {
            Style var2 = var1.getStyle();
            ClickEvent var3 = var2.getClickEvent();
            if (var3 != null && var3.getAction() == ClickEvent.Action.RUN_COMMAND) {
                param0.getServer().getCommands().performPrefixedCommand(createCommandSourceStack(param0, param1, param2), var3.getValue());
                var0 = true;
            }
        }

        return var0;
    }

    private static CommandSourceStack createCommandSourceStack(ServerPlayer param0, ServerLevel param1, BlockPos param2) {
        String var0 = param0.getName().getString();
        Component var1 = param0.getDisplayName();
        return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(param2), Vec2.ZERO, param1, 2, var0, var1, param1.getServer(), param0);
    }
}
