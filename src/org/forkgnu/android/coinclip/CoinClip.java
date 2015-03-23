/*
 * Copyright (c) 2015 Albert Lee <trisk@forkgnu.org>.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be 
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.forkgnu.android.coinclip;

import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findField;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.newInstance;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import java.lang.reflect.Field;

import java.util.Date;

public class CoinClip implements IXposedHookLoadPackage
{
	private static final long MILLISEC_PER_YEAR = 31556952000L;

	private static String encodeTrack1(String number, String name, String addl)
	{
			// IATA format: number, name, additional data (inc. date)
			String t1 = "%B" + number + "^" + name + "^" + addl + "?";
			return t1;
	}

	private static String encodeTrack23(String number, String addl)
	{
			// ABA format: number, additional data
			String t23 = ";" + number.replaceAll("[^0-9]", "") + "=" + addl + "?";
			return t23;
	}

	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable
	{
		if (!lpparam.packageName.equals("com.onlycoin.android"))
			return;

		final Class<?> coinUser = findClass("com.onlycoin.android.data.User", lpparam.classLoader);
		final Class<?> coinCard = findClass("com.onlycoin.android.data.Card", lpparam.classLoader);
		final Class<?> coinSecureCard = findClass("com.onlycoin.android.data.SecureCard", lpparam.classLoader);
		final Class<?> coinStripeInfo = findClass("com.onlycoin.android.data.SecureCard$StripeInfo", lpparam.classLoader);
		final Class<?> coinCardListAdapter = findClass("com.onlycoin.android.ui.CardListAdapter", lpparam.classLoader);
		final Class<?> coinWalletFragment = findClass("com.onlycoin.android.ui.WalletFragment", lpparam.classLoader);
		final Class<?> coinAddCardFragment = findClass("com.onlycoin.android.ui.card.AddCardFragment", lpparam.classLoader);
		final Class<?> coinCardReaderFragment = findClass("com.onlycoin.android.ui.card.CardReaderFragment", lpparam.classLoader);
		final Field scAuthenticated = findField(coinSecureCard, "authenticated");
		
		findAndHookMethod(coinUser, "isDiagnosticsUser",
			new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable
				{
					param.setResult(true);
				}
			}
		);

		findAndHookMethod(coinUser, "isDiagnosticsUser", coinUser,
			new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable
				{
					param.setResult(true);
				}
			}
		);

		findAndHookMethod(coinSecureCard, "containsStripeInfo",
			new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable
				{
					String enc = (String) getObjectField(param.thisObject, "encryptedManualPan");
					if (enc != null && !enc.isEmpty())
						param.setResult(true);
				}
			}

		);

		findAndHookMethod(coinSecureCard, "extractStripeInfo",
			new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable
				{
					if (getObjectField(param.thisObject, "encryptedStripeInfo") != null)
						return;

					String pan = (String) callMethod(param.thisObject, "getManualPan");
					if (pan == null) {
						String nick = (String) getObjectField(param.thisObject, "nick");
						XposedBridge.log("getManualPan() failed for card: " + nick);
						pan = "0000000000000000";
					}

					String month = (String) getObjectField(param.thisObject, "expirationMonth");
					String year = (String) getObjectField(param.thisObject, "expirationYear");
					String expiry;
					if (month != null && !month.isEmpty() && year != null && !year.isEmpty()) {
						expiry = year + month;
					} else {
						Date future  = new Date(System.currentTimeMillis() + 5 * MILLISEC_PER_YEAR);
						expiry = String.format("%ty%tm", future, future);
					}

					Object stripeInfo = newInstance(coinStripeInfo);
					try {
						setObjectField(stripeInfo, "pan", pan);
						setObjectField(stripeInfo, "first6Pan", pan.substring(0, 5));
						setObjectField(stripeInfo, "last4Pan", pan.substring(pan.length() - 4));
						setObjectField(stripeInfo, "track1", encodeTrack1(pan, " ", expiry));
						setObjectField(stripeInfo, "track23", encodeTrack23(pan, expiry));
						setObjectField(stripeInfo, "expiry", expiry);
					} catch (IllegalArgumentException e) {
						XposedBridge.log(e);
					}
					param.setResult(stripeInfo);
				}
			}

		);

		findAndHookMethod(coinCardListAdapter, "update", coinSecureCard,
			new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable
				{
					try {
						scAuthenticated.setBoolean(param.args[1], true);
					} catch (IllegalArgumentException e) {
						XposedBridge.log(e);
					}
				}
			}
		);

		findAndHookMethod(coinWalletFragment, "didCheck", coinSecureCard, "android.widget.TextView", "android.widget.CompoundButton",
			new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable
				{
					try {
						scAuthenticated.setBoolean(param.args[0], true);
					} catch (IllegalArgumentException e) {
						XposedBridge.log(e);
					}
				}
			}
		);

		findAndHookMethod(coinAddCardFragment, "showAuthorize", coinSecureCard, "com.onlycoin.android.ui.CoinActivity", "com.onlycoin.android.utils.Async$Block",
			new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable
				{
					try {
						scAuthenticated.setBoolean(param.args[0], true);
					} catch (IllegalArgumentException e) {
						XposedBridge.log(e);
					}
				}
			}
		);

		findAndHookMethod(coinAddCardFragment, "setSecureCard", coinSecureCard,
			new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable
				{
					try {
						scAuthenticated.setBoolean(param.args[0], true);
					} catch (IllegalArgumentException e) {
						XposedBridge.log(e);
					}
				}
			}
		);

		findAndHookMethod(coinCardReaderFragment, "setSecureCard", coinSecureCard,
			new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable
				{
					try {
						scAuthenticated.setBoolean(param.args[0], true);
					} catch (IllegalArgumentException e) {
						XposedBridge.log(e);
					}
				}
			}
		);


		findAndHookMethod("com.onlycoin.android.ui.card.AddCardFragment$21$1$1$3$1", lpparam.classLoader, "call", "android.util.Pair",
			new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable
				{
					try {
						setObjectField(getObjectField(param.args[0], "first"), "errors", null);
					} catch (IllegalArgumentException e) {
						XposedBridge.log(e);
					}
				}
			}
		);
	}
}
