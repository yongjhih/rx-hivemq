/*
 * Copyright 2017 Andrew Chen
 * Copyright 2015 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.acme.plugin;

import com.acme.callbacks.*;
import com.acme.callbacks.advanced.*;
import com.acme.configuration.MyConfiguration;
import com.google.common.base.Charsets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hivemq.spi.PluginEntryPoint;
import com.hivemq.spi.callback.CallbackPriority;
import com.hivemq.spi.callback.registry.CallbackRegistry;
import com.hivemq.spi.message.CONNECT;
import com.hivemq.spi.message.PUBLISH;
import com.hivemq.spi.message.QoS;
import com.hivemq.spi.message.RetainedMessage;
import com.hivemq.spi.message.Topic;
import com.hivemq.spi.security.ClientData;
import com.hivemq.spi.services.AsyncSubscriptionStore;
import com.hivemq.spi.services.BlockingRetainedMessageStore;
import com.hivemq.spi.services.RetainedMessageStore;

import net.javacrumbs.futureconverter.guavarx2.FutureConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import rx.hivemq.RxHiveMQ;

/**
 * This is the main class of the plugin, which is instanciated during the HiveMQ start up process.
 *
 * @author Christian Götz, Andrew Chen
 */
public class HelloWorldMainClass extends PluginEntryPoint {

    Logger log = LoggerFactory.getLogger(HelloWorldMainClass.class);

    private final BlockingRetainedMessageStore retainedMessageStore;
    private final MyConfiguration myConfiguration;
    private final AsyncSubscriptionStore subscriptionStore;
    private final SendListOfAllClientsOnPublish sendListOfAllClientsOnPublish;

    @Inject
    public HelloWorldMainClass(final BlockingRetainedMessageStore retainedMessageStore,
                               final MyConfiguration myConfiguration,
                               final AsyncSubscriptionStore subscriptionStore,
                               final SendListOfAllClientsOnPublish sendListOfAllClientsOnPublish) {
        this.retainedMessageStore = retainedMessageStore;
        this.myConfiguration = myConfiguration;
        this.subscriptionStore = subscriptionStore;
        this.sendListOfAllClientsOnPublish = sendListOfAllClientsOnPublish;
    }

    /**
     * This method is executed after the instanciation of the whole class. It is used to initialize
     * the implemented callbacks and make them known to the HiveMQ core.
     */
    @PostConstruct
    public void postConstruct() {
        final CallbackRegistry callbackRegistry = getCallbackRegistry();

        RxHiveMQ.brokerStarts(callbackRegistry, CallbackPriority.MEDIUM).subscribe(new Action() {
            @Override
            public void run() throws Exception {
                log.info("Property from property file is: " + myConfiguration.getMyProperty());
            }
        });
        RxHiveMQ.clientConnects(callbackRegistry, CallbackPriority.MEDIUM).subscribe(
                new Consumer<RxHiveMQ.Pair<CONNECT, ClientData>>() {
            @Override
            public void accept(@NonNull final RxHiveMQ.Pair<CONNECT, ClientData> pair)
                    throws Exception {
                log.info("Client {} is connecting", pair.right.getClientId());
            }
        });
        RxHiveMQ.publishReceiveds(callbackRegistry, CallbackPriority.MEDIUM).subscribe(
                new Consumer<RxHiveMQ.Pair<PUBLISH, ClientData>>() {
                    @Override
                    public void accept(@NonNull final RxHiveMQ.Pair<PUBLISH, ClientData> pair)
                            throws Exception {
                        log.info("Client " + pair.right.getClientId() + " sent a message to topic "
                                + pair.left.getTopic() + ": "
                                + new String(pair.left.getPayload(), Charsets.UTF_8));
                    }
                });
        RxHiveMQ.scheduleds(callbackRegistry, "0/5 * * * * ?").subscribe(
                new Consumer<String>() {
                    @Override
                    public void accept(@NonNull final String text)
                            throws Exception {
                        log.info("Scheduled Callback is doing maintenance!");
                    }
                });
        RxHiveMQ.scheduleds(callbackRegistry, "0/40 * * * * ?").subscribe(
                new Consumer<String>() {
            @Override
            public void accept(@NonNull final String text)
                    throws Exception {
                final Set<RetainedMessage> retainedMessages = retainedMessageStore.getRetainedMessages();
                for (final RetainedMessage retainedMessage : retainedMessages) {
                    if (retainedMessage != null) {
                        retainedMessageStore.remove(retainedMessage.getTopic());
                    }
                }
                log.info("Scheduled Callback is doing maintenance!");
            }
        });
        RxHiveMQ.clientConnects(callbackRegistry, CallbackPriority.MEDIUM)
                .flatMapSingle(new Function<RxHiveMQ.Pair<CONNECT, ClientData>, SingleSource<RxHiveMQ.Pair<String, String>>>() {
                    @Override
                    public SingleSource<RxHiveMQ.Pair<String, String>> apply(@NonNull RxHiveMQ.Pair<CONNECT, ClientData> pair) throws Exception {
                        final String clientId = pair.right.getClientId();

                        log.info("Client {} is connecting", clientId);

                        // Adding a subscription without automatically for the client
                        return addClientToTopic(clientId, "devices/" + clientId + "/sensor");
                    }

                    /**
                     * Add a Subscription for a certain client
                     */
                    private Single<RxHiveMQ.Pair<String, String>> addClientToTopic(
                            @NonNull final String clientId,
                            @NonNull final String topic) {
                        //The AsyncSubscriptionStore returns a ListenableFuture object
                        return FutureConverter.toSingle(subscriptionStore.addSubscription(clientId,
                                        new Topic(topic, QoS.valueOf(0))))
                                .map(new Function<Void, RxHiveMQ.Pair<String, String>>() {
                                    @Override
                                    public RxHiveMQ.Pair<String, String> apply(@NonNull Void v) throws Exception {
                                        return new RxHiveMQ.Pair<String, String>(clientId, topic);
                                    }
                                });
                    }
                })
                .subscribe(new Consumer<RxHiveMQ.Pair<String, String>>() {
                    @Override
                    public void accept(@NonNull final RxHiveMQ.Pair<String, String> pair)
                            throws Exception {
                        log.info("Added subscription to {} for client {}", pair.left, pair.right);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable e) throws Exception {
                        log.error("Failed to add subscription because of {}", e.getCause());
                    }
                });
        RxHiveMQ.publishReceiveds(callbackRegistry, CallbackPriority.MEDIUM)
                .subscribe(sendListOfAllClientsOnPublish);

        addRetainedMessage("/default", "Hello World.");
    }

    /**
     * Programmatically add a new Retained Message.
     */
    public void addRetainedMessage(String topic, String message) {

        if (!retainedMessageStore.contains(topic)) {
            retainedMessageStore.addOrReplace(
                    new RetainedMessage(topic, message.getBytes(), QoS.valueOf(1)));
        }
    }
}
