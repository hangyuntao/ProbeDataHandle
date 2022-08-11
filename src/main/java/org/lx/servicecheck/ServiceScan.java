package org.lx.servicecheck;

public interface ServiceScan {

    String getName();

    boolean isActive(String ip, int port);

}
