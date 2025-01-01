/*
 * This file is part of ViaCosmicReach - https://github.com/RaphiMC/ViaCosmicReach
 * Copyright (C) 2024-2025 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.viacosmicreach.viaproxy;

import net.lenni0451.lambdaevents.EventHandler;
import net.lenni0451.reflect.stream.RStream;
import net.raphimc.netminecraft.constants.IntendedState;
import net.raphimc.netminecraft.constants.MCPipeline;
import net.raphimc.viacosmicreach.api.CosmicReachProtocolVersion;
import net.raphimc.viacosmicreach.netty.LengthCodec;
import net.raphimc.viacosmicreach.netty.PacketIdCodec;
import net.raphimc.viacosmicreach.protocol.storage.ItchAccountStorage;
import net.raphimc.vialoader.netty.VLPipeline;
import net.raphimc.viaproxy.ViaProxy;
import net.raphimc.viaproxy.plugins.ViaProxyPlugin;
import net.raphimc.viaproxy.plugins.events.*;
import net.raphimc.viaproxy.plugins.events.types.ITyped;
import net.raphimc.viaproxy.proxy.session.ProxyConnection;
import net.raphimc.viaproxy.saves.impl.accounts.Account;
import net.raphimc.viaproxy.ui.I18n;
import net.raphimc.viaproxy.ui.ViaProxyWindow;

import javax.swing.*;

public class ViaCosmicReachPlugin extends ViaProxyPlugin {

    @Override
    public void onEnable() {
        ViaProxy.EVENT_MANAGER.register(this);
    }

    @EventHandler
    private void onViaProxyLoaded(final ViaProxyLoadedEvent event) {
        if (ViaProxy.getViaProxyWindow() == null) {
            return;
        }
        final ViaProxyWindow ui = ViaProxy.getViaProxyWindow();

        final JPanel accountsTabPanel = RStream.of(ui.accountsTab).withSuper().fields().by("contentPane").get();
        final JPanel body = (JPanel) accountsTabPanel.getComponent(0);
        final JPanel borderPanel = (JPanel) body.getComponent(body.getComponentCount() - 1);
        final JPanel buttonPanel = (JPanel) borderPanel.getComponent(0);

        final JButton addItchAccountButton = new JButton("Itch Account");
        addItchAccountButton.addActionListener(e -> {
            final String apiKey = JOptionPane.showInputDialog(ui, "Itch API Key", "Add Account", JOptionPane.PLAIN_MESSAGE);
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                try {
                    final ItchAccount account = new ItchAccount(apiKey.trim());
                    ViaProxy.getSaveManager().accountsSave.addAccount(account);
                    ViaProxy.getSaveManager().save();
                    RStream.of(ui.accountsTab).methods().by("addAccount").invokeArgs(account);
                    ViaProxyWindow.showInfo(I18n.get("tab.accounts.add.success", account.getName()));
                } catch (Throwable t) {
                    SwingUtilities.invokeLater(() -> ViaProxyWindow.showException(t));
                }
            }
        });
        buttonPanel.add(addItchAccountButton);
    }

    @EventHandler
    private void onProtocolTranslatorInit(ProtocolTranslatorInitEvent event) {
        event.registerPlatform(ViaCosmicReachPlatformImpl::new);
    }

    @EventHandler
    private void onProxy2ServerChannelInitialize(Proxy2ServerChannelInitializeEvent event) {
        if (event.getType() != ITyped.Type.POST) return;
        final ProxyConnection proxyConnection = ProxyConnection.fromChannel(event.getChannel());
        if (proxyConnection.getServerVersion().equals(CosmicReachProtocolVersion.cosmicReachLatest)) {
            event.getChannel().pipeline().remove(MCPipeline.COMPRESSION_HANDLER_NAME);
            event.getChannel().pipeline().remove(MCPipeline.ENCRYPTION_HANDLER_NAME);
            event.getChannel().pipeline().replace(MCPipeline.SIZER_HANDLER_NAME, MCPipeline.SIZER_HANDLER_NAME, new LengthCodec());
            event.getChannel().pipeline().addBefore(VLPipeline.VIA_CODEC_NAME, "viacosmicreach-packet-id", new PacketIdCodec());
        }
    }

    @EventHandler
    private void onPreConnect(PreConnectEvent event) {
        if (event.getServerVersion().equals(CosmicReachProtocolVersion.cosmicReachLatest)) {
            if (event.getIntendedState() == IntendedState.STATUS) {
                if (!ViaProxy.getConfig().getCustomMotd().isBlank()) {
                    event.setCancelMessage(ViaProxy.getConfig().getCustomMotd());
                }
                event.setCancelMessage("§7ViaProxy is working!\n§7Connect to join the configured server");
            }
        }
    }

    @EventHandler
    private void handleItchAccount(FillPlayerDataEvent event) {
        final Account account = event.getProxyConnection().getUserOptions().account();
        if (event.getProxyConnection().getServerVersion().equals(CosmicReachProtocolVersion.cosmicReachLatest) && account instanceof ItchAccount itchAccount) {
            final net.raphimc.viacosmicreach.protocol.model.account.ItchAccount itchAccountModel = net.raphimc.viacosmicreach.protocol.model.account.ItchAccount.create(
                    itchAccount.getUsername(),
                    itchAccount.getDisplayName(),
                    itchAccount.getCoverUrl(),
                    itchAccount.getUrl(),
                    itchAccount.getId()
            );
            event.getProxyConnection().getUserConnection().put(new ItchAccountStorage(itchAccountModel, itchAccount.getApiKey()));
        }
    }

}
