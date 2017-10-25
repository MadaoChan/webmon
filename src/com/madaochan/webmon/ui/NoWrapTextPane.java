package com.madaochan.webmon.ui;

import javax.swing.*;

/**
 * 不会自动换行的JTextPane
 * @author MadaoChan
 * @since 2017/10/25
 */
public class NoWrapTextPane extends JTextPane {
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getUI().getPreferredSize(this).width
                <= getParent().getSize().width;
    }
}
