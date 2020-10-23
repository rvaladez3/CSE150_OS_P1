package nachos.threads;

import nachos.machine.*;

import java.util.Map.Entry;
import java.util.TreeMap;
/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
	this.waitLock = new Lock();
	this.waitQ = new TreeMap<Long, KThread>();
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
    long time = Machine.timer().getTime();
	boolean interrupt = Machine.interrupt().disabled();
	if(this.waitQ.size()>=1) {
	//	System.out.print(" time:"+this.waitQ.firstKey() + "at: "+time);

		while (this.waitQ.size() > 0 && this.waitQ.firstKey() <= time) {
		//		System.out.println("MONKA IT MADE IT");
				this.waitQ.get(this.waitQ.firstKey()).ready();
				waitQ.remove(this.waitQ.firstKey());
			}
		}
	
	KThread.yield();
	Machine.interrupt().restore(interrupt);
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	// for now, cheat just to get something working (busy waiting is bad)
	long wakeTime = Machine.timer().getTime() + x;
//	System.out.println("waiting for: "+wakeTime);
	KThread k = KThread.currentThread();
	if( x<= 0) {
	//	System.out.println("MONKA IT MADE IT");

		return;
	}
//	waitLock.acquire();
    boolean interrupt = Machine.interrupt().disable();

//	System.out.print("WaitQ size before:" + this.waitQ.size());

	this.waitQ.put(wakeTime,KThread.currentThread());
//	System.out.print("WaitQ size:" + this.waitQ.size());

	KThread.sleep();
//	System.out.println("IIIr here");
    Machine.interrupt().restore(interrupt);

   // waitLock.release();
//	System.out.println("Does it get here");

    }
    private Lock waitLock;
	private TreeMap<Long,KThread> waitQ = null;
}
