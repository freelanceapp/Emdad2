package com.apps.emdad.interfaces;


public interface Listeners {

    interface SignUpListener{

        void openSheet();
        void closeSheet();
        void checkDataValid();
        void checkReadPermission();
        void checkCameraPermission();
        void male();
        void female();
    }

    interface BackListener
    {
        void back();
    }
    interface LoginListener{
        void validate();
        void showCountryDialog();
    }
}
