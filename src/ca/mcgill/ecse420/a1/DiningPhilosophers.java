package ca.mcgill.ecse420.a1;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophers {

	public static void main(String[] args) {

		int numberOfPhilosophers = 5;
		Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
		ReentrantLock[] chopsticks = new ReentrantLock[numberOfPhilosophers];

		// Create threadpool
		ExecutorService executor = Executors.newCachedThreadPool();

		// Initialize chopsticks
		for (int i = 0; i < numberOfPhilosophers; i++) {
			chopsticks[i] = new ReentrantLock();
		}

		// Create multiple threads representing philosophers; deadlocked or proper
		for (int i = 0; i < numberOfPhilosophers; i++) {
			philosophers[i] = new ProperPhilosopher(i, chopsticks[i], chopsticks[(i + 1) % numberOfPhilosophers]);
			// philosophers[i] = new DeadlockPhilosopher(i, chopsticks[i], chopsticks[(i +
			// 1) % numberOfPhilosophers]);

			executor.execute(philosophers[i]);
		}

		// Teardown
		executor.shutdown();
	}

	/**
	 * 
	 * Superclass Philosopher
	 * 
	 * @author Petar Basta
	 * 
	 */
	public static abstract class Philosopher implements Runnable {

		protected int number;
		protected ReentrantLock leftChopstick;
		protected ReentrantLock rightChopstick;

		public Philosopher(int number, ReentrantLock leftChopstick, ReentrantLock rightChopstick) {
			this.number = number;
			this.leftChopstick = leftChopstick;
			this.rightChopstick = rightChopstick;
		}
	}

	/**
	 * 
	 * Non-deadlock non-starvation case
	 * 
	 * @author Petar Basta
	 * 
	 */
	public static class ProperPhilosopher extends Philosopher implements Runnable {

		public static LinkedList<Integer> queue = new LinkedList<Integer>();

		public ProperPhilosopher(int number, ReentrantLock leftChopstick, ReentrantLock rightChopstick) {
			super(number, leftChopstick, rightChopstick);
		}

		@Override
		public void run() {
			boolean isFirst;

			// Run forever
			while (true) {

				isFirst = false;

				// Lock queue
				synchronized (queue) {
					// If first in queue, set isFirst to true
					if (!queue.isEmpty() && queue.getFirst() == this.number) {
						isFirst = true;
					}
				}

				// If first in queue
				if (isFirst) {
					// Try to pick up left chopstick
					if (leftChopstick.tryLock()) {
						// Try to pick up right chopstick
						if (rightChopstick.tryLock()) {

							// Has both chopsticks, ready to eat
							// Remove from front of queue
							synchronized (queue) {
								queue.removeFirst();
							}

							System.out.println("Philosopher " + this.number + " is now eating.");

							try {
								// Eat for 0-5 seconds
								long random = (long) (Math.random() * 4999 + 1);
								Thread.sleep(random);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							// Put down both chopsticks
							leftChopstick.unlock();
							rightChopstick.unlock();

							System.out.println("Philosopher " + this.number + " ate and is now thinking.");

							try {
								// Think for 0-5 seconds
								long random = (long) (Math.random() * 4999 + 1);
								Thread.sleep(random);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

						} else {
							// If right chopstick unavailable, return left chopstick
							leftChopstick.unlock();
						}
					}
				}
				// If not first in queue
				else {
					// Lock queue
					synchronized (queue) {
						// If not in queue, add to back of queue
						if (!queue.contains(this.number)) {
							queue.add(this.number);
							System.out.println("Philosopher " + this.number + " added to queue.");
							System.out.println(queue);
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 * Deadlocked case
	 * 
	 * @author Petar Basta
	 * 
	 */
	public static class DeadlockPhilosopher extends Philosopher implements Runnable {

		public DeadlockPhilosopher(int number, ReentrantLock leftChopstick, ReentrantLock rightChopstick) {
			super(number, leftChopstick, rightChopstick);
		}

		@Override
		public void run() {
			
			// Run forever
			while (true) {
				// Wait for left chopstick to become available and pick it up
				while (!leftChopstick.tryLock());
				System.out.println("Philosopher " + this.number + " has left chopstick.");
				// Wait for right chopstick to become available and pick it up
				while (!rightChopstick.tryLock());
				
				System.out.println("Philosopher " + this.number + " is now eating.");
				
				// Put both chopsticks down
				leftChopstick.unlock();
				rightChopstick.unlock();

				System.out.println("Philosopher " + this.number + " ate and is now thinking.");
			}
		}
	}
}