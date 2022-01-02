package fr.commons.generique.controller.db;

import static android.provider.DocumentsContract.EXTRA_INITIAL_URI;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.commons.generique.controller.utils.Log;

public class DBHelper extends SQLiteOpenHelper {

	public static final int IMPORT_DATABASE = 99;

	private final String DB_PATH;

	private final String DB_NAME;

	private static DBHelper instance;

	public DBHelper(Context ctx, String db_name, int db_version) {
		super(ctx, db_name, null,
				db_version);
		this.DB_NAME = db_name;
		this.DB_PATH = ctx.getApplicationInfo().dataDir + "/databases/";
		createFromAssetIfNotExist(ctx);
	}

	public static DBHelper getInstance() {
		return instance;
	}

	public static void setInstance(DBHelper dbHelper) {
		DBHelper.instance = dbHelper;
	}

	private void createFromAssetIfNotExist(Context ctx) {
		if (!isDataBaseExist()) {
			SQLiteDatabase db = this.getReadableDatabase();
			Log.info("SqLiteDatabase create with version " + db.getVersion());
			this.close();
			// database not found copy the database from assests
			// create input stream
			try (InputStream mInput = ctx.getAssets().open(this.DB_NAME)) {
				// open output stream
				String outFileName = this.DB_PATH + this.DB_NAME;
				try (OutputStream mOutput = new FileOutputStream(outFileName)) {
					Log.info("SqLiteDatabase copy database to " + outFileName);

					// copy db from assets to real location
					byte[] mBuffer = new byte[1024];
					int mLength;
					while ((mLength = mInput.read(mBuffer)) > 0) {
						mOutput.write(mBuffer, 0, mLength);
					}

					// close streams
					mOutput.flush();
				}
			} catch (IOException ioe) {
				throw new Error("ErrorCopyingDataBase", ioe);
			}
		}
	}

	/**
	 * Check that the database exists here:
	 * /data/data/PACKAGE/databases/DATABASENAME
	 */
	private boolean isDataBaseExist() {
		return new File(this.DB_PATH + this.DB_NAME).exists();
	}

	/**
	 * Open the database, so we can query it
	 */
	public SQLiteDatabase openDB() {
		File file = new File(this.DB_PATH + this.DB_NAME);
		return SQLiteDatabase.openOrCreateDatabase(file, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.debug("onCreate");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.debug("onUpgrade() from " + oldVersion + " to " + newVersion);
		db.setVersion(newVersion);
	}

	private static String getUpdateDBFileName(final int oldVersion) {
		return "database_version_"
				+ oldVersion + "_to_"
				+ (oldVersion + 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.debug("Downgrade from " + oldVersion + " to " + newVersion);
		db.setVersion(newVersion);
	}

	/**
	 * Update database with sql file located by given filename
	 *
	 * @param db       Database to upgrade
	 * @param fileName Path of sql file
	 * @throws IOException       ioexception
	 * @throws NotFoundException if sql file is not found
	 * @throws SQLException      sqlexception
	 */
	private static void updateFromFile(Context ctx, SQLiteDatabase db, String fileName)
			throws IOException, NotFoundException, SQLException {
		db.beginTransaction();
		// Open the resource
		InputStream in = ctx.getResources()
				.openRawResource(ctx.getResources().getIdentifier(fileName,
						"raw", ctx.getPackageName()));
		try (BufferedReader inReader = new BufferedReader(new InputStreamReader(in))) {
			// Iterate through lines
			while (inReader.ready()) {
				String stmt = inReader.readLine();
				db.execSQL(stmt);
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}


	/**
	 * Copies the database file at the specified location over the current
	 * internal application database.
	 */
	public boolean importDatabase(Context ctx, Uri uriDbExternalFile) throws IOException {
		// Close the SQLiteOpenHelper so it will commit the created empty
		// database to internal storage.
		close();
		try (InputStream is = ctx.getContentResolver().openInputStream(uriDbExternalFile)) {
			Path oldDbPath = new File(this.DB_PATH + this.DB_NAME).toPath();
			Files.copy(is, oldDbPath, StandardCopyOption.REPLACE_EXISTING);
			// Access the copied database so SQLiteHelper will cache it and mark
			// it as created.
			getWritableDatabase().close();
			return true;
		} catch (Exception e) {
			Log.error("Impossible d'importer le fichier", e);
		}
		return false;
	}

	public void importDatabase(Activity activity) {

		Intent chooseFileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		chooseFileIntent.setTypeAndNormalize("*/*");
		chooseFileIntent.putExtra(EXTRA_INITIAL_URI, Environment.DIRECTORY_DOWNLOADS);

		chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose a file");
		activity.startActivityForResult(chooseFileIntent, IMPORT_DATABASE);
	}

	public void exportDatabase(Context ctx) {
		String dateStr = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

		ContentValues contentValues = new ContentValues();
		contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "MyDatabasetest_" + dateStr + "_.db");
		contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.sqlite3");
		contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

		ContentResolver resolver = ctx.getContentResolver();
		Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);

		Path pathDb = new File(this.DB_PATH + this.DB_NAME).toPath();
		try (OutputStream os = resolver.openOutputStream(uri)) {
			Files.copy(pathDb, os);
			os.flush();
			Toast.makeText(ctx, "Téléchargement de la base de donnée OK", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(ctx, "Téléchargement de la base de donnée NOK", Toast.LENGTH_LONG).show();
		}
	}

}