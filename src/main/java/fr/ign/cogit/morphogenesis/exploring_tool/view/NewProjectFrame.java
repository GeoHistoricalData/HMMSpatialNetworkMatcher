package fr.ign.cogit.morphogenesis.exploring_tool.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.jfree.ui.tabbedui.VerticalLayout;

import fr.ign.cogit.morphogenesis.exploring_tool.controler.NewGraphPanelControler;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.Parameters;
import fr.ign.cogit.morphogenesis.exploring_tool.utils.io.FileManagement;

public class NewProjectFrame extends JFrame {

  private static final long serialVersionUID = 1L;
  private List<File> files;
  private String format;
  private String directory;
  private MainFrame mainFrame;
  private String mode;
  private NewGraphPanelControler controler;

  String frameTitle = "Nouveau projet - Sélectipn des fichiers de graphe"; //$NON-NLS-1$
  String noneFileFound = "Aucun fichier trouvé pour ce format : "; //$NON-NLS-1$
  String warning = "Erreur"; //$NON-NLS-1$
  String info1 = "   Liste des fichiers"; //$NON-NLS-1$
  String info2 = "   Fichiers sélectionnés"; //$NON-NLS-1$
  String ok = "Ok"; //$NON-NLS-1$
  String annuler = "Annuler"; //$NON-NLS-1$
  String noFileSelectedText = "Aucun fichier sélectionné"; //$NON-NLS-1$
  String frameDatesTitle = "Indiquez les dates des graphes"; //$NON-NLS-1$
  String datesWarining = "Il manque des dates."; //$NON-NLS-1$
  String dateTooShort = "Format de date non valide (XXXX, p.e 1885)"; //$NON-NLS-1$

  public NewProjectFrame(MainFrame mainFrame, String directory, String format,
      String mode) {
    this.directory = directory;
    this.mainFrame = mainFrame;
    this.format = format;
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.controler = new NewGraphPanelControler(this, this.mainFrame, mode);
    if (format.equals(Parameters.SHAPEFILE)) {
      this.files = FileManagement.getValidShapefilesInDirectory(new File(
          this.directory));
    }
    if (this.files.isEmpty()) {
      JOptionPane.showMessageDialog(this,
          noneFileFound + this.format.toLowerCase(), warning,
          JOptionPane.WARNING_MESSAGE);
      this.mainFrame.setEnabled(true);
      return;
    }

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());

    // info sur les listes
    JPanel panelInfo = new JPanel(new GridLayout(1, 2, 0, 0));
    JLabel label1 = new JLabel(info1);
    JLabel label2 = new JLabel(info2);
    panelInfo.add(label1);
    panelInfo.add(label2);

    // les listes
    JPanel p = new JPanel(new GridLayout(1, 2, 10, 0));
    TransferHandler h = new ListItemTransferHandler();
    p.setBorder(BorderFactory.createEmptyBorder());
    // liste de gaiche
    DefaultListModel listModel1 = new DefaultListModel();
    for (File f : this.files) {
      listModel1.addElement(f.getName().toString());
    }
    JList filesList1 = new JList(listModel1);

    filesList1.getSelectionModel().setSelectionMode(
        ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    filesList1.setDropMode(DropMode.INSERT);
    filesList1.setDragEnabled(true);
    filesList1.setTransferHandler(h);

    // liste de droite
    DefaultListModel listModel2 = new DefaultListModel();
    JList filesList2 = new JList(listModel2);
    filesList2.getSelectionModel().setSelectionMode(
        ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    filesList2.setDropMode(DropMode.INSERT);
    filesList2.setDragEnabled(true);
    filesList2.setTransferHandler(h);

    p.add(new JScrollPane(filesList1));
    p.add(new JScrollPane(filesList2));

    // Les boutons OK et annuler
    JPanel panelButtons = new JPanel();
    JButton butOk = new JButton(ok);
    butOk.addActionListener(new okPressed(this, listModel2));
    JButton butCancel = new JButton(annuler);
    butCancel.addActionListener(new cancelPressed(this));
    panelButtons.add(butOk);
    panelButtons.add(butCancel);

    mainPanel.add(panelInfo, BorderLayout.NORTH);
    mainPanel.add(p, BorderLayout.CENTER);
    mainPanel.add(panelButtons, BorderLayout.SOUTH);
    mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    this.setContentPane(mainPanel);
    this.setResizable(false);
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.setTitle(frameTitle);
    this.pack();
    this.setLocationRelativeTo(null);
  }

  /**
   * Drag and Drop manager
   * @author bcostes
   * 
   */
  class ListItemTransferHandler extends TransferHandler {
    private static final long serialVersionUID = 1L;
    private final DataFlavor localObjectFlavor;
    private Object[] transferedObjects = null;

    public ListItemTransferHandler() {
      localObjectFlavor = new ActivationDataFlavor(Object[].class,
          DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
    }

    private JList source = null;

    protected Transferable createTransferable(JComponent c) {
      source = (JList) c;
      indices = source.getSelectedIndices();
      transferedObjects = source.getSelectedValues();
      return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
    }

    public boolean canImport(TransferSupport info) {
      return info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
    }

    public int getSourceActions(JComponent c) {
      return MOVE; // TransferHandler.COPY_OR_MOVE;
    }

    public boolean importData(TransferSupport info) {
      if (!canImport(info)) {
        return false;
      }
      JList target = (JList) info.getComponent();
      JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
      DefaultListModel listModel = (DefaultListModel) target.getModel();
      int index = dl.getIndex();
      // boolean insert = dl.isInsert();
      int max = listModel.getSize();
      if (index < 0 || index > max) {
        index = max;
      }
      addIndex = index;

      try {
        Object[] values = (Object[]) info.getTransferable().getTransferData(
            localObjectFlavor);
        for (int i = 0; i < values.length; i++) {
          int idx = index++;
          listModel.add(idx, values[i]);
          target.addSelectionInterval(idx, idx);
        }
        addCount = (target == source) ? values.length : 0;
        return true;
      } catch (UnsupportedFlavorException ufe) {
        ufe.printStackTrace();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      return false;
    }

    protected void exportDone(JComponent c, Transferable data, int action) {
      cleanup(c, action == MOVE);
    }

    private void cleanup(JComponent c, boolean remove) {
      if (remove && indices != null) {
        JList source = (JList) c;
        DefaultListModel model = (DefaultListModel) source.getModel();
        // If we are moving items around in the same list, we
        // need to adjust the indices accordingly, since those
        // after the insertion point have moved.
        if (addCount > 0) {
          for (int i = 0; i < indices.length; i++) {
            if (indices[i] >= addIndex) {
              indices[i] += addCount;
            }
          }
        }
        for (int i = indices.length - 1; i >= 0; i--) {
          model.remove(indices[i]);
        }
      }
      indices = null;
      addCount = 0;
      addIndex = -1;
    }

    private int[] indices = null;
    private int addIndex = -1; // Location where items were added
    private int addCount = 0; // Number of items added.
  }

  public class okPressed implements ActionListener {
    private NewProjectFrame frame;
    private DefaultListModel model;

    public okPressed(NewProjectFrame frame, DefaultListModel model) {
      this.frame = frame;
      this.model = model;

    }

    public void actionPerformed(ActionEvent e) {
      List<File> filesSelected = new ArrayList<File>();
      for (int i = 0; i < model.getSize(); i++) {
        String fileName = (String) model.getElementAt(i);
        for (File f : frame.files) {
          if (f.getName().equals(fileName)) {
            filesSelected.add(f);
            break;
          }
        }
      }
      if (filesSelected.isEmpty()) {
        JOptionPane.showMessageDialog(this.frame, noFileSelectedText, warning,
            JOptionPane.WARNING_MESSAGE);
        return;
      }

      this.frame.showFrameDates(filesSelected);

    }
  }

  public class cancelPressed implements ActionListener {
    private NewProjectFrame frame;

    public cancelPressed(NewProjectFrame frame) {
      this.frame = frame;
    }

    public void actionPerformed(ActionEvent e) {
      this.frame.setVisible(false);
      this.frame.dispose();
      this.frame.setEnabled(true);
      System.gc();
    }
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public void showFrameDates(List<File> filesSelected) {
    this.setVisible(false);
    JFrame frameDates = new JFrame();
    frameDates.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setPreferredSize(new Dimension(700, 350));
    JPanel panelCenter = new JPanel(new VerticalLayout());
    List<JTextField> dateFields = new ArrayList<JTextField>();
    for (File f : filesSelected) {
      JPanel panelLocal = new JPanel();
      panelLocal.setPreferredSize(new Dimension(690, 40));
      JLabel fileName = new JLabel(f.getName());
      fileName.setPreferredSize(new Dimension(550, 30));
      panelLocal.add(fileName);
      JTextField fieldDate = new JTextField();
      fieldDate.setPreferredSize(new Dimension(120, 25));
      fieldDate.setDocument(new JTextFieldLimit(4));
      dateFields.add(fieldDate);
      panelLocal.add(fieldDate);
      panelCenter.add(panelLocal);
    }
    mainPanel.add(new JScrollPane(panelCenter), BorderLayout.CENTER);
    JPanel panelButton = new JPanel();
    JButton butOk = new JButton(ok);
    butOk.addActionListener(new butDatesOkPressed(this, frameDates, dateFields,
        filesSelected));
    panelButton.add(butOk);
    mainPanel.add(panelButton, BorderLayout.SOUTH);
    frameDates.setContentPane(mainPanel);
    frameDates.setResizable(false);
    frameDates.setTitle(frameDatesTitle);
    frameDates.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    frameDates.pack();
    frameDates.setLocationRelativeTo(null);
    frameDates.setVisible(true);
  }

  public class butDatesOkPressed implements ActionListener {
    private NewProjectFrame frame;
    private JFrame frame2;
    private List<JTextField> dateFields;
    private List<File> filesSelected;

    public butDatesOkPressed(NewProjectFrame frame, JFrame frame2,
        List<JTextField> dateFields, List<File> filesSelected) {
      this.frame = frame;
      this.frame2 = frame2;
      this.dateFields = dateFields;
      this.filesSelected = filesSelected;
    }

    public void actionPerformed(ActionEvent e) {
      Map<String, List<File>> mapFileDate = new HashMap<String, List<File>>();
      int cpt = 0;
      for (JTextField textField : this.dateFields) {
        String date = textField.getText();
        if (date.equals("")) {
          JOptionPane.showMessageDialog(this.frame, datesWarining, warning,
              JOptionPane.WARNING_MESSAGE);
          return;
        }
        if (!date.matches("[0-9]{4}")) {
          JOptionPane.showMessageDialog(this.frame, dateTooShort, warning,
              JOptionPane.WARNING_MESSAGE);
          return;
        }
        if (mapFileDate.containsKey(date)) {
          mapFileDate.get(date).add(filesSelected.get(cpt));
        } else {
          List<File> l = new ArrayList<File>();
          l.add(filesSelected.get(cpt));
          mapFileDate.put(date, l);
        }
        cpt++;
      }
      this.frame2.setVisible(false);
      this.frame2.dispose();
      this.frame2 = null;

      frame.controler.setFiles(mapFileDate);
      Thread t = new Thread(this.frame.controler);
      t.start();

    }
  }

  public class JTextFieldLimit extends PlainDocument {

    private static final long serialVersionUID = 1L;
    private int limit;

    JTextFieldLimit(int limit) {
      super();
      this.limit = limit;
    }

    public void insertString(int offset, String str, AttributeSet attr)
        throws BadLocationException {
      if (str == null)
        return;

      if ((getLength() + str.length()) <= limit) {
        super.insertString(offset, str, attr);
      }
    }
  }

  public String getMode() {
    return mode;
  }

}
