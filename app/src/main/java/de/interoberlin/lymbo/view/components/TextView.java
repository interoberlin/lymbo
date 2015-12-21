package de.interoberlin.lymbo.view.components;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

import de.interoberlin.lymbo.R;
import de.interoberlin.lymbo.core.model.v1.impl.EGravity;
import de.interoberlin.lymbo.core.model.v1.impl.Text;
import de.interoberlin.lymbo.util.Configuration;
import de.interoberlin.lymbo.util.TranslationUtil;

public class TextView extends LinearLayout {
    // --------------------
    // Constructors
    // --------------------

    public TextView(Context context) {
        super(context);
    }

    public TextView(Context context, Text t) {
        super(context);
        inflate(context, R.layout.component_text, this);

        android.widget.TextView tvTitle = (android.widget.TextView) findViewById(R.id.tvTitle);

        // Set value
        if (TranslationUtil.contains(t.getTranslations(), Configuration.getLanguage(context)))
            tvTitle.setText(TranslationUtil.get(t.getTranslations(), Configuration.getLanguage(context)));
        else
            tvTitle.setText(t.getValue());

        // Attribute : lines
        if (t.getLines() != 0)
            tvTitle.setLines(t.getLines());

        // Attribute : gravity
        if (t.getGravity() == EGravity.START)
            setGravity(Gravity.START);
        else if (t.getGravity() == EGravity.CENTER)
            setGravity(Gravity.CENTER);
        else if (t.getGravity() == EGravity.END)
            setGravity(Gravity.END);
    }
}
