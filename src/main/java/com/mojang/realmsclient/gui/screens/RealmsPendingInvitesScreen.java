package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RowButton;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsPendingInvitesScreen extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    static final ResourceLocation ACCEPT_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/accept_icon.png");
    static final ResourceLocation REJECT_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/reject_icon.png");
    private static final Component NO_PENDING_INVITES_TEXT = Component.translatable("mco.invites.nopending");
    static final Component ACCEPT_INVITE_TOOLTIP = Component.translatable("mco.invites.button.accept");
    static final Component REJECT_INVITE_TOOLTIP = Component.translatable("mco.invites.button.reject");
    private final Screen lastScreen;
    @Nullable
    Component toolTip;
    boolean loaded;
    RealmsPendingInvitesScreen.PendingInvitationSelectionList pendingInvitationSelectionList;
    int selectedInvite = -1;
    private Button acceptButton;
    private Button rejectButton;

    public RealmsPendingInvitesScreen(Screen param0, Component param1) {
        super(param1);
        this.lastScreen = param0;
    }

    @Override
    public void init() {
        this.pendingInvitationSelectionList = new RealmsPendingInvitesScreen.PendingInvitationSelectionList();
        (new Thread("Realms-pending-invitations-fetcher") {
                @Override
                public void run() {
                    RealmsClient var0 = RealmsClient.create();
    
                    try {
                        List<PendingInvite> var1 = var0.pendingInvites().pendingInvites;
                        List<RealmsPendingInvitesScreen.Entry> var2 = var1.stream()
                            .map(param0 -> RealmsPendingInvitesScreen.this.new Entry(param0))
                            .collect(Collectors.toList());
                        RealmsPendingInvitesScreen.this.minecraft
                            .execute(() -> RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.replaceEntries(var2));
                    } catch (RealmsServiceException var7) {
                        RealmsPendingInvitesScreen.LOGGER.error("Couldn't list invites");
                    } finally {
                        RealmsPendingInvitesScreen.this.loaded = true;
                    }
    
                }
            })
            .start();
        this.addWidget(this.pendingInvitationSelectionList);
        this.acceptButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.invites.button.accept"), param0 -> {
            this.accept(this.selectedInvite);
            this.selectedInvite = -1;
            this.updateButtonStates();
        }).bounds(this.width / 2 - 174, this.height - 32, 100, 20).build());
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, param0 -> this.minecraft.setScreen(new RealmsMainScreen(this.lastScreen)))
                .bounds(this.width / 2 - 50, this.height - 32, 100, 20)
                .build()
        );
        this.rejectButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.invites.button.reject"), param0 -> {
            this.reject(this.selectedInvite);
            this.selectedInvite = -1;
            this.updateButtonStates();
        }).bounds(this.width / 2 + 74, this.height - 32, 100, 20).build());
        this.updateButtonStates();
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.minecraft.setScreen(new RealmsMainScreen(this.lastScreen));
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    void updateList(int param0) {
        this.pendingInvitationSelectionList.removeAtIndex(param0);
    }

    void reject(final int param0) {
        if (param0 < this.pendingInvitationSelectionList.getItemCount()) {
            (new Thread("Realms-reject-invitation") {
                @Override
                public void run() {
                    try {
                        RealmsClient var0 = RealmsClient.create();
                        var0.rejectInvitation(RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.children().get(param0).pendingInvite.invitationId);
                        RealmsPendingInvitesScreen.this.minecraft.execute(() -> RealmsPendingInvitesScreen.this.updateList(param0));
                    } catch (RealmsServiceException var2) {
                        RealmsPendingInvitesScreen.LOGGER.error("Couldn't reject invite");
                    }

                }
            }).start();
        }

    }

    void accept(final int param0) {
        if (param0 < this.pendingInvitationSelectionList.getItemCount()) {
            (new Thread("Realms-accept-invitation") {
                @Override
                public void run() {
                    try {
                        RealmsClient var0 = RealmsClient.create();
                        var0.acceptInvitation(RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.children().get(param0).pendingInvite.invitationId);
                        RealmsPendingInvitesScreen.this.minecraft.execute(() -> RealmsPendingInvitesScreen.this.updateList(param0));
                    } catch (RealmsServiceException var2) {
                        RealmsPendingInvitesScreen.LOGGER.error("Couldn't accept invite");
                    }

                }
            }).start();
        }

    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.toolTip = null;
        this.renderBackground(param0);
        this.pendingInvitationSelectionList.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 12, 16777215);
        if (this.toolTip != null) {
            this.renderMousehoverTooltip(param0, this.toolTip, param1, param2);
        }

        if (this.pendingInvitationSelectionList.getItemCount() == 0 && this.loaded) {
            param0.drawCenteredString(this.font, NO_PENDING_INVITES_TEXT, this.width / 2, this.height / 2 - 20, 16777215);
        }

        super.render(param0, param1, param2, param3);
    }

    protected void renderMousehoverTooltip(GuiGraphics param0, @Nullable Component param1, int param2, int param3) {
        if (param1 != null) {
            int var0 = param2 + 12;
            int var1 = param3 - 12;
            int var2 = this.font.width(param1);
            param0.fillGradient(var0 - 3, var1 - 3, var0 + var2 + 3, var1 + 8 + 3, -1073741824, -1073741824);
            param0.drawString(this.font, param1, var0, var1, 16777215);
        }
    }

    void updateButtonStates() {
        this.acceptButton.visible = this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite);
        this.rejectButton.visible = this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite);
    }

    private boolean shouldAcceptAndRejectButtonBeVisible(int param0) {
        return param0 != -1;
    }

    @OnlyIn(Dist.CLIENT)
    class Entry extends ObjectSelectionList.Entry<RealmsPendingInvitesScreen.Entry> {
        private static final int TEXT_LEFT = 38;
        final PendingInvite pendingInvite;
        private final List<RowButton> rowButtons;

        Entry(PendingInvite param0) {
            this.pendingInvite = param0;
            this.rowButtons = Arrays.asList(new RealmsPendingInvitesScreen.Entry.AcceptRowButton(), new RealmsPendingInvitesScreen.Entry.RejectRowButton());
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            this.renderPendingInvitationItem(param0, this.pendingInvite, param3, param2, param6, param7);
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            RowButton.rowButtonMouseClicked(RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, this, this.rowButtons, param2, param0, param1);
            return true;
        }

        private void renderPendingInvitationItem(GuiGraphics param0, PendingInvite param1, int param2, int param3, int param4, int param5) {
            param0.drawString(RealmsPendingInvitesScreen.this.font, param1.worldName, param2 + 38, param3 + 1, 16777215, false);
            param0.drawString(RealmsPendingInvitesScreen.this.font, param1.worldOwnerName, param2 + 38, param3 + 12, 7105644, false);
            param0.drawString(
                RealmsPendingInvitesScreen.this.font, RealmsUtil.convertToAgePresentationFromInstant(param1.date), param2 + 38, param3 + 24, 7105644, false
            );
            RowButton.drawButtonsInRow(param0, this.rowButtons, RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, param2, param3, param4, param5);
            RealmsUtil.renderPlayerFace(param0, param2, param3, 32, param1.worldOwnerUuid);
        }

        @Override
        public Component getNarration() {
            Component var0 = CommonComponents.joinLines(
                Component.literal(this.pendingInvite.worldName),
                Component.literal(this.pendingInvite.worldOwnerName),
                Component.literal(RealmsUtil.convertToAgePresentationFromInstant(this.pendingInvite.date))
            );
            return Component.translatable("narrator.select", var0);
        }

        @OnlyIn(Dist.CLIENT)
        class AcceptRowButton extends RowButton {
            AcceptRowButton() {
                super(15, 15, 215, 5);
            }

            @Override
            protected void draw(GuiGraphics param0, int param1, int param2, boolean param3) {
                float var0 = param3 ? 19.0F : 0.0F;
                param0.blit(RealmsPendingInvitesScreen.ACCEPT_ICON_LOCATION, param1, param2, var0, 0.0F, 18, 18, 37, 18);
                if (param3) {
                    RealmsPendingInvitesScreen.this.toolTip = RealmsPendingInvitesScreen.ACCEPT_INVITE_TOOLTIP;
                }

            }

            @Override
            public void onClick(int param0) {
                RealmsPendingInvitesScreen.this.accept(param0);
            }
        }

        @OnlyIn(Dist.CLIENT)
        class RejectRowButton extends RowButton {
            RejectRowButton() {
                super(15, 15, 235, 5);
            }

            @Override
            protected void draw(GuiGraphics param0, int param1, int param2, boolean param3) {
                float var0 = param3 ? 19.0F : 0.0F;
                param0.blit(RealmsPendingInvitesScreen.REJECT_ICON_LOCATION, param1, param2, var0, 0.0F, 18, 18, 37, 18);
                if (param3) {
                    RealmsPendingInvitesScreen.this.toolTip = RealmsPendingInvitesScreen.REJECT_INVITE_TOOLTIP;
                }

            }

            @Override
            public void onClick(int param0) {
                RealmsPendingInvitesScreen.this.reject(param0);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class PendingInvitationSelectionList extends RealmsObjectSelectionList<RealmsPendingInvitesScreen.Entry> {
        public PendingInvitationSelectionList() {
            super(RealmsPendingInvitesScreen.this.width, RealmsPendingInvitesScreen.this.height, 32, RealmsPendingInvitesScreen.this.height - 40, 36);
        }

        public void removeAtIndex(int param0) {
            this.remove(param0);
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 36;
        }

        @Override
        public int getRowWidth() {
            return 260;
        }

        @Override
        public void renderBackground(GuiGraphics param0) {
            RealmsPendingInvitesScreen.this.renderBackground(param0);
        }

        @Override
        public void selectItem(int param0) {
            super.selectItem(param0);
            this.selectInviteListItem(param0);
        }

        public void selectInviteListItem(int param0) {
            RealmsPendingInvitesScreen.this.selectedInvite = param0;
            RealmsPendingInvitesScreen.this.updateButtonStates();
        }

        public void setSelected(@Nullable RealmsPendingInvitesScreen.Entry param0) {
            super.setSelected(param0);
            RealmsPendingInvitesScreen.this.selectedInvite = this.children().indexOf(param0);
            RealmsPendingInvitesScreen.this.updateButtonStates();
        }
    }
}
