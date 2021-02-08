package ca.mcgill.ecse420.a1;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophers {

	// add a random duration for thinking and eating states so that the logged
	// output of this program can be digested by the human eye
	public static void main(String[] args) {

		int numberOfPhilosophers = 5;
		Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
		ReentrantLock[] chopsticks = new ReentrantLock[numberOfPhilosophers];

		ExecutorService executor = Executors.newCachedThreadPool();

		for (int i = 0; i < numberOfPhilosophers; i++) {
			chopsticks[i] = new ReentrantLock();
		}

		for (int i = 0; i < numberOfPhilosophers; i++) {
			philosophers[i] = new ProperPhilosopher(i, chopsticks[i], chopsticks[(i + 1) % numberOfPhilosophers]);
//			philosophers[i] = new DeadlockPhilosopher(i, chopsticks[i], chopsticks[(i + 1) % numberOfPhilosophers]);

			executor.execute(philosophers[i]);
		}

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
		protected ReentrantLock left;
		protected ReentrantLock right;

		public Philosopher(int number, ReentrantLock left, ReentrantLock right) {
			this.number = number;
			this.left = left;
			this.right = right;
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

		public ProperPhilosopher(int number, ReentrantLock left, ReentrantLock right) {
			super(number, left, right);
		}

		@Override
		public void run() {
			boolean isFirst;

			while (true) {

				isFirst = false;

				synchronized (queue) {
					if (!queue.isEmpty() && queue.getFirst() == this.number) {
						isFirst = true;
					}
				}

				if (isFirst) {
					if (left.tryLock()) {
						if (right.tryLock()) {

							// eat
							synchronized (queue) {
								queue.removeFirst();
							}

							System.out.println("Philosopher " + this.number + " is now eating.");

							try {
								// eat for 0-5 seconds
								long random = (long) (Math.random() * 4999 + 1);
								Thread.sleep(random);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							left.unlock();
							right.unlock();

							System.out.println("Philosopher " + this.number + " ate and is now thinking.");

							try {
								// think for 0-5 seconds
								long random = (long) (Math.random() * 4999 + 1);
								Thread.sleep(random);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						} else {
							left.unlock();
						}
					}
				} else { // Add to queue if not empty
					synchronized (queue) {
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

		public DeadlockPhilosopher(int number, ReentrantLock left, ReentrantLock right) {
			super(number, left, right);
		}

		@Override
		public void run() {
			while (true) {
				while (!left.tryLock());
				System.out.println("Philosopher " + this.number + " has left chopstick.");
				while (!right.tryLock());
				System.out.println("Philosopher " + this.number + " is now eating.");

				left.unlock();
				right.unlock();

				System.out.println("Philosopher " + this.number + " ate and is now thinking.");
			}
		}
	}
}