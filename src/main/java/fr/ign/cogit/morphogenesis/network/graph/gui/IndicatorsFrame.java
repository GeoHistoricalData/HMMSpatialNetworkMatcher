package fr.ign.cogit.morphogenesis.network.graph.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.DefaultPolarItemRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.tabbedui.VerticalLayout;

import fr.ign.cogit.morphogenesis.network.analysis.indicators.MainIndicator;
import fr.ign.cogit.morphogenesis.network.graph.AbstractGraph;
import fr.ign.cogit.morphogenesis.network.graph.io.Indicators;

public class IndicatorsFrame extends JFrame {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private MainFrame f;
  AbstractGraph<?, ?> graph;
  private ChartPanel chartPanel;
  private JPanel mainPanel;
  private JComboBox indicators, graphic;
  private int indicatorSelected = 0;
  private double[] valuesIndicator;
  private String oldGraphicSelected = "Distribution";

  public IndicatorsFrame(MainFrame f, AbstractGraph<?, ?> graph) {
    this.f = f;
    this.graph = graph;

    this.setPreferredSize(new Dimension(900, 550));
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.setResizable(false);
    this.setAlwaysOnTop(true);
    f.setEnabled(false);

    mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());

    JPanel indicatorPanel = new JPanel();
    JLabel labelColor = new JLabel("Indicateur : ");
    indicators = new JComboBox();
    indicators.addItem("---");
    indicators.addActionListener(new indicatorChanged(this));
    File file = new File(ParamFrame.class.getResource("/config/indicators.txt")
        .getFile());
    try {
      FileReader fr = new FileReader(file);
      BufferedReader br = new BufferedReader(fr);
      String line = "";
      while ((line = br.readLine()) != null) {
        indicators.addItem(line);
      }
      br.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    indicatorPanel.add(labelColor);
    indicatorPanel.add(indicators);

    graphic = new JComboBox();
    graphic.addActionListener(new graphicChangedListerner(this));
    graphic.addItem("Distribution X-Y");
    graphic.addItem("Distribution LOG-LOG");
    graphic.addItem("Distribution cumulative X-Y");
    graphic.addItem("Distribution cumulative LOG-LOG");
    graphic.setSelectedIndex(0);
    indicatorPanel.add(graphic);

    this.chartPanel = new ChartPanel(null);

    JPanel buttonPanel = new JPanel();
    JButton cancel = new JButton("Fermer");
    cancel.addActionListener(new cancelActionPerformed(this));
    buttonPanel.add(cancel);

    mainPanel.add(indicatorPanel, BorderLayout.NORTH);
    mainPanel.add(chartPanel, BorderLayout.CENTER);
    mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    this.setContentPane(mainPanel);
    this.pack();
  }

  class indicatorChanged implements ActionListener {

    private IndicatorsFrame f;

    public indicatorChanged(IndicatorsFrame f) {
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      JComboBox box = (JComboBox) e.getSource();
      if (f.indicatorSelected != box.getSelectedIndex()) {
        indicatorSelected = box.getSelectedIndex();
        if (indicatorSelected != 0) {
          String indicatorName = (String) box.getSelectedItem();

          if (indicatorName.equals(Indicators.SMALLWORLD)) {
            f.graphic.setEnabled(false);
            this.f.valuesIndicator = new double[4];
            this.f.valuesIndicator[0] = this.f.graph
                .getCharacteristicPathLength();
            this.f.valuesIndicator[1] = this.f.graph.getClusteringCoefficient();
            double[] random = this.f.graph.getSmallWorldProperties();
            this.f.valuesIndicator[2] = random[0];
            this.f.valuesIndicator[3] = random[1];
            this.f.smallWorldProperty(this.f.valuesIndicator);

          } else {

            f.valuesIndicator = MainIndicator.values(indicatorName,
                graph.getPop());
            int cpt = 0;
            List<Double> l = new ArrayList<Double>();
            for (int i = 0; i < f.valuesIndicator.length; i++) {
              l.add(f.valuesIndicator[i]);
            }
            Collections.sort(l);
            for (double d : l) {
              f.valuesIndicator[cpt] = d;
              cpt++;
            }
            if (indicatorName.equals(Indicators.ORIENTATION)
                || indicatorName.equals(Indicators.INTERSECTION)) {
              f.graphic.setEnabled(false);
              f.spiderWebDistribution(f.valuesIndicator);
            } else {
              f.graphic.setEnabled(true);
              f.graphic.setSelectedIndex(0);
            }
          }
        }

      }
    }
  }

  class cancelActionPerformed implements ActionListener {

    private IndicatorsFrame f;

    public cancelActionPerformed(IndicatorsFrame f) {
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      this.f.setVisible(false);
      this.f.f.setEnabled(true);
      this.f.dispose();
    }
  }

  class graphicChangedListerner implements ActionListener {

    private IndicatorsFrame f;

    public graphicChangedListerner(IndicatorsFrame f) {
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      if (indicatorSelected == 0) {
        return;
      }
      JComboBox cb = (JComboBox) e.getSource();
      String petName = (String) cb.getSelectedItem();

      if (!petName.equals(oldGraphicSelected)) {
        if (petName.equals("Distribution X-Y")) {
          f.distribution(this.f.valuesIndicator);
        } else if (petName.equals("Distribution LOG-LOG")) {
          f.distributionLogLog(this.f.valuesIndicator);
        } else if (petName.equals("Distribution cumulative X-Y")) {
          f.cumulativeDistribution(this.f.valuesIndicator);
        } else if (petName.equals("Distribution cumulative LOG-LOG")) {
          f.cumulativeDistributionLogLog(this.f.valuesIndicator);
        }
      }

      oldGraphicSelected = petName;
    }
  }

  public void distribution(double[] values) {
    int pas = 100;
    HistogramDataset dataset = new HistogramDataset();
    dataset.setType(HistogramType.FREQUENCY);
    dataset.addSeries("Histogram", values, pas);
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

  public void smallWorldProperty(double[] valuesIndicator) {
    JPanel panel = new JPanel();
    panel.setLayout(new VerticalLayout());
    JLabel label1 = new JLabel("L: " + valuesIndicator[0]);
    label1.setPreferredSize(new Dimension(340, 40));
    JLabel label2 = new JLabel("C : " + valuesIndicator[1]);
    label2.setPreferredSize(new Dimension(340, 40));
    JLabel label3 = new JLabel("LRandom : " + valuesIndicator[2]);
    label3.setPreferredSize(new Dimension(340, 40));
    JLabel label4 = new JLabel("CRandom : " + valuesIndicator[3]);
    label4.setPreferredSize(new Dimension(340, 40));

    panel.add(label1);
    panel.add(label2);
    panel.add(label3);
    panel.add(label4);

    JFrame f = new JFrame();
    f.setPreferredSize(new Dimension(350, 200));
    f.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    f.setContentPane(panel);
    f.setAlwaysOnTop(true);
    f.pack();
    f.setVisible(true);
  }

  public void distributionLogLog(double[] values) {

    int pas = 100;
    HistogramDataset dataset = new HistogramDataset();
    dataset.setType(HistogramType.FREQUENCY);
    dataset.addSeries("Histogram", values, pas);

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

  public void cumulativeDistribution(double[] values) {

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

  public void cumulativeDistributionLogLog(double[] values) {

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

  public void spiderWebDistribution(double[] values) {
    // Précision: 1 degré
    int prec = 2;
    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
    for (int i = 0; i < values.length; i++) {
      int index = (int) ((180. / Math.PI) * values[i]) / prec;
      if (map.containsKey(index)) {
        map.put(index, map.get(index) + 1);
      } else {
        map.put(index, 1);
      }
    }

    final XYSeries series = new XYSeries("");
    for (Integer i : map.keySet()) {
      series.add(prec * i, map.get(i));
      series.add((prec * i + 180), map.get(i));
    }
    XYSeriesCollection data = new XYSeriesCollection();
    data.addSeries(series);

    String plotTitle = "Spider Web Distribution";
    boolean show = false;
    boolean toolTips = false;
    boolean urls = false;
    JFreeChart chart = ChartFactory.createPolarChart(plotTitle, data, show,
        toolTips, urls);
    PolarPlot plot = (PolarPlot) chart.getPlot();
    final DefaultPolarItemRenderer renderer = (DefaultPolarItemRenderer) plot
        .getRenderer();
    renderer.setSeriesFilled(2, true);
    this.chartPanel.setChart(chart);
    this.chartPanel.repaint();
    this.repaint();
  }

}
