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
	
	this.waitQ = new TreeMap<Long, KThread>();
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
	boolean interrupt = Machine.interrupt().disabled();
	if(waitQ.size()>=1) {
		for (Entry<Long, KThread> it : waitQ.entrySet()) {
			if(it.getKey() <= Machine.timer().getTime()) {
				it.getValue().ready();
				waitQ.remove(it.getKey());
			}
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
	
	if( x<= 0) {
		return;
	}
	waitQ.put(wakeTime,KThread.currentThread());
	KThread.sleep();
    }

	private TreeMap<Long,KThread> waitQ = null;
}
