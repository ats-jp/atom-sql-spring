package jp.ats.atomsql.spring;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.springframework.jdbc.core.JdbcTemplate;

import jp.ats.atomsql.BatchPreparedStatementSetter;
import jp.ats.atomsql.Constants;
import jp.ats.atomsql.Executor;
import jp.ats.atomsql.PreparedStatementSetter;
import jp.ats.atomsql.RowMapper;

/**
 * @author 千葉 哲嗣
 */
class JdbcTemplateExecutor implements Executor {

	private final JdbcTemplate jdbcTemplate;

	/**
	 * @param jdbcTemplate
	 */
	public JdbcTemplateExecutor(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
	}

	/**
	 * @see JdbcTemplate#batchUpdate(String, org.springframework.jdbc.core.BatchPreparedStatementSetter)
	 * @param sql
	 * @param bpss
	 */
	@Override
	public void batchUpdate(String sql, BatchPreparedStatementSetter bpss) {
		// MySQLのPareparedStatement#toString()対策でSQLの先頭に改行を付与
		jdbcTemplate.batchUpdate(Constants.NEW_LINE + sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {

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
	 * @param <T>
	 * @param sql
	 * @param pss
	 * @param rowMapper
	 * @return {@link Stream}
	 */
	@Override
	public <T> Stream<T> queryForStream(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) {
		// MySQLのPareparedStatement#toString()対策でSQLの先頭に改行を付与
		return jdbcTemplate.queryForStream(Constants.NEW_LINE + sql, (ps) -> pss.setValues(ps), (rs, rowNum) -> rowMapper.mapRow(rs, rowNum));
	}

	/**
	 * @see JdbcTemplate#update(String, org.springframework.jdbc.core.PreparedStatementSetter)
	 * @param sql
	 * @param pss
	 * @return int
	 */
	@Override
	public int update(String sql, PreparedStatementSetter pss) {
		// MySQLのPareparedStatement#toString()対策でSQLの先頭に改行を付与
		return jdbcTemplate.update(Constants.NEW_LINE + sql, (ps) -> pss.setValues(ps));
	}

	@Override
	public void logSql(Log log, String originalSql, String sql, boolean insecure, PreparedStatement ps) {
		if (insecure) {
			log.info(originalSql);
		} else {
			log.info(ps.toString());
		}
	}
}
