package nachos.threads;

import nachos.machine.*;
import nachos.threads.PriorityScheduler.ThreadState;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

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
    	int ticketcount = 0;
    	PriorityQueue pq;
    
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
    	
    
    
    protected ThreadState getThreadState(KThread thread) {
    	return LotteryPS.getThreadState(thread);
    }
    public int getPriority(KThread thread) {
    	return LotteryPS.getPriority(thread);
    }
    public void setPriority(KThread thread, int priority) {
    	LotteryPS.setPriority(thread,priority);
    }
    public boolean increasePriority() {
    	return LotteryPS.increasePriority();
    }
    public boolean decreasePriority() {
    	return LotteryPS.decreasePriority();
    }
    protected class LotteryQueue extends ThreadQueue {
    	
    	LotteryQueue(boolean transferPriority) {
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
    	protected ThreadState Holder = null;
    	//Holder for the threads in ThreadState
    	protected int effectivePriority = priorityMinimum;
    	//integer holding eff priority 0
    	protected LinkedList<ThreadState> PQueue = new LinkedList<ThreadState>();
    	//LinkedList of ThreadState that tracks all the threads in the waiting queue.
        }
    protected PriorityScheduler LotteryPS;
}
