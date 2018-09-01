package com.up72.server.mina.utils.redis;

import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient {
	private JedisPool pool;
	private Map<String, String> params;

	public RedisClient(Map<String, String> params) {
		this.params = params;
		this.init();
	}
	
	private void init() {
		String redisHost = "localhost";
		int redisPort = 8998;
		int maxActive = 3000;
		int maxIdle = 200;
		int maxWait = 100000;
		boolean testOnBorrow = true;

		try {
			redisHost = (String)this.params.get("redis.ip");
		} catch (Exception var13) {

		}

		try {
			redisPort = Integer.parseInt((String)this.params.get("redis.port"));
		} catch (Exception var12) {

		}
		
		
		try {
			maxActive = Integer.parseInt((String)this.params.get("redis.pool.maxActive"));
		} catch (Exception var11) {

		}

		try {
			maxIdle = Integer.parseInt((String)this.params.get("redis.pool.maxIdle"));
		} catch (Exception var10) {

		}

		try {
			maxWait = Integer.parseInt((String)this.params.get("redis.pool.maxWait"));
		} catch (Exception var9) {

		}

		try {
			testOnBorrow = Boolean.parseBoolean((String)this.params.get("redis.pool.testOnBorrow"));
		} catch (Exception var8) {

		}
		
//		System.out.println("redisHost="+redisHost);
//		System.out.println("redisPort="+redisPort);
//		System.out.println("maxIdle="+maxIdle);
//		System.out.println("maxActive="+maxActive);
//		System.out.println("maxWait="+maxWait);
//		System.out.println("testOnBorrow="+testOnBorrow);

		JedisPoolConfig conf = new JedisPoolConfig();
		conf.setMaxIdle(maxIdle);
//		conf.setMaxTotal(maxActive);
//		conf.setMaxWaitMillis(maxWait);
		conf.setMaxActive(maxActive);
		conf.setMaxWait(maxWait);
		conf.setTestOnBorrow(testOnBorrow);
		conf.setTestOnReturn(true);
		this.pool = new JedisPool(conf, redisHost, redisPort,1000000);
	}

	public Jedis getJedis() {
		return this.pool.getResource();
	}

	public void returnJedis(Jedis jedis) {
		if(jedis != null) {
			this.pool.returnResource(jedis);
//			jedis.close();
		}

	}
	
	public void returnBrokenJedis(Jedis jedis) {
		if(jedis != null) {
			this.pool.returnBrokenResource(jedis);
//			jedis.close();
		}
		
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}
}
