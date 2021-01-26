package ca.mcgill.ecse420.a1;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Writing to CSV Reference:
//https://stackabuse.com/reading-and-writing-csvs-in-java/

public class MatrixMultiplication {

  private static final int PREFERRED_NUM_THREADS = 4;
  private static int numThreads = 6;
  private static int MATRIX_SIZE = 2000;
  private static ExecutorService executor;

  public static void main(String[] args) {
    try {
      testExecTimeVSNumThreads();
      testSeqAndParallelExecTimeVSMatrixSize();
    } catch (IOException e) {
      System.out.println("errored");
    }

    // Generate two random matrices, same size
//    double[][] a = generateRandomMatrix(matrixSize, matrixSize);
//    double[][] b = generateRandomMatrix(matrixSize, matrixSize);
//    double[][] a = {{1, 2, 3}, {4, 5, 2}, {0, 5, 3}};
//    double[][] b = {{2, 2, 2}, {3, 3, 0}, {1, 4, 5}};
//    long seqStartTime = System.currentTimeMillis();
//    double[][] seqNonRecProd = sequentialMultiplyMatrix(a, b);
//    long seqEndTime = System.currentTimeMillis();
//
//    executor = Executors.newFixedThreadPool(numThreads);
//
//    long pStartTime = System.currentTimeMillis();
//    double[][] parallelNonRecProd = parallelMultiplyMatrix(a,b);
//    long pEndTime = System.currentTimeMillis();

//    System.out.println("Sequential non recursive , naive O(n^3)");
//    for(double[] x: seqNonRecProd)
//      System.out.println(Arrays.toString(x));

//    System.out.println("\nSequential time is " +
//        (seqEndTime - seqStartTime) + " milliseconds");

//    System.out.println("Parallel non recursive , naive O(n^3)");
//    for(double[] x: parallelNonRecProd)
//      System.out.println(Arrays.toString(x));

//    System.out.println("\nParallel time is " +
//        (pEndTime - pStartTime) + " milliseconds");
  }

  // testing out square matrix sizes 100, 200, 500, 1000, 2000
  // with 4 threads
  public static void testSeqAndParallelExecTimeVSMatrixSize() throws IOException {
    int[] matrixSizes = {100, 200, 500, 1000, 2000};

    FileWriter csvWriter = new FileWriter("Part5.csv");
    csvWriter.append("Sequential");
    csvWriter.append("\n");
    csvWriter.append("MatrixSize");
    csvWriter.append(",");
    csvWriter.append("ExecutionTime");
    csvWriter.append("\n");

    double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
    double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);

    // Sequential multiplication
    for(int matrixSize : matrixSizes) {
      System.out.println("Part 1.5");
      System.out.println("Running " + matrixSize + " by " + matrixSize + " matrix multiplication sequentially");
      long seqStartTime = System.currentTimeMillis();
      sequentialMultiplyMatrix(a, b);
      long seqEndTime = System.currentTimeMillis();
      long duration = seqEndTime - seqStartTime;
      csvWriter.append(String.valueOf(matrixSize));
      csvWriter.append(",");
      csvWriter.append(String.valueOf(duration));
      csvWriter.append("\n");
    }

    csvWriter.append("NumThreads");
    csvWriter.append(String.valueOf(PREFERRED_NUM_THREADS));
    csvWriter.append("\n");

    csvWriter.append("Parallel");
    csvWriter.append("\n");
    csvWriter.append("MatrixSize");
    csvWriter.append(",");
    csvWriter.append("ExecutionTime");
    csvWriter.append("\n");

    // Parallel multiplication
    for(int matrixSize : matrixSizes) {
      System.out.println("Part 1.5");
      System.out.println("Running " + matrixSize + " by " + matrixSize + " matrix multiplication in parallel with " + PREFERRED_NUM_THREADS + " threads.");
      executor = Executors.newFixedThreadPool(PREFERRED_NUM_THREADS);
      long pStartTime = System.currentTimeMillis();
      parallelMultiplyMatrix(a, b);
      long pEndTime = System.currentTimeMillis();
      long duration = pEndTime - pStartTime;
      csvWriter.append(String.valueOf(matrixSize));
      csvWriter.append(",");
      csvWriter.append(String.valueOf(duration));
      csvWriter.append("\n");
    }

    csvWriter.flush();
    csvWriter.close();
  }

  // varying numThreads from 2 to 6 for matrixSize = 2000
  public static void testExecTimeVSNumThreads() throws IOException {
    double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
    double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);

    FileWriter csvWriter = new FileWriter("Part4.csv");
    csvWriter.append("NumThreads");
    csvWriter.append(",");
    csvWriter.append("ExecutionTime");
    csvWriter.append("\n");

    for (int i = 2; i <= numThreads; i++) {
      System.out.println("Part 1.4");
      System.out.println("Running 2000 by 2000 matrix multiplication in parallel with " + i + " threads.");
      executor = Executors.newFixedThreadPool(numThreads);
      long pStartTime = System.currentTimeMillis();
      parallelMultiplyMatrix(a, b);
      long pEndTime = System.currentTimeMillis();
      long duration = pEndTime - pStartTime;
      csvWriter.append(String.valueOf(i));
      csvWriter.append(",");
      csvWriter.append(String.valueOf(duration));
      csvWriter.append("\n");
    }

    csvWriter.flush();
    csvWriter.close();
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

    // row-view of matrix multiplication
    // each task will compute 1 cell value of the product matrix
    for (int i = 0; i < r1; i++) { // for each row of first matrix
      for (int j = 0; j < c2; j++) { // for each column of second matrix
        executor.execute(new MatrixMultiplyTask(a[i], b, i, j, product));
      }
    }

    executor.shutdown();

    // Wait until all tasks are finished
    while (!executor.isTerminated()) {
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

    MatrixMultiplyTask(double[] rowA, double[][] matrixB, int rowIndex, int colIndex,
        double[][] prod) {
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
      for (int i = 0; i < dimension; i++) {
        cellValue += rowA[i] * matrixB[i][colIndex];
      }
      prod[rowIndex][colIndex] = cellValue;
    }
  }

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

