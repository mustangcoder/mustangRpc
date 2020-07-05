package org.mustangcoder.rpc.register;


public interface Register {
    void register(String className, Object provider);

    void unregister(String className);

    Object getProvider(String className);
}
