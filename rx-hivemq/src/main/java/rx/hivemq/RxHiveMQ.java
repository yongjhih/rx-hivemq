/*
 * Copyright 2017 Andrew Chen
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

package rx.hivemq;

import com.hivemq.spi.callback.*;
import com.hivemq.spi.callback.events.*;
import com.hivemq.spi.callback.events.broker.OnBrokerStart;
import com.hivemq.spi.callback.exception.*;
import com.hivemq.spi.callback.registry.CallbackRegistry;
import com.hivemq.spi.message.CONNECT;
import com.hivemq.spi.security.ClientData;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Cancellable;

/**
 * OnBrokerStart
 * OnBrokerStop
 * OnConnectCallback
 * OnDisconnectCallback
 * OnPublishReceivedCallback
 * OnPublishSendCallback
 * OnSubscribeCallback
 * OnTopicSubscription
 * OnUnsubscribeCallback
 * OnConnackSend
 * OnPingCallback
 * OnPubackReceived
 * OnPubackSend
 * OnPubcompReceived
 * OnPubcompSend
 * OnPubrecReceived
 * OnPubrecSend
 * OnPubrelReceived
 * OnPubrelSend
 * OnSubackSend
 * OnUnsubackSend
 * AfterLoginCallback
 * OnAuthenticationCallback
 * OnAuthorizationCallback
 * OnInsufficientPermissionDisconnect
 * RestrictionsAfterLoginCallback
 * ClusterDiscoveryCallback
 * ScheduledCallback
 * OnBrokerStart
 */
public class RxHiveMQ {
    @NonNull
    @CheckReturnValue
    public static Completable brokerStarts(final CallbackRegistry callbackRegistry) {
        return brokerStarts(callbackRegistry, CallbackPriority.MEDIUM);
    }

    @NonNull
    @CheckReturnValue
    public static Completable brokerStarts(final CallbackRegistry callbackRegistry, final int priority) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull final CompletableEmitter emitter) throws Exception {
                final OnBrokerStart callback = new OnBrokerStart() {
                    @Override
                    public void onBrokerStart() throws BrokerUnableToStartException {
                        if (!emitter.isDisposed()) {
                            emitter.onComplete();
                        }
                    }

                    @Override
                    public int priority() {
                        return priority;
                    }
                };

                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        callbackRegistry.removeCallback(callback);
                    }
                });

                callbackRegistry.addCallback(callback);
            }
        });
    }
}
