package de.interoberlin.lymbo.util;


import java.util.ArrayList;
import java.util.List;

import de.interoberlin.lymbo.core.model.v1.impl.Tag;

public class ModelUtil {

    // --------------------
    // Methods
    // --------------------

    public static List<Tag> copy(List<Tag> tags) {
        List<Tag> copy = new ArrayList<>();

        for (Tag t : tags) {
            copy.add(t.clone());
        }

        return copy;
    }
}