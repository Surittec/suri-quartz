package br.com.surittec.suriquartz.util;

import java.lang.reflect.Field;

import org.quartz.Scheduler;
import org.quartz.core.QuartzScheduler;
import org.quartz.core.QuartzSchedulerResources;
import org.quartz.impl.RemoteScheduler;
import org.quartz.impl.StdScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.surittec.suriquartz.spi.AuditStore;

public class AuditStoreUtil {

	private static final Logger log = LoggerFactory.getLogger(AuditStoreUtil.class);

	public static AuditStore getAuditStore(Scheduler scheduler) {
		try {

			QuartzScheduler qs = null;

			if (scheduler instanceof StdScheduler) {
				Field qsField = StdScheduler.class.getDeclaredField("sched");
				qsField.setAccessible(true);
				qs = (QuartzScheduler) qsField.get(scheduler);

			} else if (scheduler instanceof RemoteScheduler) {
				Field rsField = RemoteScheduler.class.getDeclaredField("rsched");
				rsField.setAccessible(true);
				qs = (QuartzScheduler) rsField.get(scheduler);
			}

			Field qsrField = QuartzScheduler.class.getDeclaredField("resources");
			qsrField.setAccessible(true);

			QuartzSchedulerResources qsr = (QuartzSchedulerResources) qsrField.get(qs);

			if (qsr.getJobStore() instanceof AuditStore) {
				return (AuditStore) qsr.getJobStore();
			} else {
				log.warn("Plugin AuditListener[%s] not active because job store is not Audit JDBC-based ");
			}

		} catch (Exception e) {
			log.error("", e);
		}

		return null;
	}

}
