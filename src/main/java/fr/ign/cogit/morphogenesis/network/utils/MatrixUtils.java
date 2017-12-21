package fr.ign.cogit.morphogenesis.network.utils;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

public class MatrixUtils {

  public static DoubleMatrix2D sum(DoubleMatrix2D M1, DoubleMatrix2D M2) {
    int n = M1.rows();
    int m = M1.columns();
    DoubleMatrix2D M = new SparseDoubleMatrix2D(n, m);
    for (int row = 0; row < n; row++) {
      for (int column = 0; column < m; column++) {
        M.setQuick(row, column,
            M1.getQuick(row, column) + M2.getQuick(row, column));
      }
    }

    return M;
  }

}
