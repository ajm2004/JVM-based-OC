// You should develop your tests here to assure yourself that your class 
// meets the UR published in the coursework specification.

//3 example tests are provided - but these may not be used as is in the marking scripts

//Note that you may use *any* classes in this Tests class that are available in SE 17. 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Tests {

	public void ur1_example_test(){
		System.out.println("\n\n\n*********** UR1 *************");
		
		//Instantiate OS simulation for single processor
		OS os =  new OS();	
		os.set_number_of_processors(1);		
		boolean successful_test = true;
		for (int expected_pid=0; expected_pid<4; expected_pid++) {
			int pid = os.reg(1);
			System.out.println("pid returned = " + pid + ", pid expected = "  + expected_pid);
			if (pid != expected_pid) successful_test = false;
		}
		if (successful_test) System.out.println("\nUR1: Passed");
		else System.out.println("\nUR1: Fail!");
	}	
	
	
	int  test_timeout = 300; //Timeout 	
	
	//Declare global list of events for ease of access (yup, lazy, not best practice)
	ConcurrentLinkedQueue<String> events;		
	
	//Declare sleep with local exception handling:
	void sleep (int delay) {try {Thread.sleep(delay);} catch (InterruptedException e) {e.printStackTrace();} }
	
	//Define process simulation threads:
	class ProcessSimThread2 extends Thread {
		int pid = -1;
		int start_session_length=0;
		OS os;
		ProcessSimThread2(OS os){this.os = os;} //Constructor stores reference to os for use in run()
		
		public void run(){

			os.start(pid); 
			events.add("pid="+pid+", session=0");
			try {Thread.sleep(start_session_length);} catch (InterruptedException e) {e.printStackTrace();}
			
			os.schedule(pid);
			events.add("pid="+pid+", session=1");
			
			os.schedule(pid);
			events.add("pid="+pid+", session=2");
			
			os.terminate(pid);
			//events.add("pid="+pid+", session=3"); //could test this return as well ....
		};	
	};	

	//UR2 - Single Process, Single Processor, Single Priority Queue
	
	public void ur2_example_test() {
		System.out.println("\n\n\n*********** UR2 *************");
		events = new ConcurrentLinkedQueue<String>(); // List of process events

		// Instantiate OS simulation for a single processor
		OS os = new OS();
		os.set_number_of_processors(1);
		int priority1 = 1;

		// Register one process and create its simulation thread
		int pid = os.reg(priority1);
		ProcessSimThread2 p = new ProcessSimThread2(os);
		p.start_session_length = 100; // Arbitrary start session length for testing
		p.pid = pid;

		// Start the process thread
		p.start();

		// Expected event sequence for UR2, corrected for formatting
		String[] expected = {
			"pid=" + pid + ", session=0", // Note the comma and space to match the actual format
			"pid=" + pid + ", session=1",
			"pid=" + pid + ", session=2"
		};

		// Delay to allow the process to complete its execution
		sleep(test_timeout);

		System.out.println("\nUR2 - NOW CHECKING");

		// Check expected events against actual
		String test_status = "UR2 PASSED";
		Iterator<String> iterator = events.iterator();
		int index = 0;
		while (iterator.hasNext() && index < expected.length) {
			String event = iterator.next();
			if (!event.equals(expected[index])) {
				test_status = "UR2 FAILED - NO MARKS";
				System.out.println("Expected event = " + expected[index] + " actual event = " + event + " --- ERROR");
				break; // Exit on the first error
			} else {
				System.out.println("Expected event = " + expected[index] + " actual event = " + event + " --- MATCH");
			}
			index++;
		}

		System.out.println("\n" + test_status);
	}

	public void ur3_example_test(){
		/*********************
		 * 
		 * UR3 � Multiple Processes, Single Processor, Single Priority Queue
		 * 
		 * Creates three processes p0-p2
		 * 
		 * p0's start session is set at 150mS 
		 * p1's start is delayed by 50 to ensure that p1 has gained control of (is scheduled onto) the 'single processor'
		 * p2's start is delayed by 100ms to ensure that p1 is before it in the ready queue
		 * 
		 *  Hence scheduling must be p0, p1, p2 repeated for FIFO queue		 #
		 * 		
		 ***********************/	
		
		System.out.println("\n\n\n*********** UR3 *************");		
		events = new ConcurrentLinkedQueue<String>(); //List of process events
		
		//Instantiate OS simulation for single processor
		OS os =  new OS();	
		os.set_number_of_processors(1);
		int priority1 = 1;
		
		//Create three process simulation threads:

		int pid0 = os.reg(priority1); 
		ProcessSimThread2 p0 = new ProcessSimThread2(os); 
		p0.start_session_length = 150; p0.pid = pid0;
		
		int pid1 = os.reg(priority1); 
		ProcessSimThread2 p1 = new ProcessSimThread2(os); 
		p1.start_session_length = 0; p1.pid = pid1;
		
		int pid2 = os.reg(priority1); 				
		ProcessSimThread2 p2 = new ProcessSimThread2(os); 
		p2.start_session_length = 0; p2.pid = pid2;		
		
		//Start the treads making sure that p0 will get to its first os.start()
		p0.start();
		sleep(50); // Start p2 one third way through p0's start session
		p1.start();
		sleep(50); // Start p3 two thirds way through p0's start session
		p2.start();

		//Created EXPECTED ORDERING OF SESSION EVENTS (will test for sessions 0, 1 & 2 in this example)
		String[] expected = { "pid=0, session=0", "pid=1, session=0", "pid=2, session=0", "pid=0, session=1", "pid=1, session=1", "pid=2, session=1", "pid=0, session=2", "pid=1, session=2", "pid=2, session=2"};
		
		//Delay to allow p0-p2 to terminate
		sleep(test_timeout);
	

		System.out.println("\nUR3 - NOW CHECKING");
		//Check expected events against actual:
		String test_status = "UR3 PASSED";
		if (events.size() == expected.length) {
			 Iterator <String> iterator = events.iterator(); 
			 int index=0;
			 while (iterator.hasNext()) {
				 String event = iterator.next();
				 if (event.equals(expected[index])) System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- MATCH");
				 else {
					 test_status = "UR3 FAILED - NO MARKS";	
					 System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- ERROR");
				 }
				 index++;
			 }
		} else {
			System.out.println("Number of events expected = " + expected.length + ", number of events reported = " + events.size());
			test_status = "UR3 FAILED - NO MARKS";			
		}		

		System.out.println("\n" + test_status);	
	}
	
	public void ur4_example_test(){
		
		/*********************
		 * 
		 * UR4 � Multiple Processes, Two Processors, Single Priority Queue
		 * 
		 * Creates three processes p0-p2
		 * 
		 * p0's start session is set at 250mS to ensure that it will grab and keep control of the first available processor
		 * p1's start session is set at 50mS to ensure that p1 has gained control the second processor before p2 starts
		 * p2's start is delayed by 25ms to ensure that it is on the ready queue before when p1 finishes its start session
		 * 
		 * 
		 * 		

		EXPECTED ORDERING OF SESSION ENDS:
			pid=0, session=0
			pid=2, session=1
			pid=1, session=2		
			pid=1, session=1
			pid=2, session=1
			pid=1, session=2
			pid=2, session=2
			pid=0, session=1
			pid=0, session=2
			
		*/
		
		System.out.println("\n\n\n*********** UR4 *************");
		events = new ConcurrentLinkedQueue<String>(); //List of process events
		
		//Instantiate OS simulation for two processors
		OS os =  new OS();	
		os.set_number_of_processors(2);
		int priority1 = 1;
		
		//Create two process simulation threads:

		int pid0 = os.reg(priority1); 
		ProcessSimThread2 p0 = new ProcessSimThread2(os); 
		p0.start_session_length = 250; p0.pid = pid0; //p0 grabs first processor and keeps it for 250ms
		
		int pid1 = os.reg(priority1); 
		ProcessSimThread2 p1 = new ProcessSimThread2(os); 
		p1.start_session_length = 50;  p1.pid = pid1; //p1 grabs 2nd processor and keeps it for 50ms
		
		int pid2 = os.reg(priority1); 			
		ProcessSimThread2 p2 = new ProcessSimThread2(os); 
		p2.start_session_length = 0; p2.pid = pid2;	//p2 tries to get processor straight away but has to wait for p1 os.schedule call
		
		//Start the treads making sure that p0 will get to its first os.start()
		p0.start();
		sleep(20);
		p1.start();
		sleep(25); //make sure that p1 has grabbed a processor before starting p2
		p2.start();

		//Give time for all the process threads to complete:
		sleep(test_timeout);

		//Created EXPECTED ORDERING OF SESSION EVENTS (will test for sessions 1 & 2 in this example)
		String[] expected = { "pid=0, session=0", "pid=1, session=0", "pid=2, session=0", "pid=1, session=1", "pid=2, session=1", "pid=1, session=2", "pid=2, session=2", "pid=0, session=1", "pid=0, session=2"};
		
		System.out.println("\nUR4 - NOW CHECKING");
		//Check expected events against actual:
		String test_status = "UR4 PASSED";
		if (events.size() == expected.length) {
			 Iterator <String> iterator = events.iterator(); 
			 int index=0;
			 while (iterator.hasNext()) {
				 String event = iterator.next();
				 if (event.equals(expected[index])) System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- MATCH");
				 else {
					 test_status = "UR3 FAILED - NO MARKS";	
					 System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- ERROR");
				 }
				 index++;
			 }
		} else {
			System.out.println("Number of events expected = " + expected.length + ", number of events reported = " + events.size());
			test_status = "UR4 FAILED - NO MARKS";			
		}		

		System.out.println("\n" + test_status);	
	}
	
	
	public void ur5_example_test() {
		System.out.println("\n\n\n*********** UR5 *************");
        events = new ConcurrentLinkedQueue<String>(); // List of process events for tracking execution order.

        // Instantiate OS simulation with a single processor.
        OS os = new OS();
        os.set_number_of_processors(1);

        // Register processes with different priorities.
        int highPriority = 10; // Higher priority (1 is higher than 2)
        int lowPriority = 20; // Lower priority

        // Register two high-priority processes.
        int pid0 = os.reg(highPriority);
        ProcessSimThread2 p0 = new ProcessSimThread2(os);
        p0.start_session_length = 100; // Arbitrary length for testing.
        p0.pid = pid0;

        int pid1 = os.reg(highPriority); // Same priority as p0.
        ProcessSimThread2 p1 = new ProcessSimThread2(os);
        p1.start_session_length = 0; // Same as p0 for simplicity.
        p1.pid = pid1;

        // Register one low-priority process.
        int pid2 = os.reg(lowPriority); // Lower priority than p0 and p1.
        ProcessSimThread2 p2 = new ProcessSimThread2(os);
        p2.start_session_length = 0; // Same for consistency.
        p2.pid = pid2;

        // Start the high-priority process threads.
        p0.start();
        sleep(20);
        p1.start();
        sleep(20); // Ensures p0 and p1 are started before p2 to test priority scheduling.

        // Start the low-priority process thread.
        p2.start();

        // Define the expected sequence of process execution, respecting priority and FIFO within the same priority.
        String[] expectedOrder = {
        		"pid=0, session=0", "pid=1, session=0", // Add a comma after "pid=0"
        	    "pid=0, session=1", "pid=1, session=1",
        	    "pid=0, session=2", "pid=1, session=2",
        	    "pid=2, session=0", "pid=2, session=1", "pid=2, session=2"
        };

	    // Delay to allow processes to terminate
	    sleep(test_timeout);

	    System.out.println("\nUR5 - NOW CHECKING");
	    // Check expected events against actual
	    String test_status = "UR5 PASSED";
	    if (events.size() == expectedOrder.length) {
	        Iterator<String> iterator = events.iterator();
	        int index = 0;
	        while (iterator.hasNext()) {
	            String event = iterator.next();
	            if (event.equals(expectedOrder[index])) {
	                System.out.println("Expected event = " + expectedOrder[index] + " actual event = " + event + " --- MATCH");
	            } else {
	                test_status = "UR5 FAILED - NO MARKS";
	                System.out.println("Expected event = " + expectedOrder[index] + " actual event = " + event + " --- ERROR");
	                
	            }
	            index++;
	        }
	    } else {
	        System.out.println("Number of events expected = " + expectedOrder.length + " number of events reported = " + events.size());
	        test_status = "UR5 FAILED - NO MARKS";
	    }

	    System.out.println("\n" + test_status);
	}
	
	
	
	public void ur6_example_test() {

		
		System.out.println("\n\n\n*********** UR6 *************");
        events = new ConcurrentLinkedQueue<String>(); //List of process events
		
		//Instantiate OS simulation for two processors
		OS os =  new OS();	
		os.set_number_of_processors(2);
		int priority1 = 10;
		int priority2 = 20;
		
		//Create two process simulation threads:

		int pid0 = os.reg(priority1); 
		ProcessSimThread2 p0 = new ProcessSimThread2(os); 
		p0.start_session_length = 250; p0.pid = pid0; //p0 grabs first processor and keeps it for 250ms
		
		int pid1 = os.reg(priority2); 
		ProcessSimThread2 p1 = new ProcessSimThread2(os); 
		p1.start_session_length = 50;  p1.pid = pid1; //p1 grabs 2nd processor and keeps it for 50ms
		
		int pid2 = os.reg(priority1); 			
		ProcessSimThread2 p2 = new ProcessSimThread2(os); 
		p2.start_session_length = 0; p2.pid = pid2;	//p2 tries to get processor straight away but has to wait for p1 os.schedule call
		
		//Start the treads making sure that p0 will get to its first os.start()
		p0.start();
		sleep(20);
		p1.start();
		sleep(25); //make sure that p1 has grabbed a processor before starting p2
		p2.start();

		//Give time for all the process threads to complete:
		sleep(test_timeout);

		//Created EXPECTED ORDERING OF SESSION EVENTS (will test for sessions 1 & 2 in this example)
		String[] expected = { 
				"pid=0, session=0", 
				"pid=1, session=0", 
				"pid=2, session=0", 
				"pid=2, session=1", 
				"pid=2, session=2", 
				"pid=1, session=1", 
				"pid=1, session=2", 
				"pid=0, session=1", 
				"pid=0, session=2"};
	    
	    // Delay to allow processes to terminate.
	    sleep(test_timeout);

	    System.out.println("\nUR6 - NOW CHECKING");
	    // Check expected events against actual.
	    String test_status = "UR6 PASSED";
	    if (events.size() == expected.length) {
	        Iterator<String> iterator = events.iterator();
	        int index = 0;
	        while (iterator.hasNext()) {
	            String event = iterator.next();
	            if (event.equals(expected[index])) {
	                System.out.println("Expected event = " + expected[index] + " actual event = " + event + " --- MATCH");
	            } else {
	                test_status = "UR6 FAILED - NO MARKS";
	                System.out.println("Expected event = " + expected[index] + " actual event = " + event + " --- ERROR");
	                // Exit on the first error.
	            }
	            index++;
	        }
	    } else {
	        System.out.println("Number of events expected = " + expected.length + ", number of events reported = " + events.size());
	        test_status = "UR6 FAILED - NO MARKS";
	    }

	    System.out.println("\n" + test_status);
	}

}


