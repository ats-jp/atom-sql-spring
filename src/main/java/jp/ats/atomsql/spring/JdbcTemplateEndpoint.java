package jp.ats.atomsql.spring;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import jp.ats.atomsql.BatchPreparedStatementSetter;
import jp.ats.atomsql.ConnectionProxy;
import jp.ats.atomsql.Constants;
import jp.ats.atomsql.Endpoint;
import jp.ats.atomsql.PreparedStatementSetter;
import jp.ats.atomsql.RowMapper;
import jp.ats.atomsql.SimpleConnectionProxy;

/**
 * @author 千葉 哲嗣
 */
class JdbcTemplateEndpoint implements Endpoint {

	private final JdbcTemplate jdbcTemplate;

	/**
	 * @param jdbcTemplate
	 */
	public JdbcTemplateEndpoint(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
	}

	/**
	 * @see JdbcTemplate#batchUpdate(String, org.springframework.jdbc.core.BatchPreparedStatementSetter)
	 */
	@Override
	public int[] batchUpdate(String sql, BatchPreparedStatementSetter bpss) {
		// MySQLのPareparedStatement#toString()対策でSQLの先頭に改行を付与
		return jdbcTemplate.batchUpdate(Constants.NEW_LINE + sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				bpss.setValues(ps, i);
			}

			@Override
			public int getBatchSize() {
				return bpss.getBatchSize();
			}
		});

	}

	/**
	 * @see JdbcTemplate#queryForStream(String, org.springframework.jdbc.core.PreparedStatementSetter, org.springframework.jdbc.core.RowMapper)
	 */
	@Override
	public <T> Stream<T> queryForStream(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) {
		// MySQLのPareparedStatement#toString()対策でSQLの先頭に改行を付与
		return jdbcTemplate.queryForStream(Constants.NEW_LINE + sql, (ps) -> pss.setValues(ps), (rs, rowNum) -> rowMapper.mapRow(rs, rowNum));
	}

	/**
	 * @see JdbcTemplate#update(String, org.springframework.jdbc.core.PreparedStatementSetter)
	 */
	@Override
	public int update(String sql, PreparedStatementSetter pss) {
		// MySQLのPareparedStatement#toString()対策でSQLの先頭に改行を付与
		return jdbcTemplate.update(Constants.NEW_LINE + sql, (ps) -> pss.setValues(ps));
	}

	@Override
	public void logSql(Log log, String originalSql, String sql, PreparedStatement ps) {
		log.info("sql:" + Constants.NEW_LINE + ps.toString());
	}

	@Override
	public void bollowConnection(Consumer<ConnectionProxy> consumer) {
		jdbcTemplate.execute((ConnectionCallback<Object>) con -> {
			consumer.accept(new SimpleConnectionProxy(con));

			return null;
		});
	}
}
