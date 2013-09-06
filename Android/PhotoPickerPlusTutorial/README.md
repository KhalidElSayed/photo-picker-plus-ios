Introduction
====

PhotoPickerPlusTutorial is a tutorial project that shows how to use the PhotoPicker+ component. It contains Chute SDK library as well as PhotoPicker+ library. This tutorial enables browsing your locally-stored photos as weel as your albums and photos from your social galleries, photo selection and display.

![tutorial1](/screenshots/tutorial1.png)![tutorial2](/screenshots/tutorial2.png)![tutorial3](/screenshots/tutorial3.png)![tutorial4](/screenshots/tutorial4.png)![tutorial5](/screenshots/tutorial5.png)

Setup
====

* Add the PhotoPicker+ component to your project by either copying all the resources and source code or by adding it as an Android Library project.

* Go through [ProjectSetup.md](../ChutePhotoPicker+/ProjectSetup.md) and [PhotoPicker+ documentation](../ChutePhotoPicker+/README.md) for more info.

* Register the activities, services and the application class into AndroidManifest.xml file:

    ```
        <application
        android:name=".PhotoPickerPlusTutorialApp"
        android:allowBackup="true"
        android:configChanges="keyboardHidden|orientation|screenSize"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/PhotoPickerTheme" >
        <service android:name="com.dg.libs.rest.services.HTTPRequestExecutorService" />

        <activity
            android:name="com.chute.android.photopickerplustutorial.activity.PhotoPickerPlusTutorialActivity"
            android:label="@string/app_name" >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity android:name="com.chute.android.photopickerplus.ui.activity.ServicesActivity" >
        </activity>
        <activity android:name="com.chute.android.photopickerplus.ui.activity.AssetActivity" >
        </activity>
        <activity
            android:name="com.chute.sdk.v2.api.authentication.AuthenticationActivity"
             android:configChanges="orientation|screenSize" 
            android:theme="@android:style/Theme.Light.NoTitleBar" >
        </activity>
        </application>

    ```


Usage
====

##PhotoPickerPlusTutorialApp.java 
This class is the extended Application class. It is registered inside the "application" tag in the manifest and is used for initializing the utility classes used in the component.
PhotoPickerPlusTutorialApp can extend PhotoPickerPlusApp as shown in this tutorial:

<pre><code>
public class PhotoPickerPlusTutorialApp extends PhotoPickerPlusApp {

}
</code></pre>

This way you can use your own methods and code inside the Application class. 

If you decide to extend the Application class instead of PhotoPickerPlusApp you must copy the all the code below:

<pre><code>
public class PhotoPickerPlusTutorialApp extends Application {

    public static final String APP_ID = "4f3c39ff38ecef0c89000003";
    public static final String APP_SECRET = "c9a8cb57c52f49384ab6117c4f6483a1a5c5a14c4a50d4cef276a9a13286efc9";

    private static ImageLoader createImageLoader(Context context) {
		ImageLoader imageLoader = new ImageLoader(context, R.drawable.placeholder);
		imageLoader.setDefaultImageSize((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 75, context.getResources()
						.getDisplayMetrics()));
		return imageLoader;
    }

    private ImageLoader mImageLoader;

    @Override
    public void onCreate() {
	super.onCreate();
	mImageLoader = createImageLoader(this);
	PreferenceUtil.init(getApplicationContext());
	PhotoPickerPreferenceUtil.init(getApplicationContext());
        ALog.setDebugTag("PhotoPicker");
        ALog.setDebugLevel(DebugLevel.ALL);
        Chute.init(this, new AuthConstants(APP_ID, APP_SECRET));

    PhotoPickerConfiguration config = new PhotoPickerConfiguration.Builder(
        getApplicationContext())
        .isMultiPicker(true)
        .accountList(AccountType.FACEBOOK, AccountType.INSTAGRAM)
        .localMediaList(LocalMediaType.ALL_PHOTOS, LocalMediaType.TAKE_PHOTO)
        .configUrl(ConfigEndpointURLs.SERVICES_CONFIG_URL)
        .build();
    PhotoPicker.getInstance().init(config);
    }

    @Override
    public Object getSystemService(String name) {
	if (ImageLoader.IMAGE_LOADER_SERVICE.equals(name)) {
	    return mImageLoader;
	} else {
	    return super.getSystemService(name);
	}
    }

}
</code></pre>

PhotoPickerPlusTutorialApp can also be neglected by registering PhotoPickerPlusApp into the manifest instead of PhotoPickerPlusTutoiralApp if you don't need to extend the Application class.

##PhotoPickerPlusTutorialActivity.java 
PhotoPicker+ component shows a list of services and device photos albums. You can authenticate using Facebook, Flickr, Instagram, Picasa, Google Drive, Google+, Skydrive and Dropbox, browse albums and photos, browse device photos as well as take a photo with the camera. 
After selecting photos, a result is returned to the activity that started the component i.e. PhotoPickerPlusTutorialAcitivity where the selected photos are displayed in a grid.

<pre><code>
@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	if (resultCode != Activity.RESULT_OK) {
	    return;
	}
	final PhotoActivityIntentWrapper wrapper = new PhotoActivityIntentWrapper(data);
	grid.setAdapter(new GridAdapter(PhotoPickerPlusTutorialActivity.this, wrapper.getMediaCollection()));
	Log.d(TAG, wrapper.getMediaCollection().toString());
    }
</code></pre>

PhotoActivityIntentWrapper encapsulates different information available for the selected image. Some of the additional info might be null depending of its availability. Different AccountMediaModel image paths can point to the same location if there are no additional sizes available.


      

    
      
