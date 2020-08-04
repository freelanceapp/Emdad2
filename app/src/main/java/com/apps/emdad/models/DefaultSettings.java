package com.apps.emdad.models;

import java.io.Serializable;

public class DefaultSettings implements Serializable {
    private boolean isLanguageSelected = false;
    private boolean showIntroSlider = true;

    public DefaultSettings() {
    }

    public boolean isLanguageSelected() {
        return isLanguageSelected;
    }

    public void setLanguageSelected(boolean languageSelected) {
        isLanguageSelected = languageSelected;
    }

    public boolean isShowIntroSlider() {
        return showIntroSlider;
    }

    public void setShowIntroSlider(boolean showIntroSlider) {
        this.showIntroSlider = showIntroSlider;
    }
}
