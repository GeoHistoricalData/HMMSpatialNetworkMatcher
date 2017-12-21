package fr.ign.cogit.morphogenesis.network.graph.rewriting.gui;

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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.ui.tabbedui.VerticalLayout;

import fr.ign.cogit.morphogenesis.network.graph.rewriting.GeometricalGraph;
import fr.ign.cogit.morphogenesis.network.graph.rewriting.TopologicalGraph;

public class ParamFrame extends JFrame {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private MainFrame f;
  private JButton next, previous, ok, cancel;
  private JCheckBox useDistricts;
  private int current_page = 0;
  private JPanel panelButton, mainPanel;
  private JScrollPane panelCentral;
  private List<JCheckBox> centralitiesPrimal, centralitiesDual;

  public ParamFrame(MainFrame f) {
    this.f = f;
    this.setPreferredSize(new Dimension(500, 500));
    this.setTitle("Nouveau projet");
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.setResizable(false);

    centralitiesDual = new ArrayList<JCheckBox>();
    centralitiesPrimal = new ArrayList<JCheckBox>();

    this.mainPanel = new JPanel(new BorderLayout());

    this.panelButton = new JPanel();
    cancel = new JButton("Annuler");
    cancel.addActionListener(new cancelActionPerfromed(this));
    previous = new JButton("Précédent");
    previous.addActionListener(new previousActionPerfromed(this));
    next = new JButton("Suivant");
    next.addActionListener(new nextActionPerfromed(this));
    ok = new JButton("ok");
    ok.addActionListener(new okActionPerfromed(this));
    panelButton.add(cancel);
    panelButton.add(previous);
    panelButton.add(next);
    panelButton.add(ok);

    panelCentral = new JScrollPane();

    if (!this.f.usePrimalAnalysis()) {
      current_page++;
    }

    buildPage(current_page);

    mainPanel.add(panelButton, BorderLayout.SOUTH);
    mainPanel.add(panelCentral, BorderLayout.CENTER);
    this.setContentPane(mainPanel);
    this.pack();
  }

  private void buildPage(int page) {
    JPanel pane = null;
    switch (page) {
      case 0:
        pane = buildPage0();
        break;
      case 1:
        pane = buildPage1();
        break;
      case 2:
        pane = buildPage2();
        break;
    }
    this.panelCentral.removeAll();
    this.mainPanel.remove(panelCentral);
    this.panelCentral = new JScrollPane(pane);
    mainPanel.add(panelCentral, BorderLayout.CENTER);
    this.pack();
    repaint();
  }

  private JPanel buildPage2() {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("Districts"));
    panel.setLayout(new VerticalLayout());
    panel.add(Box.createRigidArea(new Dimension(0, 15)));

    useDistricts = new JCheckBox("Réduire l'analyse au quartier sélectionné");
    // TODO : gérer les distrcits
    useDistricts.setEnabled(false);
    panel.add(useDistricts);

    if (this.f.useDualAnalysis()) {
      this.previous.setEnabled(true);
    }
    this.next.setEnabled(false);
    this.ok.setEnabled(true);

    return panel;
  }

  private JPanel buildPage0() {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("Analyse primale"));
    panel.setLayout(new VerticalLayout());
    panel.add(Box.createRigidArea(new Dimension(0, 15)));

    if (this.centralitiesPrimal.isEmpty()) {

      File f = new File(ParamFrame.class.getResource(
          "/config/centralities_primal.txt").getFile());

      try {

        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        String line = "";
        while ((line = br.readLine()) != null) {
          JCheckBox c = new JCheckBox(line);
          c.setPreferredSize(new Dimension(450, 30));
          c.setSelected(true);
          panel.add(c);
          centralitiesPrimal.add(c);
        }
        br.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      for (JCheckBox c : this.centralitiesPrimal) {
        panel.add(c);
      }
    }

    if (f.useDualAnalysis()) {
      this.next.setEnabled(true);
      this.ok.setEnabled(false);
      this.previous.setEnabled(false);
    } else {
      if (this.f.useDistrictAnalysis()
          && this.f.getPanelGuiGlobal().getDistrictSelected() != null) {
        next.setEnabled(true);
        ok.setEnabled(false);
        this.previous.setEnabled(false);
      } else {
        next.setEnabled(false);
        ok.setEnabled(true);
        this.previous.setEnabled(false);
      }
    }

    return panel;
  }

  private JPanel buildPage1() {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("Analyse duale"));
    panel.setLayout(new VerticalLayout());

    panel.add(Box.createRigidArea(new Dimension(0, 15)));

    if (this.centralitiesDual.isEmpty()) {

      File f = new File(ParamFrame.class.getResource(
          "/config/centralities_dual.txt").getFile());

      try {

        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        String line = "";
        while ((line = br.readLine()) != null) {
          JCheckBox c = new JCheckBox(line);
          c.setPreferredSize(new Dimension(450, 30));
          c.setSelected(true);
          panel.add(c);
          centralitiesDual.add(c);
        }
        br.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      for (JCheckBox c : this.centralitiesDual) {
        panel.add(c);
      }
    }

    if (f.usePrimalAnalysis()) {

      this.previous.setEnabled(true);
    }
    if (this.f.useDistrictAnalysis()
        && this.f.getPanelGuiGlobal().getDistrictSelected() != null) {
      next.setEnabled(true);
      ok.setEnabled(false);
    } else {
      next.setEnabled(false);
      ok.setEnabled(true);
    }

    return panel;
  }

  class nextActionPerfromed implements ActionListener {

    public ParamFrame f;

    public nextActionPerfromed(ParamFrame f) {
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      switch (f.current_page) {
        case 0:
          current_page++;
          buildPage(1);
          break;
        case 1:
          current_page++;
          buildPage(2);
          break;
      }
    }
  }

  class previousActionPerfromed implements ActionListener {

    public ParamFrame f;

    public previousActionPerfromed(ParamFrame f) {
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      switch (f.current_page) {
        case 1:
          current_page--;
          buildPage(0);

          break;
        case 2:
          current_page--;
          buildPage(1);
          break;
      }
    }
  }

  class cancelActionPerfromed implements ActionListener {
    public ParamFrame f;

    public cancelActionPerfromed(ParamFrame f) {
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      f.setVisible(false);
      f.f.setEnabled(true);
      f.dispose();
    }

  }

  class okActionPerfromed implements ActionListener {
    public ParamFrame f;

    public okActionPerfromed(ParamFrame f) {
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {

      List<String> centralitiesPrimal = new ArrayList<String>();
      List<String> centralitiesDual = new ArrayList<String>();

      for (JCheckBox c : f.centralitiesPrimal) {
        if (!c.isSelected()) {
          continue;
        }
        centralitiesPrimal.add(c.getText());
      }

      for (JCheckBox c : f.centralitiesDual) {
        if (!c.isSelected()) {
          continue;
        }
        centralitiesDual.add(c.getText());
      }

      // TODO: gérer les districts
      /*
       * if (useDistricts.isSelected()) {
       * this.f.f.setReductAnalysisToDistrictSelected(true); }
       */

      f.setVisible(false);
      if (!centralitiesPrimal.isEmpty()) {
        this.f.f.calculateCentralities((GeometricalGraph) this.f.f
            .getGraphById("Global Geometrical Graph"), centralitiesPrimal);
      }
      if (!centralitiesDual.isEmpty()) {
        this.f.f.calculateCentralities((TopologicalGraph) this.f.f
            .getGraphById("Global Topological Graph"), centralitiesDual);
      }
      f.f.setEnabled(true);
      f.dispose();
    }

  }

}
