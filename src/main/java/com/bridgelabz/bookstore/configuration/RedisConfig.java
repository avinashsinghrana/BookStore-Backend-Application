package com.bridgelabz.bookstore.configuration;

import com.bridgelabz.bookstore.BookStoreBackendApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactorySupplier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.redis.host}")
    private String HOST_NAME;

    @Value("${spring.redis.port}")
    private int PORT;

    //    @Bean
//    protected JedisConnectionFactory jedisConnectionFactory() {
//        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHostName, redisPort);
//        configuration.setPassword(RedisPassword.of("password"));
//        JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder().usePooling().build();
//        JedisConnectionFactory factory = new JedisConnectionFactory(configuration, jedisClientConfiguration);
//        Objects.requireNonNull(factory.getPoolConfig()).setMaxIdle(30);
//        factory.getPoolConfig().setMinIdle(10);
//        factory.afterPropertiesSet();
//        return factory;
//    }
//
////	@Bean
////	JedisConnectionFactory jedisConnectionFactory() {
////		JedisConnectionFactory factory = new JedisConnectionFactory();
////		factory.setHostName(redisHostName);
////		factory.setPort(redisPort);
////		factory.setUsePool(true);
////		return factory;
////	}
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate() {
//        final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setHashKeySerializer(new GenericToStringSerializer<Object>(Object.class));
//        redisTemplate.setHashValueSerializer(new JdkSerializationRedisSerializer());
//        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
//        redisTemplate.setConnectionFactory(jedisConnectionFactory());
//        redisTemplate.setEnableTransactionSupport(true);
//        return redisTemplate;
//    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.additionalMessageConverters(new MappingJackson2HttpMessageConverter()).additionalMessageConverters(new StringHttpMessageConverter())
                .requestFactory(new ClientHttpRequestFactorySupplier()).build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        logger.info("CONNECTING REDIS SERVER .....");
        JedisConnectionFactory jedisConFactory = new JedisConnectionFactory();
        jedisConFactory.setHostName(HOST_NAME);
        logger.info("REDIS HOST :> " + HOST_NAME);
        jedisConFactory.setPort(Integer.valueOf(PORT));
        logger.info("REDIS PORT :> " + PORT);
        logger.info("REDIS SERVER CONNECTED");
        return jedisConFactory;
    }
}
