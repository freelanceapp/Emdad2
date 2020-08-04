package com.apps.emdad.interfaces;


public interface Listeners {

    interface BackListener
    {
        void back();
    }
    interface LoginListener{
        void validate();
        void showCountryDialog();
    }
}
