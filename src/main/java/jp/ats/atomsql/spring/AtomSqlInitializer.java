package jp.ats.atomsql.spring;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import jp.ats.atomsql.AtomSql;
import jp.ats.atomsql.Configure;
import jp.ats.atomsql.Endpoints;
import jp.ats.atomsql.PropertiesConfigure;
import jp.ats.atomsql.SimpleConfigure;
import jp.ats.atomsql.Utils;
import jp.ats.atomsql.annotation.SqlProxy;

/**
 * Atom SQLをSpringで使用できるように初期化するクラスです。<br>
 * {@link SqlProxy}が付与されたクラスを{@link Autowired}可能にします。<br>
 * application.propertiesにプレフィックスatomsqlを付加することで各設定を記述することが可能です<br>
 * @see SpringApplication#addInitializers(ApplicationContextInitializer...)
 * @author 千葉 哲嗣
 */
public class AtomSqlInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

	@Override
	public void initialize(GenericApplicationContext context) {
		List<Class<?>> classes;
		try {
			classes = Utils.loadProxyClasses();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		BeanDefinitionCustomizer customizer = bd -> {
			bd.setScope(BeanDefinition.SCOPE_SINGLETON);
			bd.setLazyInit(true);
			bd.setAutowireCandidate(true);
			bd.setPrimary(true);
		};

		context.registerBean(AtomSql.class, () -> new AtomSql(configure(context), endpoints(context)), customizer);

		classes.forEach(c -> {
			@SuppressWarnings("unchecked")
			var casted = (Class<Object>) c;
			context.registerBean(casted, () -> context.getBean(AtomSql.class).of(casted), customizer);
		});
	}

	private static Configure configure(GenericApplicationContext context) {
		var environment = context.getEnvironment();
		var enableLog = environment.getProperty("atomsql.enable-log", Boolean.class);

		if (enableLog == null) return new PropertiesConfigure();

		var logStackTracePattern = environment.getProperty("atomsql.log-stacktrace-pattern");

		return new SimpleConfigure(enableLog, Pattern.compile(logStackTracePattern));
	}

	private static Endpoints endpoints(GenericApplicationContext context) {
		var map = context.getBeansOfType(JdbcTemplate.class);
		var primary = context.getBean(JdbcTemplate.class);

		if (map.size() == 1) {
			return new Endpoints(new JdbcTemplateEndpoint(primary));
		}

		var entries = map.entrySet().stream().map(e -> {
			var jdbcTemplate = e.getValue();
			return new Endpoints.Entry(e.getKey(), new JdbcTemplateEndpoint(jdbcTemplate), jdbcTemplate == primary);
		}).toArray(Endpoints.Entry[]::new);

		return new Endpoints(entries);
	}
}
