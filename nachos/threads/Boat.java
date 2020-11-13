package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	System.out.println("\n ***Testing Boats with only 2 children***");
	//begin(10, 148, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }
    static int ChildrenWhoLeftOahu;			//Oahu Counter to keep track of how many children have left Oahu, used to avoid cases where there is no one to bring the boat back to Oahu.
    static Communicator confirmconvey;		//Single way communication to Begin() which allows threads to indicate they believe the simulation is over
    static Condition boatwait;				//Queue for children and adults to sleep in when they are either waiting for the boat or waiting for others to come to Molokai
    static boolean boatlocation;    //current location of boat, either set to Oahu or Molokai
    static boolean ChildComeBack;   //true if child has piloted boat from Molokai to Oahu
    static Lock trip;				//makes sure only one thread is using the boat by making both the calculations of whether a thread should take a boat as well as the action of taking the boat atomic
	static boolean finished;        //indicates completion of child itenerary for all threads. Equivalent of the news the last child brings to Molokai after observing that transfer is completed
	
    static int pilot, passenger, ChildrenonOahu, ChildrenonMolokai, AdultsonOahu, AdultsonMolokai ;
	static int adult = 2;
	static int child = 1;
	static int free = 0;
	static boolean Oahu = false;
	static boolean Molokai = true;
	static int wake = 0; //0 is when it is waiting, 1 is ready to wakeall the speakers and run the waitqueue 
	
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
	finished = false;    
	confirmconvey = new Communicator();
	boatwait = new Condition(trip);
	// Instantiate global variables here
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.

/*	Runnable r = new Runnable() {
	    public void run() {
                SampleItinerary();
            }
        };
        KThread t = new KThread(r);
        t.setName("Sample Boat Thread");
        t.fork();
  */      

                
        for(int it = 0; it< children;it++) {	//Create # of children threads as indicated by begin() input
        	Runnable child = new Runnable() {
        	    public void run() {
                        ChildItinerary();		
                    }
                };

        	KThread cthread = new KThread(child);

        	cthread.fork();						//fork child thread and run ChildItinerary()
     //   	System.out.print("Child forked.");

        }
        for(int it = 0; it < adults;it++) {	//Create # of adult threads as indicated by begin() input
            Runnable adult = new Runnable() {
            	public void run() {
                        AdultItinerary();
                     }
                };

        	KThread athread = new KThread(adult);
        	athread.fork();					//fork adult thread and run AdultItinerary()
        }
        
       KThread.yield();
       int confirm = 0;
       while(confirm != children + adults) {
           confirm += confirmconvey.listen();			//listen to threads finish, and continue waiting until we are sure all threads have finished properly

      // 	System.out.print("Child says it done");
       }
    }

    static void AdultItinerary()
    {
    	boolean threadlocation= Oahu;
   // 	System.out.print("addddddddd.");

	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/

    	trip.acquire();
   // 	System.out.print("childrenwholeft: "+ ChildrenWhoLeftOahu);

    	while(threadlocation == Oahu) {
    	if(ChildrenWhoLeftOahu>= 1 && threadlocation == Oahu && boatlocation == Oahu) { //check to see if there are 1+ child left in oahu

    		AdultsonMolokai++;		//and if there is, it sends an adult to molokai
    		AdultsonOahu--;			// we decrement adult from oahu
    		bg.AdultRowToMolokai(); //boat is now in molokai
    		boatlocation = Molokai; //true
    		threadlocation = Molokai; //true
    	}
    //	System.out.print("adult sleep");

    	boatwait.wake();
    	boatwait.sleep();
    	}
    //	System.out.print("Adult done");

    	trip.release();
    	confirmconvey.speak(1);
    	KThread.finish();
    }

    static void ChildItinerary()
    {
    //	System.out.print("Childetegwhgrwrthh in.");

    	boolean threadlocation= Oahu;
    	trip.acquire();//set the lock

    	while(!finished) {
    		//if(ChildrenonOahu == 0 && AdultsonOahu == 0) {finished = true;}
    	if(threadlocation == Molokai && boatlocation == Molokai && pilot == free) {//if the boat is at molokai and has no pilot
    		pilot = child;
    		ChildrenonMolokai--; //children get off to go to oahu
    		bg.ChildRowToOahu();//boat is bring rowed to oahu
    		pilot = free;
    		ChildrenonOahu++; //children return to oahu

    		boatlocation = Oahu; //boat is at oahu false
    		threadlocation = Oahu; //false
    		ChildrenWhoLeftOahu--;
    	}
    //	trip.release();
    	
    	if((ChildrenWhoLeftOahu == 0 || IslandAdults(threadlocation)==0)  && threadlocation == Oahu && boatlocation == Oahu) {  // empty boat on Oahu
    		if(pilot == free) {
    		pilot = child;
    		
    		if(ChildrenonOahu >1 ) {
    			if(!boatwait.waitQueue.isEmpty()) {
    			boatwait.wake();}
    		//	System.out.print("Stuck here?");
    			boatwait.sleep();
    					//wait for passenger
    		}else {
    			if(IslandAdults(threadlocation) == 0) {
    	 //       	System.out.print("not here tho right");
    				
    			 finished = true;// WE are done
    			
    		}
    			ChildrenonOahu--;
    			bg.ChildRowToMolokai();
        		boatlocation = Molokai;			//if no one else to take, go alone	
        		ChildrenonMolokai++;
        		ChildrenWhoLeftOahu++;
        		threadlocation = Molokai;
    		
    		}
    		}else{			//child is piloting, time go go
    			if(IslandAdults(threadlocation) == 0 && ChildrenonOahu == 2) {
    		    	 //       	System.out.print("not here tho right");
    		    				
    		    			 finished = true;// WE are done
    		    			
    		    		}
    		passenger = child;
    		ChildrenonOahu -=2;
    		bg.ChildRowToMolokai();		//Two children travel from Oahu to Molokai. Increment proper variables.
    		bg.ChildRideToMolokai();
    		ChildrenonMolokai+=2;
    		ChildrenWhoLeftOahu +=2;
    		boatlocation = Molokai;
    		passenger = pilot = free;	
    		}
    		
    		
    		threadlocation = Molokai;
    	}
 //   	System.out.print("Child sleep");

		boatwait.wake();				//wake next thread waiting for boat, sleep
		boatwait.sleep();
    	}
        if(!boatwait.waitQueue.isEmpty()) {

 			boatwait.wake();
 	   //    	System.out.print("getting next thread up");
 	
        }
       	trip.release();
   // 	System.out.print("Child done we done");

       confirmconvey.speak(1);

        KThread.finish();    }

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
    static int IslandChildren(boolean location) {
    	if(location == Oahu) {
    		return ChildrenonOahu;
    	}else return ChildrenonMolokai;
    }
    static int IslandAdults(boolean location) {
    	if(location == Oahu) {
    		return AdultsonOahu;
    	}else return AdultsonMolokai;
    }
    
}
