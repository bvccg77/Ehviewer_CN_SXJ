/*
 * Copyright 2015 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.drawable;

import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import androidx.annotation.NonNull;
import com.hippo.conaco.Conaco;
import com.hippo.conaco.ConacoTask;
import com.hippo.conaco.Unikery;
import com.hippo.lib.image.ImageBitmap;
import com.hippo.widget.ObservedTextView;

public class UnikeryDrawable extends WrapDrawable implements Unikery<ImageBitmap>,
        ObservedTextView.OnWindowAttachListener {

    private static final String TAG = UnikeryDrawable.class.getSimpleName();

    private int mTaskId = Unikery.INVALID_ID;

    private final ObservedTextView mTextView;
    private final Conaco<ImageBitmap> mConaco;
    private String mUrl;

    private ImageBitmap imageBitmap;

    public UnikeryDrawable(ObservedTextView textView, Conaco<ImageBitmap> conaco) {
        mTextView = textView;
        mTextView.setOnWindowAttachListener(this);
        mConaco = conaco;
    }

    @Override
    public void onAttachedToWindow() {
        load(mUrl);
    }

    @Override
    public void onDetachedFromWindow() {
        mConaco.cancel(this);
        clearDrawable();
    }

    public void load(String url) {
        if (url != null) {
            mUrl = url;
            mConaco.load(new ConacoTask.Builder<ImageBitmap>().setUnikery(this).setUrl(url).setKey(url));
//            ConacoTask.Builder<ImageBitmap> builder =new ConacoTask.Builder<>();
//            builder.url = url;
//            builder.unikery = this;
//            builder.key = url;
//            mConaco.load(builder);
        }
    }

    private void clearDrawable() {
//        Drawable drawable = getDrawable();
//        if (drawable instanceof ImageDrawable) {
//            ((ImageDrawable) drawable).recycle();
//        }
        setDrawable(null);
        if (imageBitmap != null) {
            imageBitmap.release();
            imageBitmap = null;
        }
    }

    @Override
    public void setDrawable(Drawable drawable) {
        // Remove old callback
        Drawable oldDrawable = getDrawable();
        if (oldDrawable != null) {
            oldDrawable.setCallback(null);
        }

        super.setDrawable(drawable);

        if (drawable != null) {
            drawable.setCallback(mTextView);
        }

        updateBounds();
        if (drawable != null) {
            invalidateSelf();
        }
    }

    @Override
    public void setTaskId(int id) {
        mTaskId = id;
    }

    @Override
    public int getTaskId() {
        return mTaskId;
    }

    @Override
    public void invalidateSelf() {
        CharSequence cs = mTextView.getText();
        mTextView.setText(cs);
    }

    @Override
    public void onMiss(int source) {}

    @Override
    public void onRequest() {

    }

//    @Override
//    public void onRequest() {}

    @Override
    public void onProgress(long singleReceivedSize, long receivedSize, long totalSize) {}

    @Override
    public void onWait() {}

    @Override
    public boolean onGetValue(@NonNull ImageBitmap value, int source) {
        Drawable drawable;
        try {
            drawable =value.getDrawable();
        } catch (Exception e) {
            Log.d(TAG, "The ImageBitmap is recycled", e);
            return false;
        }

        clearDrawable();

        setDrawable(drawable);
        imageBitmap = value;
        if (drawable instanceof AnimatedImageDrawable animatedImageDrawable)
            animatedImageDrawable.start();

        return true;
    }

    @Override
    public void onFailure() {
        // Empty
    }

    @Override
    public void onCancel() {
        // Empty
    }
}
