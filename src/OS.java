import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition; //Note that the 'notifyAll' method or similar polling mechanism MUST not be used

// IMPORTANT:
//
//'Thread safe' and 'synchronized' classes (e.g. those in java.util.concurrent) other than the two imported above MUST not be used.
//
//You MUST not use the keyword 'synchronized', or any other `thread safe` classes or mechanisms  
//or any delays or 'busy waiting' (spin lock) methods. Furthermore, you must not use any delays such as Thread.sleep().

//However, you may import non-tread safe classes e.g.:
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;


//Your OS class must handle exceptions locally i.e. it must not explicitly 'throw' exceptions 
//otherwise the compilation with the Test classes will fail!!!


public class OS implements OS_sim_interface {

        private ReentrantLock lock = new ReentrantLock(); // Lock for synchronization
        private int pidCounter = 0; // Process ID counter
        private LinkedList<Integer> Processors = new LinkedList<>(); // Track all registered processes
        private Map<Integer, Queue<Integer>> readyQueues = new HashMap<>(); // Track processes in ready state
        private Map<Integer, Integer> processPriority = new HashMap<>(); // Track processor allocation
        private Map<Integer, Condition> processCondition = new HashMap<>(); // Track process conditions

        @Override
        public void set_number_of_processors(int nProcessors) {
            lock.lock();
            try {
                // Initialize processors as unallocated
                // The length of the array represents the number of processors
                // Each element in the array represents the process ID currently running on that processor
                for (int i = 0; i < nProcessors; i++) {
                	Processors.add(i);
                }
            } finally {
                lock.unlock();
            }
        }
        
        @Override
        public int reg(int priority) {
            lock.lock();
            try {
                // Register a new process with the given priority
    			int pid = pidCounter++;
                // Add the process to the ready queue if it does not exist
                if (!readyQueues.containsKey(priority)) {
                	readyQueues.put(priority, new LinkedList<>());
                }
                // Condition variable for the process
    			processCondition.put(pid, lock.newCondition());
                // Set the priority of the process
    			processPriority.put(pid, priority);
    			return pid;
            } finally {
                lock.unlock();
            }
        }
        
        @Override
    	public void start(int ID) {
        	lock.lock();
    		try {
                // Get priority queue of the process
    			Queue<Integer> queue = readyQueues.get(processPriority.get(ID));
                // Add the process to the queue
    			if (Processors.isEmpty()) {
    				queue.add(ID);
                    // Wait until the process is scheduled
    				while (queue.peek() != ID || Processors.isEmpty()) {
    					processCondition.get(ID).await();
    				}
                    // Remove the process from the queue
    				queue.poll();
    			}
    			else {
                    // Remove processor
    				Processors.poll(); 
    			}
    		} catch (InterruptedException e) {
    			Thread.currentThread().interrupt();
    		} finally {
    			lock.unlock();
    		}
    	}

    	@Override
    	public void schedule(int ID) {
    		lock.lock();
    		try {
                // Get the priority of the process
    			int p = processPriority.get(ID);
                // Get the queue of the priority
    			Queue<Integer> q = readyQueues.get(p);
                // Add the process to the queue
    			q.add(ID);
                // Set processor is available
    			Processors.add(0); 
                // Finding highest priority process
                // TreeSet to maintain the order of the priority
    			for (Integer priority : new TreeSet<>(readyQueues.keySet())) {
        			Queue<Integer> queue = readyQueues.get(priority);
                    // Check if the queue is not empty and processor is available
        			if (!queue.isEmpty() && !Processors.isEmpty()) {
        				Integer Process = queue.peek(); 
        				if (Process != null) {
                            // Signal the process
        					processCondition.get(Process).signal(); 
                            // Wait until the process is scheduled
        					while (queue.peek()!= null && queue.peek() != ID) {
        						processCondition.get(ID).await();
        					}
                            // Exit the loop
        					break; 
        				}
        			}
        		}
                // Return to the caller
    			q.poll();
    		} catch (InterruptedException e) {
    			Thread.currentThread().interrupt();
    		} finally {
    			lock.unlock();
    		}
    		
    	}


    	@Override
    	public void terminate(int ID) {
    		lock.lock();
            try {
                // Remove the process from the processor
            	Processors.add(0);
                // Finding highest priority process
                //TreeSet to maintain the order of the priority
            	for (Integer priority : new TreeSet<>(readyQueues.keySet())) {
        			Queue<Integer> queue = readyQueues.get(priority);
                    // Check if the queue is not empty and processor is available
        			if (!queue.isEmpty() && !Processors.isEmpty()) {
        				Integer Process = queue.peek();
        				if (Process != null) {
                            // Signal the process
        					processCondition.get(Process).signal(); 
                            // Wait until the process is scheduled
        					while (queue.peek()!= null && queue.peek() != ID) {
        						processCondition.get(ID).await();
        					}
                            // Exit the loop
        					break;
        				}
        			}
        		}
            } catch (InterruptedException e) {
            	Thread.currentThread().interrupt();
			} finally {
                lock.unlock();
            }
    		
    	}

}

