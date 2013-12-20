package us.shandian.mod.listanims;

import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.AlphaAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

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
		
		XposedHelpers.findAndHookMethod(AbsListView.class, "initAbsListView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable{
				// Init all the fields we need
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mIsWidget", false);
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mIsScrolling", false);
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mIsTap", false);
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mHeight", 0);
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mWidth", 0);
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mPositionV", 0);
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mInverse", new Handler() {
					@Override
					public void handleMessage(Message msg) {
						// Just inverse it
						boolean mIsTap = (Boolean) XposedHelpers.getAdditionalInstanceField(msg.obj, "mIsTap");
						XposedHelpers.setAdditionalInstanceField(msg.obj, "mIsTap", !mIsTap);
					}
				});
				
				// Then set drawing cache
				((AbsListView) param.thisObject).setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE | ViewGroup.PERSISTENT_SCROLLING_CACHE);
				
			}
		});
		
		XposedHelpers.findAndHookMethod(AbsListView.class, "onLayout", boolean.class, int.class, int.class, int.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				AbsListView list = (AbsListView) param.thisObject;
				
				// Get only things we need
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mHeight", list.getHeight());
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mWidth", list.getWidth());
			}
		});
		
		XposedHelpers.findAndHookMethod(AbsListView.class, "obtainView", int.class, boolean[].class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				boolean mIsScrolling = (Boolean) XposedHelpers.getAdditionalInstanceField(param.thisObject, "mIsScrolling");
				boolean mIsWidget = (Boolean) XposedHelpers.getAdditionalInstanceField(param.thisObject, "mIsWidget");
				
				// Replace the view with animated
				if (mIsScrolling && !mIsWidget) {
					View child = (View) param.getResult();
					child = setAnimation(child, (AbsListView) param.thisObject);
					param.setResult(child);
				}
			}
		});
		
		XposedHelpers.findAndHookMethod(AbsListView.class, "scrollIfNeeded", int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				int mTouchMode = XposedHelpers.getIntField(param.thisObject, "mTouchMode");
				int TOUCH_MODE_SCROLL = XposedHelpers.getStaticIntField(AbsListView.class, "TOUCH_MODE_SCROLL");
				
				if (mTouchMode == TOUCH_MODE_SCROLL) {
					XposedHelpers.setAdditionalInstanceField(param.thisObject, "mIsWidget", false);
				}
			}
		});
		
		XposedHelpers.findAndHookMethod(AbsListView.class, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Handler mInverse = (Handler) XposedHelpers.getAdditionalInstanceField(param.thisObject, "mInverse");
				MotionEvent ev = (MotionEvent) param.args[0];
				int action = ev.getAction();
				// Set the tap mode
				switch (action & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_DOWN:
						XposedHelpers.setAdditionalInstanceField(param.thisObject, "mIsTap", true);
						mInverse.sendMessage(mInverse.obtainMessage(0, param.thisObject));
						break;
					case MotionEvent.ACTION_UP:
						XposedHelpers.setAdditionalInstanceField(param.thisObject, "mIsTap", false);
						break;
				}
			}
		});
		
		XposedHelpers.findAndHookMethod(AbsListView.class, "reportScrollStateChange", int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				int mLastScrollState = XposedHelpers.getIntField(param.thisObject, "mLastScrollState");
				int newState = (Integer) param.args[0];
				boolean mIsScrolling = true;
				
				// Set the scrolling state
				if (newState != mLastScrollState) {
					if (newState == OnScrollListener.SCROLL_STATE_IDLE) {
						mIsScrolling = false;
					}
				}
				
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mIsScrolling", mIsScrolling);
			}
		});
		
		XposedHelpers.findAndHookMethod(AbsListView.class, "handleDataChanged", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mIsWidget", true);
			}
		});
	}
	
	private View setAnimation(View view, AbsListView thisObject) {
		prefs.reload();
		// Get the needed fields
		int mPositionV = (Integer) XposedHelpers.getAdditionalInstanceField(thisObject, "mPositionV");
		int mHeight = (Integer) XposedHelpers.getAdditionalInstanceField(thisObject, "mHeight");
		int mWidth = (Integer) XposedHelpers.getAdditionalInstanceField(thisObject, "mWidth");
		Context mContext = (Context) XposedHelpers.getObjectField(thisObject, "mContext");
		
        int listAnimationMode = prefs.getInt(LISTVIEW_ANIMATION, 0);
		if (listAnimationMode == 0 || view == null) {
			return view;
		}
		
		int scrollY = 0;
		boolean down = false;
		Animation anim = null;
		int listAnimationInterpolatorMode = prefs.getInt(LISTVIEW_INTERPOLATOR, 0);
		
		try {
			scrollY = thisObject.getChildAt(0).getTop();
		} catch (NullPointerException e) {
			scrollY = mPositionV;
		}
		
		if (mPositionV < scrollY) {
			down = true;
		}
		
		mPositionV = scrollY;
		
		switch (listAnimationMode) {
			case 1:
				anim = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f);
				break;
			case 2:
				anim = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f,
										Animation.RELATIVE_TO_SELF, 1.0f,
										Animation.RELATIVE_TO_SELF, 1.0f);
				break;
			case 3:
				anim = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f,
										Animation.RELATIVE_TO_SELF, 0.5f,
										Animation.RELATIVE_TO_SELF, 0.5f);
				break;
			case 4:
				anim = new AlphaAnimation(0.0f, 1.0f);
				break;
			case 5:
				anim = new TranslateAnimation(0.0f, 0.0f, -mHeight, 0.0f);
				break;
			case 6:
				anim = new TranslateAnimation(0.0f, 0.0f, mHeight, 0.0f);
				break;
			case 7:
				if (down) {
					anim = new TranslateAnimation(0.0f, 0.0f, -mHeight, 0.0f);
				} else {
					anim = new TranslateAnimation(0.0f, 0.0f, mHeight, 0.0f);
				}
				break;
			case 8:
				if (down) {
					anim = new TranslateAnimation(0.0f, 0.0f, mHeight, 0.0f);
				} else {
					anim = new TranslateAnimation(0.0f, 0.0f, -mHeight, 0.0f);
				}
				break;
			case 9:
				anim = new TranslateAnimation(-mWidth, 0.0f, 0.0f, 0.0f);
				break;
			case 10:
				anim = new TranslateAnimation(mWidth, 0.0f, 0.0f, 0.0f);
				break;
			default:
				return view;
		}
		
		switch (listAnimationInterpolatorMode) {
			case 1:
				anim.setInterpolator(AnimationUtils.loadInterpolator(
										mContext, android.R.anim.accelerate_interpolator));
				break;
			case 2:
				anim.setInterpolator(AnimationUtils.loadInterpolator(
										mContext, android.R.anim.decelerate_interpolator));
				break;
			case 3:
				anim.setInterpolator(AnimationUtils.loadInterpolator(
										mContext, android.R.anim.accelerate_decelerate_interpolator));
				break;
			case 4:
				anim.setInterpolator(AnimationUtils.loadInterpolator(
										mContext, android.R.anim.anticipate_interpolator));
				break;
			case 5:
				anim.setInterpolator(AnimationUtils.loadInterpolator(
										mContext, android.R.anim.overshoot_interpolator));
				break;
			case 6:
				anim.setInterpolator(AnimationUtils.loadInterpolator(
										mContext, android.R.anim.anticipate_overshoot_interpolator));
				break;
			case 7:
				anim.setInterpolator(AnimationUtils.loadInterpolator(
										mContext, android.R.anim.bounce_interpolator));
				break;
			default:
				break;
		}
		anim.setDuration(500);
		view.startAnimation(anim);
		
		// Set back the needed fields
		XposedHelpers.setAdditionalInstanceField(thisObject, "mPositionV", mPositionV);
		return view;
	}
}
