package il.ac.hit.project.main.model.report;

/**
 * Immutable snapshot of a task counts per status for export.
 * Acts as a simple transport object; contains no I/O or formatting logic.
 *
 * @param completedTasks number of completed tasks (>= 0)
 * @param inProgressTasks number of tasks currently in progress (>= 0)
 * @param todoTasks number of tasks not yet started (>= 0)
 */

public record ReportData(long completedTasks, long inProgressTasks, long todoTasks) {}

