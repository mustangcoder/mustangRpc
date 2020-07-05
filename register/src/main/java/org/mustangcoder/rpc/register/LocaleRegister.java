package org.mustangcoder.rpc.register;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocaleRegister implements Register {

    private static final Map<String, Object> PROVIDER_MAP = new ConcurrentHashMap<>();

    private static final LocaleRegister INSTANCE = new LocaleRegister();

    public static LocaleRegister getInstance() {
        return INSTANCE;
    }

    @Override
    public void register(String className, Object provider) {
        PROVIDER_MAP.put(className, provider);
        log(provider.toString());
    }

    @Override
    public void unregister(String className) {
        PROVIDER_MAP.remove(className);
    }

    @Override
    public Object getProvider(String className) {
        return PROVIDER_MAP.get(className);
    }

    private void log(String msg) {
        System.out.println("LocaleRegister: " + msg);
    }
}
