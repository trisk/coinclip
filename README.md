# CoinClip

Removes restrictions in the [Coin](http://onlycoin.com) app.

CoinClip is a module for the [Xposed framework](http://forum.xda-developers.com/xposed) on Android that removes certain use restrictions that are present in the Coin app. Coin is an electronic device shaped like a credit card that replaces many cards you may carry in your wallet or purse.

I just received my Coin beta device last week, and while the card works great, I'm still missing some pending authorisations Coin uses to verify credit cards. Also, I have e-gift cards I wish to convert to physical versions for ease of use in stores -- a magnetic card writer would be cost-prohibitive. It was faster for me to address these problems in software.

Don't use CoinClip for nefarious purposes. Use at your own risk.

## Installing CoinClip
You will need:
* A rooted Android device
* The [Xposed framework](http://forum.xda-developers.com/xposed)
* The [Coin app](https://play.google.com/store/apps/details?id=com.onlycoin.android) (duh)

You can use the [Xposed Installer](http://repo.xposed.info/module/de.robv.android.xposed.installer) to install Xposed and manage modules.
Make sure your **Settings->Security->Unknown Sources** has "Allow installation of apps from unknown sources" checked.

You can download CoinClip using the Xposed Installer by searching for "coinclip" in the Download section.

Once you have installed CoinClip, Xposed should prompt you to reboot. After rebooting, just start the Coin app.

## Building CoinClip
If you don't trust the prebuilt copy of a module with access to your financial information, you are encouraged to build CoinClip from source yourself.

You will need:
* The [Android SDK](https://developer.android.com/sdk/installing/index.html)
* [Apache Ant](http://ant.apache.org/)
* [Git](http://git-scm.com/)

You may need to add the Android SDK `tools` directory to your `$PATH`.
To produce a debug build (on unix), run:
```shell
PATH="<path to your Android SDK 'tools' directory>:$PATH"
export PATH
git clone https://github.com/trisk/coinclip.git
android update project -p coinclip
cd coinclip
ant debug
```

You can install the resulting file: `bin/coinclip-debug.apk`

See [these instructions](https://developer.android.com/tools/building/building-cmdline-ant.html) for producing a release build signed with your own private key.
