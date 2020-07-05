package org.mustangcoder.rpc.protocol;

import java.io.Serializable;

public class RpcInvokeProtocol implements Serializable {
    private String className;
    private String methodName;
    private Object[] params;
    private Class<?>[] paramTypes;

    public RpcInvokeProtocol() {
    }

    public RpcInvokeProtocol(String className, String methodName, Object[] params, Class<?>[] paramTypes) {
        this.className = className;
        this.methodName = methodName;
        this.params = params;
        this.paramTypes = paramTypes;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }
}
