package com.example.cmsfp.utils;

import java.util.UUID;

public class Utils {
       public static String generateUniqueId() {
           return UUID.randomUUID().toString().substring(28);
        }
}
