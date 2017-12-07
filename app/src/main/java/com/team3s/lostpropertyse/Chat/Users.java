package com.team3s.lostpropertyse.Chat;


public class Users {
    private String isim;
    private String sure;
    private String resimId;

    public Users(String isim, String sure, String resimId) {
        this.isim = isim;
        this.sure = sure;
        this.resimId = resimId;
    }

    public String getIsim() {
        return isim;
    }

    public void setIsim(String isim) {
        this.isim = isim;
    }

    public String getSure() {
        return sure;
    }

    public void setSure(String sure) {
        this.sure = sure;
    }

    public String getResimId() {
        return resimId;
    }

    public void setResimId(String resimId) {
        this.resimId = resimId;
    }
}