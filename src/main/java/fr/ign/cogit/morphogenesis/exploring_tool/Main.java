package fr.ign.cogit.morphogenesis.exploring_tool;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import fr.ign.cogit.morphogenesis.exploring_tool.view.MainFrame;

public class Main {

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      // Set cross-platform Java L&F (also called "Metal")
      MetalLookAndFeel.setCurrentTheme(new OceanTheme());
      UIManager.setLookAndFeel(new MetalLookAndFeel());
    } catch (UnsupportedLookAndFeelException e) {
      // handle exception
    }

    MainFrame frame = new MainFrame();
  }

}
