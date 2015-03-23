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
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import java.lang.reflect.Field;

public class CoinClip implements IXposedHookLoadPackage
{
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable
	{
		if (!lpparam.packageName.equals("com.onlycoin.android"))
			return;

		final Class<?> coinUser = findClass("com.onlycoin.android.data.User", lpparam.classLoader);
		final Class<?> coinJsonMessage = findClass("com.onlycoin.android.data.JsonMessage", lpparam.classLoader);
		final Class<?> coinCard = findClass("com.onlycoin.android.data.Card", lpparam.classLoader);
		final Class<?> coinSecureCard = findClass("com.onlycoin.android.data.SecureCard", lpparam.classLoader);
		final Field jmErrors = findField(coinJsonMessage, "errors");
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

		findAndHookMethod("com.onlycoin.android.ui.card.AddCardFragment$21$1$1$3$1", lpparam.classLoader, "call", "android.util.Pair",
			new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable
				{
					java.lang.Object card = getObjectField(param.args[0], "first");
					jmErrors.set(card, null);
				}
			}
		);
	}
}
