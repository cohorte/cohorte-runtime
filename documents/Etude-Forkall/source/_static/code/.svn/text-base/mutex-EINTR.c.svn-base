/*
 * From kernel/mutex.c
 * 	function __mutex_lock_common()
 *	around line 245
 */

for (;;) {
	if (atomic_xchg(&lock->count, -1) == 1)
		break;

	/*
	 * got a signal? (This code gets eliminated in the
	 * TASK_UNINTERRUPTIBLE case.)
	 */
	if (unlikely(signal_pending_state(state, task))) {
		mutex_remove_waiter(lock, &waiter,
					task_thread_info(task));
		mutex_release(&lock->dep_map, 1, ip);
		spin_unlock_mutex(&lock->wait_lock, flags);

		debug_mutex_free_waiter(&waiter);
		preempt_enable();
		return -EINTR;
	}
	__set_task_state(task, state);

	/* didnt get the lock, go to sleep: */
	spin_unlock_mutex(&lock->wait_lock, flags);
	preempt_enable_no_resched();
	schedule();
	preempt_disable();
	spin_lock_mutex(&lock->wait_lock, flags);
}
