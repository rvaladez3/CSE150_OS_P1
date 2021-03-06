package nachos.threads;

import nachos.machine.*;
import nachos.threads.LotteryScheduler.LotteryQueue;
import nachos.threads.PriorityScheduler.ThreadState;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.lang.Math;

/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
    /**
     * Allocate a new lottery scheduler.
     */
    public LotteryScheduler() {
    	TicketTrack = 0;
    //	LotteryQueue pq;
    
    	final int priorityDefault = 1;
        /**
         * The minimum priority that a thread can have. Do not change this value.
         */
        final int priorityMinimum = 0;
        /**
         * The maximum priority that a thread can have. Do not change this value.
         */
        final int priorityMaximum = Integer.MAX_VALUE;    
    }
    
    /**
     * Allocate a new lottery thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer tickets from waiting threads
     *					to the owning thread.
     * @return	a new lottery thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	// implement me	
    	return new LotteryQueue(transferPriority);

    }
    	
    
   
    protected ThreadState2 getThreadState2(KThread thread) {
    	if (thread.schedulingState == null)
    	    thread.schedulingState = new ThreadState2(thread);

    	return (ThreadState2) thread.schedulingState;
    }
    public int getPriority(KThread thread) {
    	return getPriority(thread);
    }
    public void setPriority(KThread thread, int priority) {
    	setPriority(thread,priority);
    }
    public boolean increasePriority() {
    	return increasePriority();
    }
    public boolean decreasePriority() {
    	return decreasePriority();
    }
    protected class LotteryQueue extends ThreadQueue {
    	
    	LotteryQueue(boolean transferPriority) {
    	    this.transferPriority = transferPriority;
    	    this.PQueue = new LinkedList<ThreadState2>();
    	}

    	public void waitForAccess(KThread thread) {
    	    Lib.assertTrue(Machine.interrupt().disabled());
    	    ThreadState2 nextThread = getThreadState2(thread);
    	    this.PQueue.add(nextThread);
    	    nextThread.waitForAccess(this);
    	}

    	public void acquire(KThread thread) {
    	    Lib.assertTrue(Machine.interrupt().disabled());
    	    //getThreadState2(thread).acquire(this);
    	    ThreadState2 nextThread = getThreadState2(thread);
    	    if(this.Holder != null) {
    	    	this.Holder.release(this);
    	    }
    	    this.Holder = nextThread;
    	    nextThread.acquire(this);
    	}

    	public KThread nextThread() {
    	    Lib.assertTrue(Machine.interrupt().disabled());
    	    // implement me
    	    ThreadState2 nextThread = this.pickNextThread();
    	    
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
    	protected ThreadState2 pickNextThread() {
    		
    	
    		int winningticket = 0 + ((int)Math.random()*TicketTrack); 
    		ThreadState2 nextThread = null;
    		
    		for(ThreadState2 currentThread: this.PQueue) {
    			if(currentThread.ticketS <= winningticket && currentThread.ticketE>=winningticket) {
    				return currentThread;
    			}
    		}
    		return null;
    	}
    	
    	public void print() {
    	    Lib.assertTrue(Machine.interrupt().disabled());
    	    /*for(ThreadState2 nextThread: this.PQueue) {
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
    			for(ThreadState2 currentThread: this.PQueue) {
    				this.effectivePriority += currentThread.getEffectivePriority();
    				////Takes max prior of queue of threads and stores into effectprior
    			}
    			this.valid_bit = false;
    		}
    		return effectivePriority;
    	}
    	public void remove() {
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
    	protected ThreadState2 Holder = null;
    	//Holder for the threads in ThreadState2
    	protected int effectivePriority = priorityMinimum;
    	//integer holding eff priority 0
    	protected LinkedList<ThreadState2> PQueue = new LinkedList<ThreadState2>();
    	//LinkedList of ThreadState2 that tracks all the threads in the waiting queue.
        }
    protected class ThreadState2 {
    	/**
    	 * Allocate a new <tt>ThreadState2</tt> object and associate it with the
    	 * specified thread.
    	 *
    	 * @param	thread	the thread this state belongs to.
    	 */
    	public ThreadState2(KThread thread) {
    	    this.thread = thread;
    	    this.resHaveCurr = new LinkedList<LotteryQueue>();
    	    this.resWaitFor = new LinkedList<LotteryQueue>();
    	    this.ticketS=-1;
        	this.ticketE=-1;
    	    
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
    		ticketS = TicketTrack+1;
    		if(this.resHaveCurr.isEmpty()) {
    			return this.getPriority();
    		}
    		else if(this.valid_bit) {
    			this.effectivePriority = this.getPriority();
    			for(LotteryQueue e : this.resHaveCurr) {
    				this.effectivePriority = Math.max(this.effectivePriority, e.getEffectivePriority());
    				//Takes max prior of queue of threads and stores into effectprior
    			}
    			this.valid_bit = false;
    		}
    		TicketTrack += this.effectivePriority;
    		ticketE = TicketTrack;

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
    	    
    	    for(LotteryQueue e: resWaitFor) {
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
    	public void waitForAccess(LotteryQueue waitQueue) {
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
    	public void acquire(LotteryQueue waitQueue) {
    	    // implement me
    		this.resHaveCurr.add(waitQueue);
    		this.resWaitFor.remove(waitQueue);
    		this.remove();
    	}	

    	public KThread fetchThread() {
    		return thread;
    	}
    	
    	public void release(LotteryQueue waitQueue) {
    		this.resHaveCurr.remove(waitQueue);
    		this.remove();
    	}

    	void remove() {
    		if(this.valid_bit) {
    			return;
    		}
    		this.valid_bit = true;
    		for (LotteryQueue e: this.resWaitFor) {
    			e.remove();
    		}
    	}


    	/** The thread with which this object is associated. */	   
    	protected KThread thread;
    	int ticketS= 0;
    	int ticketE= 0;
    	/** The priority of the associated thread. */
    	protected int priority;
    	protected boolean valid_bit = false;
    	//check if eff priority is invalidated in this ThreadState2
    	protected int effectivePriority = priorityMinimum;
    	//integer holding eff priority 0
    	protected LinkedList<LotteryQueue> resHaveCurr = new LinkedList<LotteryQueue>();

    	//LinkedList of LotteryQueue that tracks resources that are currently held
    	protected LinkedList<LotteryQueue> resWaitFor = new LinkedList<LotteryQueue>();
    	//LinkedList of LotteryQueue that tracks resources that are being waited
    	



        }
    int TicketTrack;
}
