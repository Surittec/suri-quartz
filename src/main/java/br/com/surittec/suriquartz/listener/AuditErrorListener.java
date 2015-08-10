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
package br.com.surittec.suriquartz.listener;

import java.util.Arrays;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.JobPersistenceException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.surittec.suriquartz.annotation.Audit;
import br.com.surittec.suriquartz.util.AuditStoreUtil;
import br.com.surittec.util.exception.BusinessException;
import br.com.surittec.util.exception.ExceptionUtil;
import br.com.surittec.util.message.Message;

public class AuditErrorListener implements JobListener, SchedulerPlugin {

	private static final String LISTENER_NAME = "AuditErrorListener";

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public String getName() {
		return LISTENER_NAME;
	}

	@Override
	public void initialize(String name, Scheduler scheduler, ClassLoadHelper loadHelper) throws SchedulerException {
		scheduler.getListenerManager().addJobListener(new AuditErrorListener());
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		try {

			Audit audit = context.getJobDetail().getJobClass().getAnnotation(Audit.class);
			if (audit != null && !jobException.getMessage().equals("")) {
				TriggerKey triggerKey =
						(context.getPreviousFireTime() != null || context.getNextFireTime() != null) ? context.getTrigger().getKey() : null;

				if (audit.onlyTemporaryTrigger() && triggerKey != null)
					return;

				long fireTime = context.getFireTime().getTime();
				AuditStoreUtil.getAuditStore(context.getScheduler()).storeAuditError(context.getTrigger().getJobKey(),
						triggerKey, fireTime, fireTime + context.getJobRunTime(), getStackTrace(jobException), getMessages(jobException));
			}
		} catch (JobPersistenceException e) {
			log.error("Couldn't audit job: " + e.getMessage(), e);
		}
	}

	@Override
	public void start() {
		log.info(String.format("Starting Audit error Listener: %s", LISTENER_NAME));
	}

	@Override
	public void shutdown() {
		log.info(String.format("Stopping Audit error Listener: %s", LISTENER_NAME));
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {

	}

	/*
	 * Private Methods
	 */

	private String getStackTrace(Throwable e) {
		if (e.getCause().getCause() == null) {
			return ExceptionUtil.getStackTrace(e);
		} else {
			return ExceptionUtil.getStackTrace(e.getCause().getCause());
		}
	}

	private List<Message> getMessages(Throwable e) {
		if (e.getCause().getCause() != null && e.getCause().getCause() instanceof BusinessException) {
			return ((BusinessException) e.getCause().getCause()).getErrors();
		} else
			return Arrays.asList(new Message("", null, e.getMessage(), null));
	}
}
