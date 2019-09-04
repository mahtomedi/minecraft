package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RowButton;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsPendingInvitesScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RealmsScreen lastScreen;
    private String toolTip;
    private boolean loaded;
    private RealmsPendingInvitesScreen.PendingInvitationSelectionList pendingInvitationSelectionList;
    private RealmsLabel titleLabel;
    private int selectedInvite = -1;
    private RealmsButton acceptButton;
    private RealmsButton rejectButton;

    public RealmsPendingInvitesScreen(RealmsScreen param0) {
        this.lastScreen = param0;
    }

    @Override
    public void init() {
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.pendingInvitationSelectionList = new RealmsPendingInvitesScreen.PendingInvitationSelectionList();
        (new Thread("Realms-pending-invitations-fetcher") {
                @Override
                public void run() {
                    RealmsClient var0 = RealmsClient.createRealmsClient();
    
                    try {
                        List<PendingInvite> var1 = var0.pendingInvites().pendingInvites;
                        List<RealmsPendingInvitesScreen.PendingInvitationSelectionListEntry> var2 = var1.stream()
                            .map(param0 -> RealmsPendingInvitesScreen.this.new PendingInvitationSelectionListEntry(param0))
                            .collect(Collectors.toList());
                        Realms.execute(() -> RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.replaceEntries(var2));
                    } catch (RealmsServiceException var7) {
                        RealmsPendingInvitesScreen.LOGGER.error("Couldn't list invites");
                    } finally {
                        RealmsPendingInvitesScreen.this.loaded = true;
                    }
    
                }
            })
            .start();
        this.buttonsAdd(
            this.acceptButton = new RealmsButton(1, this.width() / 2 - 174, this.height() - 32, 100, 20, getLocalizedString("mco.invites.button.accept")) {
                @Override
                public void onPress() {
                    RealmsPendingInvitesScreen.this.accept(RealmsPendingInvitesScreen.this.selectedInvite);
                    RealmsPendingInvitesScreen.this.selectedInvite = -1;
                    RealmsPendingInvitesScreen.this.updateButtonStates();
                }
            }
        );
        this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 50, this.height() - 32, 100, 20, getLocalizedString("gui.done")) {
            @Override
            public void onPress() {
                Realms.setScreen(new RealmsMainScreen(RealmsPendingInvitesScreen.this.lastScreen));
            }
        });
        this.buttonsAdd(
            this.rejectButton = new RealmsButton(2, this.width() / 2 + 74, this.height() - 32, 100, 20, getLocalizedString("mco.invites.button.reject")) {
                @Override
                public void onPress() {
                    RealmsPendingInvitesScreen.this.reject(RealmsPendingInvitesScreen.this.selectedInvite);
                    RealmsPendingInvitesScreen.this.selectedInvite = -1;
                    RealmsPendingInvitesScreen.this.updateButtonStates();
                }
            }
        );
        this.titleLabel = new RealmsLabel(getLocalizedString("mco.invites.title"), this.width() / 2, 12, 16777215);
        this.addWidget(this.titleLabel);
        this.addWidget(this.pendingInvitationSelectionList);
        this.narrateLabels();
        this.updateButtonStates();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            Realms.setScreen(new RealmsMainScreen(this.lastScreen));
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    private void updateList(int param0) {
        this.pendingInvitationSelectionList.removeAtIndex(param0);
    }

    private void reject(final int param0) {
        if (param0 < this.pendingInvitationSelectionList.getItemCount()) {
            (new Thread("Realms-reject-invitation") {
                @Override
                public void run() {
                    try {
                        RealmsClient var0 = RealmsClient.createRealmsClient();
                        var0.rejectInvitation(RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.children().get(param0).pendingInvite.invitationId);
                        Realms.execute(() -> RealmsPendingInvitesScreen.this.updateList(param0));
                    } catch (RealmsServiceException var2) {
                        RealmsPendingInvitesScreen.LOGGER.error("Couldn't reject invite");
                    }

                }
            }).start();
        }

    }

    private void accept(final int param0) {
        if (param0 < this.pendingInvitationSelectionList.getItemCount()) {
            (new Thread("Realms-accept-invitation") {
                @Override
                public void run() {
                    try {
                        RealmsClient var0 = RealmsClient.createRealmsClient();
                        var0.acceptInvitation(RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.children().get(param0).pendingInvite.invitationId);
                        Realms.execute(() -> RealmsPendingInvitesScreen.this.updateList(param0));
                    } catch (RealmsServiceException var2) {
                        RealmsPendingInvitesScreen.LOGGER.error("Couldn't accept invite");
                    }

                }
            }).start();
        }

    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.toolTip = null;
        this.renderBackground();
        this.pendingInvitationSelectionList.render(param0, param1, param2);
        this.titleLabel.render(this);
        if (this.toolTip != null) {
            this.renderMousehoverTooltip(this.toolTip, param0, param1);
        }

        if (this.pendingInvitationSelectionList.getItemCount() == 0 && this.loaded) {
            this.drawCenteredString(getLocalizedString("mco.invites.nopending"), this.width() / 2, this.height() / 2 - 20, 16777215);
        }

        super.render(param0, param1, param2);
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

    private void updateButtonStates() {
        this.acceptButton.setVisible(this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite));
        this.rejectButton.setVisible(this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite));
    }

    private boolean shouldAcceptAndRejectButtonBeVisible(int param0) {
        return param0 != -1;
    }

    public static String getAge(PendingInvite param0) {
        return RealmsUtil.convertToAgePresentation(System.currentTimeMillis() - param0.date.getTime());
    }

    @OnlyIn(Dist.CLIENT)
    class PendingInvitationSelectionList extends RealmsObjectSelectionList<RealmsPendingInvitesScreen.PendingInvitationSelectionListEntry> {
        public PendingInvitationSelectionList() {
            super(RealmsPendingInvitesScreen.this.width(), RealmsPendingInvitesScreen.this.height(), 32, RealmsPendingInvitesScreen.this.height() - 40, 36);
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
        public boolean isFocused() {
            return RealmsPendingInvitesScreen.this.isFocused(this);
        }

        @Override
        public void renderBackground() {
            RealmsPendingInvitesScreen.this.renderBackground();
        }

        @Override
        public void selectItem(int param0) {
            this.setSelected(param0);
            if (param0 != -1) {
                List<RealmsPendingInvitesScreen.PendingInvitationSelectionListEntry> var0 = RealmsPendingInvitesScreen.this.pendingInvitationSelectionList
                    .children();
                PendingInvite var1 = var0.get(param0).pendingInvite;
                String var2 = RealmsScreen.getLocalizedString("narrator.select.list.position", param0 + 1, var0.size());
                String var3 = Realms.joinNarrations(Arrays.asList(var1.worldName, var1.worldOwnerName, RealmsPendingInvitesScreen.getAge(var1), var2));
                Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", var3));
            }

            this.selectInviteListItem(param0);
        }

        public void selectInviteListItem(int param0) {
            RealmsPendingInvitesScreen.this.selectedInvite = param0;
            RealmsPendingInvitesScreen.this.updateButtonStates();
        }
    }

    @OnlyIn(Dist.CLIENT)
    class PendingInvitationSelectionListEntry extends RealmListEntry {
        final PendingInvite pendingInvite;
        private final List<RowButton> rowButtons;

        PendingInvitationSelectionListEntry(PendingInvite param0) {
            this.pendingInvite = param0;
            this.rowButtons = Arrays.asList(
                new RealmsPendingInvitesScreen.PendingInvitationSelectionListEntry.AcceptRowButton(),
                new RealmsPendingInvitesScreen.PendingInvitationSelectionListEntry.RejectRowButton()
            );
        }

        @Override
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            this.renderPendingInvitationItem(this.pendingInvite, param2, param1, param5, param6);
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            RowButton.rowButtonMouseClicked(RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, this, this.rowButtons, param2, param0, param1);
            return true;
        }

        private void renderPendingInvitationItem(PendingInvite param0, int param1, int param2, int param3, int param4) {
            RealmsPendingInvitesScreen.this.drawString(param0.worldName, param1 + 38, param2 + 1, 16777215);
            RealmsPendingInvitesScreen.this.drawString(param0.worldOwnerName, param1 + 38, param2 + 12, 7105644);
            RealmsPendingInvitesScreen.this.drawString(RealmsPendingInvitesScreen.getAge(param0), param1 + 38, param2 + 24, 7105644);
            RowButton.drawButtonsInRow(this.rowButtons, RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, param1, param2, param3, param4);
            RealmsTextureManager.withBoundFace(param0.worldOwnerUuid, () -> {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RealmsScreen.blit(param1, param2, 8.0F, 8.0F, 8, 8, 32, 32, 64, 64);
                RealmsScreen.blit(param1, param2, 40.0F, 8.0F, 8, 8, 32, 32, 64, 64);
            });
        }

        @OnlyIn(Dist.CLIENT)
        class AcceptRowButton extends RowButton {
            AcceptRowButton() {
                super(15, 15, 215, 5);
            }

            @Override
            protected void draw(int param0, int param1, boolean param2) {
                RealmsScreen.bind("realms:textures/gui/realms/accept_icon.png");
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.pushMatrix();
                RealmsScreen.blit(param0, param1, param2 ? 19.0F : 0.0F, 0.0F, 18, 18, 37, 18);
                RenderSystem.popMatrix();
                if (param2) {
                    RealmsPendingInvitesScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.invites.button.accept");
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
            protected void draw(int param0, int param1, boolean param2) {
                RealmsScreen.bind("realms:textures/gui/realms/reject_icon.png");
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.pushMatrix();
                RealmsScreen.blit(param0, param1, param2 ? 19.0F : 0.0F, 0.0F, 18, 18, 37, 18);
                RenderSystem.popMatrix();
                if (param2) {
                    RealmsPendingInvitesScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.invites.button.reject");
                }

            }

            @Override
            public void onClick(int param0) {
                RealmsPendingInvitesScreen.this.reject(param0);
            }
        }
    }
}
