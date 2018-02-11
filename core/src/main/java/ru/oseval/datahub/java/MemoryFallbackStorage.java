package ru.oseval.datahub.java;

import java.util.Optional;
import java.util.function.Function;

public class MemoryFallbackStorage extends MemoryStorage {
    private Datahub.Storage storage;
    public MemoryFallbackStorage(Datahub.Storage storage) {
        this.storage = storage;
    }
    @Override
    public void register(String entityId, Object dataClock, Function<Void, Void> callback) {
        super.register(entityId, dataClock, v -> {
            storage.register(entityId, dataClock, callback);
            return null;
        });
    }

    @Override
    public void change(String entityId, Object dataClock, Function<Void, Void> callback) {
        super.change(entityId, dataClock, v -> {
            storage.change(entityId, dataClock, callback);
            return null;
        });
    }

    @Override
    public void getLastClock(String entityId, Function<Optional<Object>, Void> callback) {
        storage.getLastClock(entityId, clockOpt -> {
            if (clockOpt.isPresent()) {
                callback.apply(clockOpt);
            } else {
                super.getLastClock(entityId, callback);
            }

            return null;
        });
    }
}
