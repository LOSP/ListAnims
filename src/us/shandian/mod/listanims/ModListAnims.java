package us.shandian.mod.listanims;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XSharedPreferences;

import us.shandian.mod.listanims.ui.ListAnimSettings;

public class ModListAnims implements IXposedHookZygoteInit
{

	private static XSharedPreferences prefs;
	
	public static final String PACKAGE_NAME = ModListAnims.class.getPackage().getName();
	public static final String PREF = "ListAnimSettings";
	
	public static final String LISTVIEW_ANIMATION = "listview_animation";
	public static final String LISTVIEW_INTERPOLATOR = "listview_interpolator";
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable
	{
		prefs = new XSharedPreferences(PACKAGE_NAME, PREF);
		prefs.makeWorldReadable();
	}

}
