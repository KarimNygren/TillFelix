package com.passwordmanager.api;

public class Password {
    private String key;
    private String value;
    private String date;

    private String id;

    public Password(String key, String value, String date, String id) {
        this.key = key;
        this.value = value;
        this.date = date;
        this.id = id;
    }

    public Password(){

    }

    public void setKey(String key){
        this.key = key;
    }

    public void setValue(String value){
        this.value = value;
    }

    public void setDate(String date){this.date = date;}

    public void setId(String id){this.id = id; }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getDate(){return date; }

    public String getId(){return id; }


    @Override
    public String toString() {
        return key + " - " + value;
    }
}
