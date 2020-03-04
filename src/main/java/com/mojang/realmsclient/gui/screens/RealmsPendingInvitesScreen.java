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
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsPendingInvitesScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation ACCEPT_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/accept_icon.png");
    private static final ResourceLocation REJECT_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/reject_icon.png");
    private final Screen lastScreen;
    private String toolTip;
    private boolean loaded;
    private RealmsPendingInvitesScreen.PendingInvitationSelectionList pendingInvitationSelectionList;
    private RealmsLabel titleLabel;
    private int selectedInvite = -1;
    private Button acceptButton;
    private Button rejectButton;

    public RealmsPendingInvitesScreen(Screen param0) {
        this.lastScreen = param0;
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
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
        this.acceptButton = this.addButton(new Button(this.width / 2 - 174, this.height - 32, 100, 20, I18n.get("mco.invites.button.accept"), param0 -> {
            this.accept(this.selectedInvite);
            this.selectedInvite = -1;
            this.updateButtonStates();
        }));
        this.addButton(
            new Button(
                this.width / 2 - 50, this.height - 32, 100, 20, I18n.get("gui.done"), param0 -> this.minecraft.setScreen(new RealmsMainScreen(this.lastScreen))
            )
        );
        this.rejectButton = this.addButton(new Button(this.width / 2 + 74, this.height - 32, 100, 20, I18n.get("mco.invites.button.reject"), param0 -> {
            this.reject(this.selectedInvite);
            this.selectedInvite = -1;
            this.updateButtonStates();
        }));
        this.titleLabel = new RealmsLabel(I18n.get("mco.invites.title"), this.width / 2, 12, 16777215);
        this.addWidget(this.titleLabel);
        this.narrateLabels();
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

    private void updateList(int param0) {
        this.pendingInvitationSelectionList.removeAtIndex(param0);
    }

    private void reject(final int param0) {
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

    private void accept(final int param0) {
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
    public void render(int param0, int param1, float param2) {
        this.toolTip = null;
        this.renderBackground();
        this.pendingInvitationSelectionList.render(param0, param1, param2);
        this.titleLabel.render(this);
        if (this.toolTip != null) {
            this.renderMousehoverTooltip(this.toolTip, param0, param1);
        }

        if (this.pendingInvitationSelectionList.getItemCount() == 0 && this.loaded) {
            this.drawCenteredString(this.font, I18n.get("mco.invites.nopending"), this.width / 2, this.height / 2 - 20, 16777215);
        }

        super.render(param0, param1, param2);
    }

    protected void renderMousehoverTooltip(String param0, int param1, int param2) {
        if (param0 != null) {
            int var0 = param1 + 12;
            int var1 = param2 - 12;
            int var2 = this.font.width(param0);
            this.fillGradient(var0 - 3, var1 - 3, var0 + var2 + 3, var1 + 8 + 3, -1073741824, -1073741824);
            this.font.drawShadow(param0, (float)var0, (float)var1, 16777215);
        }
    }

    private void updateButtonStates() {
        this.acceptButton.visible = this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite);
        this.rejectButton.visible = this.shouldAcceptAndRejectButtonBeVisible(this.selectedInvite);
    }

    private boolean shouldAcceptAndRejectButtonBeVisible(int param0) {
        return param0 != -1;
    }

    @OnlyIn(Dist.CLIENT)
    class Entry extends ObjectSelectionList.Entry<RealmsPendingInvitesScreen.Entry> {
        private final PendingInvite pendingInvite;
        private final List<RowButton> rowButtons;

        Entry(PendingInvite param0) {
            this.pendingInvite = param0;
            this.rowButtons = Arrays.asList(new RealmsPendingInvitesScreen.Entry.AcceptRowButton(), new RealmsPendingInvitesScreen.Entry.RejectRowButton());
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
            RealmsPendingInvitesScreen.this.font.draw(param0.worldName, (float)(param1 + 38), (float)(param2 + 1), 16777215);
            RealmsPendingInvitesScreen.this.font.draw(param0.worldOwnerName, (float)(param1 + 38), (float)(param2 + 12), 7105644);
            RealmsPendingInvitesScreen.this.font
                .draw(RealmsUtil.convertToAgePresentationFromInstant(param0.date), (float)(param1 + 38), (float)(param2 + 24), 7105644);
            RowButton.drawButtonsInRow(this.rowButtons, RealmsPendingInvitesScreen.this.pendingInvitationSelectionList, param1, param2, param3, param4);
            RealmsTextureManager.withBoundFace(param0.worldOwnerUuid, () -> {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiComponent.blit(param1, param2, 32, 32, 8.0F, 8.0F, 8, 8, 64, 64);
                GuiComponent.blit(param1, param2, 32, 32, 40.0F, 8.0F, 8, 8, 64, 64);
            });
        }

        @OnlyIn(Dist.CLIENT)
        class AcceptRowButton extends RowButton {
            AcceptRowButton() {
                super(15, 15, 215, 5);
            }

            @Override
            protected void draw(int param0, int param1, boolean param2) {
                RealmsPendingInvitesScreen.this.minecraft.getTextureManager().bind(RealmsPendingInvitesScreen.ACCEPT_ICON_LOCATION);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                float var0 = param2 ? 19.0F : 0.0F;
                GuiComponent.blit(param0, param1, var0, 0.0F, 18, 18, 37, 18);
                if (param2) {
                    RealmsPendingInvitesScreen.this.toolTip = I18n.get("mco.invites.button.accept");
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
                RealmsPendingInvitesScreen.this.minecraft.getTextureManager().bind(RealmsPendingInvitesScreen.REJECT_ICON_LOCATION);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                float var0 = param2 ? 19.0F : 0.0F;
                GuiComponent.blit(param0, param1, var0, 0.0F, 18, 18, 37, 18);
                if (param2) {
                    RealmsPendingInvitesScreen.this.toolTip = I18n.get("mco.invites.button.reject");
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
        public boolean isFocused() {
            return RealmsPendingInvitesScreen.this.getFocused() == this;
        }

        @Override
        public void renderBackground() {
            RealmsPendingInvitesScreen.this.renderBackground();
        }

        @Override
        public void selectItem(int param0) {
            this.setSelectedItem(param0);
            if (param0 != -1) {
                List<RealmsPendingInvitesScreen.Entry> var0 = RealmsPendingInvitesScreen.this.pendingInvitationSelectionList.children();
                PendingInvite var1 = var0.get(param0).pendingInvite;
                String var2 = I18n.get("narrator.select.list.position", param0 + 1, var0.size());
                String var3 = NarrationHelper.join(
                    Arrays.asList(var1.worldName, var1.worldOwnerName, RealmsUtil.convertToAgePresentationFromInstant(var1.date), var2)
                );
                NarrationHelper.now(I18n.get("narrator.select", var3));
            }

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
