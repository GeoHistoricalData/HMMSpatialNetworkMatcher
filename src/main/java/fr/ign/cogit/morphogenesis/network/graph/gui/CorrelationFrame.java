package fr.ign.cogit.morphogenesis.network.graph.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

public class CorrelationFrame extends JFrame {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private MainFrame f;
  private JTabPane tabPane;

  public CorrelationFrame(MainFrame f, JTabPane tabPane) {
    this.f = f;
    this.tabPane = tabPane;
    this.f.setEnabled(false);
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.setTitle("Correlation");

    JTabbedPane panel = new JTabbedPane();
    panel.add("Pearson correlation", this.getPearsonTab());
    panel.add("Spearmans correlation", this.getSpearmansTab());

    JPanel panelBut = new JPanel();
    JButton ok = new JButton("Fermer");
    ok.addActionListener(new okButtonAction(this));
    panelBut.add(ok);

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());
    mainPanel.add(panel, BorderLayout.CENTER);
    mainPanel.add(panelBut, BorderLayout.SOUTH);
    this.setContentPane(mainPanel);
    this.pack();
  }

  private Component getSpearmansTab() {

    Map<Shape, Double> map = this.tabPane.getCentralities()
        .get(this.tabPane.getCentralities().keySet().iterator().next())
        .getValues();

    double[][] values = new double[map.keySet().size()][this.tabPane
        .getCentralities().size()];

    int cptj = 0;
    for (String s : this.tabPane.getCentralities().keySet()) {
      Map<Shape, Double> map2 = this.tabPane.getCentralities().get(s)
          .getValues();

      int cpti = 0;
      for (Shape shape : map.keySet()) {
        Shape shape2 = null;
        for (Shape ss : map2.keySet()) {
          if (ss.getBounds().equals(shape.getBounds())) {
            shape2 = ss;
            break;
          }
        }
        values[cpti][cptj] = map2.get(shape2);
        cpti++;
      }
      cptj++;
    }

    SpearmansCorrelation cor = new SpearmansCorrelation();
    RealMatrix M = cor.computeCorrelationMatrix(values);
    String[][] donnees = new String[M.getRowDimension()][M.getColumnDimension() + 1];
    DecimalFormat df = new DecimalFormat("0.00");
    for (int i = 0; i < M.getRowDimension(); i++) {
      for (int j = 1; j <= M.getColumnDimension(); j++) {
        donnees[i][j] = df.format(M.getEntry(i, j - 1));
      }
    }

    String[] entetes = new String[this.tabPane.getCentralities().keySet()
        .size() + 1];
    entetes[0] = "";
    int cpt = 1;
    for (String s : this.tabPane.getCentralities().keySet()) {
      entetes[cpt] = s;
      cpt++;
    }

    for (int i = 0; i < M.getRowDimension(); i++) {
      donnees[i][0] = entetes[i + 1];
    }

    JTable tableau = new JTable(donnees, entetes);
    JScrollPane pane = new JScrollPane(tableau);

    return pane;
  }

  private JScrollPane getPearsonTab() {

    Map<Shape, Double> map = this.tabPane.getCentralities()
        .get(this.tabPane.getCentralities().keySet().iterator().next())
        .getValues();

    double[][] values = new double[map.keySet().size()][this.tabPane
        .getCentralities().size()];

    int cptj = 0;
    for (String s : this.tabPane.getCentralities().keySet()) {
      Map<Shape, Double> map2 = this.tabPane.getCentralities().get(s)
          .getValues();

      int cpti = 0;
      for (Shape shape : map.keySet()) {
        Shape shape2 = null;
        for (Shape ss : map2.keySet()) {
          if (ss.getBounds().equals(shape.getBounds())) {
            shape2 = ss;
            break;
          }
        }
        values[cpti][cptj] = map2.get(shape2);
        cpti++;
      }
      cptj++;
    }

    PearsonsCorrelation cor = new PearsonsCorrelation(values);

    RealMatrix M = cor.getCorrelationMatrix();

    String[][] donnees = new String[M.getRowDimension()][M.getColumnDimension() + 1];
    DecimalFormat df = new DecimalFormat("0.00");
    for (int i = 0; i < M.getRowDimension(); i++) {
      for (int j = 1; j <= M.getColumnDimension(); j++) {
        donnees[i][j] = df.format(M.getEntry(i, j - 1));
      }
    }

    String[] entetes = new String[this.tabPane.getCentralities().keySet()
        .size() + 1];
    entetes[0] = "";
    int cpt = 1;
    for (String s : this.tabPane.getCentralities().keySet()) {
      entetes[cpt] = s;
      cpt++;
    }

    for (int i = 0; i < M.getRowDimension(); i++) {
      donnees[i][0] = entetes[i + 1];
    }

    JTable tableau = new JTable(donnees, entetes);
    JScrollPane pane = new JScrollPane(tableau);

    return pane;
  }

  class okButtonAction implements ActionListener {

    private CorrelationFrame f;

    public okButtonAction(CorrelationFrame f) {
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      this.f.setVisible(false);
      this.f.f.setEnabled(true);
      this.f.dispose();
    }
  }

}
