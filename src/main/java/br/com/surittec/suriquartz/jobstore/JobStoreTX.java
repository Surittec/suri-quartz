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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.quartz.JobKey;
import org.quartz.JobPersistenceException;
import org.quartz.TriggerKey;

import br.com.surittec.suriquartz.spi.AuditStore;
import br.com.surittec.util.message.Message;

public class JobStoreTX extends org.quartz.impl.jdbcjobstore.JobStoreTX implements AuditStore {

	private AuditDelegate auditDelegate;

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void storeAudit(final JobKey jobKey, final TriggerKey triggerKey, final long startTime, final long endTime)
			throws JobPersistenceException {

		executeWithoutLock(new TransactionCallback() {
			public Object execute(Connection conn) throws JobPersistenceException {
				try {
					return getAuditDelegate().audit(conn, jobKey, triggerKey, startTime, endTime);
				} catch (IOException | SQLException e) {
					throw new JobPersistenceException("Couldn't audit job: " + e.getMessage(), e);
				}
			}
		});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void storeAuditError(final JobKey jobKey, final TriggerKey triggerKey, final long startTime, final long endTime, final String stacktrace,
			final List<Message> errosMessages) throws JobPersistenceException {

		executeWithoutLock(new TransactionCallback() {
			public Object execute(Connection conn) throws JobPersistenceException {
				try {
					return getAuditDelegate().auditError(conn, jobKey, triggerKey, startTime, endTime, stacktrace, errosMessages);
				} catch (IOException | SQLException e) {
					throw new JobPersistenceException("Couldn't audit job: " + e.getMessage(), e);
				}
			}
		});
	}

	protected AuditDelegate getAuditDelegate() {
		if (auditDelegate == null) {
			auditDelegate = new AuditDelegate(tablePrefix, instanceName, instanceId);
		}
		return auditDelegate;
	}

}
