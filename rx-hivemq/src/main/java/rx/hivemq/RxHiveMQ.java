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
import com.hivemq.spi.callback.schedule.ScheduledCallback;
import com.hivemq.spi.message.CONNECT;
import com.hivemq.spi.message.PUBLISH;
import com.hivemq.spi.security.ClientData;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiConsumer;
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
    /*
    @NonNull
    @CheckReturnValue
    @Deprecated
    public static Completable brokerStarts(final CallbackRegistry callbackRegistry) {
        return brokerStarts(callbackRegistry, CallbackPriority.MEDIUM);
    }
    */

    /**
     * @param callbackRegistry
     * @param priority
     * @return
     */
    @NonNull
    @CheckReturnValue
    public static Completable brokerStarts(@NonNull final CallbackRegistry callbackRegistry, final int priority) {
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

    /**
     * @param <L>
     * @param <R>
     */
    // TODO Move out
    public static class Pair<L, R> {
        public L left;
        public R right;

        Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }
    }

    /**
     * @param callbackRegistry
     * @param priority
     * @return
     */
    @NonNull
    @CheckReturnValue
    public static Observable<Pair<PUBLISH, ClientData>>
        publishReceiveds(@NonNull final CallbackRegistry callbackRegistry,
                   final int priority) {
        return Observable.create(new ObservableOnSubscribe<Pair<PUBLISH, ClientData>>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<Pair<PUBLISH, ClientData>> emitter) throws Exception {
                final OnPublishReceivedCallback callback = new OnPublishReceivedCallback() {
                    @Override
                    public void onPublishReceived(@NonNull final PUBLISH publish,
                                           @NonNull final ClientData clientData)
                            throws OnPublishReceivedException {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(new Pair(publish, clientData));
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

    /**
     * @param callbackRegistry
     * @param priority
     * @return
     */
    @NonNull
    @CheckReturnValue
    public static Observable<Pair<CONNECT, ClientData>>
            clientConnects(@NonNull final CallbackRegistry callbackRegistry,
                           final int priority) {
        return Observable.create(new ObservableOnSubscribe<Pair<CONNECT, ClientData>>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<Pair<CONNECT, ClientData>> emitter) throws Exception {
                final OnConnectCallback callback = new OnConnectCallback() {
                    @Override
                    public void onConnect(@NonNull final CONNECT connect,
                                          @NonNull final ClientData clientData) throws RefusedConnectionException {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(new Pair(connect, clientData));
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

    /**
     * @param callbackRegistry
     * @param cronExpression
     * @return
     */
    @NonNull
    @CheckReturnValue
    public static Observable<String>
        scheduleds(@NonNull final CallbackRegistry callbackRegistry,
                   @NonNull final String cronExpression) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<String> emitter) throws Exception {
                final ScheduledCallback callback = new ScheduledCallback() {
                    @Override
                    public void execute() {
                        emitter.onNext(cronExpression);
                    }

                    @Override
                    public String cronExpression() {
                        return cronExpression;
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
