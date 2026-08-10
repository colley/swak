package com.swak.jdbc.spi;

import com.swak.common.spi.SpiPriority;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * AbstractSwakJdbcTemplate.java
 *
 * @author colley.ma
 * @since 2.3.3
 */
public abstract class AbstractSwakJdbcTemplate extends JdbcTemplate implements SwakJdbcTemplate {
	protected int priority = SpiPriority.SPI_PRIORITY;
	protected String name = SpiPriority.SPI_NAME;

	public AbstractSwakJdbcTemplate() {
		super();
	}

	public AbstractSwakJdbcTemplate(DataSource dataSource) {
		super(dataSource);
	}

	public AbstractSwakJdbcTemplate(DataSource dataSource, boolean lazyInit) {
		super(dataSource, lazyInit);
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public  int priority() {
		return this.priority;
	}


	@Override
	public  String getName() {
		return this.name;
	}
}
