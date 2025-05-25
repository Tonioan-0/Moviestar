// ScrollView.java
package com.esa.moviestar.components;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.List;

public class ScrollView extends Control {
    // Constants
    private static final String DEFAULT_STYLE_CLASS = "scroll-view";
    private static final String DEFAULT_ARROW_ICON = "M.5564 30.712c.1632.1175.3434.176.5236.176.1989 0 .3976-.0714.5724-.2135l17.28-14.0399c.3158-.2566.5076-.7066.5076-1.1905 0-.4841-.1918-.9341-.44-1.1441L1.6524.2135c-.333-.2707-.7526-.2848-1.096-.0374C.213.4234 0 .8936 0 1.404 6 19.5 6 11.7 0 28.6v1.3C0 29.9944.213 30.4646.5564 30.712z";

    // Properties
    private final StringProperty title = new SimpleStringProperty(this, "title", "title" );
    private final StringProperty buttonText = new SimpleStringProperty(this, "buttonText", "Watch more" );
    private final StringProperty arrowIcon = new SimpleStringProperty(this, "arrowIcon", DEFAULT_ARROW_ICON );
    private final ObjectProperty<Paint> backgroundColor = new SimpleObjectProperty<>(this, "backgroundColor ", Color.GRAY );
    private final ObjectProperty<Paint> foreColor = new SimpleObjectProperty<>(this, "foreColor", Color.BLACK );
    private final ObjectProperty<Paint> edgeColor = new SimpleObjectProperty<>(this, "edgeColor" , null );
    private final DoubleProperty radius = new SimpleDoubleProperty(this, "radius", 0 );
    private final DoubleProperty spacing = new SimpleDoubleProperty(this, "spacing" , 4 );
    private final ObservableList<Node> items = FXCollections.observableArrayList( );
    // Constructor
    public ScrollView( ) {
        this("" );
    }

    public ScrollView(String title ) {
        this.title.set(title );
        initialize( );
    }
    public ScrollView(String title, Color backgroundColor , Color foregroundColor ) {
        this.foreColor.set(foregroundColor );
        this.backgroundColor.set(backgroundColor);
        this.title.set(title );
        initialize( );
    }
    public ScrollView(String title,Color backgroundColor ,Color foregroundColor,Color edgeColor ) {
        this.foreColor.set(foregroundColor );
        this.backgroundColor.set(backgroundColor);
        this.edgeColor.set(edgeColor );
        this.title.set(title);
        initialize();
    }
    public ScrollView(String title,Color backgroundColor , Color foregroundColor,Color edgeColor,Double border ) {
        this.foreColor.set( foregroundColor);
        this.backgroundColor.set(backgroundColor );
        this.edgeColor.set(edgeColor );
        this.title.set(title );
        initialize( );
        this.setClipRadius(border );
    }
    private void initialize() {
        getStyleClass( ).add( DEFAULT_STYLE_CLASS);
        new ScrollViewSkin( this);
    }

    @Override
    protected Skin<?> createDefaultSkin( ) {
        return  new ScrollViewSkin(this );
    }

    //Main method to change the c

    public void setContent(List<Node> objects ) {
        items.clear( );
        items.addAll(objects );
    }

    public void addItem(Node item ) {
        items.add(item );
    }

    public void removeItem(Node item ) {
        items.remove(item );
    }

    // Getters and Setters for properties
    public String getTitle( ) {
        return title.get( );
    }

    public StringProperty titleProperty( ) {
        return title;
    }

    public void setTitle(String title ) {
        this.title.set(title );
    }

    public String getButtonText( ) {
        return buttonText.get( );
    }

    public StringProperty buttonTextProperty( ) {
        return buttonText;
    }

    public void setButtonText(String text ) {
        this.buttonText.set(text );
    }

    public String getArrowIcon( ) {
        return arrowIcon.get( );
    }

    public StringProperty arrowIconProperty( ) {
        return arrowIcon;
    }

    public void setArrowButtonIcon(String svg ) {
        this.arrowIcon.set(svg );
    }

    public Paint getBackgroundColor( ) {
        return backgroundColor.get( );
    }

    public ObjectProperty<Paint> backgroundColorProperty( ) {
        return backgroundColor;
    }

    public void setBackgroundColor(Paint color ) {
        this.backgroundColor.set(color );
    }

    public Paint getForeColor( ) {
        return foreColor.get( );
    }

    public ObjectProperty<Paint> foreColorProperty( ) {
        return foreColor;
    }

    public void setForeColor(Color color ) {
        this.foreColor.set(color );
    }

    public double getSpacing( ) {
        return spacing.get( );
    }

    public DoubleProperty spacingProperty( ) {
        return spacing;
    }

    public void setSpacing(double value ) {
        this.spacing.set(value );
    }

    public ObservableList<Node> getItems( ) {
        return items;
    }

    public Paint getEdgeColor( ) {
       return this.edgeColor.get( );
    }
    public void setEdgeColor(Color c ) {
       edgeColor.set(c );
    }
    public ObjectProperty<Paint> edgeColorProperty() {
        return edgeColor;
    }

    public double getRadius( ) {
        return radius.get( );
    }
    public void setClipRadius(double n ) {
         radius.set(n );
    }
    public DoubleProperty radiusProperty() {
        return radius;
    }

}

