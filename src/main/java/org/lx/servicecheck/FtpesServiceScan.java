package org.lx.servicecheck;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class FtpesServiceScan implements ServiceScan {

	@Override
	public String getName() {
		return "ftps";
	}

	@Override
	public boolean isActive(String ip, int port) {
		System.out.println("run FtpesServiceScan " + ip);
		FTPClient client = null;
		try {
			client = FTPServerTools.getFTPClient("ftpes", ip, port, "sss", "123");
			return true;
		} catch (KeyManagementException e) {
		} catch (NoSuchAlgorithmException e) {
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		} catch (FTPIllegalReplyException e) {
		} catch (FTPException e) {
			return true;
		} finally {
			if (client != null) {
				try {
					client.disconnect(true);
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (FTPIllegalReplyException e) {
					e.printStackTrace();
				} catch (FTPException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

}
