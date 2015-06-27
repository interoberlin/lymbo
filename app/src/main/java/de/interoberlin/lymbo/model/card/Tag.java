package de.interoberlin.lymbo.model.card;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.interoberlin.lymbo.R;
import de.interoberlin.lymbo.model.Displayable;
import de.interoberlin.lymbo.util.ColorUtil;

public class Tag implements Displayable {
    private boolean checked = true;
    private String name = "";

    // --------------------
    // Constructor
    // --------------------

    public Tag(String name) {
        this.name = name;
    }

    // --------------------
    // Methods
    // --------------------

    @Override
    public View getView(Context c, Activity a, ViewGroup parent) {
        LayoutInflater li = LayoutInflater.from(c);
        CardView cvTag = (CardView) li.inflate(R.layout.component_tag, parent, false);

        TextView tvText = (TextView) cvTag.findViewById(R.id.tvText);
        cvTag.setCardBackgroundColor(ColorUtil.getColorByString(c, name));

        // Attribute : value
        tvText.setTextColor(c.getResources().getColor(R.color.white));
        tvText.setText(name);

        return cvTag;
    }

    @Override
    public View getEditableView(Context c, Activity a, ViewGroup parent) {
        return new View(c);
    }

    public Tag clone() {
        Tag t = new Tag(this.getName());
        t.setChecked(false);
        return t;
    }

    // --------------------
    // Getters / Setters
    // --------------------

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
