package com.esa.moviestar.components;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The popupMenu provides a customizable popup menu component.
 * It supports single or multiple columns of menu items.
 * The menu can be dynamically populated with any  Node, and includes
 * options for adding separators and controlling the display position relative to an anchor node.
 * This component is styled to integrate with the application (it use general.css)
 * Sources:
 * <a href="https://docs.oracle.com/javase/8/javafx/api/javafx/stage/Popup.html">...</a>
 */
public class PopupMenu{

    private final Popup popup;
    private final HBox columnsContainer;
    private final List<VBox> columns;
    private double columnWidth ;
    private int   columnsCount = 1;
    private boolean positionShowLeft = true;

    public PopupMenu(){

        this(255,1);
    }
    public PopupMenu( double columnWidth){
        this(columnWidth,1);
    }

    public PopupMenu(double columnWidth , int columnsCount){
        this.columnWidth = columnWidth;
        this.popup = new Popup();
        this.columnsCount = Math.max(1, columnsCount);
        this.columns = new ArrayList<>( columnsCount);
        this.columnsContainer = new HBox(4);

        for (int i = 0; i <   columnsCount; i++){
            VBox column = createColumn();
            columns.add(column);
            columnsContainer.getChildren().add(column);
        }

        columnsContainer.setPadding(new Insets(8));
        columnsContainer.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/esa/moviestar/styles/general.css")).toExternalForm());

        DropShadow shadow = new DropShadow(){{
            setRadius(10.0);
            setOffsetY(3.0);
            setColor(Color.rgb(0, 0, 0, 0.2));
        }};
        columnsContainer.getStyleClass().addAll("surface-opaque", "medium-item");
        columnsContainer.setEffect(shadow);

        StackPane container = new StackPane(columnsContainer);
        popup.getContent().add(container);
        popup.setAutoHide(true);
    }

    private VBox createColumn(){
        return new VBox(4)
       {{
            setPadding(new Insets(0));
            setMaxWidth(columnWidth);
            setMinWidth(columnWidth);
        }};
    }

    /**
     * Adds an item to the first column
     * @param n The node to add
     */
    public void addItem(Node n){
        addItem(0, n);
    }

    /**
     * Adds an item to the specified column
     * @param columnIndex The index of the column (0-based)
     * @param n The node to add
     */
    public void addItem(int columnIndex, Node n){
        if(columnIndex < 0 || columnIndex >=columns.size())
            return;
        columns.get(columnIndex ).getChildren().add(n );
    }

    /**
     * Adds a separator to the first column
     */
    public void addSeparator(){
        addSeparator(0);
    }

    /**
     * Adds a separator to the specified column
     * @param columnIndex The index of the column (0-based)
     */
    public void addSeparator(int columnIndex){
        if( columnIndex < 0 || columnIndex >=columns.size())
            return;
        columns.get(columnIndex ).getChildren().add(new VBox(){{ setHeight(4); }});
    }


    public int addColumn(){
        VBox newColumn = createColumn();
        columns.add(newColumn );
        columnsContainer.getChildren().add( newColumn);
        columnsCount++ ;
        return   columnsCount - 1;
    }


    public int getNumberOfColumns(){
        return   columnsCount;
    }

    /**
     * Shows the popup menu anchored to the specified node
     * @param anchor The node to anchor the popup to
     */
    public void show(Node anchor){
        if (anchor == null || anchor.getScene() == null)
            return;

        double anchorX = positionShowLeft ? anchor.localToScreen(-columnWidth*  columnsCount+anchor.getLayoutBounds().getWidth(), 0).getX():anchor.localToScreen(anchor.getLayoutBounds().getWidth(), 0).getX();
        double anchorY = anchor.localToScreen(0, 8).getY();
        double posY = anchorY + anchor.getBoundsInLocal().getHeight();
        popup.show(anchor.getScene().getWindow(), anchorX, posY);
    }

    public void close(){
        this.popup.hide();
    }

    public void dispose(){
        close();
        if (columns != null){
            for (VBox column : columns){
                if (column != null){
                    column.getChildren().clear();
                }
            }
            columns.clear();
        }
        if (columnsContainer != null){
            columnsContainer.getChildren().clear();
            columnsContainer.setEffect(null);
            if (columnsContainer.getStylesheets() != null){
                columnsContainer.getStylesheets().clear();
            }
        }
        if (popup != null && popup.getContent() != null){
            popup.getContent().clear();
        }
    }
}