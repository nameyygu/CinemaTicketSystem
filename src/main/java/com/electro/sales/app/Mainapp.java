package com.electro.sales.app;

import com.electro.sales.util.InitDB;
import com.electro.sales.view.MainFrame;

public class Mainapp {
    public static void main(String[] args) {
        new MainFrame().setVisible(true);
        InitDB.init();
    }

}
