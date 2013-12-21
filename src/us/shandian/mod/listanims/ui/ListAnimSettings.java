package us.shandian.mod.listanims.ui;

import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.ListPreference;
import android.preference.CheckBoxPreference;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;

import us.shandian.mod.listanims.R;
import us.shandian.mod.listanims.ModListAnims;

public class ListAnimSettings extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener
{
	private ListPreference mAnimations;
	private ListPreference mInterpolators;
	private CheckBoxPreference mImproveCache;
	
	private static SharedPreferences prefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		prefs = getSharedPreferences(ModListAnims.PREF, Context.MODE_WORLD_READABLE);
		mAnimations = (ListPreference) findPreference(ModListAnims.LISTVIEW_ANIMATION);
		mInterpolators = (ListPreference) findPreference(ModListAnims.LISTVIEW_INTERPOLATOR);
		mImproveCache = (CheckBoxPreference) findPreference(ModListAnims.LISTVIEW_IMPROVE_CACHE);
		
		mAnimations.setValue(String.valueOf(prefs.getInt(ModListAnims.LISTVIEW_ANIMATION, 0)));
		mAnimations.setSummary(mAnimations.getEntry());
		mAnimations.setOnPreferenceChangeListener(this);
			
		mInterpolators.setValue(String.valueOf(prefs.getInt(ModListAnims.LISTVIEW_INTERPOLATOR, 0)));
		mInterpolators.setSummary(mInterpolators.getEntry());
		mInterpolators.setOnPreferenceChangeListener(this);
		mInterpolators.setEnabled(prefs.getInt(ModListAnims.LISTVIEW_ANIMATION, 0) > 0);
		
		mImproveCache.setChecked(prefs.getBoolean(ModListAnims.LISTVIEW_IMPROVE_CACHE, true));
		mImproveCache.setOnPreferenceClickListener(this);
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		
		if (preference == mAnimations) {
			int value = Integer.valueOf((String) newValue);
			int index = mAnimations.findIndexOfValue((String) newValue);
			prefs.edit().putInt(ModListAnims.LISTVIEW_ANIMATION, index).commit();
			mAnimations.setSummary(mAnimations.getEntries()[index]);
			mInterpolators.setEnabled(value > 0);
		} else if (preference == mInterpolators) {
			int value = Integer.valueOf((String) newValue);
			int index = mInterpolators.findIndexOfValue((String) newValue);
			prefs.edit().putInt(ModListAnims.LISTVIEW_INTERPOLATOR, index).commit();
			mInterpolators.setSummary(mInterpolators.getEntries()[index]);
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		if (preference == mImproveCache) {
			prefs.edit().putBoolean(ModListAnims.LISTVIEW_IMPROVE_CACHE, mImproveCache.isChecked()).commit();
		}
		return true;
	}
}
