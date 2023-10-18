package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsPlayerScreen extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation OPTIONS_BACKGROUND = new ResourceLocation("minecraft", "textures/gui/options_background.png");
    private static final Component QUESTION_TITLE = Component.translatable("mco.question");
    static final Component NORMAL_USER_TOOLTIP = Component.translatable("mco.configure.world.invites.normal.tooltip");
    static final Component OP_TOOLTIP = Component.translatable("mco.configure.world.invites.ops.tooltip");
    static final Component REMOVE_ENTRY_TOOLTIP = Component.translatable("mco.configure.world.invites.remove.tooltip");
    private static final int NO_ENTRY_SELECTED = -1;
    private final RealmsConfigureWorldScreen lastScreen;
    final RealmsServer serverData;
    RealmsPlayerScreen.InvitedObjectSelectionList invitedObjectSelectionList;
    int column1X;
    int columnWidth;
    private Button removeButton;
    private Button opdeopButton;
    int playerIndex = -1;
    private boolean stateChanged;

    public RealmsPlayerScreen(RealmsConfigureWorldScreen param0, RealmsServer param1) {
        super(Component.translatable("mco.configure.world.players.title"));
        this.lastScreen = param0;
        this.serverData = param1;
    }

    @Override
    public void init() {
        this.column1X = this.width / 2 - 160;
        this.columnWidth = 150;
        int var0 = this.width / 2 + 12;
        this.invitedObjectSelectionList = new RealmsPlayerScreen.InvitedObjectSelectionList();
        this.invitedObjectSelectionList.setLeftPos(this.column1X);
        this.addWidget(this.invitedObjectSelectionList);

        for(PlayerInfo var1 : this.serverData.players) {
            this.invitedObjectSelectionList.addEntry(var1);
        }

        this.playerIndex = -1;
        this.addRenderableWidget(
            Button.builder(
                    Component.translatable("mco.configure.world.buttons.invite"),
                    param0 -> this.minecraft.setScreen(new RealmsInviteScreen(this.lastScreen, this, this.serverData))
                )
                .bounds(var0, row(1), this.columnWidth + 10, 20)
                .build()
        );
        this.removeButton = this.addRenderableWidget(
            Button.builder(Component.translatable("mco.configure.world.invites.remove.tooltip"), param0 -> this.uninvite(this.playerIndex))
                .bounds(var0, row(7), this.columnWidth + 10, 20)
                .build()
        );
        this.opdeopButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.invites.ops.tooltip"), param0 -> {
            if (this.serverData.players.get(this.playerIndex).isOperator()) {
                this.deop(this.playerIndex);
            } else {
                this.op(this.playerIndex);
            }

        }).bounds(var0, row(9), this.columnWidth + 10, 20).build());
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_BACK, param0 -> this.backButtonClicked())
                .bounds(var0 + this.columnWidth / 2 + 2, row(12), this.columnWidth / 2 + 10 - 2, 20)
                .build()
        );
        this.updateButtonStates();
    }

    void updateButtonStates() {
        this.removeButton.visible = this.shouldRemoveAndOpdeopButtonBeVisible(this.playerIndex);
        this.opdeopButton.visible = this.shouldRemoveAndOpdeopButtonBeVisible(this.playerIndex);
        this.invitedObjectSelectionList.updateButtons();
    }

    private boolean shouldRemoveAndOpdeopButtonBeVisible(int param0) {
        return param0 != -1;
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.backButtonClicked();
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    private void backButtonClicked() {
        if (this.stateChanged) {
            this.minecraft.setScreen(this.lastScreen.getNewScreen());
        } else {
            this.minecraft.setScreen(this.lastScreen);
        }

    }

    void op(int param0) {
        RealmsClient var0 = RealmsClient.create();
        UUID var1 = this.serverData.players.get(param0).getUuid();

        try {
            this.updateOps(var0.op(this.serverData.id, var1));
        } catch (RealmsServiceException var5) {
            LOGGER.error("Couldn't op the user", (Throwable)var5);
        }

        this.updateButtonStates();
    }

    void deop(int param0) {
        RealmsClient var0 = RealmsClient.create();
        UUID var1 = this.serverData.players.get(param0).getUuid();

        try {
            this.updateOps(var0.deop(this.serverData.id, var1));
        } catch (RealmsServiceException var5) {
            LOGGER.error("Couldn't deop the user", (Throwable)var5);
        }

        this.updateButtonStates();
    }

    private void updateOps(Ops param0) {
        for(PlayerInfo var0 : this.serverData.players) {
            var0.setOperator(param0.ops.contains(var0.getName()));
        }

    }

    void uninvite(int param0) {
        this.updateButtonStates();
        if (param0 >= 0 && param0 < this.serverData.players.size()) {
            PlayerInfo var0 = this.serverData.players.get(param0);
            RealmsConfirmScreen var1 = new RealmsConfirmScreen(param1 -> {
                if (param1) {
                    RealmsClient var0x = RealmsClient.create();

                    try {
                        var0x.uninvite(this.serverData.id, var0.getUuid());
                    } catch (RealmsServiceException var5) {
                        LOGGER.error("Couldn't uninvite user", (Throwable)var5);
                    }

                    this.serverData.players.remove(this.playerIndex);
                    this.playerIndex = -1;
                    this.updateButtonStates();
                }

                this.stateChanged = true;
                this.minecraft.setScreen(this);
            }, QUESTION_TITLE, Component.translatable("mco.configure.world.uninvite.player", var0.getName()));
            this.minecraft.setScreen(var1);
        }

    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.invitedObjectSelectionList.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 17, -1);
        int var0 = row(12) + 20;
        param0.setColor(0.25F, 0.25F, 0.25F, 1.0F);
        param0.blit(OPTIONS_BACKGROUND, 0, var0, 0.0F, 0.0F, this.width, this.height - var0, 32, 32);
        param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        String var1 = this.serverData.players != null ? Integer.toString(this.serverData.players.size()) : "0";
        param0.drawString(this.font, Component.translatable("mco.configure.world.invited.number", var1), this.column1X, row(0), -1, false);
    }

    @OnlyIn(Dist.CLIENT)
    class Entry extends ObjectSelectionList.Entry<RealmsPlayerScreen.Entry> {
        private static final int X_OFFSET = 3;
        private static final int Y_PADDING = 1;
        private static final int BUTTON_WIDTH = 8;
        private static final int BUTTON_HEIGHT = 7;
        private static final WidgetSprites REMOVE_BUTTON_SPRITES = new WidgetSprites(
            new ResourceLocation("player_list/remove_player"), new ResourceLocation("player_list/remove_player_highlighted")
        );
        private static final WidgetSprites MAKE_OP_BUTTON_SPRITES = new WidgetSprites(
            new ResourceLocation("player_list/make_operator"), new ResourceLocation("player_list/make_operator_highlighted")
        );
        private static final WidgetSprites REMOVE_OP_BUTTON_SPRITES = new WidgetSprites(
            new ResourceLocation("player_list/remove_operator"), new ResourceLocation("player_list/remove_operator_highlighted")
        );
        private final PlayerInfo playerInfo;
        private final List<AbstractWidget> children = new ArrayList<>();
        private final ImageButton removeButton;
        private final ImageButton makeOpButton;
        private final ImageButton removeOpButton;

        public Entry(PlayerInfo param0) {
            this.playerInfo = param0;
            int param1 = RealmsPlayerScreen.this.serverData.players.indexOf(this.playerInfo);
            int var0 = RealmsPlayerScreen.this.invitedObjectSelectionList.getRowRight() - 16 - 9;
            int var1 = RealmsPlayerScreen.this.invitedObjectSelectionList.getRowTop(param1) + 1;
            this.removeButton = new ImageButton(
                var0, var1, 8, 7, REMOVE_BUTTON_SPRITES, param1x -> RealmsPlayerScreen.this.uninvite(param1), CommonComponents.EMPTY
            );
            this.removeButton.setTooltip(Tooltip.create(RealmsPlayerScreen.REMOVE_ENTRY_TOOLTIP));
            this.children.add(this.removeButton);
            var0 += 11;
            this.makeOpButton = new ImageButton(var0, var1, 8, 7, MAKE_OP_BUTTON_SPRITES, param1x -> RealmsPlayerScreen.this.op(param1), CommonComponents.EMPTY);
            this.makeOpButton.setTooltip(Tooltip.create(RealmsPlayerScreen.NORMAL_USER_TOOLTIP));
            this.children.add(this.makeOpButton);
            this.removeOpButton = new ImageButton(
                var0, var1, 8, 7, REMOVE_OP_BUTTON_SPRITES, param1x -> RealmsPlayerScreen.this.deop(param1), CommonComponents.EMPTY
            );
            this.removeOpButton.setTooltip(Tooltip.create(RealmsPlayerScreen.OP_TOOLTIP));
            this.children.add(this.removeOpButton);
            this.updateButtons();
        }

        public void updateButtons() {
            this.makeOpButton.visible = !this.playerInfo.isOperator();
            this.removeOpButton.visible = !this.makeOpButton.visible;
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (!this.makeOpButton.mouseClicked(param0, param1, param2)) {
                this.removeOpButton.mouseClicked(param0, param1, param2);
            }

            this.removeButton.mouseClicked(param0, param1, param2);
            return true;
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            int var0;
            if (!this.playerInfo.getAccepted()) {
                var0 = -6250336;
            } else if (this.playerInfo.getOnline()) {
                var0 = 8388479;
            } else {
                var0 = -1;
            }

            RealmsUtil.renderPlayerFace(param0, RealmsPlayerScreen.this.column1X + 2 + 2, param2 + 1, 8, this.playerInfo.getUuid());
            param0.drawString(RealmsPlayerScreen.this.font, this.playerInfo.getName(), RealmsPlayerScreen.this.column1X + 3 + 12, param2 + 1, var0, false);
            this.children.forEach(param5x -> {
                param5x.setY(param2 + 1);
                param5x.render(param0, param6, param7, param9);
            });
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.playerInfo.getName());
        }
    }

    @OnlyIn(Dist.CLIENT)
    class InvitedObjectSelectionList extends RealmsObjectSelectionList<RealmsPlayerScreen.Entry> {
        public InvitedObjectSelectionList() {
            super(RealmsPlayerScreen.this.columnWidth + 10, RealmsPlayerScreen.row(12) + 20, RealmsPlayerScreen.row(1), RealmsPlayerScreen.row(12) + 20, 13);
        }

        public void updateButtons() {
            if (RealmsPlayerScreen.this.playerIndex != -1) {
                this.getEntry(RealmsPlayerScreen.this.playerIndex).updateButtons();
            }

        }

        public void addEntry(PlayerInfo param0) {
            this.addEntry(RealmsPlayerScreen.this.new Entry(param0));
        }

        @Override
        public int getRowWidth() {
            return (int)((double)this.width * 1.0);
        }

        @Override
        public void selectItem(int param0) {
            super.selectItem(param0);
            this.selectInviteListItem(param0);
        }

        public void selectInviteListItem(int param0) {
            RealmsPlayerScreen.this.playerIndex = param0;
            RealmsPlayerScreen.this.updateButtonStates();
        }

        public void setSelected(@Nullable RealmsPlayerScreen.Entry param0) {
            super.setSelected(param0);
            RealmsPlayerScreen.this.playerIndex = this.children().indexOf(param0);
            RealmsPlayerScreen.this.updateButtonStates();
        }

        @Override
        public int getScrollbarPosition() {
            return RealmsPlayerScreen.this.column1X + this.width - 5;
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 13;
        }
    }
}
