package com.expidev.gcmapp.sql;

/**
 * Created by William.Randall on 1/19/2015.
 */
public enum TableNames
{
    ASSOCIATED_MINISTRIES("associated_ministries"),
    USER("user"),
    ASSIGNMENTS("assignments");

    private String tableName;

    private TableNames(String tableName)
    {
        this.tableName = tableName;
    }

    public String getTableName()
    {
        return tableName;
    }
}
