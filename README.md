# RxHiveMQ

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
