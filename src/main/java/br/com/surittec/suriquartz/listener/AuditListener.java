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

import org.quartz.JobExecutionContext;
import org.quartz.JobPersistenceException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.EverythingMatcher;
import org.quartz.listeners.TriggerListenerSupport;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;

import br.com.surittec.suriquartz.annotation.Audit;
import br.com.surittec.suriquartz.spi.AuditStore;
import br.com.surittec.suriquartz.util.AuditStoreUtil;

public class AuditListener extends TriggerListenerSupport implements SchedulerPlugin {

	private String name;

	private AuditStore auditStore;

	/*
	 * Public Methods
	 */

	@Override
	public void initialize(String name, Scheduler scheduler, ClassLoadHelper loadHelper) throws SchedulerException {
		this.name = name;
		this.auditStore = AuditStoreUtil.getAuditStore(scheduler);

		if (auditStore != null) {
			scheduler.getListenerManager().addTriggerListener(this, EverythingMatcher.allTriggers());
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction triggerInstructionCode) {
		try {

			Audit audit = context.getJobDetail().getJobClass().getAnnotation(Audit.class);
			if (audit != null) {
				TriggerKey triggerKey = (context.getPreviousFireTime() != null || context.getNextFireTime() != null) ? trigger.getKey() : null;

				if (audit.onlyTemporaryTrigger() && triggerKey != null)
					return;

				long fireTime = context.getFireTime().getTime();
				auditStore.storeAudit(trigger.getJobKey(), triggerKey, fireTime, fireTime + context.getJobRunTime());
			}

		} catch (JobPersistenceException e) {
			getLog().error("Couldn't audit job: " + e.getMessage(), e);
		}
	}

	@Override
	public void start() {
		getLog().info(String.format("Starting AuditListener: %s", name));
	}

	@Override
	public void shutdown() {
		getLog().info(String.format("Stoping AuditListener: %s", name));
	}

}
