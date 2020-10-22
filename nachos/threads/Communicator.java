package nachos.threads;

import nachos.machine.*;

import java.util.List;
/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    	Lock = new Lock();
    	speakers = new Condition2(Lock);
    	listeners = new Condition2(Lock);
    	s = null;
    	//0 is considered not null
    	//1 is considered null;
    }
    
    private static Integer s;
    Lock lock;
    Condition2 speakers;
    Condition2 listeners;
    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    	Lock.acquire();		//Acquire lock
    	
    	while(s != null) {		//While there is a thread speaking, sleep speakers
    		speakers.sleep();
    	}
    	s = word;				//The thread speaks and stores word into s
    	listeners.wake();		//Listener thread wakes
    	
    	Lock.release();		//Release lock
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	int threadReturn;		//variable to store and save the s value
    	Lock.acquire();			//Acquire lock
    	while(s == null) {		//While there is no thread speaking, sleep listeners
    		listeners.sleep();	
    	}
    	threadReturn = s.intValue();		//stores word into threadReturn
    	s = null;
    	speakers.wakeALL();		//Wake up all speakers waiting
    	
    	Lock.release();			//Release lock
    	
	return threadReturn;
    }
}

