package com.madaochan.webmon.ui;

import com.madaochan.webmon.connection.Connection;
import com.madaochan.webmon.debug.DebugUtils;

import javax.swing.*;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author MadaoChan
 * @since 2017/10/23
 */
public class MainPanel {
    private JPanel panelMain;
    private JTextArea textAreaInfo;
    private JButton buttonQuery;

    public MainPanel() {

        buttonQuery.addActionListener(e -> {
            buttonQuery.setEnabled(false);
            textAreaInfo.insert("----------",0);
            List<String> urlList = DebugUtils.buildTestUrlList();
            for (String url : urlList) {
                ConnectionRunnable runnable = new ConnectionRunnable(textAreaInfo, url);
                Thread thread = new Thread(runnable);
                thread.start();
            }
            buttonQuery.setEnabled(true);
        });
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
        System.out.println("finalize");
        super.finalize();
    }

    private static class ConnectionRunnable implements Runnable {

        private WeakReference<JTextArea> textAreaRef;
        private String url;
        private boolean isStop = false;

        public ConnectionRunnable(JTextArea textArea, String url) {
            this.textAreaRef = new WeakReference<>(textArea);
            this.url = url;
        }

        public void setStop(boolean stop) {
            isStop = stop;
        }

        @Override
        public void run() {
            String response = new Connection().getResponseCode(url);
            JTextArea textArea = textAreaRef.get();
            if (!isStop && textArea != null) {
                textArea.insert(response, 0);
                textArea.invalidate();
            }
        }
    }

}
