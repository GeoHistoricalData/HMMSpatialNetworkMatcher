package fr.ign.cogit.morphogenesis.exploring_tool.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Year;
import org.jfree.ui.RectangleInsets;

import fr.ign.cogit.morphogenesis.exploring_tool.model.layers.GraphLayer;

public class ViewGlobalMorphologicalIndicator extends JFrame {

  private static final long serialVersionUID = 1L;
  private MainFrame mainFrame;
  private String indicator;
  private ChartPanel chartPanel;

  String layer = "Couche : "; //$NON-NLS-1$

  public ViewGlobalMorphologicalIndicator(MainFrame mainFrame, String indicator) {
    this.mainFrame = mainFrame;
    this.indicator = indicator;
    this.setPreferredSize(new Dimension(800, 550));
    this.setResizable(false);
    this.setTitle(indicator);

    JPanel mainPanel = new JPanel();

    PanelLayer panelLayer = this.mainFrame.getCurrentPanel();

    int cpt = 0;
    String[] columnNames = { "Dates", indicator };
    String[][] data = new String[panelLayer.getGraphLayer().size()][2];
    for (GraphLayer gl : panelLayer.getGraphLayer()) {
      data[cpt][0] = gl.getG().getDate();
      data[cpt][1] = Double.toString(gl.getG()
          .getGlobalMorphologicalIndicators().get(indicator));
      cpt++;
    }
    JTable graphTable = new JTable(data, columnNames);
    // info couche
    graphTable.setEnabled(false);
    JScrollPane sP = new JScrollPane(graphTable);
    sP.setPreferredSize(new Dimension(780, 120));
    mainPanel.add(sP);

    // le graph
    this.chartPanel = new ChartPanel(null);
    createChartPanel();
    mainPanel.add(chartPanel);
    // la liste
    this.setContentPane(mainPanel);
    this.pack();
  }

  @SuppressWarnings("deprecation")
  private void createChartPanel() {
    TimeSeries s1 = new TimeSeries(indicator, Year.class);
    for (GraphLayer gl : this.mainFrame.getCurrentPanel().getGraphLayer()) {
      s1.add(new Year(Integer.parseInt(gl.getG().getDate())), gl.getG()
          .getGlobalMorphologicalIndicators().get(indicator));
    }
    TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(s1);
    JFreeChart jfreechart = ChartFactory.createTimeSeriesChart("", // title
        "Date", // x-axis label
        indicator, // y-axis label
        dataset, // data
        true, // create legend?
        true, // generate tooltips?
        false // generate URLs?
        );
    jfreechart.setBackgroundPaint(Color.white);
    XYPlot plot = (XYPlot) jfreechart.getPlot();
    plot.setBackgroundPaint(Color.lightGray);
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);
    plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

    XYItemRenderer r = plot.getRenderer();
    if (r instanceof XYLineAndShapeRenderer) {
      XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
      renderer.setBaseShapesVisible(true);
      renderer.setBaseShapesFilled(true);
      renderer.setDrawSeriesLineAsPath(false);
      renderer.setLinesVisible(false);
    }

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

}
