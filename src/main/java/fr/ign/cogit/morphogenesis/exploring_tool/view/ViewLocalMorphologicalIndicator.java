package fr.ign.cogit.morphogenesis.exploring_tool.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import fr.ign.cogit.morphogenesis.exploring_tool.model.layers.GraphLayer;

public class ViewLocalMorphologicalIndicator extends JFrame {

  private static final long serialVersionUID = 1L;
  private MainFrame mainFrame;
  private String indicator;
  private ChartPanel chartPanel;
  private CheckBoxList listGraphs;
  private JComboBox combo;
  private int mode = -1; // mode : 0: distribution, 1: cumulative distribution

  String layer = "Couche : "; //$NON-NLS-1$

  public ViewLocalMorphologicalIndicator(MainFrame mainFrame, String indicator) {
    this.mainFrame = mainFrame;
    this.indicator = indicator;
    this.setPreferredSize(new Dimension(950, 600));
    this.setResizable(false);
    this.setTitle(indicator);

    JPanel mainPanel = new JPanel();

    PanelLayer panelLayer = this.mainFrame.getCurrentPanel();

    this.combo = new JComboBox();
    combo.addItem("Distribution");
    combo.addItem("Cumulative distribution");
    combo.addItem("Log-Log Cumulative distribution");
    combo.setSelectedIndex(0);
    combo.setPreferredSize(new Dimension(200, 25));
    combo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateChartPanel();
      }
    });
    JPanel panelCombo = new JPanel();
    panelCombo.setLayout(new FlowLayout());
    panelCombo.add(combo);
    mainPanel.add(panelCombo);

    int cpt = 0;
    this.listGraphs = new CheckBoxList();
    for (GraphLayer gl : panelLayer.getGraphLayer()) {
      JCheckBox c = new JCheckBox(gl.getG().getDate());
      c.setSelected(true);
      c.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          updateChartPanel();
        }
      });
      listGraphs.addCheckbox(c);
      cpt++;
    }
    // le graph
    this.chartPanel = new ChartPanel(null);
    this.chartPanel.setPreferredSize(new Dimension(750, 480));
    updateChartPanel();
    mainPanel.add(chartPanel);
    mainPanel.add(listGraphs);

    // la liste
    this.setContentPane(mainPanel);
    this.pack();
  }

  private void updateChartPanel() {

    int the_mode = this.combo.getSelectedIndex();
    if (the_mode == this.mode) {
      return;
    }
    this.mode = the_mode;

    NumberAxis domainAxis = null;
    NumberAxis rangeAxis = null;
    String plotTitle = indicator;
    String xaxis = "value";
    String yaxis = "%";
    PlotOrientation orientation = PlotOrientation.VERTICAL;
    boolean show = true;
    boolean toolTips = false;
    boolean urls = false;
    XYSeriesCollection dataset = new XYSeriesCollection();
    JCheckBox[] listC = new JCheckBox[this.listGraphs.getModel().getSize()];
    for (int i = 0; i < listC.length; i++) {
      listC[i] = (JCheckBox) this.listGraphs.getModel().getElementAt(i);
    }
    double min = Double.MAX_VALUE;
    if (mode == 2) {
      // log mode
      min = getMinValue(listC);
    }

    int cpt = 0;
    for (JCheckBox c : listC) {
      if (c.isSelected()) {
        double[][] serieT = null;
        switch (this.mode) {
          case 0:
            // distribution
            serieT = this.createDistribution(this.mainFrame.getCurrentPanel()
                .getGraphLayer().get(cpt).getG()
                .getLocalMorphologicalIndicator(indicator), 100);
            domainAxis = new NumberAxis(xaxis);
            rangeAxis = new NumberAxis(yaxis);
            break;
          case 1:
            // distribution cumulative
            serieT = this.createCumulativeDistribution(this.mainFrame
                .getCurrentPanel().getGraphLayer().get(cpt).getG()
                .getLocalMorphologicalIndicator(indicator), 100);
            domainAxis = new NumberAxis(xaxis);
            rangeAxis = new NumberAxis(yaxis);
            break;
          case 2:
            // distribution cumulative LOG LOG
            serieT = this.createLogLogCumulativeDistribution(this.mainFrame
                .getCurrentPanel().getGraphLayer().get(cpt).getG()
                .getLocalMorphologicalIndicator(indicator), 100, min);
            domainAxis = new LogarithmicAxis(xaxis);
            rangeAxis = new LogarithmicAxis(yaxis);
            break;
        }

        XYSeries series = new XYSeries(this.mainFrame.getCurrentPanel()
            .getGraphLayer().get(cpt).getG().getDate());
        for (int i = 0; i < serieT.length; i++) {
          series.add(serieT[i][0], serieT[i][1]);
        }
        dataset.addSeries(series);
      }
      cpt++;
    }

    JFreeChart jfreechart = ChartFactory.createXYLineChart(plotTitle, xaxis,
        yaxis, dataset, orientation, show, toolTips, urls);
    final XYPlot plot = jfreechart.getXYPlot();
    plot.setDomainAxis(domainAxis);
    plot.setRangeAxis(rangeAxis);
    jfreechart.setBackgroundPaint(Color.white);
    this.chartPanel.setChart(jfreechart);
    this.chartPanel.repaint();
    this.repaint();
  }

  public class CheckBoxList extends JList {
    private static final long serialVersionUID = 1L;
    protected Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    public CheckBoxList() {
      setCellRenderer(new CellRenderer());

      addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          int index = locationToIndex(e.getPoint());

          if (index != -1) {
            JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
            checkbox.setSelected(!checkbox.isSelected());
            repaint();
          }
        }
      });

      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public void addCheckbox(JCheckBox checkBox) {
      ListModel currentList = this.getModel();
      JCheckBox[] newList = new JCheckBox[currentList.getSize() + 1];
      for (int i = 0; i < currentList.getSize(); i++) {
        newList[i] = (JCheckBox) currentList.getElementAt(i);
      }
      newList[newList.length - 1] = checkBox;
      setListData(newList);
    }

    protected class CellRenderer implements ListCellRenderer {
      public Component getListCellRendererComponent(JList list, Object value,
          int index, boolean isSelected, boolean cellHasFocus) {
        JCheckBox checkbox = (JCheckBox) value;
        checkbox.setBackground(isSelected ? getSelectionBackground()
            : getBackground());
        checkbox.setForeground(isSelected ? getSelectionForeground()
            : getForeground());
        checkbox.setEnabled(isEnabled());
        checkbox.setFont(getFont());
        checkbox.setFocusPainted(false);
        checkbox.setBorderPainted(true);
        checkbox.setBorder(isSelected ? UIManager
            .getBorder("List.focusCellHighlightBorder") : noFocusBorder);
        return checkbox;
      }
    }
  }

  private double[][] createDistribution(double[] values, int pas) {
    double epsilon = 0.00001;
    List<Double> valuesL = new ArrayList<Double>();
    for (double d : values) {
      valuesL.add(d);
    }
    Collections.sort(valuesL);
    double min = valuesL.get(0);
    double max = valuesL.get(valuesL.size() - 1);
    double step = (max - min) / ((double) pas);
    double[][] result = new double[pas][2];
    for (int i = 0; i < pas; i++) {
      result[i][0] = min + step * (i + 1);
      result[i][1] = 0;
    }
    int bornSup = 1;
    for (Double d : valuesL) {
      if (d < min + bornSup * step + epsilon) {
        result[bornSup - 1][1]++;
      } else {
        result[bornSup][1]++;
        bornSup++;
      }
    }
    for (int i = 0; i < pas; i++) {
      result[i][1] = result[i][1] / ((double) values.length);
    }
    return result;
  }

  private double[][] createCumulativeDistribution(double[] values, int pas) {
    List<Double> valuesL = new ArrayList<Double>();
    for (double d : values) {
      valuesL.add(d);
    }
    Collections.sort(valuesL);
    double min = valuesL.get(0);
    double max = valuesL.get(valuesL.size() - 1);
    double step = (max - min) / ((double) 100d);
    double[][] result = new double[100][2];
    for (int i = 0; i < 100; i++) {
      result[i][0] = min + i * step;
      result[i][1] = 0;
    }
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < 100; j++) {
        if (values[i] >= values[0] + j * step) {
          result[j][1]++;
        }
      }
    }

    max = result[0][1];
    for (int j = 0; j < 100; j++) {
      result[j][1] = result[j][1] / max;
    }

    return result;
  }

  private double[][] createLogLogCumulativeDistribution(double[] values,
      int pas, double minn) {
    List<Double> valuesL = new ArrayList<Double>();
    for (double d : values) {
      if (minn > 0) {
        valuesL.add(d);
      } else {
        valuesL.add(d + Math.abs(minn) + 1);
      }
    }
    Collections.sort(valuesL);
    double min = valuesL.get(0);
    double max = valuesL.get(valuesL.size() - 1);
    double step = (max - min) / ((double) 100d);
    double[][] result = new double[100][2];
    for (int i = 0; i < 100; i++) {
      result[i][0] = min + i * step;
      result[i][1] = 0;
    }
    for (int i = 0; i < valuesL.size(); i++) {
      for (int j = 0; j < 100; j++) {
        if (valuesL.get(i) >= min + j * step) {
          result[j][1]++;
        }
      }
    }

    max = result[0][1];
    for (int j = 0; j < 100; j++) {
      result[j][1] = result[j][1] / max;
    }

    return result;
  }

  private double getMinValue(JCheckBox[] listC) {
    List<Double> valuesL = new ArrayList<Double>();
    int cpt = 0;
    for (JCheckBox c : listC) {
      if (c.isSelected()) {
        double[] d = this.mainFrame.getCurrentPanel().getGraphLayer().get(cpt)
            .getG().getLocalMorphologicalIndicator(indicator);
        for (double dd : d) {
          valuesL.add(dd);
        }
      }
    }
    Collections.sort(valuesL);
    return valuesL.get(0);
  }
}
