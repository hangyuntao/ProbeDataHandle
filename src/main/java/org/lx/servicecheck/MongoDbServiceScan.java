package org.lx.servicecheck;

import com.mongodb.*;

import java.net.InetSocketAddress;

public class MongoDbServiceScan implements ServiceScan {

	@Override
	public String getName() {
		return "mongodb";
	}

	@Override
	public boolean isActive(String ip, int port) {
		System.out.println("run MongoDbServiceScan " + ip);
		try {
			MongoClientOptions options = MongoClientOptions.builder().socketTimeout(2000).connectTimeout(2000)
					.serverSelectionTimeout(2000).build();
			MongoClient mongoClient = new MongoClient(new ServerAddress(new InetSocketAddress(ip, port)),
					options);
			mongoClient.listDatabaseNames().first();
			mongoClient.close();
		} catch (MongoSocketOpenException e) {
		} catch (MongoTimeoutException e) {
		} catch (MongoInternalException e) {
		} catch (MongoCommandException e) {
			return true;
		} catch (Exception e) {
		}
		return false;
	}

}
