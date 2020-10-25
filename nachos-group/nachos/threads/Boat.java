package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    static Lock trip;
    
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	System.out.println("\n ***Testing Boats with only 2 children***");
	begin(0, 2, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }
    static int ChildrenWhoLeftOahu;
    static Condition waitAdultOahu;
    static Condition waitChildOahu;

    static Condition boatwait;
    static boolean boatlocation;    //current location of boat, either set to Oahu or Molokai
    static boolean ChildComeBack;   //true if child has piloted boat from Molokai to Oahu
    static int pilot, passenger, ChildrenonOahu, ChildrenonMolokai, AdultsonOahu, AdultsonMolokai ;
	static int adult = 2;
	static int child = 1;
	static int free = 0;
	static boolean Oahu = false;
	static boolean Molokai = true;
    public static void begin( int adults, int children, BoatGrader b )
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b; 
	trip = new Lock(); 
	pilot = free;
	passenger = free;
	boatlocation = Oahu;
	ChildrenonOahu = children;
	AdultsonOahu = adults;
	ChildrenWhoLeftOahu = 0;
	boatwait = new Condition(trip);
	// Instantiate global variables here
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.

	Runnable r = new Runnable() {
	    public void run() {
                SampleItinerary();
            }
        };
        KThread t = new KThread(r);
        t.setName("Sample Boat Thread");
        t.fork();

    }

    static void AdultItinerary()
    {
    	boolean threadlocation= Oahu;

	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
    	trip.acquire();

    	if(ChildrenWhoLeftOahu>= 1 && threadlocation == Oahu && boatlocation == Oahu) { //check to see if there are 1+ child left in oahu
    		AdultsonMolokai++;		//and if there is, it sends an adult to molokai
    		AdultsonOahu--;			// we decrement adult from oahu
    		bg.AdultRowToMolokai(); //boat is now in molokai
    		boatlocation = Molokai; //true
    		threadlocation = Molokai; //true
    	}else {
    		KThread.yield();
    	}
    	trip.release();

    }

    static void ChildItinerary()
    {
    	boolean threadlocation= Oahu;
    	boolean notfinished = true;
    	while(notfinished) {
    	trip.acquire();//set the lock
    	if(threadlocation == Molokai && boatlocation == Molokai && pilot == free) {//if the boat is at molokai and has no pilot
    		 
    		ChildrenonMolokai--; //children get off to go to oahu
    		bg.ChildRowToOahu();//boat is bring rowed to oahu
    		ChildrenonOahu++; //children return to oahu

    		boatlocation = Oahu; //boat is at oahu false
    		threadlocation = Oahu; //false
    		ChildrenWhoLeftOahu--;
    	}
    	trip.release();
    	
    	if(ChildrenWhoLeftOahu == 0  && threadlocation == Oahu && boatlocation == Oahu) {  // empty boat on Oahu
    		if(pilot == free) {
    		pilot = child;
    		
    		if(ChildrenonOahu >1) {
    			
    			boatwait.wake();
    			
    			boatwait.sleep();
    					//wait for passenger
    		}else {
    			bg.ChildRowToMolokai();
    			if(AdultsonOahu == 0) {
    			KThread.finish(); // WE are done
    			
    		}
    		
    		}
    		}else{			//child is piloting, time go go
    			
    		passenger = child;
    		bg.ChildRowToOahu();
    		bg.ChildRideToOahu();
    			
    		}
    		
    	}
    	
		boatwait.wake();
		boatwait.sleep();
    	}
       	trip.release();
       	
    }

    static void SampleItinerary()
    {
	// Please note that this isn't a valid solution (you can't fit
	// all of them on the boat). Please also note that you may not
	// have a single thread calculate a solution and then just play
	// it back at the autograder -- you will be caught.
	System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
	bg.AdultRowToMolokai();
	bg.ChildRideToMolokai();
	bg.AdultRideToMolokai();
	bg.ChildRideToMolokai();
    }
    
}
