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

import static org.web3j.utils.Numeric.toHexString;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.permissioning.SmartContractPermissioningController;
import tech.pegasys.pantheon.tests.acceptance.dsl.account.Account;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.RunnableNode;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.JsonRequestFactories;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.Transaction;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.enode.EnodeURL;

import java.io.IOException;
import java.math.BigInteger;

import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;

public class SmartContractPermissioningAllowNodeTransaction implements Transaction<Hash> {

  private static final String ADD_ENODE_IPV4_SIGNATURE = "0x680fc99c";

  private final Account sender;
  private final Address contractAddress;
  private final Node node;

  public SmartContractPermissioningAllowNodeTransaction(
      final Account sender, final Address contractAddress, final Node node) {
    this.sender = sender;
    this.contractAddress = contractAddress;
    this.node = node;
  }

  @Override
  public Hash execute(final JsonRequestFactories node) {
    final String signedTransactionData = signedTransactionData();
    try {
      String hash =
          node.eth().ethSendRawTransaction(signedTransactionData).send().getTransactionHash();
      return Hash.fromHexString(hash);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String signedTransactionData() {
    final String enodeURL = ((RunnableNode) node).enodeUrl().toASCIIString();
    final BytesValue payload =
        SmartContractPermissioningController.createPayload(
            BytesValue.fromHexString(ADD_ENODE_IPV4_SIGNATURE), new EnodeURL(enodeURL));

    RawTransaction transaction =
        RawTransaction.createTransaction(
            sender.getNextNonce(),
            BigInteger.valueOf(1000),
            BigInteger.valueOf(100_000),
            contractAddress.toString(),
            payload.toString());

    return toHexString(TransactionEncoder.signMessage(transaction, sender.web3jCredentials()));
  }
}
