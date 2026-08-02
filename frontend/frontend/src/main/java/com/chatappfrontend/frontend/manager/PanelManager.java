package com.chatappfrontend.frontend.manager;

import javafx.scene.layout.VBox;

import java.util.List;

public class PanelManager {
    private final List<VBox> panels;

    public PanelManager(List<VBox> panels) {
        this.panels = panels;
    }

    public void showPanel(VBox panelToShow){
        for(VBox panel : panels){
            panel.setVisible(false);
            panel.setManaged(false);
        }

        panelToShow.setVisible(true);
        panelToShow.setManaged(true);
    }
}