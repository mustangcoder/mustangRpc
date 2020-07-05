package org.mustangcoder.rpc.common;

import java.io.FileInputStream;
import java.util.Properties;

public class PropUtil {

    private static final Properties PROPERTIES = new Properties();

    static {
        String dir = Thread.currentThread().getClass().getResource("/").getPath();
        System.out.println("Mustang RPC load config from:" + dir);
        try {
            final String propFile = dir + "rpc.properties";
            FileInputStream fileInputStream = new FileInputStream(propFile);
            PROPERTIES.load(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getProp(String key) {
        return PROPERTIES.getProperty(key);
    }
}
