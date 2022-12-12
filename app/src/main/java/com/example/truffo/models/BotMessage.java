package com.example.truffo.models;

public class BotMessage {
    private String cnt;

    public String getCnt()
    {
        return cnt;
    }
    public void setCnt(String cnt)
    {
        this.cnt = cnt;
    }

    public BotMessage(String cnt)
    {
        this.cnt = cnt;
    }
}
