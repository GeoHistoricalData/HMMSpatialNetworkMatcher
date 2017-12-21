package fr.ign.cogit.morphogenesis.network.graph.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import fr.ign.cogit.morphogenesis.network.graph.io.ColorType;

public class ChangeColorTypeFrame extends JFrame {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private MainFrame f;
  private PanelGUI panelGui;
  private ChartPanel chartPanel;
  private JPanel mainPanel;
  private JScrollPane centerPanel;
  private JSpinner spinIntervalValue;
  private JComboBox types;
  private int colorType;
  private String oldGraphicSelected = "Distribution";

  public ChangeColorTypeFrame(MainFrame f, PanelGUI panelGui) {
    this.f = f;
    this.panelGui = panelGui;
    this.colorType = panelGui.getColorType();
    this.setPreferredSize(new Dimension(900, 550));
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.setResizable(false);
    this.setAlwaysOnTop(true);
    f.setEnabled(false);

    mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());

    JPanel colorPanel = new JPanel();
    JLabel labelColor = new JLabel("Catégorisation : ");
    types = new JComboBox();
    this.chartPanel = new ChartPanel(null);
    for (String type : ColorType.getTypes()) {
      types.addItem(type);
    }
    types.setSelectedIndex(this.colorType - 1);
    Integer value = new Integer(panelGui.getINTERVAL_SIZE());
    Integer min = new Integer(3);
    Integer max = new Integer(500);
    Integer step = new Integer(1);
    SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
    spinIntervalValue = new JSpinner(model);
    JLabel labelGraphic = new JLabel("Graphique : ");
    JComboBox graphic = new JComboBox();
    graphic.addActionListener(new graphicChangedListerner(this));
    graphic.addItem("Distribution X-Y");
    graphic.addItem("Distribution X-LOG");
    graphic.addItem("Distribution LOG-Y");
    graphic.addItem("Distribution LOG-LOG");
    graphic.addItem("Distribution cumulative X-Y");
    graphic.addItem("Distribution cumulative X-LOG");
    graphic.addItem("Distribution cumulative LOG-Y");
    graphic.addItem("Distribution cumulative LOG-LOG");
    graphic.addItem("log-log");
    graphic.setSelectedIndex(0);
    colorPanel.add(labelColor);
    colorPanel.add(types);
    colorPanel.add(new JLabel("Nb intervalles : "));
    colorPanel.add(spinIntervalValue);
    colorPanel.add(labelGraphic);
    colorPanel.add(graphic);
    centerPanel = new JScrollPane(chartPanel);

    JPanel buttonPanel = new JPanel();
    JButton ok = new JButton("Ok");
    ok.addActionListener(new okActionPerformed(this));
    JButton cancel = new JButton("Fermer");
    cancel.addActionListener(new cancelActionPerformed(this));
    buttonPanel.add(cancel);
    buttonPanel.add(ok);

    mainPanel.add(colorPanel, BorderLayout.NORTH);
    mainPanel.add(centerPanel, BorderLayout.CENTER);
    mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    this.setContentPane(mainPanel);
    this.pack();
  }

  class cancelActionPerformed implements ActionListener {

    private ChangeColorTypeFrame f;

    public cancelActionPerformed(ChangeColorTypeFrame f) {
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      f.f.setEnabled(true);
      f.setVisible(false);
      f.dispose();
    }

  }

  class okActionPerformed implements ActionListener {

    private ChangeColorTypeFrame f;

    public okActionPerformed(ChangeColorTypeFrame f) {
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      String s = (String) f.types.getSelectedItem();
      if (!s.equals(ColorType.getType(f.colorType))
          || f.panelGui.getINTERVAL_SIZE() != Integer
              .parseInt(spinIntervalValue.getValue().toString())) {
        f.colorType = ColorType.getType(s);
        f.panelGui.setINTERVAL_SIZE(Integer.parseInt(spinIntervalValue
            .getValue().toString()));
        f.panelGui.changeColorType(ColorType.getType(s));
      }
    }

  }

  class graphicChangedListerner implements ActionListener {

    private ChangeColorTypeFrame f;

    public graphicChangedListerner(ChangeColorTypeFrame f) {
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      JComboBox cb = (JComboBox) e.getSource();
      String petName = (String) cb.getSelectedItem();
      if (!petName.equals(oldGraphicSelected)) {
        if (petName.equals("Distribution X-Y")) {
          f.distribution();
        } else if (petName.equals("Distribution X-LOG")) {
          f.distributionValueLog();
        } else if (petName.equals("Distribution LOG-Y")) {
          f.distributionLogValue();
        } else if (petName.equals("Distribution LOG-LOG")) {
          f.distributionLogLog();
        } else if (petName.equals("Distribution cumulative X-Y")) {
          f.cumulativeDistribution();
        } else if (petName.equals("Distribution cumulative X-LOG")) {
          f.cumulativeDistributionValueLog();
        } else if (petName.equals("Distribution cumulative LOG-Y")) {
          f.cumulativeDistributionLogValue();
        } else if (petName.equals("Distribution cumulative LOG-LOG")) {
          f.cumulativeDistributionLogLog();
        }
      }
      oldGraphicSelected = petName;
    }
  }

  public void cumulativeDistribution() {

    double[] values = this.panelGui.getValuesSorted();
    double[] valuesCumul = new double[100];
    for (int i = 0; i < 100; i++) {
      valuesCumul[i] = 0;
    }
    double thresold = (values[values.length - 1] - values[0]) / 100.;
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < 100; j++) {
        if (values[i] >= values[0] + j * thresold) {
          valuesCumul[j]++;
        }
      }
    }

    double max = valuesCumul[0];
    for (int j = 0; j < 100; j++) {
      valuesCumul[j] = valuesCumul[j] / max;
    }

    final XYSeries series = new XYSeries("");
    for (int j = 0; j < 100; j++) {
      if (valuesCumul[j] != 0 && values[0] + j * thresold != 0) {
        series.add(values[0] + j * thresold, valuesCumul[j]);
      }
    }

    String plotTitle = "Cumulative distribution";
    String xaxis = "number";
    String yaxis = "value";
    PlotOrientation orientation = PlotOrientation.VERTICAL;
    boolean show = false;
    boolean toolTips = false;
    boolean urls = false;
    final XYSeriesCollection dataset = new XYSeriesCollection(series);
    JFreeChart chart = ChartFactory.createXYLineChart(plotTitle, xaxis, yaxis,
        dataset, orientation, show, toolTips, urls);
    final XYPlot plot = chart.getXYPlot();
    final NumberAxis domainAxis = new NumberAxis("");
    final NumberAxis rangeAxis = new NumberAxis("");
    plot.setDomainAxis(domainAxis);
    plot.setRangeAxis(rangeAxis);
    this.chartPanel.setChart(chart);
    this.chartPanel.repaint();
    this.repaint();
  }

  public void cumulativeDistributionLogValue() {

    double[] values = this.panelGui.getValuesSorted();
    double[] valuesCumul = new double[100];
    for (int i = 0; i < 100; i++) {
      valuesCumul[i] = 0;
    }
    double thresold = (values[values.length - 1] - values[0]) / 100.;
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < 100; j++) {
        if (values[i] >= values[0] + j * thresold) {
          valuesCumul[j]++;
        }
      }
    }
    double max = valuesCumul[0];
    for (int j = 0; j < 100; j++) {
      valuesCumul[j] = valuesCumul[j] / max;
    }
    final XYSeries series = new XYSeries("");
    for (int j = 0; j < 100; j++) {
      if (valuesCumul[j] != 0) {
        if (valuesCumul[j] != 0 && values[0] + j * thresold != 0) {
          series.add(values[0] + j * thresold, valuesCumul[j]);
        }
      }
    }

    String plotTitle = "Cumulative distribution LOG-Y";
    String xaxis = "number";
    String yaxis = "value";
    PlotOrientation orientation = PlotOrientation.VERTICAL;
    boolean show = false;
    boolean toolTips = false;
    boolean urls = false;
    final XYSeriesCollection dataset = new XYSeriesCollection(series);
    JFreeChart chart = ChartFactory.createXYLineChart(plotTitle, xaxis, yaxis,
        dataset, orientation, show, toolTips, urls);
    final XYPlot plot = chart.getXYPlot();
    final NumberAxis domainAxis = new LogarithmicAxis("");
    final NumberAxis rangeAxis = new NumberAxis("");
    plot.setDomainAxis(domainAxis);
    plot.setRangeAxis(rangeAxis);
    this.chartPanel.setChart(chart);
    this.chartPanel.repaint();
    this.repaint();
  }

  public void cumulativeDistributionValueLog() {

    double[] values = this.panelGui.getValuesSorted();
    double[] valuesCumul = new double[100];
    for (int i = 0; i < 100; i++) {
      valuesCumul[i] = 0;
    }
    double thresold = (values[values.length - 1] - values[0]) / 100.;
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < 100; j++) {
        if (values[i] >= values[0] + j * thresold) {
          valuesCumul[j]++;
        }
      }
    }
    double max = valuesCumul[0];
    for (int j = 0; j < 100; j++) {
      valuesCumul[j] = valuesCumul[j] / max;
    }
    final XYSeries series = new XYSeries("");
    for (int j = 0; j < 100; j++) {
      if (valuesCumul[j] != 0 && values[0] + j * thresold != 0) {
        series.add(values[0] + j * thresold, valuesCumul[j]);
      }
    }

    String plotTitle = "Cumulative distribution X-LOG";
    String xaxis = "number";
    String yaxis = "value";
    PlotOrientation orientation = PlotOrientation.VERTICAL;
    boolean show = false;
    boolean toolTips = false;
    boolean urls = false;
    final XYSeriesCollection dataset = new XYSeriesCollection(series);
    JFreeChart chart = ChartFactory.createXYLineChart(plotTitle, xaxis, yaxis,
        dataset, orientation, show, toolTips, urls);
    final XYPlot plot = chart.getXYPlot();
    final NumberAxis domainAxis = new NumberAxis("");
    final NumberAxis rangeAxis = new LogarithmicAxis("");
    plot.setDomainAxis(domainAxis);
    plot.setRangeAxis(rangeAxis);
    this.chartPanel.setChart(chart);
    this.chartPanel.repaint();
    this.repaint();
  }

  public void cumulativeDistributionLogLog() {

    double[] values = this.panelGui.getValuesSorted();
    double[] valuesCumul = new double[100];
    for (int i = 0; i < 100; i++) {
      valuesCumul[i] = 0;
    }
    double thresold = (values[values.length - 1] - values[0]) / 100.;
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < 100; j++) {
        if (values[i] >= values[0] + j * thresold) {
          valuesCumul[j]++;
        }
      }
    }
    double max = valuesCumul[0];
    for (int j = 0; j < 100; j++) {
      valuesCumul[j] = valuesCumul[j] / max;
    }
    final XYSeries series = new XYSeries("");
    for (int j = 0; j < 100; j++) {
      if (valuesCumul[j] != 0 && values[0] + j * thresold != 0) {
        series.add(values[0] + j * thresold, valuesCumul[j]);
      }
    }

    String plotTitle = "Cumulative distribution LOG-LOG";
    String xaxis = "number";
    String yaxis = "value";
    PlotOrientation orientation = PlotOrientation.VERTICAL;
    boolean show = false;
    boolean toolTips = false;
    boolean urls = false;
    final XYSeriesCollection dataset = new XYSeriesCollection(series);
    JFreeChart chart = ChartFactory.createXYLineChart(plotTitle, xaxis, yaxis,
        dataset, orientation, show, toolTips, urls);
    final XYPlot plot = chart.getXYPlot();
    final NumberAxis domainAxis = new LogarithmicAxis("");
    final NumberAxis rangeAxis = new LogarithmicAxis("");
    plot.setDomainAxis(domainAxis);
    plot.setRangeAxis(rangeAxis);
    this.chartPanel.setChart(chart);
    this.chartPanel.repaint();
    this.repaint();
  }

  public void distribution() {
    int pas = 100;
    HistogramDataset dataset = new HistogramDataset();
    dataset.setType(HistogramType.FREQUENCY);
    dataset.addSeries("Histogram", this.panelGui.getValuesSorted(), pas);
    String plotTitle = "Distribution";
    String xaxis = "value";
    String yaxis = "number";
    PlotOrientation orientation = PlotOrientation.VERTICAL;
    boolean show = false;
    boolean toolTips = false;
    boolean urls = false;
    JFreeChart chart = ChartFactory.createHistogram(plotTitle, xaxis, yaxis,
        dataset, orientation, show, toolTips, urls);
    final XYPlot plot = chart.getXYPlot();
    final NumberAxis domainAxis = new NumberAxis("");
    final NumberAxis rangeAxis = new NumberAxis("");
    plot.setDomainAxis(domainAxis);
    plot.setRangeAxis(rangeAxis);
    this.chartPanel.setChart(chart);
    this.chartPanel.repaint();
    this.repaint();
  }

  public void distributionValueLog() {
    int pas = 100;
    HistogramDataset dataset = new HistogramDataset();
    dataset.setType(HistogramType.FREQUENCY);
    dataset.addSeries("Histogram", this.panelGui.getValuesSorted(), pas);

    final XYSeries series = new XYSeries("");
    for (int i = 0; i < dataset.getItemCount(0); i++) {
      if (dataset.getXValue(0, i) > 0 && dataset.getYValue(0, i) > 0) {
        series.add(dataset.getXValue(0, i), dataset.getYValue(0, i));
      }
    }

    String plotTitle = "Distribution X-LOG";
    String xaxis = "number";
    String yaxis = "value";
    PlotOrientation orientation = PlotOrientation.VERTICAL;
    boolean show = false;
    boolean toolTips = false;
    boolean urls = false;
    final XYSeriesCollection datase2t = new XYSeriesCollection(series);
    JFreeChart chart = ChartFactory.createXYLineChart(plotTitle, xaxis, yaxis,
        datase2t, orientation, show, toolTips, urls);
    final XYPlot plot = chart.getXYPlot();
    final NumberAxis domainAxis = new NumberAxis("");
    final NumberAxis rangeAxis = new LogarithmicAxis("");
    plot.setDomainAxis(domainAxis);
    plot.setRangeAxis(rangeAxis);
    this.chartPanel.setChart(chart);
    this.chartPanel.repaint();
    this.repaint();
  }

  public void distributionLogValue() {
    int pas = 100;
    HistogramDataset dataset = new HistogramDataset();
    dataset.setType(HistogramType.FREQUENCY);
    dataset.addSeries("Histogram", this.panelGui.getValuesSorted(), pas);

    final XYSeries series = new XYSeries("");
    for (int i = 0; i < dataset.getItemCount(0); i++) {
      if (dataset.getXValue(0, i) > 0 && dataset.getYValue(0, i) > 0) {
        series.add(dataset.getXValue(0, i), dataset.getYValue(0, i));
      }
    }

    String plotTitle = "Distribution LOG-Y";
    String xaxis = "number";
    String yaxis = "value";
    PlotOrientation orientation = PlotOrientation.VERTICAL;
    boolean show = false;
    boolean toolTips = false;
    boolean urls = false;
    final XYSeriesCollection datase2t = new XYSeriesCollection(series);
    JFreeChart chart = ChartFactory.createXYLineChart(plotTitle, xaxis, yaxis,
        datase2t, orientation, show, toolTips, urls);
    final XYPlot plot = chart.getXYPlot();
    final NumberAxis domainAxis = new LogarithmicAxis("");
    final NumberAxis rangeAxis = new NumberAxis("");
    plot.setDomainAxis(domainAxis);
    plot.setRangeAxis(rangeAxis);
    this.chartPanel.setChart(chart);
    this.chartPanel.repaint();
    this.repaint();
  }

  public void distributionLogLog() {
    int pas = 100;
    HistogramDataset dataset = new HistogramDataset();
    dataset.setType(HistogramType.FREQUENCY);
    dataset.addSeries("Histogram", this.panelGui.getValuesSorted(), pas);

    final XYSeries series = new XYSeries("");
    for (int i = 0; i < dataset.getItemCount(0); i++) {
      if (dataset.getXValue(0, i) > 0 && dataset.getYValue(0, i) > 0) {
        series.add(dataset.getXValue(0, i), dataset.getYValue(0, i));
      }
    }

    String plotTitle = "Distribution LOG-LOG";
    String xaxis = "number";
    String yaxis = "value";
    PlotOrientation orientation = PlotOrientation.VERTICAL;
    boolean show = false;
    boolean toolTips = false;
    boolean urls = false;
    final XYSeriesCollection datase2t = new XYSeriesCollection(series);
    JFreeChart chart = ChartFactory.createXYLineChart(plotTitle, xaxis, yaxis,
        datase2t, orientation, show, toolTips, urls);
    final XYPlot plot = chart.getXYPlot();
    final NumberAxis domainAxis = new LogarithmicAxis("");
    final NumberAxis rangeAxis = new LogarithmicAxis("");
    plot.setDomainAxis(domainAxis);
    plot.setRangeAxis(rangeAxis);
    this.chartPanel.setChart(chart);
    this.chartPanel.repaint();
    this.repaint();
  }

}
