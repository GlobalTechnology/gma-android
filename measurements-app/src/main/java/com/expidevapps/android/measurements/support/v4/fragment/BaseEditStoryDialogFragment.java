package com.expidevapps.android.measurements.support.v4.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.app.DialogFragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AlertDialog;

import com.crashlytics.android.Crashlytics;
import com.expidevapps.android.measurements.R;
import com.google.common.io.Closer;

import org.ccci.gto.android.common.picasso.view.SimplePicassoImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

public abstract class BaseEditStoryDialogFragment extends DialogFragment {
    private static final int REQUEST_CAPTURE_IMAGE = 1;
    private static final int REQUEST_SELECT_IMAGE = 2;

    private static final int MAX_DIMENSION = 400;

    @Nullable
    @Optional
    @InjectView(R.id.image)
    SimplePicassoImageView mImageView;

    @Nullable
    private CopyContentUriTask mTask = null;
    @Nullable
    File mImage = null;

    /* BEGIN lifecycle */

    @NonNull
    @Override
    public final Dialog onCreateDialog(final Bundle savedState) {
        return onCreateDialogBuilder(savedState).create();
    }

    @NonNull
    protected AlertDialog.Builder onCreateDialogBuilder(@Nullable final Bundle savedState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNegativeButton(android.R.string.cancel, null);
        return builder;
    }

    @Override
    public void onStart() {
        super.onStart();
        ButterKnife.inject(this, getDialog());
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (mTask != null) {
                        mTask.cancel(true);
                        mTask = null;
                    }
                    mTask = new CopyContentUriTask();
                    AsyncTaskCompat.executeParallel(mTask, data.getData());
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @UiThread
    void onUpdateImage(@Nullable final File file) {
        if (mImage != null && mImage.exists() && !mImage.isDirectory()) {
            mImage.delete();
        }
        mImage = file;
        updateImageView();
    }

    @Override
    public void onStop() {
        super.onStop();
        ButterKnife.reset(this);
    }

    /* END lifecycle */

    @Optional
    @OnClick(R.id.image)
    void changeImage() {
        if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) && false) {
            // show picker if we have a camera
            final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title_dialog_stories_update_image)
                    .setView(R.layout.dialog_import_photo)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            dialog.show();
            new SelectPictureDialogHelper(dialog);
        } else {
            // otherwise just select an existing photo
            selectPhoto();
        }
    }

    void selectPhoto() {
        startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), REQUEST_SELECT_IMAGE);
    }

    void capturePhoto() {
        startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CAPTURE_IMAGE);
    }

    private void updateImageView() {
        if (mImageView != null) {
            if (mImage != null) {
                mImageView.setPicassoFile(mImage);
            } else {
                mImageView.setPicassoUri(null);
            }
        }
    }

    class SelectPictureDialogHelper {
        @NonNull
        private final Dialog mDialog;

        SelectPictureDialogHelper(@NonNull final Dialog dialog) {
            mDialog = dialog;
            ButterKnife.inject(this, mDialog);
        }

        @Optional
        @OnClick(R.id.capture)
        void onCapturePhoto() {
            capturePhoto();
            mDialog.dismiss();
        }

        @Optional
        @OnClick(R.id.choose)
        void onSelectPhoto() {
            selectPhoto();
            mDialog.dismiss();
        }
    }

    private class CopyContentUriTask extends AsyncTask<Uri, Void, File> {
        /**
         * this instance variable is for backwards compatibility with Gingerbread only
         */
        @Nullable
        @Deprecated
        private File mFile;

        @Override
        @Nullable
        @WorkerThread
        protected File doInBackground(final Uri... uris) {
            // only process the first Uri
            final Uri uri = uris != null && uris.length > 0 ? uris[0] : null;
            if (uri == null) {
                return null;
            }

            // load the bitmap using the ContentResolver and watching for OOM exceptions
            final ContentResolver cr = getActivity().getContentResolver();
            Bitmap img;
            try {
                final BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 1;
                while (true) {
                    // short-circuit execution because we've been canceled
                    if (isCancelled()) {
                        return null;
                    }

                    // safely attempt to load the selected image
                    try {
                        final Closer closer = Closer.create();
                        try {
                            final InputStream is = closer.register(cr.openInputStream(uri));

                            // decode the bitmap
                            img = BitmapFactory.decodeStream(is, null, opts);
                            break;
                        } catch (final Throwable t) {
                            throw closer.rethrow(t);
                        } finally {
                            closer.close();
                        }
                    } catch (final OutOfMemoryError oom) {
                        // try scaling input image, abort if we get too crazy with shrinking the image
                        opts.inSampleSize *= 2;
                        if (opts.inSampleSize <= 256) {
                            continue;
                        }

                        // rethrow OOM exception
                        throw oom;
                    }
                }
            } catch (final IOException e) {
                Crashlytics.logException(e);
                return null;
            }

            // resize image to MAX_DIMENSION
            final int inWidth = img.getWidth();
            final int inHeight = img.getHeight();
            if (inHeight > MAX_DIMENSION || inWidth > MAX_DIMENSION) {
                // calculate target size enforcing MAX_DIMENSION
                float scale = ((float) MAX_DIMENSION) / inWidth;
                if (scale * inHeight > MAX_DIMENSION) {
                    scale = ((float) MAX_DIMENSION) / inHeight;
                }

                // only scale if necessary
                if (scale < 1) {
                    Matrix matrix = new Matrix();
                    matrix.postScale(scale, scale);
                    final Bitmap target = Bitmap.createBitmap(img, 0, 0, inWidth, inHeight, matrix, false);

                    // recycle source if we have a different target
                    if (target != img) {
                        img.recycle();
                        img = target;
                    }
                }
            }

            // save image to a temporary file
            try {
                final File imageDir = new File(getActivity().getFilesDir(), "story-images");
                if (!imageDir.isDirectory() && !imageDir.mkdirs()) {
                    throw new RuntimeException("Unable to create story images directory");
                }

                mFile = File.createTempFile("img", ".jpg", imageDir);

                final Closer closer = Closer.create();
                try {
                    final OutputStream out = closer.register(new FileOutputStream(mFile));
                    img.compress(Bitmap.CompressFormat.JPEG, 80, out);
                } catch (final Throwable t) {
                    throw closer.rethrow(t);
                } finally {
                    closer.close();
                }

                return mFile;
            } catch (final IOException e) {
                Crashlytics.logException(e);

                // cleanup file on error
                if (mFile != null) {
                    mFile.delete();
                }
            }

            return null;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        protected void onCancelled(File file) {
            super.onCancelled(file);

            if (file != null) {
                file.delete();
            }
        }

        /**
         * This is implemented for Gingerbread compatibility only
         */
        @Override
        @MainThread
        @Deprecated
        protected void onCancelled() {
            super.onCancelled();

            // delete the local file if we canceled this task and the file exists
            if (mFile != null) {
                mFile.delete();
            }
        }

        @Override
        @MainThread
        protected void onPostExecute(@Nullable final File file) {
            super.onPostExecute(file);

            // update the image
            onUpdateImage(file);
        }
    }
}
