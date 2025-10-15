package com.example;

import io.github.cdimascio.dotenv.Dotenv;

public final class dbSetup {
    private static final Dotenv dotenv = Dotenv.load();

    public static final String user = dotenv.get("DB_USER");
    public static final String pswd = dotenv.get("DB_PASSWORD");
}