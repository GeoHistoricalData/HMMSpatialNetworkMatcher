package fr.ign.cogit.v2.utils;

import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.jet.math.Mult;
import cern.jet.math.PlusMult;

/**
 * Utilitaire pour la librairie Colt
 * @author bcostes
 * 
 */
public class ColtUtils {

  private static final Logger logger = Logger.getLogger(ColtUtils.class);

  /**
   * Multiplication d'une matrice par un scalaire
   * @param A
   * @param a
   * @return
   */
  public static DoubleMatrix2D multScalar(DoubleMatrix2D A, double a) {
    return A.assign(Mult.mult(a));
  }

  /**
   * Addition de deux matrices de mêmes dimentions
   * @param A
   * @param B
   * @return
   */
  public static DoubleMatrix2D plus(DoubleMatrix2D A, DoubleMatrix2D B) {
    if (A.rows() != B.rows() || A.columns() != B.columns()) {
      if (logger.isInfoEnabled()) {
        logger.info("Matrix dimensions do not fit, null value returned");
      }
      return null;
    }
    DoubleMatrix2D C = A.assign(B, PlusMult.plusMult(1));
    return C;
  }

  /**
   * Soustraction de deux matrices de mêmes dimentions
   * @param A
   * @param B
   * @return
   */
  public static DoubleMatrix2D minus(DoubleMatrix2D A, DoubleMatrix2D B) {
    if (A.rows() != B.rows() || A.columns() != B.columns()) {
      if (logger.isInfoEnabled()) {
        logger.info("Matrix dimensions do not fit, null value returned");
      }
      return null;
    }
    DoubleMatrix2D C = A.assign(B, PlusMult.minusMult(1));
    return C;
  }

  /**
   * Donne la matrice identitée de tailel n
   * @param n
   * @return
   */
  public static DoubleMatrix2D identityMatrix(int n) {
    DoubleFactory2D fact = DoubleFactory2D.sparse;
    return fact.identity(n);
  }

  /**
   * Supprime la row'ème ligne de la matrice A
   * @param A
   * @param row
   * @return
   */
  public static DoubleMatrix2D deleteRow(DoubleMatrix2D A, int row) {
    DoubleMatrix2D B = new SparseDoubleMatrix2D(A.rows() - 1, A.columns());
    int cptRow = 0;
    for (int i = 0; i < A.rows(); i++) {
      if (i == row) {
        continue;
      }
      for (int j = 0; j < A.columns(); j++) {
        B.setQuick(cptRow, j, A.getQuick(i, j));
      }
      cptRow++;
    }
    return B;
  }

  /**
   * Supprime la column'ème colonne de la matrice A
   * @param A
   * @param row
   * @return
   */
  public static DoubleMatrix2D deleteColumn(DoubleMatrix2D A, int column) {
    DoubleMatrix2D B = new SparseDoubleMatrix2D(A.rows(), A.columns() - 1);
    for (int i = 0; i < A.rows(); i++) {
      int cptCol = 0;
      for (int j = 0; j < A.columns(); j++) {
        if (j == column) {
          continue;
        }
        B.setQuick(i, cptCol, A.getQuick(i, j));
        cptCol++;
      }
    }
    return B;
  }

  /**
   * Supprime la ieme ligne et ieme colonne de la matrice A
   * @param A
   * @param row
   * @return
   */
  public static DoubleMatrix2D delete(DoubleMatrix2D A, int i) {
    DoubleMatrix2D B = new SparseDoubleMatrix2D(A.rows() - 1, A.columns() - 1);
    int cptRow = 0;
    for (int k = 0; k < A.rows(); k++) {
      if (k == i) {
        continue;
      }
      int cptColumn = 0;
      for (int j = 0; j < A.columns(); j++) {
        if (j == i) {
          continue;
        }
        B.setQuick(cptRow, cptColumn, A.getQuick(k, j));
        cptColumn++;
      }
      cptRow++;
    }
    return B;
  }

  /*
   * public static void main(String args[]) {
   * 
   * }
   */
}
