package com.madaochan.webmon.mail;

import com.madaochan.webmon.constants.Constants;
import com.madaochan.webmon.file.FileUtils;
import com.madaochan.webmon.time.TimeUtils;
import com.sun.mail.util.MailSSLSocketFactory;

import javax.mail.*;
import javax.mail.event.TransportListener;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 邮件发送器
 * @author MadaoChan
 * @since 2017/10/25
 */
public class MailSender {

    private String mailSubject;
    private String mailContent;
    private String username;
    private String password;
    private Address[] to;
    private boolean isSSL = true;
    private String smtpHost;
    private String port;

    private TransportListener listener;

    private final String MAIL_REGEX = "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
    private final String DEBUG = "true";

    public MailSender(String mailContent, TransportListener listener) {
        this.mailContent = mailContent;
        this.listener = listener;
        initConfig();
    }

    /**
     * 初始化发邮件配置
     */
    private void initConfig() {
        FileUtils fileUtils = new FileUtils();
        Properties prop = fileUtils.readProperties(fileUtils.getRootDir() + Constants.MAIL_CONFIG_FILENAME);
        username = prop.getProperty("username");
        password = prop.getProperty("password");
        smtpHost = prop.getProperty("smtp_host");
        port = prop.getProperty("smtp_port");
        mailSubject = prop.getProperty("subject") + TimeUtils.getCurrentTime();
        isSSL = prop.getProperty("use_ssl","0").equals("1");
        to = initReceivers(prop.getProperty("to"));
    }

    /**
     * 初始化收件人
     * @param receiversRawText 收件人原始字符串（用,分隔）
     * @return 地址数组
     */
    private Address[] initReceivers(String receiversRawText) {
        if (receiversRawText == null || receiversRawText.trim().length() == 0) {
            return null;
        }

        String[] rawReceiver = receiversRawText.split(Constants.MAIL_ADDR_SPLITTER);
        ArrayList<String> receiverList = new ArrayList<>();
        Pattern pattern = Pattern.compile(MAIL_REGEX);

        for (String receiver : rawReceiver) {
            if (pattern.matcher(receiver).matches()) {
                receiverList.add(receiver);
            }
        }

        Address[] addresses = new Address[receiverList.size()];
        for (int i=0; i<receiverList.size(); i++) {
            try {
                addresses[i] = new InternetAddress(receiverList.get(i));
            } catch (AddressException e) {
                e.printStackTrace();
            }
        }
        return addresses;
    }

    /**
     * 发送邮件
     */
    public void sendMail() {

        Properties props = new Properties();
        // 开启debug调试
        props.setProperty("mail.debug", String.valueOf(Constants.DEBUG));
        // 发送服务器需要身份验证
        props.setProperty("mail.smtp.auth", "true");
        // 发送邮件协议名称
        props.setProperty("mail.transport.protocol", "smtp");
        // 设置邮件服务器主机名
        props.setProperty("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", port);
        props.put("mail.smtps.timeout", 15000);
        props.put("mail.smtps.connectiontimeout", 15000);

        if (isSSL) {
            String SSL_FACTORY="javax.net.ssl.SSLSocketFactory";
            props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put("mail.smtp.socketFactory.port", port);
            try {
                MailSSLSocketFactory sf = new MailSSLSocketFactory();
                sf.setTrustAllHosts(true);
                props.put("mail.smtp.ssl.socketFactory", sf);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }

        // 获取默认的 Session 对象。
        Session session = Session.getDefaultInstance(props);

        try{
            // 创建默认的 MimeMessage 对象。
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setSubject(mailSubject);
            message.setContent(mailContent, "text/html;charset=utf-8" );

            // 发邮件
            Transport transport = session.getTransport();
            transport.addTransportListener(listener);
            transport.connect(username, password);
            transport.sendMessage(message, to);

        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
