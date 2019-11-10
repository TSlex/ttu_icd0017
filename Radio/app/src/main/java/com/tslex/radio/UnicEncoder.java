package com.tslex.radio;

public class UnicEncoder {
    public static String encode(String s){
        StringBuilder result = new StringBuilder();
//        String req = ".\\{0\\}(?=\\\\u[A-Za-z0-9]\\{4,\\})|.\\{0\\}(?<=\\\\u[A-Za-z0-9]\\{4\\})";
        for (String element : s.split(".{0}(?=\\\\u[A-Za-z0-9]{4,})|.{0}(?<=\\\\u[A-Za-z0-9]{4})")) {
//            System.out.println("->" + element.substring(0, 2));
            if (element.length() > 1 && element.substring(0, 2).equals("\\u")){
//                System.out.println(element);
                element = String.valueOf(Character.valueOf((char) Integer.parseInt(element.replace("\\u", ""), 16)));
            }

            result.append(element);
        }

        return result.toString();
    }
}
