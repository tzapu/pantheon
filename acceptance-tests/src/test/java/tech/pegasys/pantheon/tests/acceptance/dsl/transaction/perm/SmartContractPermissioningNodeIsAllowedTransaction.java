/*
 * Copyright 2018 ConsenSys AG.
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
package tech.pegasys.pantheon.tests.acceptance.dsl.transaction.perm;

import static tech.pegasys.pantheon.ethereum.permissioning.SmartContractPermissioningController.checkTransactionResult;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.permissioning.SmartContractPermissioningController;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.RunnableNode;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.JsonRequestFactories;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.Transaction;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.enode.EnodeURL;

import java.io.IOException;

import org.web3j.protocol.core.DefaultBlockParameterName;

public class SmartContractPermissioningNodeIsAllowedTransaction implements Transaction<Boolean> {

  private static final String IS_NODE_ALLOWED_IPV4_SIGNATURE = "0x6863d6c0";

  private final Address contractAddress;
  private final Node node;

  public SmartContractPermissioningNodeIsAllowedTransaction(
      final Address contractAddress, final Node node) {
    this.contractAddress = contractAddress;
    this.node = node;
  }

  @Override
  public Boolean execute(final JsonRequestFactories node) {
    try {
      final String value =
          node.eth().ethCall(payload(), DefaultBlockParameterName.LATEST).send().getValue();
      return checkTransactionResult(BytesValue.fromHexString(value));
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private org.web3j.protocol.core.methods.request.Transaction payload() {
    final String sourceEnodeURL = ((RunnableNode) node).enodeUrl().toASCIIString();
    final BytesValue payload =
        SmartContractPermissioningController.createPayload(
            BytesValue.fromHexString(IS_NODE_ALLOWED_IPV4_SIGNATURE), new EnodeURL(sourceEnodeURL));

    return org.web3j.protocol.core.methods.request.Transaction.createFunctionCallTransaction(
        null, null, null, null, contractAddress.toString(), payload.toString());
  }
}
