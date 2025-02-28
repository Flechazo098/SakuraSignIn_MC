package com.flechazo.sakura.screen.component;


import java.util.ArrayList;
import java.util.Collections;

public class TextList extends ArrayList < IText > {
    public TextList() {
    }

    public TextList(IText... elements) {
        super(elements.length);
        Collections.addAll(this, elements);
    }

    public TextList put(IText... elements) {
        Collections.addAll(this, elements);
        return this;
    }

    public IText get(int index) {
        if (index >= this.size()) {
            return super.get(index % this.size());
        } else {
            return super.get(index);
        }
    }
}
