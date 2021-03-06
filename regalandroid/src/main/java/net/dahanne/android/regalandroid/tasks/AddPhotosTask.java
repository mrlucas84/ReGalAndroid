/**
 *  ReGalAndroid, a gallery client for Android, supporting G2, G3, etc...
 *  URLs: https://github.com/anthonydahanne/ReGalAndroid , http://blog.dahanne.net
 *  Copyright (c) 2010 Anthony Dahanne
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.dahanne.android.regalandroid.tasks;

import java.io.File;
import java.util.ArrayList;

import net.dahanne.android.regalandroid.activity.Settings;
import net.dahanne.android.regalandroid.remote.RemoteGalleryConnectionFactory;
import net.dahanne.android.regalandroid.utils.AndroidUriUtils;
import net.dahanne.android.regalandroid.utils.ShowUtils;
import net.dahanne.gallery.commons.remote.GalleryConnectionException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.Window;

public class AddPhotosTask extends AsyncTask<Object, Integer, Void> {
	private String exceptionMessage = null;
	Activity activity;
	private String galleryUrl;
	private final ProgressDialog progressDialog;

	public AddPhotosTask(Activity context, ProgressDialog progressDialog) {
		super();
		activity = context;
		this.progressDialog = progressDialog;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Void doInBackground(Object... parameters) {
		String galleryUrl = (String) parameters[0];
		Integer albumName = (Integer) parameters[1];
		ArrayList<Uri> photoUris = (ArrayList<Uri>) parameters[2];
		boolean mustLogIn = (Boolean) parameters[3];

		try {
			if (mustLogIn) {
				RemoteGalleryConnectionFactory.getInstance().loginToGallery();
			}

			for (Uri photoUri : photoUris) {
				File imageFile = AndroidUriUtils.getFileFromUri(photoUri, activity);
				String imageName = AndroidUriUtils.getFileNameFromUri(photoUri,
						activity);
				RemoteGalleryConnectionFactory.getInstance().uploadPictureToGallery(galleryUrl, albumName,
						imageFile, imageName,
						Settings.getDefaultSummary(activity),
						Settings.getDefaultDescription(activity));
				publishProgress((int) (((photoUris.indexOf(photoUri) + 1) / (float) photoUris
						.size()) * 10000));
			}
		} catch (GalleryConnectionException e) {
			exceptionMessage = e.getMessage();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void useless) {

		progressDialog.dismiss();
		if (exceptionMessage != null) {
			// Something went wrong
			ShowUtils.getInstance().alertConnectionProblem(exceptionMessage,
					galleryUrl, activity);
		} else {
			ShowUtils.getInstance().toastImagesSuccessfullyAdded(activity);
		}

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		activity.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, 0);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		activity.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, values[0]);
	}

}
