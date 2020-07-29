package com.apps.emdad.interfaces;


public interface Listeners {

    interface LoginListener {
        void checkDataLogin(String phone_code, String phone);
    }
    interface TransFerListener {
        void checkData(String amount);
    }
    interface SkipListener
    {
        void skip();
    }
    interface CreateAccountListener
    {
        void createNewAccount();
    }

    interface ShowCountryDialogListener
    {
        void showDialog();
    }

    interface SignUpListener
    {
        void checkDataValid();

    }
    interface EditprofileListener
    {
        void Editprofile(String name);
        void Editprofile(String englishname, String arabicname);


    }

    interface BackListener
    {
        void back();
    }


    interface SettingActions
    {
        void contactUs();
        void terms();
        void aboutApp();
        void logout();
        void share();
        void rateApp();
        void arLang();
        void enLang();
        void whatsapp();
        void twitter();
        void instagram();

    }



    interface ForgetListner
    {
        void forget();
    }
    interface PasswordListner {
        void checkDatapass(String pass);
    }
    interface ForgetpasswordListner {
        void checkDataForget(String phone_code, String phone);
    }
}
