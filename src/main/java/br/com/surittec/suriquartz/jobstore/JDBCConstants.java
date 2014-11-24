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

import org.quartz.impl.jdbcjobstore.StdJDBCConstants;

public interface JDBCConstants extends StdJDBCConstants{

	String TABLE_AUDIT = "AUDIT";
	
	String COL_ID = "ID";
	
	String INSERT_AUDIT = "INSERT INTO "
            + TABLE_PREFIX_SUBST + TABLE_AUDIT + " (" 
            + COL_ID + ", " + COL_SCHEDULER_NAME + ", " 
            + COL_JOB_NAME + ", " + COL_JOB_GROUP + ", " 
            + COL_TRIGGER_NAME + ", " + COL_TRIGGER_GROUP + ", "
            + COL_START_TIME + ", " + COL_END_TIME+ ") " 
            + " VALUES(?, " + SCHED_NAME_SUBST + ", ?, ?, ?, ?, ?, ?)";
	
}
