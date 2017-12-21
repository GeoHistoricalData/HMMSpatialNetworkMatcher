package fr.ign.cogit.morphogenesis.exploring_tool.utils.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManagement {

  public final static String SHP = "shp"; //$NON-NLS-1$
  public final static String DBF = "dbf"; //$NON-NLS-1$
  public final static String SHX = "shx"; //$NON-NLS-1$
  public final static String COORD = "coord"; //$NON-NLS-1$

  /**
   * Returns the valid shapefiles in a directory and its sub-directories. A
   * valid shapefile is a shapefile which has at least the SHP, DBF and SHX
   * extensions.
   * 
   * @param directory the directory to search in
   * @return the correct shapefiles in a directory and its sub-directories
   */
  public static List<File> getValidShapefilesInDirectory(File directory) {
    List<File> files = new ArrayList<File>();

    if (directory.isDirectory()) {
      File[] list = directory.listFiles();
      if (list != null) {
        for (int i = 0; i < list.length; i++) {
          File f = list[i];
          if (f.isFile()
              && (f.getName().endsWith("." + SHP) || f.getName().endsWith("." + SHP.toUpperCase()))) { //$NON-NLS-1$ //$NON-NLS-2$
            String troncatedPath = f.getAbsolutePath().substring(0,
                f.getAbsolutePath().length() - 3);
            if ((new File(troncatedPath + DBF).isFile() || new File(
                troncatedPath + DBF.toUpperCase()).isFile())
                && (new File(troncatedPath + SHX).isFile() || new File(
                    troncatedPath + SHX.toUpperCase()).isFile())) {
              files.add(list[i]);
            }
          } else {
            files.addAll(getValidShapefilesInDirectory(list[i]));
          }
        }
      }
    }

    return files;
  }

}
