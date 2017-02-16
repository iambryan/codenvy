/*
 *  [2015] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
'use strict';
import {CodenvyPayment, ICreditCard} from '../../components/api/codenvy-payment.factory';
import {BillingService} from './billing.service';
import {CodenvyTeam} from '../../components/api/codenvy-team.factory';

interface ICreditCardProblem {
  message: string;
  [propName: string]: string;
}

enum Tab {Summary, Card, Invoices}

export class BillingController {
  $log: ng.ILogService;
  $mdDialog: ng.material.IDialogService;
  $q: ng.IQService;
  confirmDialogService: any;
  cheAPI: any;
  codenvyPayment: CodenvyPayment;
  codenvyTeam: CodenvyTeam;
  cheNotification: any;
  billingService: BillingService;

  creditCard: ICreditCard;
  origCreditCard: ICreditCard;
  cardInfoForm: ng.IFormController;
  selectedTabIndex: number;
  accountId: string;
  loading: boolean;

  tab: Object = Tab;

  /**
   * @ngInject for Dependency injection
   */
  constructor ($log: ng.ILogService, $mdDialog: ng.material.IDialogService, $q: ng.IQService,
               $rootScope: che.IRootScopeService, confirmDialogService: any, cheAPI: any, codenvyPayment: CodenvyPayment,
               codenvyTeam: CodenvyTeam,cheNotification: any, billingService: BillingService) {
    this.$log = $log;
    this.$mdDialog = $mdDialog;
    this.$q = $q;
    this.confirmDialogService = confirmDialogService;
    this.cheAPI = cheAPI;
    this.codenvyPayment = codenvyPayment;
    this.codenvyTeam = codenvyTeam;
    this.cheNotification = cheNotification;
    this.billingService = billingService;

    $rootScope.showIDE = false;

    this.accountId = '';

    this.selectedTabIndex = Tab.Summary;

    this.fetchCreditCard();
  }

  /**
   * Fetches account ID.
   *
   * @return {IPromise<any>}
   */
  fetchAccountId(): ng.IPromise<any> {
    return this.codenvyTeam.fetchTeams().then(() => {
      this.accountId = this.codenvyTeam.getPersonalAccount().id;
    }, (error: any) => {
      if (error.status === 304) {
        this.accountId = this.codenvyTeam.getPersonalAccount().id;
      }
    });
  }

  /**
   * Fetches and stores a credit card.
   *
   */
  fetchCreditCard(): ng.IPromise<any> {
    this.loading = true;

    return this.fetchAccountId().then(() => {
      return this.billingService.fetchCreditCard(this.accountId);
    }).then((creditCard: ICreditCard) => {
      this.creditCard = creditCard;
      this.origCreditCard = angular.copy(this.creditCard);
    }).finally(() => {
      this.loading = false;
    });
  }

  /**
   * Gets credit card and creates a copy
   */
  getCreditCard(): void {
    this.creditCard = this.billingService.getCreditCard(this.accountId);
    this.origCreditCard = angular.copy(this.creditCard);
  }

  /**
   * Callback when credit card has been changed.
   *
   * @param creditCard {ICreditCard}
   */
  creditCardChanged(creditCard: ICreditCard): void {
    this.creditCard = angular.copy(creditCard);
  }

  /**
   * Deletes existing credit card.
   */
  creditCardDeleted(): void {
    let promise = this.confirmDialogService.showConfirmDialog('Remove billing information',
      'Are you sure you want to delete all your billing information? This is irreversible.', 'Delete');

    promise.then(() => {
      this.loading = true;
      this.billingService.removeCreditCard(this.creditCard.accountId, this.creditCard.token).then(() => {
        return this.fetchCreditCard();
      },(error: any) => {
        this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to delete the credit card.');
      }).finally(() => {
        this.loading = false;
      });
    });
  }

  /**
   * Adds new credit card or updates an existing one.
   */
  saveCard(): void {
    this.loading = true;

    let savePromise;
    if (this.creditCard.token) {
      // update exiting card
      savePromise = this.billingService.updateCreditCard(this.accountId, this.creditCard);
    } else {
      // add new card
      savePromise = this.billingService.addCreditCard(this.accountId, this.creditCard);
    }

    savePromise.then((data: any) => {
      if (data && data.problems && data.problems.length) {
        this.showProblems(data.problems);
      } else {
        return this.fetchCreditCard();
      }
    }, (error: any) => {
      if (error && error.problems && error.problems.length) {
        this.showProblems(error.problems);
      } else {
        let problem: ICreditCardProblem = {message: 'Failed to save the credit card.'};
        this.showProblems([problem]);
      }
    }).finally(() => {
      this.loading = false;
    });
  }

  /**
   * Cancels credit card information changes
   */
  cancelCard(): void {
    this.creditCard = angular.copy(this.origCreditCard);
  }

  /**
   * Registers card info form
   * Gets a credit card
   *
   * @param form {ng.IFormController}
   */
  onCreditCardSelect(form: ng.IFormController): void {
    this.cardInfoForm = form;
    this.getCreditCard();
  }

  /**
   * Returns true if form on Card tab is valid
   *
   * @return {boolean}
   */
  isSaveButtonDisabled(): boolean {
    return !(this.cardInfoForm && this.cardInfoForm.$valid)
      || angular.equals(this.creditCard, this.origCreditCard);
  }

  /**
   * Returns true if "Save" button should be visible
   *
   * @return {boolean}
   */
  isSaveButtonVisible(): boolean {
    return this.selectedTabIndex === Tab.Card && !this.loading;
  }

  /**
   * Creates popup with list of error messages.
   *
   * @param {ICreditCardProblem[]} problems
   */
  showProblems(problems: ICreditCardProblem[]): void {
    let messages: string[] = [];
    problems.forEach((problem: {message: string, [propName: string]: string}) => {
      messages.push(problem.message);
    });

    this.$mdDialog.show({
      controller: 'ErrorPopupController',
      controllerAs: 'errorPopupController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        messages: messages
      },
      templateUrl: 'app/billing/error-popup/error-popup.html'
    });
  }

}
