package de.interoberlin.lymbo.model.card.components;

public class Answer {
    private String value = "";
    private boolean correct = false;
    private boolean selected = false;

    // --------------------
    // Constructors
    // --------------------

    public Answer() {
    }

    // --------------------
    // Methods
    // --------------------

    // --------------------
    // Getters / Setters
    // --------------------

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}