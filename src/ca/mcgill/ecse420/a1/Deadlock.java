package ca.mcgill.ecse420.a1;

public class Deadlock {
  public static Object Lock1 = new Object();
  public static Object Lock2 = new Object();

  public static void main(String args[]) {
    Task1 t1 = new Task1();
    Task2 t2 = new Task2();

    Thread thread1 = new Thread(t1);
    Thread thread2 = new Thread(t2);
    System.out.println("start");
    thread1.start();
    thread2.start();
    System.out.println("end");
  }

  private static class Task1 implements Runnable {

    @Override
    public void run() {
      synchronized (Lock1) {
        System.out.println("Thread 1: Holding lock 1...");

        try { Thread.sleep(10); }
        catch (InterruptedException e) {}
        System.out.println("Thread 1: Waiting for lock 2...");

        synchronized (Lock2) {
          System.out.println("Thread 1: Holding lock 1 & 2...");
        }
      }
    }
  }

  private static class Task2 implements Runnable {


    @Override
    public void run() {
      synchronized (Lock2) {
        System.out.println("Thread 2: Holding lock 2...");

        try { Thread.sleep(10); }
        catch (InterruptedException e) {}
        System.out.println("Thread 2: Waiting for lock 1...");

        synchronized (Lock1) {
          System.out.println("Thread 2: Holding lock 1 & 2...");
        }
      }
    }
  }
}
