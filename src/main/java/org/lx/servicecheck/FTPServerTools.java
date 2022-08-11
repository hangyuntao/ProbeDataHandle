package org.lx.servicecheck;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPConnector;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class FTPServerTools {

	public static FTPClient getFTPClient(String protocol, String ip, int port, String name, String pass)
			throws NoSuchAlgorithmException, KeyManagementException, IllegalStateException, IOException,
			FTPIllegalReplyException, FTPException {

		FTPClient ftpClient = new FTPClient();
		
		FTPConnector connector = ftpClient.getConnector();
		connector.setReadTimeout(2);
		connector.setConnectionTimeout(2);
		connector.setCloseTimeout(2);
		if (protocol.startsWith("ftp")) {
			ftpClient.setType(FTPClient.TYPE_BINARY);
		} else {
			throw new IllegalArgumentException("no protocol match");
		}
		if ("ftps".equals(protocol)) {
			ftpClient.setSecurity(FTPClient.SECURITY_FTPS);
			setSsl(ftpClient);
		} else if ("ftpes".equals(protocol)) {
			ftpClient.setSecurity(FTPClient.SECURITY_FTPES);
			setSsl(ftpClient);
		}
		ftpClient.connect(ip, port);
		ftpClient.login(name, pass);
		return ftpClient;
	}

	private static void setSsl(FTPClient ftpClient) throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, new TrustManager[] { new MyX509TrustManager() }, new SecureRandom());
		ftpClient.setSSLSocketFactory(sslContext.getSocketFactory());
	}

	static class MyX509TrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
				throws java.security.cert.CertificateException {
		}

		@Override
		public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
				throws java.security.cert.CertificateException {
		}

		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}

}
