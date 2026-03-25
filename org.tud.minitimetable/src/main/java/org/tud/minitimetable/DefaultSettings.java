package org.tud.minitimetable;

import org.tud.minitimetable.extern.solver.MiniZinc;
import org.tud.minitimetable.extern.solver.ProcessLogger.DefaultFileLog;

public class DefaultSettings {

	/**
	 * 10 Minutes
	 */
	public static final Long DEFAULT_TIME_LIMIT_IN_MS = 10l * 60l * 1000l;

	/**
	 * 4 Threads
	 */
	public static final Integer DEFAULT_NUMBER_OF_THREADS = 4;

	public static void applyDefaultMiniZincConfiguration(MiniZinc minizinc) {

//		minizinc.getConfig().runnerLog = new DefaultFileLog("%s-log");
		minizinc.getConfig().backendLog = new DefaultFileLog("%s-backend");
		minizinc.getConfig().solverOutput = new DefaultFileLog("%s-solution");
		minizinc.getConfig().solverModelLog = new DefaultFileLog("%s-lp.lp", true);

		minizinc.getConfig().timeLimitMS = DefaultSettings.DEFAULT_TIME_LIMIT_IN_MS;
		minizinc.getConfig().threads = DefaultSettings.DEFAULT_NUMBER_OF_THREADS;

	}

}
