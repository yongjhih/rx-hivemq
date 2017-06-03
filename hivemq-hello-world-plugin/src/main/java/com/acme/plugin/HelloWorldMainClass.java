/*
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
import com.hivemq.spi.PluginEntryPoint;
import com.hivemq.spi.callback.CallbackPriority;
import com.hivemq.spi.callback.registry.CallbackRegistry;
import com.hivemq.spi.message.CONNECT;
import com.hivemq.spi.message.PUBLISH;
import com.hivemq.spi.message.QoS;
import com.hivemq.spi.message.RetainedMessage;
import com.hivemq.spi.security.ClientData;
import com.hivemq.spi.services.BlockingRetainedMessageStore;
import com.hivemq.spi.services.RetainedMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import rx.hivemq.RxHiveMQ;

/**
 * This is the main class of the plugin, which is instanciated during the HiveMQ start up process.
 *
 * @author Christian Götz
 */
public class HelloWorldMainClass extends PluginEntryPoint {

    Logger log = LoggerFactory.getLogger(HelloWorldMainClass.class);

    private final BlockingRetainedMessageStore retainedMessageStore;
    private final MyConfiguration myConfiguration;

    @Inject
    public HelloWorldMainClass(final BlockingRetainedMessageStore retainedMessageStore,
                               final MyConfiguration myConfiguration) {
        this.retainedMessageStore = retainedMessageStore;
        this.myConfiguration = myConfiguration;
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
        RxHiveMQ.clientConnects(callbackRegistry, CallbackPriority.MEDIUM).subscribe(new Consumer<RxHiveMQ.Pair<CONNECT, ClientData>>() {
            @Override
            public void accept(@NonNull RxHiveMQ.Pair<CONNECT, ClientData> connectClientDataPair) throws Exception {
                log.info("Client {} is connecting", connectClientDataPair.right.getClientId());
            }
        });
        RxHiveMQ.publishReceiveds(callbackRegistry, CallbackPriority.MEDIUM).subscribe(new Consumer<RxHiveMQ.Pair<PUBLISH, ClientData>>() {
            @Override
            public void accept(@NonNull RxHiveMQ.Pair<PUBLISH, ClientData> connectClientDataPair) throws Exception {
                log.info("Client {} is connecting", connectClientDataPair.right.getClientId());
            }
        });
        /*
        callbackRegistry.addCallback(publishReceived);
        callbackRegistry.addCallback(simpleScheduledCallback);
        callbackRegistry.addCallback(scheduledClearRetainedCallback);
        callbackRegistry.addCallback(addSubscriptionOnClientConnect);
        callbackRegistry.addCallback(sendListOfAllClientsOnPublish);
        */

        addRetainedMessage("/default", "Hello World.");
    }

    /**
     * Programmatically add a new Retained Message.
     */
    public void addRetainedMessage(String topic, String message) {

        if (!retainedMessageStore.contains(topic))
            retainedMessageStore.addOrReplace(new RetainedMessage(topic, message.getBytes(), QoS.valueOf(1)));
    }
}
