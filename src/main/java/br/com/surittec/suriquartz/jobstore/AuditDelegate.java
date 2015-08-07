/*
 * SURITTEC
 * Copyright 2014, SURITTEC CONSULTORIA LTDA, 
 * and individual contributors as indicated by the @authors tag
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package br.com.surittec.suriquartz.jobstore;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.quartz.impl.jdbcjobstore.Util;

import br.com.surittec.util.message.Message;

public class AuditDelegate implements JDBCConstants {

	protected String tablePrefix;
	protected String schedName;
	protected String instanceId;

	public AuditDelegate(String tablePrefix, String schedName, String instanceId) {
		this.tablePrefix = tablePrefix;
		this.schedName = schedName;
		this.instanceId = instanceId;
	}

	public int audit(Connection conn, JobKey jobKey, TriggerKey triggerKey, long startTime, long endTime)
			throws IOException, SQLException {

		PreparedStatement ps = null;

		int insertResult = 0;

		try {
			ps = conn.prepareStatement(rtp(INSERT_AUDIT));

			ps.setString(1, UUID.randomUUID().toString());
			ps.setString(2, jobKey.getName());
			ps.setString(3, jobKey.getGroup());

			if (triggerKey != null) {
				ps.setString(4, triggerKey.getName());
				ps.setString(5, triggerKey.getGroup());
			} else {
				ps.setNull(4, Types.VARCHAR);
				ps.setNull(5, Types.VARCHAR);
			}

			ps.setBigDecimal(6, new BigDecimal(String.valueOf(startTime)));
			ps.setBigDecimal(7, new BigDecimal(String.valueOf(endTime)));

			insertResult = ps.executeUpdate();
		} finally {
			closeStatement(ps);
		}

		return insertResult;
	}

	public int auditError(Connection conn, JobKey jobKey, TriggerKey triggerKey, long startTime, long endTime, String stacktrace,
			List<Message> errosMessage)
			throws IOException, SQLException {

		PreparedStatement ps = null;
		PreparedStatement psInsertMsg = null;

		int insertResult = 0;

		try {
			ps = conn.prepareStatement(rtp(INSERT_AUDIT_ERROR));

			String idAuditError = UUID.randomUUID().toString();

			ps.setString(1, idAuditError);
			ps.setString(2, jobKey.getName());
			ps.setString(3, jobKey.getGroup());

			if (triggerKey != null) {
				ps.setString(4, triggerKey.getName());
				ps.setString(5, triggerKey.getGroup());
			} else {
				ps.setNull(4, Types.VARCHAR);
				ps.setNull(5, Types.VARCHAR);
			}

			ps.setBigDecimal(6, new BigDecimal(String.valueOf(startTime)));
			ps.setBigDecimal(7, new BigDecimal(String.valueOf(endTime)));
			ps.setString(8, stacktrace);

			insertResult = ps.executeUpdate();

			psInsertMsg = conn.prepareStatement(rtp(INSERT_AUDIT_ERROR_MSG));
			for (Message message : errosMessage) {
				psInsertMsg.setString(1, UUID.randomUUID().toString());
				psInsertMsg.setString(2, idAuditError);
				psInsertMsg.setString(3, message.getCode());
				psInsertMsg.setString(4, message.getMessage());
				psInsertMsg.executeUpdate();
			}

		} finally {
			closeStatement(ps);
			closeStatement(psInsertMsg);
		}

		return insertResult;
	}

	/**
	 * Cleanup helper method that closes the given <code>Statement</code> while
	 * ignoring any errors.
	 */
	protected static void closeStatement(Statement statement) {
		if (null != statement) {
			try {
				statement.close();
			} catch (SQLException ignore) {
			}
		}
	}

	/**
	 * <p>
	 * Replace the table prefix in a query by replacing any occurrences of "{0}"
	 * with the table prefix.
	 * </p>
	 * 
	 * @param query
	 *            the unsubstitued query
	 * @return the query, with proper table prefix substituted
	 */
	protected final String rtp(String query) {
		return Util.rtp(query, tablePrefix, getSchedulerNameLiteral());
	}

	private String schedNameLiteral = null;

	protected String getSchedulerNameLiteral() {
		if (schedNameLiteral == null)
			schedNameLiteral = "'" + schedName + "'";
		return schedNameLiteral;
	}
}
