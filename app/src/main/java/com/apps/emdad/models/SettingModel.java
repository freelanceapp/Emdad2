package com.apps.emdad.models;

import java.io.Serializable;

public class SettingModel implements Serializable {
    private Setting settings;

    public Setting getSettings() {
        return settings;
    }

    public static class Setting implements Serializable{
        private String facebook;
        private String twitter;
        private String instagram;
        private String telegram;
        private String whatsapp;

        public String getFacebook() {
            return facebook;
        }

        public String getTwitter() {
            return twitter;
        }

        public String getInstagram() {
            return instagram;
        }

        public String getTelegram() {
            return telegram;
        }

        public String getWhatsapp() {
            return whatsapp;
        }
    }
}
