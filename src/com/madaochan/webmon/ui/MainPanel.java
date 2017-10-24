package com.madaochan.webmon.ui;

import com.madaochan.webmon.controller.MainPanelController;

import javax.swing.*;

/**
 * @author MadaoChan
 * @since 2017/10/23
 */
public class MainPanel {
    private JPanel panelMain;
    private JTextArea textAreaInfo;
    private JButton buttonQuery;
    private JButton buttonClean;
    private JScrollPane scrollPane;

    private MainPanelController controller;

    public MainPanel() {

        buttonQuery.addActionListener(e -> {
            getController().doQuery();
        });
        buttonClean.addActionListener(e -> {
            getController().doClean();
        });
    }

    private MainPanelController getController() {
        if (controller == null) {
            controller = new MainPanelController(textAreaInfo, buttonQuery);
        }
        return controller;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainPanel");
        frame.setContentPane(new MainPanel().panelMain);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (controller != null) {
            controller.destroy();
        }
    }
}
