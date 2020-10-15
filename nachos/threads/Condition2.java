package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
	
	waitQueue = new LinkedList<KThread>();
	
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	Machine.interrupt().disable();
	boolean interrupt = Machine.interrupt().enabled();
	KThread thread = KThread.currentThread();
	
	waitQueue.add(thread);
	
	conditionLock.release();
	KThread.sleep();
	conditionLock.acquire();
	
	Machine.interrupt().restore(interrupt);
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	Machine.interrupt().disable();
	KThread thread = KThread.currentThread();
	
	if(!waitQueue.isEmpty()) {
		waitQueue.pop();
		thread = waitQueue.getFirst();
	}
	Machine.interrupt().enable();
	
    }

    /*
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	Machine.interrupt().disable();
	while (!waitQueue.isEmpty())
	    wake();
	Machine.interrupt().enable();
    }

    private Lock conditionLock;
    private LinkedList<KThread> waitQueue;
}
