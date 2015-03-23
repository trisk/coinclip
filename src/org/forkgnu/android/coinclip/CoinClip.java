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
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import java.lang.reflect.Field;

import android.util.Pair;

public class CoinClip implements IXposedHookLoadPackage
{
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable
	{
		if (!lpparam.packageName.equals("com.onlycoin.android"))
			return;

		final Class<?> card = findClass("com.onlycoin.android.data.Card", lpparam.classLoader);
		final Class<?> secureCard = findClass("com.onlycoin.android.data.SecureCard", lpparam.classLoader);
		final Field errors = findField(card, "errors");
		final Field authenticated = findField(secureCard, "authenticated");
		
		XposedBridge.log("Loaded app: " + lpparam.packageName);

		findAndHookMethod("com.onlycoin.android.data.User", lpparam.classLoader, "isDiagnosticsUser",
			new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable
				{
					param.setResult(true);
				}
			}
		);

		findAndHookMethod("com.onlycoin.android.ui.card.AddCardFragment$21$1$1$3$1", lpparam.classLoader, "call",
			new XC_MethodHook()
			{
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable
				{
					Pair<java.lang.Object, java.lang.Exception> p = (Pair<java.lang.Object, java.lang.Exception>)param.args[0];
					errors.set(p.first, null);
				}
			}
		);
	}
}
