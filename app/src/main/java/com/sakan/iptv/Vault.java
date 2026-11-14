package com.sakan.iptv;
import android.content.Context;
import android.security.keystore.*;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
final class Vault {
 static javax.crypto.SecretKey key() throws Exception {KeyStore s=KeyStore.getInstance("AndroidKeyStore");s.load(null);if(!s.containsAlias("sakan_accounts")){KeyGenerator g=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");g.init(new KeyGenParameterSpec.Builder("sakan_accounts",KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());g.generateKey();}return (javax.crypto.SecretKey)s.getKey("sakan_accounts",null);}
 static String load(Context c) throws Exception {String v=c.getSharedPreferences("vault",0).getString("accounts","");if(v.isEmpty())return "[]";String[] p=v.split(":");Cipher x=Cipher.getInstance("AES/GCM/NoPadding");x.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(p[0],0)));return new String(x.doFinal(Base64.decode(p[1],0)),"UTF-8");}
 static void save(Context c,String data) throws Exception {Cipher x=Cipher.getInstance("AES/GCM/NoPadding");x.init(Cipher.ENCRYPT_MODE,key());String v=Base64.encodeToString(x.getIV(),2)+":"+Base64.encodeToString(x.doFinal(data.getBytes("UTF-8")),2);if(!c.getSharedPreferences("vault",0).edit().putString("accounts",v).commit())throw new Exception("تعذر حفظ الحساب");}
}
