package com.w4po.securechat;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.widget.Toast;

import androidx.annotation.Nullable;

import net.kibotu.pgp.Pgp;

import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPKeyRingGenerator;

import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

public class Helper {
    public static final String PACKAGE_NAME = "com.w4po.securechat";
    private static final String DB_PATH = "/data/data/" + PACKAGE_NAME + "/databases/";
    private static final String DB_NAME = "DB";
    private static SQLiteDatabase DB;

    public static void initialize() {
        // insert modern BC as first crypto provider
        Security.insertProviderAt(new BouncyCastleProvider(), 0);

        createDB();
    }

    private static void createDB() {
        try {
            if (DB == null || !DB.isOpen())
                DB = SQLiteDatabase.openOrCreateDatabase(DB_PATH + DB_NAME, null);

            DB.execSQL("CREATE TABLE IF NOT EXISTS keys (uid VARCHAR PRIMARY KEY, privateKey VARCHAR, publicKey VARCHAR)");
            DB.execSQL("CREATE TABLE IF NOT EXISTS users (uid VARCHAR PRIMARY KEY, name VARCHAR, status VARCHAR, image VARCHAR)");
            DB.execSQL("CREATE TABLE IF NOT EXISTS messages (id VARCHAR, fromUID VARCHAR, toUID VARCHAR, content VARCHAR, timestamp INTEGER, PRIMARY KEY (id, fromUID, toUID))");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static UserKey getUserKeys(String uid) {
        UserKey userKeys = null;

        if (DB != null && DB.isOpen()) {
            Cursor mCursor = DB.rawQuery("SELECT * FROM keys WHERE uid = '" + uid + "' LIMIT 1", null);

            if (mCursor.moveToNext()) {
                userKeys = new UserKey(uid, mCursor.getString(1), mCursor.getString(2));
                Pgp.setPrivateKey(userKeys.privateKey);
            }

            mCursor.close();
        }

        return userKeys;
    }
    public static void insertUserKeys(UserKey userKeys) {
        if (DB != null && DB.isOpen()) {
            DB.execSQL("INSERT OR REPLACE INTO keys VALUES ('" + userKeys.uid + "', '" + userKeys.privateKey + "', '" + userKeys.publicKey + "')");
        }
    }
    public static UserKey generateNewKeys(String uid) {
        UserKey userKeys = null;
        try {
            PGPKeyRingGenerator krgen = Pgp.generateKeyRingGenerator(PACKAGE_NAME.toCharArray());
            userKeys = new UserKey(uid, Pgp.genPGPPrivKey(krgen), Pgp.genPGPPublicKey(krgen));
            Pgp.setPrivateKey(userKeys.privateKey);
        } catch (PGPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userKeys;
    }

    public static String encrypt(String plaintext, String publicKey) throws IOException, PGPException {
        Pgp.setPublicKey(publicKey);
        return Pgp.encrypt(plaintext);
    }
    public static String decrypt(String ciphertext) throws Exception {
        return Pgp.decrypt(ciphertext, PACKAGE_NAME);
    }

    public static ArrayList<String> getChatsUIDs (String currentUID) {
        ArrayList<String> ids = null;
        if (DB != null && DB.isOpen()) {
//            Cursor mCursor = DB.rawQuery("SELECT * FROM messages WHERE fromUID = '" + currentUID + "' OR toUID = '" + currentUID + "' ORDER BY timestamp DESC", null);
            Cursor mCursor = DB.rawQuery("SELECT DISTINCT CASE WHEN fromUID = '" + currentUID + "' THEN toUID WHEN toUID = '" + currentUID + "' THEN fromUID END AS value FROM messages WHERE value IS NOT NULL ORDER BY timestamp DESC", null);

            ids = new ArrayList<>(mCursor.getCount());

            while (mCursor.moveToNext()) {
                ids.add(mCursor.getString(0));
            }
        }
        return ids;
    }

    public static List<Message> getMessages(String currentUID, String otherUID) {
        List<Message> messages = null;
        if (DB != null && DB.isOpen()) {
            Cursor mCursor = DB.rawQuery("SELECT * FROM messages WHERE (fromUID = '" + currentUID + "' AND toUID = '" +
                    otherUID + "') OR (fromUID = '" + otherUID + "' AND toUID = '" + currentUID + "')", null);

            messages = new ArrayList<>(mCursor.getCount());

            while (mCursor.moveToNext()) {
                messages.add(new Message(mCursor.getString(0), mCursor.getString(1),
                        mCursor.getString(2), mCursor.getString(3), mCursor.getLong(4)));
            }
        }
        return messages;
    }
    public static void insertMessage(Message message) {
        if (DB != null && DB.isOpen()) {
            DB.execSQL("INSERT OR REPLACE INTO messages VALUES ('" + message.getMessageID() + "', '" + message.getFrom() +
                    "', '" + message.getTo() + "', '" + message.getMessage() + "', " + message.getTimestamp() + ")");
        }
    }

    public static void startActivity(android.content.Context from, Class<?> toClass, @Nullable Integer flags) {
        Intent intent = new Intent(from, toClass);

        if (flags != null)
            intent.addFlags(flags);

        from.startActivity(intent);
    }

    public static void makeToast(android.content.Context context, String msg, int duration) {
        Toast.makeText(context, msg, duration).show();
    }
}