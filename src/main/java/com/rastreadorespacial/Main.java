package com.rastreadorespacial;

import com.rastreadorespacial.config.AppConfig;
import com.rastreadorespacial.menu.MainMenuScreen;
import com.rastreadorespacial.menu.MenuContext;
import com.rastreadorespacial.menu.MenuNavigator;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        AppConfig.initDirectories();
        MenuContext context = new MenuContext();
        MenuNavigator navigator = new MenuNavigator(new Scanner(System.in), System.out);
        navigator.iniciar(new MainMenuScreen(navigator, context));
        navigator.loop();
    }
}
