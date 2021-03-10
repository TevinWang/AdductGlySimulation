package bpde;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import mdlaf.MaterialLookAndFeel;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Hello world!
 *
 */
public class App {
    private static JLabel statusLabel;
    private static JLabel statusLabel2;
    private static JPanel controlPanel;
    private static JPanel titlePanel;
    private static JFrame frame;
    private static JButton bPDEbutton;
    private static JButton glyButton;
    private static JButton adductButton;
    private static JButton returnButton;
    private static Container pane;
    private static JPanel sequencePanel;
    private static JLabel baseLabel;
    private static Cell lungCell;
    private static JPanel adductPanel;
    private static JPanel adductButtonPanel;
    private static JLabel adductLabel;
    private static JTextArea editTextArea;
    private static JLabel probabilityLabel;
    private static JPanel probabilityPanel;
    private static double previousProbability;
    private static JScrollPane adductScrollPane;
    private static JLabel probabilityLabel1;
    private static JLabel probabilityLabel2;
    private static JLabel probabilityLabel3;
    private static JLabel probabilityLabel4;
    private static JTextArea editTextArea1;
    private static JTextArea editTextArea2;
    private static JTextArea editTextArea3;
    private static JTextArea editTextArea4;
    private static JPanel glyPanel;
    private static JPanel glyButtonPanel;
    private static JButton glyButton1;
    private static JButton glyButton2;
    private static JButton glyButton3;
    private static JPanel leftPanel;
    private static double percent;
    private static JLabel glyPercent;

    public App() {
        runGUI();
    }

    public static void main(String[] args) throws Exception {
        App app = new App();

    }

    private void runGUI() {
        try {
            UIManager.setLookAndFeel(new MaterialLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        frame = new JFrame("BPDE Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1920, 1080);
        frame.setLayout(new GridLayout(2, 1));
        previousProbability = 0.6;

        titlePanel = new JPanel();
        titlePanel.setSize(1200, 200);
        titlePanel.setLayout(new GridLayout(2, 1));
        statusLabel2 = new JLabel("BPDE/GLYCOSYLASE SIMULATION\n", JLabel.CENTER);
        statusLabel2.setFont(new Font("SF Pro", Font.BOLD, 50));
        statusLabel = new JLabel("by Tevin Wang & Kingshuk Daschowdhury", JLabel.CENTER);
        statusLabel.setSize(1200, 100);
        statusLabel2.setSize(1200, 100);
        titlePanel.add(statusLabel2);
        titlePanel.add(statusLabel);

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        bPDEbutton = new JButton("Start Benzo-based PAH simulation");
        controlPanel.add(bPDEbutton); // Adds Button to content pane of frame

        bPDEbutton.setActionCommand("start");
        bPDEbutton.addActionListener(new ButtonClickListener());
        bPDEbutton.setVerticalAlignment(JButton.CENTER);

        glyButton = new JButton("Start Glycosylase simulation");
        glyButton.setActionCommand("gly");
        glyButton.addActionListener(new ButtonClickListener());
        glyButton.setVerticalAlignment(JButton.CENTER);
        controlPanel.add(glyButton);

        frame.add(titlePanel);
        frame.add(controlPanel);

        frame.setVisible(true);
    }

    private class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            if (command.equals("start")) {
                // statusLabel.setText("Simulation initiated...");
                // button.setVisible(false);
                // controlPanel.revalidate();
                // controlPanel.repaint();
                lungCell = new Cell();
                animateSequence(lungCell, false);
            } else if (command.equals("adduct")) {
                previousProbability = Double.parseDouble(editTextArea.getText());
                lungCell.rankAndChangeAdducts(lungCell.oldSequence, lungCell.adducts,
                        Double.parseDouble(editTextArea.getText()), false);
                frame.remove(adductPanel);
                frame.remove(sequencePanel);
                animateSequence(lungCell, true);
            } else if (command.equals("return")) {
                frame.remove(adductPanel);
                frame.remove(sequencePanel);
                frame.setLayout(new GridLayout(2, 1));
                frame.add(titlePanel);
                frame.add(controlPanel);
                frame.revalidate();
                frame.repaint();

            } else if (command.equals("gly")) {
                lungCell = new Cell();
                frame.remove(controlPanel);
                frame.remove(titlePanel);
                animateGlycosylases(lungCell, false, false);


            } else if (command.equals("glyAdduct")) {
                lungCell.rankAndChangeAdducts(lungCell.oldSequence, lungCell.adducts, 0.3, true);
                frame.remove(leftPanel);
                frame.remove(sequencePanel);
                animateGlycosylases(lungCell, true, false);


            }else if (command.equals("glyRun")) {
                lungCell.glycosylases.get(0).setExpression(Double.parseDouble(editTextArea1.getText()));
                lungCell.glycosylases.get(1).setExpression(Double.parseDouble(editTextArea2.getText()));
                lungCell.glycosylases.get(2).setExpression(Double.parseDouble(editTextArea3.getText()));
                lungCell.glycosylases.get(3).setExpression(Double.parseDouble(editTextArea3.getText()));
                lungCell.createOxidizedBases(lungCell.oldSequence, lungCell.newSequence, lungCell.glycosylases);
                frame.remove(leftPanel);
                frame.remove(sequencePanel);
                percent = lungCell.getPercentSimilarity(lungCell.oldSequence, lungCell.finalSequence);
                animateGlycosylases(lungCell, true, true);



            }else if (command.equals("glyReturn")) {
                frame.remove(leftPanel);
                frame.remove(sequencePanel);
                frame.setLayout(new GridLayout(2, 1));
                frame.add(titlePanel);
                frame.add(controlPanel);
                frame.revalidate();
                frame.repaint();
                

            }else {
                statusLabel.setText("Cancel Button clicked.");
            }
        }

        public void animateSequence(Cell c, boolean afterAdduct) {
            frame.remove(controlPanel);
            frame.remove(titlePanel);
            frame.setLayout(new GridLayout(1, 1));
            sequencePanel = new JPanel();
            sequencePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
            adductPanel = new JPanel();
            adductPanel.setLayout(new GridLayout(3, 1));

            adductButtonPanel = new JPanel();
            adductButtonPanel.setLayout(new GridLayout(1, 2));

            probabilityPanel = new JPanel();
            probabilityPanel.setLayout(new GridLayout(2, 1));

            probabilityLabel = new JLabel("Choose the BaP dosage value:", JLabel.CENTER);
            editTextArea = new JTextArea("" + previousProbability);

            probabilityPanel.add(probabilityLabel);
            probabilityPanel.add(editTextArea);

            adductButton = new JButton("Start adduct transversion");
            adductButton.setActionCommand("adduct");
            adductButton.addActionListener(new ButtonClickListener());
            adductButton.setSize(100, 50);
            adductButton.setVerticalAlignment(JButton.CENTER);
            adductButtonPanel.add(adductButton);

            returnButton = new JButton("Return to main menu");
            returnButton.addActionListener(new ButtonClickListener());
            returnButton.setActionCommand("return");
            returnButton.setSize(100, 50);
            returnButton.setVerticalAlignment(JButton.CENTER);
            adductButtonPanel.add(returnButton);
            if (afterAdduct) {
                String label = "<html> Transversions with " + previousProbability * 100 + "% probability: <br>";
                Collections.sort(c.adducts);
                for (int i = 0; i < c.adducts.size(); i++) {
                    label += c.adducts.get(i).name + ": <b>" + c.adducts.get(i).totalChanged
                            + " changed bases </b> <br>" + "resulted from " + c.adducts.get(i).carcinogen + " <br>";
                }
                label += "</html>";
                adductLabel = new JLabel(label, JLabel.CENTER);
                adductScrollPane = new JScrollPane(adductLabel);
                adductPanel.add(adductScrollPane);
            }
            adductPanel.add(probabilityPanel);
            adductPanel.add(adductButtonPanel);
            baseLabel = new JLabel("<html> TP53 GENE SEQUENCE (FIRST 2000 BASES):    <br></html>", JLabel.CENTER);
            baseLabel.setSize(1920, 100);
            sequencePanel.add(baseLabel);
            for (int i = 10000; i < 12000; i++) {

                if (afterAdduct) {
                    baseLabel = new JLabel("" + c.newSequence.get(i), JLabel.CENTER);
                    if (c.newSequence.get(i) != c.oldSequence.get(i))
                        baseLabel.setForeground(Color.RED);
                    adductButton.setText("Restart adduct transversion");

                } else {
                    baseLabel = new JLabel("" + c.oldSequence.get(i), JLabel.CENTER);

                }
                baseLabel.setFont(new Font("SF Pro", Font.BOLD, 20));
                sequencePanel.add(baseLabel);
            }
            // frame.add(adductButton);
            frame.add(adductPanel);
            frame.add(sequencePanel);
            frame.revalidate();
            frame.repaint();

        }

        private void animateGlycosylases(Cell c, boolean afterAdduct, boolean afterGly) {
            frame.setLayout(new GridLayout(1,2));
            leftPanel = new JPanel();
            leftPanel.setLayout(new GridLayout(3, 1));
            glyPanel = new JPanel();
            glyPanel.setLayout(new GridLayout(4, 2));
            sequencePanel = new JPanel();
            sequencePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
            
            
            probabilityLabel1 = new JLabel("Input expression for " + c.glycosylases.get(0).name + " (recognizes " + c.glycosylases.get(0).substrates, JLabel.CENTER);
            editTextArea1 = new JTextArea("1.0");
            probabilityLabel2 = new JLabel("Input expression for " + c.glycosylases.get(1).name + " (recognizes " + c.glycosylases.get(1).substrates,JLabel.CENTER);
            editTextArea2 = new JTextArea("1.0");
            probabilityLabel3 = new JLabel("Input expression for " + c.glycosylases.get(2).name + " (recognizes " + c.glycosylases.get(2).substrates, JLabel.CENTER);
            editTextArea3 = new JTextArea("1.0");
            probabilityLabel4 = new JLabel("Input expression for " + c.glycosylases.get(3).name + " (recognizes " + c.glycosylases.get(3).substrates, JLabel.CENTER);
            editTextArea4 = new JTextArea("1.0");
            glyPanel.add(probabilityLabel1);
            glyPanel.add(editTextArea1);
            glyPanel.add(probabilityLabel2);
            glyPanel.add(editTextArea2);
            glyPanel.add(probabilityLabel3);
            glyPanel.add(editTextArea3);
            glyPanel.add(probabilityLabel4);
            glyPanel.add(editTextArea4);

            glyButtonPanel = new JPanel();
            glyButtonPanel.setLayout(new GridLayout(3,1));

            glyButton1 = new JButton("BaP");
            glyButton1.setActionCommand("glyAdduct");
            glyButton1.addActionListener(new ButtonClickListener());
            glyButton1.setSize(100, 50);

            glyButton2 = new JButton("Find percent similarity");
            glyButton2.setActionCommand("glyRun");
            glyButton2.addActionListener(new ButtonClickListener());
            glyButton2.setSize(100, 50);

            glyButton3 = new JButton("Return to main menu");
            glyButton3.setActionCommand("glyReturn");
            glyButton3.addActionListener(new ButtonClickListener());
            glyButton3.setSize(100, 50);

            glyButtonPanel.add(glyButton1);
            glyButtonPanel.add(glyButton2);
            glyButtonPanel.add(glyButton3);
            if (afterGly) {
                glyPercent = new JLabel("<html>Percent similarity: <b>" + (percent *100) + "%</b></html>");
                leftPanel.add(glyPercent);
            }
        
            leftPanel.add(glyPanel);
            leftPanel.add(glyButtonPanel);
            

            
            // for (Glycosylase g : lungCell.glycosylases) {

            //     probabilityLabel = new JLabel("Input expression", JLabel.CENTER);
            //     editTextArea = new JTextArea("" + previousProbability);
            //     glyPanel.add(probabilityPanel);
            //     glyPanel.add(editTextArea);
            // }
            
            baseLabel = new JLabel("<html> TP53 GENE SEQUENCE (FIRST 2000 BASES):    <br></html>", JLabel.CENTER);
            baseLabel.setSize(1920, 100);
            sequencePanel.add(baseLabel);
            for (int i = 10000; i < 12000; i++) {

                if (afterGly) {
                    baseLabel = new JLabel("" + c.finalSequence.get(i), JLabel.CENTER);
                    if (c.newSequence.get(i) != c.oldSequence.get(i))
                        baseLabel.setForeground(Color.RED);
                }
                else if (afterAdduct) {
                    baseLabel = new JLabel("" + c.newSequence.get(i), JLabel.CENTER);
                    if (c.newSequence.get(i) != c.oldSequence.get(i))
                        baseLabel.setForeground(Color.RED);
                    // adductButton.setText("Restart adduct transversion");

                } else {
                    baseLabel = new JLabel("" + c.oldSequence.get(i), JLabel.CENTER);

                }
                baseLabel.setFont(new Font("SF Pro", Font.BOLD, 20));
                sequencePanel.add(baseLabel);
            }
            frame.add(leftPanel);
            frame.add(sequencePanel);
            frame.revalidate();
            frame.repaint();
            }
        }


    }



