package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsTextureManager;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsDefaultVertexFormat;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsPlayerScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private String toolTip;
    private final RealmsConfigureWorldScreen lastScreen;
    private final RealmsServer serverData;
    private RealmsPlayerScreen.InvitedObjectSelectionList invitedObjectSelectionList;
    private int column1_x;
    private int column_width;
    private int column2_x;
    private RealmsButton removeButton;
    private RealmsButton opdeopButton;
    private int selectedInvitedIndex = -1;
    private String selectedInvited;
    private int player = -1;
    private boolean stateChanged;
    private RealmsLabel titleLabel;

    public RealmsPlayerScreen(RealmsConfigureWorldScreen param0, RealmsServer param1) {
        this.lastScreen = param0;
        this.serverData = param1;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void init() {
        this.column1_x = this.width() / 2 - 160;
        this.column_width = 150;
        this.column2_x = this.width() / 2 + 12;
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.buttonsAdd(
            new RealmsButton(1, this.column2_x, RealmsConstants.row(1), this.column_width + 10, 20, getLocalizedString("mco.configure.world.buttons.invite")) {
                @Override
                public void onPress() {
                    Realms.setScreen(new RealmsInviteScreen(RealmsPlayerScreen.this.lastScreen, RealmsPlayerScreen.this, RealmsPlayerScreen.this.serverData));
                }
            }
        );
        this.buttonsAdd(
            this.removeButton = new RealmsButton(
                4, this.column2_x, RealmsConstants.row(7), this.column_width + 10, 20, getLocalizedString("mco.configure.world.invites.remove.tooltip")
            ) {
                @Override
                public void onPress() {
                    RealmsPlayerScreen.this.uninvite(RealmsPlayerScreen.this.player);
                }
            }
        );
        this.buttonsAdd(
            this.opdeopButton = new RealmsButton(
                5, this.column2_x, RealmsConstants.row(9), this.column_width + 10, 20, getLocalizedString("mco.configure.world.invites.ops.tooltip")
            ) {
                @Override
                public void onPress() {
                    if (RealmsPlayerScreen.this.serverData.players.get(RealmsPlayerScreen.this.player).isOperator()) {
                        RealmsPlayerScreen.this.deop(RealmsPlayerScreen.this.player);
                    } else {
                        RealmsPlayerScreen.this.op(RealmsPlayerScreen.this.player);
                    }
    
                }
            }
        );
        this.buttonsAdd(
            new RealmsButton(
                0, this.column2_x + this.column_width / 2 + 2, RealmsConstants.row(12), this.column_width / 2 + 10 - 2, 20, getLocalizedString("gui.back")
            ) {
                @Override
                public void onPress() {
                    RealmsPlayerScreen.this.backButtonClicked();
                }
            }
        );
        this.invitedObjectSelectionList = new RealmsPlayerScreen.InvitedObjectSelectionList();
        this.invitedObjectSelectionList.setLeftPos(this.column1_x);
        this.addWidget(this.invitedObjectSelectionList);

        for(PlayerInfo var0 : this.serverData.players) {
            this.invitedObjectSelectionList.addEntry(var0);
        }

        this.addWidget(this.titleLabel = new RealmsLabel(getLocalizedString("mco.configure.world.players.title"), this.width() / 2, 17, 16777215));
        this.narrateLabels();
        this.updateButtonStates();
    }

    private void updateButtonStates() {
        this.removeButton.setVisible(this.shouldRemoveAndOpdeopButtonBeVisible(this.player));
        this.opdeopButton.setVisible(this.shouldRemoveAndOpdeopButtonBeVisible(this.player));
    }

    private boolean shouldRemoveAndOpdeopButtonBeVisible(int param0) {
        return param0 != -1;
    }

    @Override
    public void removed() {
        this.setKeyboardHandlerSendRepeatsToGui(false);
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
            Realms.setScreen(this.lastScreen.getNewScreen());
        } else {
            Realms.setScreen(this.lastScreen);
        }

    }

    private void op(int param0) {
        this.updateButtonStates();
        RealmsClient var0 = RealmsClient.createRealmsClient();
        String var1 = this.serverData.players.get(param0).getUuid();

        try {
            this.updateOps(var0.op(this.serverData.id, var1));
        } catch (RealmsServiceException var5) {
            LOGGER.error("Couldn't op the user");
        }

    }

    private void deop(int param0) {
        this.updateButtonStates();
        RealmsClient var0 = RealmsClient.createRealmsClient();
        String var1 = this.serverData.players.get(param0).getUuid();

        try {
            this.updateOps(var0.deop(this.serverData.id, var1));
        } catch (RealmsServiceException var5) {
            LOGGER.error("Couldn't deop the user");
        }

    }

    private void updateOps(Ops param0) {
        for(PlayerInfo var0 : this.serverData.players) {
            var0.setOperator(param0.ops.contains(var0.getName()));
        }

    }

    private void uninvite(int param0) {
        this.updateButtonStates();
        if (param0 >= 0 && param0 < this.serverData.players.size()) {
            PlayerInfo var0 = this.serverData.players.get(param0);
            this.selectedInvited = var0.getUuid();
            this.selectedInvitedIndex = param0;
            RealmsConfirmScreen var1 = new RealmsConfirmScreen(
                this, "Question", getLocalizedString("mco.configure.world.uninvite.question") + " '" + var0.getName() + "' ?", 2
            );
            Realms.setScreen(var1);
        }

    }

    @Override
    public void confirmResult(boolean param0, int param1) {
        if (param1 == 2) {
            if (param0) {
                RealmsClient var0 = RealmsClient.createRealmsClient();

                try {
                    var0.uninvite(this.serverData.id, this.selectedInvited);
                } catch (RealmsServiceException var5) {
                    LOGGER.error("Couldn't uninvite user");
                }

                this.deleteFromInvitedList(this.selectedInvitedIndex);
                this.player = -1;
                this.updateButtonStates();
            }

            this.stateChanged = true;
            Realms.setScreen(this);
        }

    }

    private void deleteFromInvitedList(int param0) {
        this.serverData.players.remove(param0);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.toolTip = null;
        this.renderBackground();
        if (this.invitedObjectSelectionList != null) {
            this.invitedObjectSelectionList.render(param0, param1, param2);
        }

        int var0 = RealmsConstants.row(12) + 20;
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        Tezzelator var1 = Tezzelator.instance;
        bind("textures/gui/options_background.png");
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var2 = 32.0F;
        var1.begin(7, RealmsDefaultVertexFormat.POSITION_TEX_COLOR);
        var1.vertex(0.0, (double)this.height(), 0.0).tex(0.0, (double)((float)(this.height() - var0) / 32.0F + 0.0F)).color(64, 64, 64, 255).endVertex();
        var1.vertex((double)this.width(), (double)this.height(), 0.0)
            .tex((double)((float)this.width() / 32.0F), (double)((float)(this.height() - var0) / 32.0F + 0.0F))
            .color(64, 64, 64, 255)
            .endVertex();
        var1.vertex((double)this.width(), (double)var0, 0.0).tex((double)((float)this.width() / 32.0F), 0.0).color(64, 64, 64, 255).endVertex();
        var1.vertex(0.0, (double)var0, 0.0).tex(0.0, 0.0).color(64, 64, 64, 255).endVertex();
        var1.end();
        this.titleLabel.render(this);
        if (this.serverData != null && this.serverData.players != null) {
            this.drawString(
                getLocalizedString("mco.configure.world.invited") + " (" + this.serverData.players.size() + ")",
                this.column1_x,
                RealmsConstants.row(0),
                10526880
            );
        } else {
            this.drawString(getLocalizedString("mco.configure.world.invited"), this.column1_x, RealmsConstants.row(0), 10526880);
        }

        super.render(param0, param1, param2);
        if (this.serverData != null) {
            if (this.toolTip != null) {
                this.renderMousehoverTooltip(this.toolTip, param0, param1);
            }

        }
    }

    protected void renderMousehoverTooltip(String param0, int param1, int param2) {
        if (param0 != null) {
            int var0 = param1 + 12;
            int var1 = param2 - 12;
            int var2 = this.fontWidth(param0);
            this.fillGradient(var0 - 3, var1 - 3, var0 + var2 + 3, var1 + 8 + 3, -1073741824, -1073741824);
            this.fontDrawShadow(param0, var0, var1, 16777215);
        }
    }

    private void drawRemoveIcon(int param0, int param1, int param2, int param3) {
        boolean var0 = param2 >= param0
            && param2 <= param0 + 9
            && param3 >= param1
            && param3 <= param1 + 9
            && param3 < RealmsConstants.row(12) + 20
            && param3 > RealmsConstants.row(1);
        bind("realms:textures/gui/realms/cross_player_icon.png");
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        RealmsScreen.blit(param0, param1, 0.0F, var0 ? 7.0F : 0.0F, 8, 7, 8, 14);
        GlStateManager.popMatrix();
        if (var0) {
            this.toolTip = getLocalizedString("mco.configure.world.invites.remove.tooltip");
        }

    }

    private void drawOpped(int param0, int param1, int param2, int param3) {
        boolean var0 = param2 >= param0
            && param2 <= param0 + 9
            && param3 >= param1
            && param3 <= param1 + 9
            && param3 < RealmsConstants.row(12) + 20
            && param3 > RealmsConstants.row(1);
        bind("realms:textures/gui/realms/op_icon.png");
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        RealmsScreen.blit(param0, param1, 0.0F, var0 ? 8.0F : 0.0F, 8, 8, 8, 16);
        GlStateManager.popMatrix();
        if (var0) {
            this.toolTip = getLocalizedString("mco.configure.world.invites.ops.tooltip");
        }

    }

    private void drawNormal(int param0, int param1, int param2, int param3) {
        boolean var0 = param2 >= param0
            && param2 <= param0 + 9
            && param3 >= param1
            && param3 <= param1 + 9
            && param3 < RealmsConstants.row(12) + 20
            && param3 > RealmsConstants.row(1);
        bind("realms:textures/gui/realms/user_icon.png");
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        RealmsScreen.blit(param0, param1, 0.0F, var0 ? 8.0F : 0.0F, 8, 8, 8, 16);
        GlStateManager.popMatrix();
        if (var0) {
            this.toolTip = getLocalizedString("mco.configure.world.invites.normal.tooltip");
        }

    }

    @OnlyIn(Dist.CLIENT)
    class InvitedObjectSelectionList extends RealmsObjectSelectionList {
        public InvitedObjectSelectionList() {
            super(RealmsPlayerScreen.this.column_width + 10, RealmsConstants.row(12) + 20, RealmsConstants.row(1), RealmsConstants.row(12) + 20, 13);
        }

        public void addEntry(PlayerInfo param0) {
            this.addEntry(RealmsPlayerScreen.this.new InvitedObjectSelectionListEntry(param0));
        }

        @Override
        public int getRowWidth() {
            return (int)((double)this.width() * 1.0);
        }

        @Override
        public boolean isFocused() {
            return RealmsPlayerScreen.this.isFocused(this);
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (param2 == 0 && param0 < (double)this.getScrollbarPosition() && param1 >= (double)this.y0() && param1 <= (double)this.y1()) {
                int var0 = RealmsPlayerScreen.this.column1_x;
                int var1 = RealmsPlayerScreen.this.column1_x + RealmsPlayerScreen.this.column_width;
                int var2 = (int)Math.floor(param1 - (double)this.y0()) - this.headerHeight() + this.getScroll() - 4;
                int var3 = var2 / this.itemHeight();
                if (param0 >= (double)var0 && param0 <= (double)var1 && var3 >= 0 && var2 >= 0 && var3 < this.getItemCount()) {
                    this.selectItem(var3);
                    this.itemClicked(var2, var3, param0, param1, this.width());
                }

                return true;
            } else {
                return super.mouseClicked(param0, param1, param2);
            }
        }

        @Override
        public void itemClicked(int param0, int param1, double param2, double param3, int param4) {
            if (param1 >= 0 && param1 <= RealmsPlayerScreen.this.serverData.players.size() && RealmsPlayerScreen.this.toolTip != null) {
                if (!RealmsPlayerScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.configure.world.invites.ops.tooltip"))
                    && !RealmsPlayerScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.configure.world.invites.normal.tooltip"))) {
                    if (RealmsPlayerScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.configure.world.invites.remove.tooltip"))) {
                        RealmsPlayerScreen.this.uninvite(param1);
                    }
                } else if (RealmsPlayerScreen.this.serverData.players.get(param1).isOperator()) {
                    RealmsPlayerScreen.this.deop(param1);
                } else {
                    RealmsPlayerScreen.this.op(param1);
                }

            }
        }

        @Override
        public void selectItem(int param0) {
            this.setSelected(param0);
            if (param0 != -1) {
                Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", RealmsPlayerScreen.this.serverData.players.get(param0).getName()));
            }

            this.selectInviteListItem(param0);
        }

        public void selectInviteListItem(int param0) {
            RealmsPlayerScreen.this.player = param0;
            RealmsPlayerScreen.this.updateButtonStates();
        }

        @Override
        public void renderBackground() {
            RealmsPlayerScreen.this.renderBackground();
        }

        @Override
        public int getScrollbarPosition() {
            return RealmsPlayerScreen.this.column1_x + this.width() - 5;
        }

        @Override
        public int getItemCount() {
            return RealmsPlayerScreen.this.serverData == null ? 1 : RealmsPlayerScreen.this.serverData.players.size();
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 13;
        }
    }

    @OnlyIn(Dist.CLIENT)
    class InvitedObjectSelectionListEntry extends RealmListEntry {
        final PlayerInfo mPlayerInfo;

        public InvitedObjectSelectionListEntry(PlayerInfo param0) {
            this.mPlayerInfo = param0;
        }

        @Override
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            this.renderInvitedItem(this.mPlayerInfo, param2, param1, param5, param6);
        }

        private void renderInvitedItem(PlayerInfo param0, int param1, int param2, int param3, int param4) {
            int var0;
            if (!param0.getAccepted()) {
                var0 = 10526880;
            } else if (param0.getOnline()) {
                var0 = 8388479;
            } else {
                var0 = 16777215;
            }

            RealmsPlayerScreen.this.drawString(param0.getName(), RealmsPlayerScreen.this.column1_x + 3 + 12, param2 + 1, var0);
            if (param0.isOperator()) {
                RealmsPlayerScreen.this.drawOpped(RealmsPlayerScreen.this.column1_x + RealmsPlayerScreen.this.column_width - 10, param2 + 1, param3, param4);
            } else {
                RealmsPlayerScreen.this.drawNormal(RealmsPlayerScreen.this.column1_x + RealmsPlayerScreen.this.column_width - 10, param2 + 1, param3, param4);
            }

            RealmsPlayerScreen.this.drawRemoveIcon(RealmsPlayerScreen.this.column1_x + RealmsPlayerScreen.this.column_width - 22, param2 + 2, param3, param4);
            RealmsPlayerScreen.this.drawString(
                RealmsScreen.getLocalizedString("mco.configure.world.activityfeed.disabled"),
                RealmsPlayerScreen.this.column2_x,
                RealmsConstants.row(5),
                10526880
            );
            RealmsTextureManager.withBoundFace(param0.getUuid(), () -> {
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RealmsScreen.blit(RealmsPlayerScreen.this.column1_x + 2 + 2, param2 + 1, 8.0F, 8.0F, 8, 8, 8, 8, 64, 64);
                RealmsScreen.blit(RealmsPlayerScreen.this.column1_x + 2 + 2, param2 + 1, 40.0F, 8.0F, 8, 8, 8, 8, 64, 64);
            });
        }
    }
}
