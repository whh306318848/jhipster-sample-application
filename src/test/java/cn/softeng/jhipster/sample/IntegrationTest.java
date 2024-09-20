package cn.softeng.jhipster.sample;

import cn.softeng.jhipster.sample.config.AsyncSyncConfiguration;
import cn.softeng.jhipster.sample.config.EmbeddedElasticsearch;
import cn.softeng.jhipster.sample.config.EmbeddedRedis;
import cn.softeng.jhipster.sample.config.EmbeddedSQL;
import cn.softeng.jhipster.sample.config.JacksonConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { JhipsterSampleApplicationApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class })
@EmbeddedRedis
@EmbeddedElasticsearch
@EmbeddedSQL
public @interface IntegrationTest {
}
