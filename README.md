Welcome to the Crashlogger wiki!

First up is to create Application class and refer Crashlogger like this
```
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashLogger crashLogger = CrashLogger.getInstance(this);
        crashLogger.initCrashLogger();
    }
}
```
Refer Application class in manifest
```
  <application
        android:name="com.arunsoorya.crashloggerExample.MyApplication"
        ..... >
```
If you want to manage logger upload-duration and period call Settings Activity
```
@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.settings){
            startActivity(new Intent(this,SettingsActivity.class));
        }
        return true;
    }
```
   
main_menu.xml file is
```
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/settings"
        android:title="Logger Settings" />

</menu>
```


