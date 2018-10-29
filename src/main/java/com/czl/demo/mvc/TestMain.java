package com.czl.demo.mvc;

public class TestMain {

 public static boolean isEquals(){
     String a ="aaaAdbc1";
     String b ="cbaAada1";
     char[] charsA = a.toCharArray();
     char[] charsB = b.toCharArray();
     int suma=0;
     int sumb=1;
    for (char c:charsA){
        suma+=(int)c;
    }

    for (char c :charsB){
        sumb+=(int)c;
    }

     System.out.println(suma);
     System.out.println(sumb-1);
     return false;
 }

    private static String lowerFirstCase(String str){
        char [] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }


    public static void main(String[] args) {
        System.out.println(lowerFirstCase("5dfsdf"));
    }

}
