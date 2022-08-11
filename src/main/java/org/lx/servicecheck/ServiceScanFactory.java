package org.lx.servicecheck;

import org.reflections.Reflections;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceScanFactory {

    private static final Map<String, ServiceScan> mapping = new ConcurrentHashMap<>();


    static {
        Reflections reflections = new Reflections("com.nms");
        Set<Class<? extends ServiceScan>> myClasses = reflections.getSubTypesOf(ServiceScan.class);
        for (Class<? extends ServiceScan> myClass : myClasses) {
            try {
                ServiceScan scan = myClass.newInstance();
                register(scan);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

    public static void register(ServiceScan scan) {
        System.out.println("register " + scan.getClass().getName());
        mapping.put(scan.getName(), scan);
    }

    public static ServiceScan getServiceScan(String name) {
        ServiceScan scan = mapping.get(name);
        return scan;
    }
}
