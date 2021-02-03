package ca.mcgill.ecse420.a1;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophers {
	
	public static LinkedList<Integer> queue;
	
	public static void main(String[] args) {

		int numberOfPhilosophers = 5;
        Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
        ReentrantLock[] chopsticks = new ReentrantLock[numberOfPhilosophers];
        queue = new LinkedList<Integer>();
        
        ExecutorService executor = Executors.newCachedThreadPool();
        
        
        for (int i = 0; i < numberOfPhilosophers; i++) {
        	chopsticks[i] = new ReentrantLock();
        }
        
        for (int i = 0; i < numberOfPhilosophers; i++) {
        	philosophers[i] = new Philosopher(i, chopsticks[i], chopsticks[(i + 1)%numberOfPhilosophers]);
        	executor.execute(philosophers[i]);
        }
        
        executor.shutdown();
	}

	public static class Philosopher implements Runnable {

		private int number;
		private ReentrantLock left;
		private ReentrantLock right;

		public Philosopher(int number, ReentrantLock left, ReentrantLock right) {
			this.number = number;
			this.left = left;
			this.right = right;
		}

		@Override
		public void run() {		
			boolean isFirst;
			
			while(true) {				
				
				isFirst = false;
				
				synchronized (queue) {
					if (!queue.isEmpty() && queue.getFirst() == this.number) {
						isFirst = true;
					}
				}
				
				if (isFirst) {
					if (left.tryLock()) {
						if (right.tryLock()) {
							//eat
							System.out.println("Philosopher " + this.number + " ate.");

							synchronized (queue) {
								queue.removeFirst();
							}
							
							left.unlock();
							right.unlock();
							
						}
						else {
							left.unlock();
						}
					}
				}
				// Add to queue if not empty
				else {
					synchronized (queue) {
						if (!queue.contains(this.number)) {
						queue.add(this.number);
						System.out.println(DiningPhilosophers.queue);
						System.out.println("Philosopher " + this.number + " added to queue.");
						}
					}
				}				
			}
		}
	}
}