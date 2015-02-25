/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.api.account.billing;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Plan;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.codenvy.api.account.PaymentService;
import com.codenvy.api.account.impl.shared.dto.CreditCard;
import com.codenvy.api.account.impl.shared.dto.Invoice;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.commons.schedule.ScheduleDelay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Charge subscription with the Braintree.
 *
 * @author Alexander Garagatyi
 */
// must be eager singleton
@Singleton
public class BraintreePaymentService implements PaymentService {
    private static final Logger LOG = LoggerFactory.getLogger(BraintreePaymentService.class);

    private final CreditCardDao           creditCardDao;
    private final BraintreeGateway        gateway;
    private final EventService            eventService;
    private       Map<String, Double>     prices;

    @Inject
    public BraintreePaymentService(CreditCardDao creditCardDao, BraintreeGateway gateway, EventService eventService) {
        this.creditCardDao = creditCardDao;
        this.gateway = gateway;
        this.eventService = eventService;
        this.prices = Collections.emptyMap();
    }

    @Override
    public void charge(Subscription subscription) throws ServerException, ConflictException, ForbiddenException {
        if (subscription == null) {
            throw new ForbiddenException("No subscription information provided");
        }
        if (subscription.getId() == null) {
            throw new ForbiddenException("Subscription id required");
        }
        String accountId = subscription.getAccountId();

        final String creditCardToken = getCreditCardToken(accountId);
        if (creditCardToken == null) {
            throw new ForbiddenException("Account hasn't credit card");
        }
        // prices should be set already by getPrices method
        final Double price = prices.get(subscription.getPlanId());
        try {
            if (price == null) {
                LOG.error("PAYMENTS# state#Error# subscriptionId#{}# message#{}#", subscription.getId(),
                          "Price of plan is not found " + subscription.getPlanId());
                throw new ServerException("Internal server error occurs. Please, contact support");
            }

            final TransactionRequest request = new TransactionRequest()
                    .paymentMethodToken(creditCardToken)
                    // add subscription id to identify charging reason
                    .customField("subscription_id", subscription.getId())
                    .options().submitForSettlement(true).done()
                    .amount(new BigDecimal(price, new MathContext(2)));

            final Result<Transaction> result = gateway.transaction().sale(request);
            final Transaction target = result.getTarget();
            if (result.isSuccess()) {
                // transaction successfully submitted for settlement
                LOG.info("PAYMENTS# state#Success# subscriptionId#{}# transactionStatus#{}# message#{}# transactionId#{}#",
                         subscription.getId(), target.getStatus(), result.getMessage(), target.getId());
                eventService.publish(CreditCardChargeEvent.creditCardChargeSuccessEvent(accountId, target.getCreditCard().getMaskedNumber(),
                                                                                        subscription.getId(), price));
            } else {
                LOG.error("PAYMENTS# state#Error# subscriptionId#{}# message#{}#", subscription.getId(), result.getMessage());
                eventService.publish(CreditCardChargeEvent.creditCardChargeFailedEvent(accountId, target.getCreditCard().getMaskedNumber(),
                                                                                       subscription.getId(), price));
                throw new ForbiddenException(result.getMessage());
            }
        } catch (ApiException e) {
            // rethrow user-friendly API exceptions
            eventService.publish(CreditCardChargeEvent.creditCardChargeFailedEvent(accountId, getCreditCardNumber(accountId),
                                                                                   subscription.getId(), price));
            throw e;
        } catch (Exception e) {
            LOG.error(String.format("PAYMENTS# state#Error# subscriptionId#%s# message#%s#", subscription.getId(), e.getLocalizedMessage()),
                      e);
            eventService.publish(CreditCardChargeEvent.creditCardChargeFailedEvent(accountId, getCreditCardNumber(accountId),
                                                                                   subscription.getId(), price));
            throw new ServerException("Internal server error occurs. Please, contact support");
        }
    }

    @Override
    public void charge(Invoice invoice)
            throws ServerException, ForbiddenException {
        if (invoice.getCreditCardId() == null) {
            throw new ForbiddenException("Credit card token can't be null");
        }
        if (invoice.getTotal() == 0) {
            throw new ForbiddenException("Amount can't be 0");
        }
        String accountId = invoice.getAccountId();
        Double price =  invoice.getTotal();
        try {
            final TransactionRequest request = new TransactionRequest()
                    .paymentMethodToken(invoice.getCreditCardId())
                    // add invoice id to identify charging reason
                    .customField("invoice_id", String.valueOf(invoice.getId()))
                    .options().submitForSettlement(true).done()
                    .amount(new BigDecimal(price, new MathContext(2)));

            final Result<Transaction> result = gateway.transaction().sale(request);
            final Transaction target = result.getTarget();
            if (result.isSuccess()) {
                // transaction successfully submitted for settlement
                LOG.info("PAYMENTS# state#Success# invoice#{}# accountId#{}# transactionStatus#{}# message#{}# transactionId#{}#",
                         invoice.getId(), accountId, target.getStatus(), result.getMessage(), target.getId());
                eventService.publish(CreditCardChargeEvent.creditCardChargeSuccessEvent(accountId, target.getCreditCard().getMaskedNumber(),
                                                                                        Long.toString(invoice.getId()), price));
            } else {
                LOG.error("PAYMENTS# state#Error# invoice#{}# accountId#{}# message#{}#", invoice.getId(), invoice.getAccountId(),
                          result.getMessage());
                eventService.publish(CreditCardChargeEvent.creditCardChargeFailedEvent(accountId, target.getCreditCard().getMaskedNumber(),
                                                                                       Long.toString(invoice.getId()), price));
                throw new ForbiddenException(result.getMessage());
            }
        } catch (ApiException e) {
            eventService.publish(CreditCardChargeEvent.creditCardChargeFailedEvent(accountId, getCreditCardNumber(accountId),
                                                                                   Long.toString(invoice.getId()), price));
            // rethrow user-friendly API exceptions
            throw e;
        } catch (Exception e) {
            LOG.error("PAYMENTS# state#Error# invoice#{}# accountId#{}# message#{}#", invoice.getId(), invoice.getAccountId(),
                      e.getMessage());
            eventService.publish(CreditCardChargeEvent.creditCardChargeFailedEvent(accountId, getCreditCardNumber(accountId),
                                                                                   Long.toString(invoice.getId()), price));
            throw new ServerException("Internal server error occurs. Please, contact support");
        }
    }

    @ScheduleDelay(delay = 1, unit = TimeUnit.HOURS)
    public void updatePrices() {
        try {
            List<Plan> plans = gateway.plan().all();
            final HashMap<String, Double> newPrices = new HashMap<>(plans.size());
            for (Plan plan : plans) {
                newPrices.put(plan.getId(), plan.getPrice().doubleValue());
            }
            this.prices = newPrices;
        } catch (Exception e) {
            LOG.error("Can't retrieve prices for subscription plans." + e.getLocalizedMessage(), e);
        }
    }

    private String getCreditCardToken(String accountId) {
        try {
            final List<CreditCard> cards = creditCardDao.getCards(accountId);

            if (!cards.isEmpty()) {
                //Now user can have only one credit card
                return cards.get(0).getToken();
            }
        } catch (ServerException | ForbiddenException e) {
            LOG.error("Can't get credit card of account " + accountId, e);
            return null;
        }

        return null;
    }

    private String getCreditCardNumber(String accountId) {
        try {
            final List<CreditCard> cards = creditCardDao.getCards(accountId);

            if (!cards.isEmpty()) {
                //Now user can have only one credit card
                return cards.get(0).getNumber();
            }
        } catch (ServerException | ForbiddenException e) {
            LOG.error("Can't get credit card of account " + accountId, e);
            return null;
        }

        return null;
    }
}
