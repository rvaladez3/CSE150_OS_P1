package nachos.threads;

import nachos.machine.*;


import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.*;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }
    
    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);
	
	getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMaximum)
	    return false;

	setPriority(thread, priority+1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    public boolean decreasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMinimum)
	    return false;

	setPriority(thread, priority-1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;    

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
	if (thread.schedulingState == null)
	    thread.schedulingState = new ThreadState(thread);

	return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {
	PriorityQueue(boolean transferPriority) {
	    this.transferPriority = transferPriority;
	    this.PQueue = new LinkedList<ThreadState>();
	}

	public void waitForAccess(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    ThreadState nextThread = getThreadState(thread);
	    this.PQueue.add(nextThread);
	    nextThread.waitForAccess(this);
	}

	public void acquire(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    //getThreadState(thread).acquire(this);
	    ThreadState nextThread = getThreadState(thread);
	    if(this.Holder != null) {
	    	this.Holder.release(this);
	    }
	    this.Holder = nextThread;
	    nextThread.acquire(this);
	}

	public KThread nextThread() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me
	    ThreadState nextThread = this.pickNextThread();
	    
	    if(nextThread == null) {
	    	return null;
	    }
	    this.PQueue.remove(nextThread);
	    this.acquire(nextThread.fetchThread());
	    
	    return nextThread.fetchThread();
	}

	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
	protected ThreadState pickNextThread() {
		int nextPrior = priorityMinimum;
		
		ThreadState nextThread = null;
		
		for(ThreadState currentThread: this.PQueue) {
			int currentPrior = currentThread.getEffectivePriority();
			if(nextThread == null || nextPrior < currentPrior) {
				nextThread = currentThread;
				nextPrior = currentPrior;
			}
		}
	    // implement me
	    return nextThread;
	}
	
	public void print() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    /*for(ThreadState nextThread: this.PQueue) {
	    	System.out.println(nextThread.getEffectivePriority());
	    } */
	    // implement me (if you want)
	}
	
	public int getEffectivePriority() {
		if(!this.transferPriority) {
			return priorityMinimum;
		}
		else if(this.valid_bit) {
			this.effectivePriority = priorityMinimum;
			for(ThreadState currentThread: this.PQueue) {
				this.effectivePriority = Math.max(this.effectivePriority, currentThread.getEffectivePriority());
				////Takes max prior of queue of threads and stores into effectprior
			}
			this.valid_bit = false;
		}
		return effectivePriority;
	}
	
	private void remove() {
		if(!this.transferPriority) {
			return;
		}
		this.valid_bit = true;
		if(this.Holder != null) {
			Holder.remove();
		}
	}

	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
	public boolean transferPriority;
	protected boolean valid_bit = false;
	//Check if the eff priority is invalidated
	protected ThreadState Holder = null;
	//Holder for the threads in ThreadState
	protected int effectivePriority = priorityMinimum;
	//integer holding eff priority 0
	protected LinkedList<ThreadState> PQueue = new LinkedList<ThreadState>();
	//LinkedList of ThreadState that tracks all the threads in the waiting queue.
    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param	thread	the thread this state belongs to.
	 */
	public ThreadState(KThread thread) {
	    this.thread = thread;
	    this.resHaveCurr = new LinkedList<PriorityQueue>();
	    this.resWaitFor = new LinkedList<PriorityQueue>();
	    
	    setPriority(priorityDefault);
	}

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	public int getPriority() {
	    return priority;
	}

	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	public int getEffectivePriority() {
	    // implement me
		if(this.resHaveCurr.isEmpty()) {
			return this.getPriority();
		}
		else if(this.valid_bit) {
			this.effectivePriority = this.getPriority();
			for(PriorityQueue e : this.resHaveCurr) {
				this.effectivePriority = Math.max(this.effectivePriority, e.getEffectivePriority());
				//Takes max prior of queue of threads and stores into effectprior
			}
			this.valid_bit = false;
		}
	    return this.effectivePriority;
	}

	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {
	    if (this.priority == priority)
		return;
	    
	    this.priority = priority;
	    
	    for(PriorityQueue e: resWaitFor) {
	    	e.remove();
	    }
	    // implement me
	}

	/**
	 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
	 * the associated thread) is invoked on the specified priority queue.
	 * The associated thread is therefore waiting for access to the
	 * resource guarded by <tt>waitQueue</tt>. This method is only called
	 * if the associated thread cannot immediately obtain access.
	 *
	 * @param	waitQueue	the queue that the associated thread is
	 *				now waiting on.
	 *
	 * @see	nachos.threads.ThreadQueue#waitForAccess
	 */
	public void waitForAccess(PriorityQueue waitQueue) {
	    // implement me
		this.resWaitFor.add(waitQueue);
		this.resHaveCurr.remove(waitQueue);
		waitQueue.remove();

	}

	/**
	 * Called when the associated thread has acquired access to whatever is
	 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
	 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
	 * <tt>thread</tt> is the associated thread), or as a result of
	 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
	 *
	 * @see	nachos.threads.ThreadQueue#acquire
	 * @see	nachos.threads.ThreadQueue#nextThread
	 */
	public void acquire(PriorityQueue waitQueue) {
	    // implement me
		this.resHaveCurr.add(waitQueue);
		this.resWaitFor.remove(waitQueue);
		this.remove();
	}	
	
	public KThread fetchThread() {
		return thread;
	}
	
	public void release(PriorityQueue waitQueue) {
		this.resHaveCurr.remove(waitQueue);
		this.remove();
	}
	
	private void remove() {
		if(this.valid_bit) {
			return;
		}
		this.valid_bit = true;
		for (PriorityQueue e: this.resWaitFor) {
			e.remove();
		}
	}

	/** The thread with which this object is associated. */	   
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority;
	protected boolean valid_bit = false;
	//check if eff priority is invalidated in this ThreadState
	protected int effectivePriority = priorityMinimum;
	//integer holding eff priority 0
	protected LinkedList<PriorityQueue> resHaveCurr = new LinkedList<PriorityQueue>();
	//LinkedList of priorityqueue that tracks resources that are currently held
	protected LinkedList<PriorityQueue> resWaitFor = new LinkedList<PriorityQueue>();
	//LinkedList of priorityqueue that tracks resources that are being waited
    }
}


