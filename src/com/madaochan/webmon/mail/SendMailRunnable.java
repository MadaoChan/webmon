package com.madaochan.webmon.mail;

import javax.mail.event.TransportListener;

/**
 * Created by MadaoChan on 2017/10/25.
 */
public class SendMailRunnable implements Runnable {

    private String content;
    private TransportListener listener;

    public SendMailRunnable(String content, TransportListener listener) {
        this.content = content;
        this.listener = listener;
    }

    @Override
    public void run() {

        String message = content.replace("\r\n", "<br/>");

        MailSender sender = new MailSender(content, listener);
        sender.sendMail();
    }


}
