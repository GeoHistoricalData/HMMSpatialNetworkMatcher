package fr.ign.cogit.morphogenesis.network.graph.rewriting.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.jfree.ui.tabbedui.VerticalLayout;

public class NewProjectFrame extends JFrame {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private MainFrame f;
  private JTextField title, filePrimal, fileDual, fileDistrict;
  private JButton choosePrimal, chooseDual, chooseDistrict, ok, cancel;
  private JRadioButton buildStroke;
  private JPanel panelButton, mainPanel;

  public NewProjectFrame(MainFrame f) {
    this.f = f;
    this.setPreferredSize(new Dimension(500, 500));
    this.setTitle("Nouveau projet");
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.setResizable(false);

    this.mainPanel = new JPanel(new BorderLayout());

    this.panelButton = new JPanel();
    cancel = new JButton("Annuler");
    cancel.addActionListener(new cancelActionPerfromed(this));

    ok = new JButton("ok");
    ok.setEnabled(false);
    ok.addActionListener(new okActionPerfromed(this));
    panelButton.add(cancel);

    panelButton.add(ok);

    JPanel panelCentral = new JPanel();
    panelCentral.setLayout(new VerticalLayout());

    JPanel panelTitle = new JPanel();
    panelTitle.setLayout(new VerticalLayout());
    panelTitle.setBorder(BorderFactory.createTitledBorder("Nom"));
    title = new JTextField("Caractérisation");
    title.setPreferredSize(new Dimension(460, 30));
    panelTitle.add(title);

    JPanel panelPrimal = new JPanel();
    panelPrimal.setLayout(new VerticalLayout());
    panelPrimal.setBorder(BorderFactory.createTitledBorder("Analyse primale"));
    JPanel panelChooseFilePrimal = new JPanel();
    if (filePrimal == null) {
      filePrimal = new JTextField();
      filePrimal.setEditable(false);
    } else {
      filePrimal = new JTextField(filePrimal.getText());
    }
    filePrimal.setPreferredSize(new Dimension(390, 28));
    panelChooseFilePrimal.add(filePrimal);
    choosePrimal = new JButton("...");
    choosePrimal.addActionListener(new choosePrimalActionPerformed(this));
    panelChooseFilePrimal.add(choosePrimal);
    panelPrimal.add(panelChooseFilePrimal);

    JPanel panelDual = new JPanel();
    panelDual.setLayout(new VerticalLayout());
    panelDual.setBorder(BorderFactory.createTitledBorder("Analyse duale"));

    JPanel panelChooseFileDual = new JPanel();
    if (fileDual == null) {
      fileDual = new JTextField();
      fileDual.setEditable(false);
    } else {
      fileDual = new JTextField(fileDual.getText());
    }
    fileDual.setPreferredSize(new Dimension(390, 28));
    panelChooseFileDual.add(fileDual);
    chooseDual = new JButton("...");
    chooseDual.addActionListener(new chooseDualActionPerformed(this));
    panelChooseFileDual.add(chooseDual);
    panelDual.add(panelChooseFileDual);
    buildStroke = new JRadioButton("Construire les strokes ?");
    buildStroke.setEnabled(false);
    buildStroke.setPreferredSize(new Dimension(450, 20));
    panelDual.add(Box.createRigidArea(new Dimension(0, 15)));
    panelDual.add(buildStroke);

    JPanel panelDistrict = new JPanel();
    panelDistrict.setLayout(new VerticalLayout());
    panelDistrict.setBorder(BorderFactory.createTitledBorder("Quartiers"));

    JPanel panelChooseFileDistrict = new JPanel();
    if (fileDistrict == null) {
      fileDistrict = new JTextField();
      fileDistrict.setEditable(false);
    } else {
      fileDistrict = new JTextField(fileDistrict.getText());
    }
    fileDistrict.setPreferredSize(new Dimension(390, 28));
    panelChooseFileDistrict.add(fileDistrict);
    chooseDistrict = new JButton("...");
    chooseDistrict.addActionListener(new chooseDistrictActionPerformed(this));
    panelChooseFileDistrict.add(chooseDistrict);
    panelDistrict.add(panelChooseFileDistrict);

    panelCentral.add(panelTitle);
    panelCentral.add(Box.createRigidArea(new Dimension(0, 25)));
    panelCentral.add(panelPrimal);
    panelCentral.add(Box.createRigidArea(new Dimension(0, 25)));
    panelCentral.add(panelDual);
    panelCentral.add(Box.createRigidArea(new Dimension(0, 25)));
    panelCentral.add(panelDistrict);
    panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    mainPanel.add(panelButton, BorderLayout.SOUTH);
    mainPanel.add(panelCentral, BorderLayout.CENTER);
    this.setContentPane(mainPanel);
    this.pack();
  }

  class chooseDistrictActionPerformed implements ActionListener {

    public NewProjectFrame f;

    public chooseDistrictActionPerformed(NewProjectFrame f) {
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      JFileChooser jfc = new JFileChooser();
      jfc = new JFileChooser();
      jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      jfc.setMultiSelectionEnabled(false);
      jfc.setDialogTitle("Choisir un fichier Shapefile");
      jfc.setCurrentDirectory(new File(
          "/media/Data/Benoit/these/analyses/centralites"));

      int result = jfc.showOpenDialog(null);
      if (result == JFileChooser.APPROVE_OPTION) {
        String fileName = jfc.getSelectedFile().getAbsolutePath();
        f.fileDistrict.setText(fileName);
      }
    }
  }

  class choosePrimalActionPerformed implements ActionListener {
    public NewProjectFrame f;

    public choosePrimalActionPerformed(NewProjectFrame f) {
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      JFileChooser jfc = new JFileChooser();
      jfc = new JFileChooser();
      jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      jfc.setMultiSelectionEnabled(false);
      jfc.setDialogTitle("Choisir un fichier Shapefile");
      jfc.setCurrentDirectory(new File(
          "/media/Data/Benoit/these/analyses/centralites"));

      int result = jfc.showOpenDialog(null);
      if (result == JFileChooser.APPROVE_OPTION) {
        String fileName = jfc.getSelectedFile().getAbsolutePath();
        f.filePrimal.setText(fileName);
        f.ok.setEnabled(true);

      }
    }

  }

  class chooseDualActionPerformed implements ActionListener {
    public NewProjectFrame f;

    public chooseDualActionPerformed(NewProjectFrame f) {
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      JFileChooser jfc = new JFileChooser();
      jfc = new JFileChooser();
      jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      jfc.setMultiSelectionEnabled(false);
      jfc.setDialogTitle("Choisir un fichier Shapefile");
      jfc.setCurrentDirectory(new File(
          "/media/Data/Benoit/these/analyses/centralites"));

      int result = jfc.showOpenDialog(null);
      if (result == JFileChooser.APPROVE_OPTION) {
        String fileName = jfc.getSelectedFile().getAbsolutePath();
        f.fileDual.setText(fileName);
        f.ok.setEnabled(true);
      }
    }

  }

  class cancelActionPerfromed implements ActionListener {
    public NewProjectFrame f;

    public cancelActionPerfromed(NewProjectFrame f) {
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      f.setVisible(false);
      f.f.setEnabled(true);
      f.dispose();
    }

  }

  class okActionPerfromed implements ActionListener {
    public NewProjectFrame f;

    public okActionPerfromed(NewProjectFrame f) {
      this.f = f;
    }

    public void actionPerformed(ActionEvent e) {
      f.f.setTitle(f.title.getText());

      if (f.filePrimal.getText().equals("") && f.fileDual.getText().equals("")) {
        return;
      }
      f.setVisible(false);
      f.dispose();

      if (!f.filePrimal.getText().equals("")) {
        f.f.setFilePrimal(f.filePrimal.getText());
        f.f.setPrimalAnalysis(true);
        f.f.createGeometricalGraph(f.filePrimal.getText());
      }
      if (!f.fileDual.getText().equals("")) {
        f.f.setFileDual(f.fileDual.getText());
        f.f.setDualAnalysis(true);
        f.f.createTopologicalGraph(f.fileDual.getText());
      }

      if (!f.fileDistrict.getText().equals("")) {
        f.f.setDistrictAnalysis(true);
      }
      f.f.initMap(fileDistrict.getText());

      f.f.setEnabled(true);
    }
  }

  public static void main(String args[]) {
    NewProjectFrame f = new NewProjectFrame(null);
    f.setVisible(true);

  }

}
