package ca.mcgill.ecse420.a1;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatrixMultiplication {

  private static final int NUMBER_THREADS = 5;
  private static final int MATRIX_SIZE = 2000;
  private static ExecutorService executor;

  public static void main(String[] args) {

    // Generate two random matrices, same size
    double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
    double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
//    double[][] a = {{1, 2, 3}, {4, 5, 2}, {0, 5, 3}};
//    double[][] b = {{2, 2, 2}, {3, 3, 0}, {1, 4, 5}};
    long seqStartTime = System.currentTimeMillis();
    double[][] seqNonRecProd = sequentialMultiplyMatrix(a, b);
    long seqEndTime = System.currentTimeMillis();

    executor = Executors.newFixedThreadPool(NUMBER_THREADS);

    long pStartTime = System.currentTimeMillis();
    double[][] parallelNonRecProd = parallelMultiplyMatrix(a,b);
    long pEndTime = System.currentTimeMillis();

//    System.out.println("Sequential non recursive , naive O(n^3)");
//    for(double[] x: seqNonRecProd)
//      System.out.println(Arrays.toString(x));

    System.out.println("\nSequential time is " +
        (seqEndTime - seqStartTime) + " milliseconds");

//    System.out.println("Parallel non recursive , naive O(n^3)");
//    for(double[] x: parallelNonRecProd)
//      System.out.println(Arrays.toString(x));

    System.out.println("\nParallel time is " +
        (pEndTime - pStartTime) + " milliseconds");
  }

  /**
   * Returns the result of a sequential matrix multiplication The two matrices are randomly
   * generated
   *
   * @param a is the first matrix
   * @param b is the second matrix
   * @return the result of the multiplication
   */
  public static double[][] sequentialMultiplyMatrix(double[][] a, double[][] b) {
    int r1 = a.length;
    int c1 = a[0].length;
    int c2 = b[0].length;

    double[][] product = new double[r1][c2];
    for (int i = 0; i < r1; i++) {
      for (int j = 0; j < c2; j++) {
        for (int k = 0; k < c1; k++) {
          product[i][j] += a[i][k] * b[k][j];
        }
      }
    }

    return product;
  }

//  public static double[][] sequentialRecursiveMultiplyMatrix(double[][] a, double[][] b) {
//    int r1 = a.length;
//    int r2 = b.length;
//    int c1 = a[0].length;
//    int c2 = b[0].length;
//
//    int dimension = r1;
//
//    int size = dimension / 2;
//    double[][] prod = new double[dimension][dimension];
//    double[][] a11b11
//  }

  /**
   * Returns the result of a concurrent matrix multiplication The two matrices are randomly
   * generated
   *
   * @param a is the first matrix
   * @param b is the second matrix
   * @return the result of the multiplication
   */
  public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b) {
    int r1 = a.length;
    int c2 = b[0].length;
    double[][] product = new double[r1][c2];
    System.out.println("in parallel");
    // row-view of matrix multiplication
    // each task will compute 1 cell value of the produt matrix
    for(int i = 0; i < r1; i++) { // for each row of first matrix
      for(int j = 0; j < c2; j++) { // for each column of second matrix
        System.out.println("executing tasks");
        executor.execute(new MatrixMultiplyTask(a[i], b, i, j, product));
      }
    }

    executor.shutdown();

    // Wait until all tasks are finished
    while (!executor.isTerminated()) {
      System.out.println("not done parallel mult yet");
    }

    return product;
  }

  // each task computes the value of 1 cell in the product
  private static class MatrixMultiplyTask implements Runnable {
    private double[] rowA;
    private double[][] matrixB;
    private int rowIndex;
    private int colIndex;
    private double[][] prod;

    MatrixMultiplyTask(double[] rowA, double[][] matrixB, int rowIndex, int colIndex, double[][] prod) {
      this.rowA = rowA;
      this.matrixB = matrixB;
      this.rowIndex = rowIndex;
      this.colIndex = colIndex;
      this.prod = prod;
    }

    @Override
    public void run() {
      double cellValue = 0;
      int dimension = rowA.length;
      for(int i = 0; i < dimension; i++) {
        cellValue += rowA[i]*matrixB[i][colIndex];
      }
      prod[rowIndex][colIndex] = cellValue;
    }
  }
  // OLD WAY OF PARALLELIZING IT
  // we can parallelize it however we want:
  // we can parallelize the row mult w/ each of the columns of the scnd matrix
  // or even each elem multiplication, we just need to explain in report how we parallelized it

//  public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b) {
//    int r1 = a.length;
//    int c2 = b[0].length;
//    double[][] product = new double[r1][c2];
//    RecursiveAction mainTask = new MatrixMultiplyTask(a, b, product, 0, r1);
//    ForkJoinPool pool = new ForkJoinPool(NUMBER_THREADS);
//    pool.invoke(mainTask);
//    return product;
//  }
//
//  private static class MatrixMultiplyTask extends RecursiveAction {
//    private static final int DIMENSION_THRESHOLD = 20;
//    private double[][] a;
//    private double[][] b;
//    private int currRow;
//    private double[][] prod;
//    private int dimension;
//
//    public MatrixMultiplyTask(double[][] a, double[][] b, double[][] prod, int currRow, int dimension) {
//      this.a = a;
//      this.b = b;
//      this.prod = prod;
//      this.currRow = currRow;
//      this.dimension = dimension;
//    }
//
//    @Override
//    protected void compute() {
//      int r1 = a.length;
//      int c1 = a[0].length;
//      int c2 = b[0].length;
//
//      if(dimension < DIMENSION_THRESHOLD) {
//        for (int i = currRow; i < r1; i++) {
//          for (int j = 0; j < c2; j++) {
//            for (int k = 0; k < c1; k++) {
//              prod[i][j] += a[i][k] * b[k][j];
//            }
//          }
//        }
//      } else {
//        for(int i = 0; i < c2; i++) {
//          prod[currRow][i] = 0;
//          for(int j = 0; j < c1; j++) {
//            prod[currRow][i] += a[currRow][j]*b[j][i];
//          }
//        }
//
//        currRow++;
//        dimension--;
//        new MatrixMultiplyTask(a, b, prod, currRow, dimension).invoke();
//      }
//
//    }
//  }
  /**
   * Populates a matrix of given size with randomly generated integers between 0-10.
   *
   * @param numRows number of rows
   * @param numCols number of cols
   * @return matrix
   */
  private static double[][] generateRandomMatrix(int numRows, int numCols) {
    double matrix[][] = new double[numRows][numCols];
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        matrix[row][col] = (double) ((int) (Math.random() * 10.0));
      }
    }
    return matrix;
  }

}

