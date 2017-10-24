package com.madaochan.webmon.ui;

import com.madaochan.webmon.controller.MainPanelController;
import com.madaochan.webmon.time.TimeUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author MadaoChan
 * @since 2017/10/23
 */
public class MainPanel {
    private JPanel panelMain;
    private JScrollPane scrollPane;
    private JTextArea textAreaInfo;
    private JButton buttonQuery;
    private JButton buttonClean;
    private JButton buttonPoll;

    private MainPanelController controller;
    private boolean isPolling = false;
    private ScheduledExecutorService scheduledExecutorService;

    public MainPanel() {

        buttonQuery.addActionListener(e -> {
            getController().doQuery();
        });

        buttonClean.addActionListener(e -> {
            getController().doClean();
        });

        buttonPoll.addActionListener(e -> {

            if (!isPolling) {
                scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(TimeUtils.getCurrentTime() + "轮询开始");
                        getController().doQuery();
                    }
                }, 5, 5, TimeUnit.SECONDS);
                isPolling = true;
                buttonPoll.setText("停止轮询");
            } else {
                if (scheduledExecutorService != null) {
                    System.out.println(TimeUtils.getCurrentTime() + "轮询停止");
                    scheduledExecutorService.shutdown();
                    scheduledExecutorService = null;
                }
                isPolling = false;
                buttonPoll.setText("开始轮询");
            }

        });

        getController().doQuery();

    }

    private MainPanelController getController() {
        if (controller == null) {
            controller = new MainPanelController(textAreaInfo, buttonQuery);
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
