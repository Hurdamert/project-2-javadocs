package com.example;

import io.github.cdimascio.dotenv.Dotenv;

/**
  * The dbSetup class loads a .env and populates a username and password 
  * a customer orders an item at the boba shop
  * @author Evan Ganske
*/
public final class dbSetup {
    private static final Dotenv dotenv = Dotenv.load();

    public static final String user = dotenv.get("DB_USER");
    public static final String pswd = dotenv.get("DB_PASSWORD");
}