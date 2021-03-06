package de.interoberlin.lymbo.view.components;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.interoberlin.lymbo.R;
import de.interoberlin.lymbo.core.model.v1.impl.components.EGravity;
import de.interoberlin.lymbo.core.model.v1.impl.components.Title;
import de.interoberlin.lymbo.util.Configuration;
import de.interoberlin.lymbo.util.TranslationUtil;

public class TitleView extends LinearLayout {

    // --------------------
    // Constructors
    // --------------------

    public TitleView(Context context) {
        super(context);
    }

    public TitleView(Context context, Title t) {
        super(context);
        inflate(context, R.layout.component_title, this);

        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);

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
