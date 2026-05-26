package com.cfc.platform.ConfigSecurity;

import io.lettuce.core.ClientOptions;
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

        // REJECT_COMMANDS: when the Lettuce connection is in the DISCONNECTED
        // state, every Redis command throws RedisException immediately (< 1ms)
        // instead of being queued in Lettuce's internal buffer and blocking the
        // calling thread until commandTimeout expires.  Without this, a burst of
        // health-check / API requests while Redis is unreachable fills the Tomcat
        // thread pool with threads all sleeping for up to 60 s (the default
        // commandTimeout), causing subsequent health checks to time out and ECS
        // to restart the container in an infinite loop.
        ClientOptions clientOptions = ClientOptions.builder()
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .autoReconnect(true)          // keep trying to reconnect in background
                .build();

        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
                LettuceClientConfiguration.builder()
                        .clientOptions(clientOptions)
                        // Belt-and-suspenders: if a command somehow reaches a connected
                        // but slow Redis, fail after 2 s instead of 60 s.
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
        // fast RedisException (REJECT_COMMANDS) instead of an infinite hang.
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
