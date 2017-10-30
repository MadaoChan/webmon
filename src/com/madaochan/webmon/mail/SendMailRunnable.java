package com.madaochan.webmon.mail;

import com.madaochan.webmon.connection.Connection;

import javax.mail.event.TransportListener;

/**
 * 发邮件Runnable
 * @author MadaoChan
 * @since 2017/10/25.
 */
public class SendMailRunnable implements Runnable {

    private String content;
    private TransportListener listener;
    private boolean isAbnormal = false;

    public SendMailRunnable(String content, TransportListener listener) {
        this.content = content;
        this.listener = listener;
    }

    @Override
    public void run() {

        String processedContent = processContent(content);
        if (isAbnormal) {
            MailSender sender = new MailSender(processedContent, listener);
            sender.sendMail();
        }
    }

    /**
     * 处理内容
     * @param content 原始内容
     * @return 处理后的内容
     */
    private String processContent(String content) {

        // 分行符号\r\n和\n变为<br/>，制表符\t变为&emsp;
        String rawMessage = content.replace("\r\n", "<br/>");
        rawMessage = rawMessage.replace("\n", "<br/>");
        rawMessage = rawMessage.replace("\t", "&emsp;&emsp;");

        String[] lines = rawMessage.split("<br/>");
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            line += "<br/>";
            // 检查每行内容，包含“异常”则变红色
            if (line.contains(Connection.ABNORMAL_STATE)) {
                isAbnormal = true;
                line = "<font color=\"red\">" + line + "</font>";
            }
            result.append(line);
        }

        return result.toString();
    }


}
