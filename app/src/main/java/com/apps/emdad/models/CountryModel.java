package com.apps.emdad.models;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.Locale;

public class CountryModel implements Serializable {

    private String code;
    private String name;
    private String dialCode;
    private int flag;
    private String currency;

    CountryModel() {
    }

    public CountryModel(String code, String name, String dialCode, int flag, String currency) {
        this.code = code;
        this.name = name;
        this.dialCode = dialCode;
        this.flag = flag;
        this.currency = currency;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
        if (TextUtils.isEmpty(this.name)) {
            this.name = (new Locale("", code)).getDisplayName();
        }

    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDialCode() {
        return this.dialCode;
    }

    public void setDialCode(String dialCode) {
        this.dialCode = dialCode;
    }

    public int getFlag() {
        return this.flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }



}
