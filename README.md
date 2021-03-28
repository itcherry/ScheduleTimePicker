# TimeRangePicker
Custom view for selection multiple time ranges. Feel free to adjust it for your needs with ease!

<img src="https://user-images.githubusercontent.com/16778669/112060607-1464af80-8b66-11eb-8e1d-1809ceb25975.gif" height="500" align="center"/>


## Usage

```xml
<com.chernysh.timerangepicker.TimePickerView
        android:id="@+id/scheduleTimePicker"                                    
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>     
```

```kotlin
scheduleTimePicker.timeRangesSelected = { timeRanges ->
            /* PUT YOUR LOGIC HERE */
}
```
Or with RxJava
```kotlin
scheduleTimePicker.timeRangesObservable()
          .subscribe { /* PUT YOUR LOGIC HERE */ }
```

## Customize
If you need customize TimePickerView, firstly you should add style set under styles.xml
```xml
     <!-- Search Text Style. -->
    <style name="TimePickerView">
        <!-- Custom values write to here for SearchEditText. -->
        <item name="android:focusable">true</item>
        <item name="android:focusableInTouchMode">true</item>
        <item name="android:enabled">true</item>
        <item name="android:hint">Search</item>
        <item name="android:imeOptions">actionSearch</item>
        <item name="android:textSize">18sp</item>
        <item name="android:maxLength">15</item>
        <item name="android:inputType">textCapSentences</item>
        <item name="android:textColorHint">#80999999</item>
        <item name="android:textColor">#000</item>
    </style>
```

Thereafter, you should give style set to app:searchTextStyle under TimePickerView
```xml
        <com.iammert.library.ui.multisearchviewlib.MultiSearchView
            android:id="@+id/multiSearchView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            app:searchTextStyle="@style/SearchTextStyle" />
```

That's it. You created own style for TimePickerView

## Setup
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation ''
}
```


License
--------


    Copyright 2021 Andrii Chernysh

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


