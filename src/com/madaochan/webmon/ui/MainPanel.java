package com.madaochan.webmon.ui;

import com.madaochan.webmon.controller.MainPanelController;

import javax.swing.*;

/**
 * @author MadaoChan
 * @since 2017/10/23
 */
public class MainPanel {
    private JPanel panelMain;
    private JButton buttonQuery;
    private JButton buttonClean;
    private JButton buttonPoll;
    private JTextPane textPaneMain;

    private MainPanelController controller;
    private boolean isPolling = false;

    private MainPanel() {

        buttonQuery.addActionListener(e -> getController().doQuery());

        buttonClean.addActionListener(e -> getController().doClean());

        buttonPoll.addActionListener(e -> {

            if (!isPolling) {
                getController().doPoll();
                isPolling = true;
                buttonPoll.setText("停止轮询");
            } else {
                getController().stopPoll();
                isPolling = false;
                buttonPoll.setText("开始轮询");
            }

        });

        getController().doQuery();

    }

    private MainPanelController getController() {
        if (controller == null) {
            controller = new MainPanelController(textPaneMain, buttonQuery);
        }
        return controller;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("网页状态查询器 v0.2");
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
