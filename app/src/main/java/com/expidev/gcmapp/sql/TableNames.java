package com.expidev.gcmapp.sql;

import com.expidev.gcmapp.db.Contract;

/**
 * Created by William.Randall on 1/19/2015.
 */
public enum TableNames
{
    ASSOCIATED_MINISTRIES("associated_ministries"),
    SESSION("session"),
    USER("user"),
    TRAINING(Contract.Training.TABLE_NAME),
    ASSIGNMENTS("assignments"),
    ALL_MINISTRIES("all_ministries"),
    TRAINING_COMPLETIONS("training_completions");

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
