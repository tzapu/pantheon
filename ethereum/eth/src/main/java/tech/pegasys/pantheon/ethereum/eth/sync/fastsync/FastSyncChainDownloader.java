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
package tech.pegasys.pantheon.ethereum.eth.sync.fastsync;

import tech.pegasys.pantheon.ethereum.ProtocolContext;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.eth.manager.EthContext;
import tech.pegasys.pantheon.ethereum.eth.sync.ChainDownloader;
import tech.pegasys.pantheon.ethereum.eth.sync.EthTaskChainDownloader;
import tech.pegasys.pantheon.ethereum.eth.sync.PipelineChainDownloader;
import tech.pegasys.pantheon.ethereum.eth.sync.SynchronizerConfiguration;
import tech.pegasys.pantheon.ethereum.eth.sync.state.SyncState;
import tech.pegasys.pantheon.ethereum.mainnet.ProtocolSchedule;
import tech.pegasys.pantheon.metrics.MetricsSystem;

public class FastSyncChainDownloader {

  private static final boolean USE_PIPELINE_DOWNLOADER = false;

  private FastSyncChainDownloader() {}

  public static <C> ChainDownloader create(
      final SynchronizerConfiguration config,
      final ProtocolSchedule<C> protocolSchedule,
      final ProtocolContext<C> protocolContext,
      final EthContext ethContext,
      final SyncState syncState,
      final MetricsSystem metricsSystem,
      final BlockHeader pivotBlockHeader) {

    final FastSyncTargetManager<C> syncTargetManager =
        new FastSyncTargetManager<>(
            config, protocolSchedule, protocolContext, ethContext, metricsSystem, pivotBlockHeader);

    if (USE_PIPELINE_DOWNLOADER) {
      return new PipelineChainDownloader<>(
          syncTargetManager,
          new FastSyncDownloadPipelineFactory(
              config, protocolSchedule, ethContext, pivotBlockHeader, metricsSystem),
          ethContext.getScheduler());
    }

    return new EthTaskChainDownloader<>(
        config,
        ethContext,
        syncState,
        syncTargetManager,
        new FastSyncCheckpointHeaderManager<>(
            config,
            protocolContext,
            ethContext,
            syncState,
            protocolSchedule,
            metricsSystem,
            pivotBlockHeader),
        new FastSyncBlockImportTaskFactory<>(
            config, protocolSchedule, protocolContext, ethContext, metricsSystem),
        metricsSystem);
  }
}
