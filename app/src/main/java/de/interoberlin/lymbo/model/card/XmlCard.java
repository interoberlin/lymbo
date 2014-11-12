package de.interoberlin.lymbo.model.card;

public class XmlCard {
    private int id;
    private String title = "";
    private String hint = "";
    private String color = "#FFFFFF";
    private XmlSide front;
    private XmlSide back;

    // -------------------------
    // Constructors
    // -------------------------

    public XmlCard() {

    }

    public XmlCard(int id, String title, String color, XmlSide front) {
        this.id = id;
        this.title = title;
        this.color = color;
        this.front = front;
    }

    public XmlCard(int id, String title, String color, XmlSide front, XmlSide back) {
        this.id = id;
        this.title = title;
        this.color = color;
        this.front = front;
        this.back = back;
    }

    // -------------------------
    // Getters / Setters
    // -------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public XmlSide getFront() {
        return front;
    }

    public void setFront(XmlSide front) {
        this.front = front;
    }

    public XmlSide getBack() {
        return back;
    }

    public void setBack(XmlSide back) {
        this.back = back;
    }
}
