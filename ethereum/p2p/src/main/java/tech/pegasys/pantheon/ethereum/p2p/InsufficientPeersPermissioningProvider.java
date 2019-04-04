/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.ethereum.p2p;

import tech.pegasys.pantheon.ethereum.p2p.api.P2PNetwork;
import tech.pegasys.pantheon.ethereum.p2p.api.PeerConnection;
import tech.pegasys.pantheon.ethereum.p2p.wire.messages.DisconnectMessage.DisconnectReason;
import tech.pegasys.pantheon.ethereum.permissioning.node.ContextualNodePermissioningProvider;
import tech.pegasys.pantheon.util.Subscribers;
import tech.pegasys.pantheon.util.enode.EnodeURL;

import java.util.Collection;
import java.util.Optional;

/**
 * A permissioning provider that only provides an answer when we have no peers outside of our
 * bootnodes
 */
public class InsufficientPeersPermissioningProvider implements ContextualNodePermissioningProvider {
  private final EnodeURL selfEnode;
  private final P2PNetwork p2pNetwork;
  private final Collection<EnodeURL> bootnodeEnodes;
  private long nonBootnodePeerConnections;
  private final Subscribers<Runnable> permissioningUpdateSubscribers = new Subscribers<>();

  /**
   * Creates the provider observing the provided p2p network
   *
   * @param p2pNetwork the p2p network to observe
   * @param selfEnode the advertised enode address of this node
   * @param bootnodeEnodes the bootnodes that this node is configured to connection to
   */
  public InsufficientPeersPermissioningProvider(
      final P2PNetwork p2pNetwork,
      final EnodeURL selfEnode,
      final Collection<EnodeURL> bootnodeEnodes) {
    this.selfEnode = selfEnode;
    this.p2pNetwork = p2pNetwork;
    this.bootnodeEnodes = bootnodeEnodes;
    this.nonBootnodePeerConnections = countP2PNetworkNonBootnodeConnections();
    p2pNetwork.subscribeConnect(this::handleConnect);
    p2pNetwork.subscribeDisconnect(this::handleDisconnect);
  }

  private boolean isNotABootnode(final PeerConnection peerConnection) {
    return bootnodeEnodes.stream().noneMatch(peerConnection::isRemoteEnode);
  }

  private long countP2PNetworkNonBootnodeConnections() {
    return p2pNetwork.getPeers().stream().filter(this::isNotABootnode).count();
  }

  @Override
  public Optional<Boolean> isPermitted(
      final EnodeURL sourceEnode, final EnodeURL destinationEnode) {
    if (nonBootnodePeerConnections > 0) {
      return Optional.empty();
    } else if (checkEnode(sourceEnode) && checkEnode(destinationEnode)) {
      return Optional.of(true);
    } else {
      return Optional.empty();
    }
  }

  private boolean checkEnode(final EnodeURL enode) {
    return (enode.sameEndpoint(selfEnode) || bootnodeEnodes.stream().anyMatch(enode::sameEndpoint));
  }

  private void handleConnect(final PeerConnection peerConnection) {
    if (isNotABootnode(peerConnection)) {
      // if the first non bootnode peer seen
      if (++nonBootnodePeerConnections == 1) {
        permissioningUpdateSubscribers.forEach(Runnable::run);
      }
    }
  }

  private void handleDisconnect(
      final PeerConnection peerConnection,
      final DisconnectReason reason,
      final boolean initiatedByPeer) {
    if (isNotABootnode(peerConnection)) {
      // if we just lost the last non bootnode
      if (--nonBootnodePeerConnections == 0) {
        permissioningUpdateSubscribers.forEach(Runnable::run);
      }
    }
  }

  @Override
  public long subscribeToUpdates(final Runnable callback) {
    return permissioningUpdateSubscribers.subscribe(callback);
  }

  @Override
  public boolean unsubscribeFromUpdates(final long id) {
    return permissioningUpdateSubscribers.unsubscribe(id);
  }
}
