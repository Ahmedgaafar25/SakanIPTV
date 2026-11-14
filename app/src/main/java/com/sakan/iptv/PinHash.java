package com.sakan.iptv;
import java.security.*;import javax.crypto.*;import javax.crypto.spec.PBEKeySpec;
final class PinHash {
 static String create(String pin)throws Exception{if(!pin.matches("[0-9]{4,8}"))throw new IllegalArgumentException("Use 4 to 8 digits");byte[] salt=new byte[24];new SecureRandom().nextBytes(salt);String algorithm;try{SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");algorithm="PBKDF2WithHmacSHA256";}catch(Exception e){algorithm="PBKDF2WithHmacSHA1";}return algorithm+":100000:"+hex(salt)+":"+hex(derive(pin,salt,100000,algorithm));}
 static boolean verify(String pin,String saved)throws Exception{String[] p=saved.split(":");if(p.length!=4)return false;int rounds=Integer.parseInt(p[1]);if(rounds<10000||rounds>300000)return false;if(!p[0].equals("PBKDF2WithHmacSHA256")&&!p[0].equals("PBKDF2WithHmacSHA1"))return false;return MessageDigest.isEqual(unhex(p[3]),derive(pin,unhex(p[2]),rounds,p[0]));}
 private static byte[] derive(String pin,byte[] salt,int rounds,String algorithm)throws Exception{PBEKeySpec key=new PBEKeySpec(pin.toCharArray(),salt,rounds,256);try{return SecretKeyFactory.getInstance(algorithm).generateSecret(key).getEncoded();}finally{key.clearPassword();}}
 static String hex(byte[] bytes){StringBuilder s=new StringBuilder();for(byte b:bytes)s.append(String.format(java.util.Locale.ROOT,"%02x",b&255));return s.toString();}
 static byte[] unhex(String s){if(s.length()%2!=0)throw new IllegalArgumentException();byte[] b=new byte[s.length()/2];for(int i=0;i<b.length;i++){int hi=Character.digit(s.charAt(2*i),16),lo=Character.digit(s.charAt(2*i+1),16);if(hi<0||lo<0)throw new IllegalArgumentException();b[i]=(byte)((hi<<4)|lo);}return b;}
}
