# RxHiveMQ

[![JitPack](https://img.shields.io/github/tag/yongjhih/rx-hivemq.svg?label=JitPack)](https://jitpack.io/#yongjhih/rx-hivemq)
[![javadoc](https://img.shields.io/github/tag/yongjhih/rx-hivemq.svg?label=javadoc)](https://jitpack.io/com/github/yongjhih/rx-hivemq/rx-hivemq/-SNAPSHOT/javadoc/)
[![CircleCI](https://circleci.com/gh/yongjhih/rx-hivemq.svg?style=svg)](https://circleci.com/gh/yongjhih/rx-hivemq)
[![codecov](https://codecov.io/gh/yongjhih/rx-hivemq/branch/master/graph/badge.svg)](https://codecov.io/gh/yongjhih/rx-hivemq)

## Usage

```kt
RxHiveMQ.brokerStarts(callbackRegistry, CallbackPriority.MEDIUM).subscribe {
}
RxHiveMQ.clientConnects(callbackRegistry, CallbackPriority.MEDIUM).subscribe { pair ->
    log.info("Client {} is connecting", pair.right.getClientId())
}
RxHiveMQ.publishReceiveds(callbackRegistry, CallbackPriority.MEDIUM).subscribe { pair ->
    log.info("Client " + pair.right.getClientId() + " sent a message to topic "
            + pair.left.getTopic() + ": "
            + new String(pair.left.getPayload(), Charsets.UTF_8))
}
RxHiveMQ.scheduleds(callbackRegistry, "0/5 * * * * ?").subscribe {
    log.info("Scheduled Callback is doing maintenance!")
}
RxHiveMQ.clientConnects(callbackRegistry, CallbackPriority.MEDIUM)
        .flatMapSingle { pair ->
            val clientId = pair.right.getClientId()
            val topic = Topic("devices/" + clientId + "/sensor", QoS.valueOf(0))
            FutureConverter.toSingle(subscriptionStore.addSubscription(clientId, topic)
        }
        .subscribe({
            log.info("Added subscription to {} for client {}", pair.left, pair.right)
        }, { e ->
            log.error("Failed to add subscription because of {}", e.getCause())
        })
```

## Installation
