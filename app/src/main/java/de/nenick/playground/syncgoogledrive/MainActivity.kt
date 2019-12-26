package de.nenick.playground.syncgoogledrive

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_SIGN_IN = 1
    private var mDriveServiceHelper: DriveServiceHelper? = null
    private var fileId: String? = null
    private val fileName = "sync.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // abort when this device has no available google play services
        val isAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (isAvailable != ConnectionResult.SUCCESS)
            throw RuntimeException("$isAvailable")

        // initial create or fetch the file
        btn_prepare.setOnClickListener {
            Toast.makeText(this, "Start preparing file for chat, pls wait ...", Toast.LENGTH_LONG)
                .show()
            mDriveServiceHelper!!.queryFiles()
                .addOnSuccessListener { findOrCreateFile(it) }
                .addOnFailureListener { throw it }
        }

        // save file changes
        btn_send.setOnClickListener {
            Toast.makeText(this, "Start save file to Google Drive, pls wait ...", Toast.LENGTH_LONG)
                .show()
            mDriveServiceHelper!!.saveFile(fileId, fileName, txt_content.text.toString())
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Save to google drive successful",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnFailureListener { throw it }
        }

        // fetch file updates
        btn_receive.setOnClickListener {
            Toast.makeText(
                this,
                "Start reading file from Google Drive, pls wait ...",
                Toast.LENGTH_LONG
            ).show()
            mDriveServiceHelper!!.readFile(fileId)
                .addOnSuccessListener {
                    txt_content.setText(it.second); Toast.makeText(
                    this,
                    "Update from google drive successful",
                    Toast.LENGTH_LONG
                ).show()
                }
                .addOnFailureListener { throw it }
        }

        // Authenticate the user. For most apps, this should be done when the user performs an
        // action that requires Drive access rather than in onCreate.
        requestSignIn()
    }

    private fun findOrCreateFile(it: FileList) {
        if (it.files.size == 0) {
            createNewFile()
        } else {
            fileId = it.files.first().id

            Toast.makeText(this, "Found file for chat", Toast.LENGTH_LONG).show()
        }
    }

    private fun createNewFile() {
        mDriveServiceHelper!!.createFile(fileName)
            .addOnSuccessListener {
                fileId = it; Toast.makeText(
                this,
                "Created file for chat",
                Toast.LENGTH_LONG
            ).show()
            }
            .addOnFailureListener { throw it }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> {
                handleSignInResult(resultData!!)
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }

    /**
     * Starts a sign-in activity using [.REQUEST_CODE_SIGN_IN].
     */
    private fun requestSignIn() {
        Toast.makeText(this, "Start sign in, pls wait ...", Toast.LENGTH_LONG).show()

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        val client = GoogleSignIn.getClient(this, signInOptions)

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.signInIntent, REQUEST_CODE_SIGN_IN)
    }

    /**
     * Handles the `result` of a completed sign-in activity initiated from [ ][.requestSignIn].
     */
    private fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener { googleAccount ->

                // Use the authenticated account to sign in to the Drive service.
                val credential = GoogleAccountCredential.usingOAuth2(
                    this, Collections.singleton(DriveScopes.DRIVE_APPDATA)
                )
                credential.selectedAccount = googleAccount.account
                val googleDriveService =
                    Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory(), credential)
                        .setApplicationName(BuildConfig.APPLICATION_ID)
                        .build()

                // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                // Its instantiation is required before handling any onClick actions.
                mDriveServiceHelper = DriveServiceHelper(googleDriveService)

                Toast.makeText(this, "Sign in successful", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { exception -> throw exception }
    }
}
