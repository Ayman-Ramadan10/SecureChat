# SecureChat ![GitHub top language](https://img.shields.io/github/languages/top/w4po/SecureChat) ![GitHub release (latest by date)](https://img.shields.io/github/v/release/w4po/SecureChat) ![GitHub Release Date](https://img.shields.io/github/release-date/w4po/SecureChat) ![GitHub all releases](https://img.shields.io/github/downloads/w4po/SecureChat/total?color=green) ![GitHub issues](https://img.shields.io/github/issues/w4po/SecureChat) ![GitHub](https://img.shields.io/github/license/w4po/SecureChat)
SecureChat is an Android chat app that sends messages that are encrypted with PGP encryption.

The app uses Firebase's realtime database as a server, and SQLite as a local database.

## HOW encryption works
User A sends a message to user B
1. a copy of the plaintext message (before encryption) is saved into user A's local database
2. the message gets encrypted with user B's public key
3. the encrypted message is sent to the server (firebase realtime database)
4. user B gets the encrypted message and decrypts it with his private key
5. user B saves the decrypted message into his local database

That way no one is able to read the message except for user B
even the sender cannot decrypt his own messages (that's why a copy of the plaintext message is saved before the encryption).

## Generating public & private keys
For the first time a user is logged in using a device the app generates his public & private keys
and saves them into the user's local DB and upload the public key to the server (Firebase)
so other users can send encrypted messages to him using his public key.

When a user is logged-in in the future, the app checks his local DB for his public & private keys
- If found then the app compares the public key from the local DB with the one in the server (Firebase)
    * if both are the same then everything is good
    * if not then the app uploads/update the one on the server with the one found locally
- If not found then the app generates new ones and upload the public key to the server (Firebase)

## Credits
* [JitPack](https://github.com/jitpack/jitpack.io/)
* [Android-PGP](https://github.com/kibotu/Android-PGP)
* [Bouncy Castle](https://github.com/rtyley/spongycastle)
* [Android Image Cropper](https://github.com/ArthurHub/Android-Image-Cropper)
* [CircleImageView](https://github.com/hdodenhof/CircleImageView)
* [Picasso](https://square.github.io/picasso/)

## SCREENSHOTS
![Main login screen](https://github.com/w4po/SecureChat/blob/master/Screenshots/Main_Login.jpg?raw=true)
![Generating PGP](https://github.com/w4po/SecureChat/blob/master/Screenshots/Generating_PGP.jpg?raw=true)
![Account_Settings](https://github.com/w4po/SecureChat/blob/master/Screenshots/Account_Settings.jpg?raw=true)
![Chats](https://github.com/w4po/SecureChat/blob/master/Screenshots/Chats.jpg?raw=true)
![Messages](https://github.com/w4po/SecureChat/blob/master/Screenshots/Messages.jpg?raw=true)
![Find_Friends](https://github.com/w4po/SecureChat/blob/master/Screenshots/Find_Friends.jpg?raw=true)
![Friend_Requests](https://github.com/w4po/SecureChat/blob/master/Screenshots/Friend_Requests.jpg?raw=true)
