package js.log;

/**
 * Logging level, also known as priority. Every level has a priority index; bigger the priority index more verbose logging. For
 * your convenience here are logging levels in verbosity order: TRACE, DEBUG, INFO, WARN, ERROR, FATAL, BUG, OFF.
 * <p>
 * Note that when enable a level all less verbose levels are enabled too. For example if one enable INFO level warnings are
 * recorder too. In order to disable a logger set its level to OFF.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public enum LogLevel {
	/** 7 - The same as {@link #DEBUG} but with finer granularity. */
	TRACE,

	/** 6 - Fine-grained informational events that are most useful to debug an application. */
	DEBUG,

	/** 5 - Informational messages that highlight the progress of the application at coarse-grained level. */
	INFO,

	/** 4 - Potentially harmful situations. */
	WARN,

	/** 3 - Error events that might still allow the application to continue running. */
	ERROR,

	/** 2 - Very severe error events that will presumably lead the application to abort. */
	FATAL,

	/** 1 - Unexpected condition likely to be a bug. */
	BUG,

	/** 0 - Log is disabled. */
	OFF
}
