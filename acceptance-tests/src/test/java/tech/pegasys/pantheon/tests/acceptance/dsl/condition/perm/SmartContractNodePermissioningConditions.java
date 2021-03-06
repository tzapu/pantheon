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
package tech.pegasys.pantheon.tests.acceptance.dsl.condition.perm;

import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.perm.SmartContractNodePermissioningTransactions;

public class SmartContractNodePermissioningConditions {

  private final SmartContractNodePermissioningTransactions transactions;

  public SmartContractNodePermissioningConditions(
      final SmartContractNodePermissioningTransactions transactions) {
    this.transactions = transactions;
  }

  public Condition nodeIsAllowed(final String address, final Node node) {
    return new WaitForTrueResponse(transactions.isNodeAllowed(address, node));
  }

  public Condition nodeIsForbidden(final String address, final Node node) {
    return new WaitForFalseResponse(transactions.isNodeAllowed(address, node));
  }

  public Condition connectionIsAllowed(final String address, final Node source, final Node target) {
    return new WaitForTrueResponse(transactions.isConnectionAllowed(address, source, target));
  }

  public Condition connectionIsForbidden(
      final String address, final Node source, final Node target) {
    return new WaitForFalseResponse(transactions.isConnectionAllowed(address, source, target));
  }
}
