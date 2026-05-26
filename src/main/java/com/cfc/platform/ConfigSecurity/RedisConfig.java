package com.cfc.platform.ConfigSecurity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * Configures the Lettuce Redis client to connect to AWS ElastiCache Valkey
 * (or a local Redis instance in dev).
 *
 * Why Lettuce over Jedis?
 *   Lettuce is non-blocking, thread-safe, and shares a single connection across
 *   the whole application — Jedis creates a connection per thread which is wasteful.
 *
 * TLS:
 *   ElastiCache Serverless (Valkey) enforces TLS on port 6379.
 *   Set redis.tls=true in docker profile, false locally.
 *
 * Why StringRedisTemplate over RedisTemplate<String,Object>?
 *   We store everything as JSON strings explicitly — no Java serialization
 *   bytes leaking into Redis, and the goboxd worker (Go) can read the same keys.
 */
@Configuration
public class RedisConfig {

    @Value("${redis.host:localhost}")
    private String host;

    @Value("${redis.port:6379}")
    private int port;

    /**
     * true  → ElastiCache Serverless (docker/prod)
     * false → plain local Redis (local dev)
     */
    @Value("${redis.tls:false}")
    private boolean tls;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration serverConfig =
                new RedisStandaloneConfiguration(host, port);

        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
                LettuceClientConfiguration.builder()
                        // Fail fast: if ElastiCache is unreachable, commands throw
                        // within 2 s instead of blocking Tomcat threads for 60 s
                        // (the Lettuce default), which would starve health-check polls.
                        .commandTimeout(Duration.ofSeconds(2))
                        // Don't delay JVM shutdown waiting for Lettuce to drain.
                        .shutdownTimeout(Duration.ZERO);

        if (tls) {
            // useSsl() enables TLS; ElastiCache has a valid AWS cert so peer
            // verification passes without any extra trust store config.
            builder.useSsl();
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(serverConfig, builder.build());
        // Do not block startup validating the connection — the app should start
        // even if Redis is temporarily unreachable.  Individual callers get a
        // fast RedisCommandTimeoutException instead of an infinite hang.
        factory.setValidateConnection(false);
        return factory;
    }

    /**
     * StringRedisTemplate: keys and values are plain UTF-8 strings.
     * The goboxd worker (Go) reads these same keys using go-redis — no
     * Java-specific serialisation headers, fully interoperable.
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }
}
