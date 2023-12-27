/**
 * Atom Sql Spring
 */
module jp.ats.atomsql.spring {

	requires jp.ats.atomsql;

	requires spring.beans;

	requires spring.boot;

	requires transitive spring.context;

	requires spring.jdbc;

	requires spring.core;

	requires spring.jcl;

	requires spring.tx;

	exports jp.ats.atomsql.spring;
}
