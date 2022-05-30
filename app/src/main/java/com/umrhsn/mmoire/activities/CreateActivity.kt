package com.umrhsn.mmoire.activities

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.adapters.ImagePickerAdapter
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.networking.BitmapScaler
import com.umrhsn.mmoire.utils.*
import java.io.ByteArrayOutputStream

/** [Uri] --> Uniform Resource Identifier --> is like a String that unambiguously identifies where does a particular resource live, which in our case is an image. The [Uri] specifies where is the direct path to locate this photo.*/

class CreateActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CreateActivity"
        private const val READ_EXTERNAL_STORAGE_PERMISSION =
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val READ_EXTERNAL_PHOTOS_CODE = 248
        private const val MIN_GAME_NAME_LENGTH = 3
        private const val MAX_GAME_NAME_LENGTH = 14
    }

    /** declaring [CreateActivity] views */
    private lateinit var rvImagePicker: RecyclerView
    private lateinit var etGameName: EditText
    private lateinit var btnSave: Button
    private lateinit var pbUploading: ProgressBar

    /** [boardSize] */
    private lateinit var boardSize: BoardSize

    /** [Firebase] database instances and related vars */
    private val storage = Firebase.storage
    private val db = Firebase.firestore
    private val chosenImageUris = mutableListOf<Uri>() // see doc at import list
    private var numImagesRequired = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        // ActionBar with no elevation
        supportActionBar?.elevation = 0f

        // set color of navigation bar
        // TODO: make navigation bar colored according to light mode or dark mode
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            window.navigationBarColor = getColor(R.color.light_purple)
//        }

        // to make a return button to be shown in the ActionBar
        /** to make the return button functional, [onOptionsItemSelected] function must be set */
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // init CreateActivity views
        rvImagePicker = findViewById(R.id.rvImagePicker)
        etGameName = findViewById(R.id.etGameName)
        btnSave = findViewById(R.id.btnSave)
        pbUploading = findViewById(R.id.pbUploading)

        // get board size passed from MainActivity
        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize

        // to display the number of images required in the ActionBar
        numImagesRequired = boardSize.getNumPairs()
        supportActionBar?.title = "Choose pics 0 / $numImagesRequired"

        // when the save button is clicked we trigger saving data to Firebase
        btnSave.setOnClickListener { saveDataToFirebase() }

        etGameName.filters = arrayOf(InputFilter.LengthFilter(MAX_GAME_NAME_LENGTH))
        etGameName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                btnSave.isEnabled = shouldEnableSaveButton()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // nothing to do here
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // nothing to do here
            }
        })

        // setup the image picking recycler view
        rvImagePicker.adapter = ImagePickerAdapter(this,
            chosenImageUris,
            boardSize,
            object : ImagePickerAdapter.ImageClickListener {
                override fun onPlaceholderClicked() {
                    if (isPermissionGranted(this@CreateActivity,
                            READ_EXTERNAL_STORAGE_PERMISSION)
                    ) {
                        // if user granted the permission launch the photo-choosing flow
                        launchIntentForPhotos()
                    } else {
                        // if the permission is not granted launch an Android system dialog to ask the user to grant permission
                        requestPermission(this@CreateActivity,
                            READ_EXTERNAL_STORAGE_PERMISSION,
                            READ_EXTERNAL_PHOTOS_CODE)
                    }
                }
            })
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    /** to make the return button functional, [onOptionsItemSelected] function must be set */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    /** this method is called regardless the user granted the permission or not */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == READ_EXTERNAL_PHOTOS_CODE) { // which we used to launch the dialog
            // we check for grant result, which can't be empty and must be equal to PERMISSION_GRANTED
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchIntentForPhotos()
            } else { // user rejected the permission, we inform the user the impact of this decision
                Toast.makeText(
                    this,
                    "In order to create a custom game, you need to provide access to your photos",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /** With this request, the app will need to get permission to access storage in the user's phone */
    private fun launchIntentForPhotos() {
        val intent = Intent(Intent.ACTION_PICK) // because it's implicit
        intent.type = "image/*" // to show images only
        intent.putExtra(
            Intent.EXTRA_ALLOW_MULTIPLE, true,
        ) // allows multiple selection of images to be all imported in one step
        resultLauncher.launch(intent) // the line of code that launches the implicit intent
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode != Activity.RESULT_OK || data == null) {
                Log.w(TAG, "No data from launched activity, user likely cancelled the flow")
                return@registerForActivityResult
            }
            /** if we reached this far in the function we know that we have valid data.
             *
             * there are 2 attributes worth concern when user picks photos:
             *
             * 1- When the launched picking-photo app supports picking only 1 photo, the selected photo comes in the [data.data] attribute, and that will be a [Uri].
             *
             * 2- the launched app supports picking multiple photos, that data comes back in the [data.clipData] attribute, which is a [ClipData].
             *
             * We prefer to use [ClipData].*/
            val selectedUri = data.data
            val clipData = data.clipData

            if (clipData != null) {
                Log.i(TAG, "clipData numImages ${clipData.itemCount}: $clipData")
                for (position in 0 until clipData.itemCount) {
                    val clipItem = clipData.getItemAt(position) // retrieve that item
                    if (chosenImageUris.size < numImagesRequired) {
                        // get imageUri out of the clipItem object, add it to chosenImageUris
                        if (!chosenImageUris.contains(clipItem.uri)) {
                            chosenImageUris.add(clipItem.uri)
                        } else {
                            Toast.makeText(this,
                                "Sorry, one or multiple images you're trying to add already exist",
                                Toast.LENGTH_LONG).show()
                        }
                        // DONE (1) add functionality to prevent user from choosing duplicate images.
                    }
                }
            } else if (selectedUri != null) {
                Log.i(TAG, "data: $selectedUri")
                if (chosenImageUris.contains(selectedUri)) {
                    chosenImageUris.add(selectedUri)
                } else {
                    Toast.makeText(this,
                        "Sorry, image you're trying to add already exists",
                        Toast.LENGTH_LONG).show()
                }
                Log.i(TAG, "called from Uri, selectedUri = $selectedUri")
            }

            rvImagePicker.post { kotlin.run { rvImagePicker.adapter?.notifyDataSetChanged() } }

            // update text in ActionBar to detected number of selected images
            supportActionBar?.title = "Choose pics (${chosenImageUris.size} / $numImagesRequired)"

            /** when app tries to fetch a relatively large image into the image picker -> app crashes
             *
             * error: [java.lang.RuntimeException: Canvas: trying to draw too large(127844352bytes) bitmap.]
             *
             * TODO: find out what block of code causes this and solve the problem */
        }

    /** handles image scaling process via means of [BitmapScaler] */
    private fun getImageByteArray(photoUri: Uri): ByteArray {
        /** get the [originalBitmap] based on the [photoUri], this depends on the api version of the phone this app is running on */
        val originalBitmap =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Android Pie or later
                val source = ImageDecoder.createSource(contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                // a previous version before Android Pie
                MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
            }
        Log.i(TAG, "Original width ${originalBitmap.width} and height ${originalBitmap.height}")
        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap, 250)
        Log.i(TAG, "Scaled width ${scaledBitmap.width} and height ${scaledBitmap.height}")
        val byteOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
        return byteOutputStream.toByteArray()
    }

    /** uploads [ByteArray] images to [FirebaseStorage] */
    private fun handleImageUploading(gameName: String) {
        pbUploading.visibility = View.VISIBLE
        var didEncounterError = false
        val uploadedImageUrls = mutableListOf<String>()
        chosenImageUris.withIndex().forEach { (index, photoUri) ->
            /** the [imageByteArray] is what we will actually upload to the Firebase and [getImageByteArray] method will handle all the logic of quality downgrading */
            val imageByteArray = getImageByteArray(photoUri)

            /** [filePath] defines where the images should live in the [FirebaseStorage] */
            val filePath = "images/$gameName/${System.currentTimeMillis()}-${index}.jpg"

            /** [photoReference] is the location where the photo will be saved int [StorageReference]
             *
             * it takes [filePath] as a parameter */
            val photoReference = storage.reference.child(filePath)
            photoReference
                .putBytes(imageByteArray) // uploads the images array
                .continueWithTask { // once the upload completes, this block will be executed
                        photoUploadTask ->
                    // progress status (conclude how many bytes were uploaded)
                    Log.i(TAG, "Uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                    // once a photo has been uploaded we want to get the corresponding download url
                    photoReference.downloadUrl
                } // the operation before is async and we want to get notified when it's completed
                .addOnCompleteListener { // handles the notification we need for this
                        downloadUrlTask ->
                    /** first thing to check: did this operation succeed or not?*/
                    if (!downloadUrlTask.isSuccessful) { // check for particular image
                        Log.e(TAG, "Exception with Firebase storage", downloadUrlTask.exception)
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT)
                            .show()
                        didEncounterError = true
                        return@addOnCompleteListener
                    }
                    if (didEncounterError) { // check for all images
                        pbUploading.visibility = View.GONE
                        return@addOnCompleteListener
                    }
                    /** make an array of [downloadUrl] to keep track of success */
                    val downloadUrl = downloadUrlTask.result.toString()
                    uploadedImageUrls.add(downloadUrl)
                    pbUploading.progress = uploadedImageUrls.size * 100 / chosenImageUris.size
                    Log.i(
                        TAG,
                        "Finished uploading $photoUri, num uploaded ${uploadedImageUrls.size}"
                    )
                    if (uploadedImageUrls.size == chosenImageUris.size)
                        handleAllImagesUploaded(gameName, uploadedImageUrls)
                }
        }
    }

    /** handle uploading all images to [FirebaseFirestore] */
    private fun handleAllImagesUploaded(gameName: String, imageUrls: MutableList<String>) {
        db
            .collection("games")
            .document(gameName)
            .set(mapOf("images" to imageUrls))
            .addOnCompleteListener { gameCreationTask ->
                pbUploading.visibility = View.GONE
                if (!gameCreationTask.isSuccessful) {
                    Log.e(TAG, "Exception with game creation", gameCreationTask.exception)
                    Toast.makeText(this, "Failed game creation", Toast.LENGTH_SHORT)
                        .show()
                    return@addOnCompleteListener
                }
                Log.i(TAG, "Successfully created game $gameName")
                showAlertDialog(this,
                    "Upload complete! Let's play your game '$gameName'",
                    null,
                    null) {
                    val resultData = Intent()
                    resultData.putExtra(EXTRA_GAME_NAME, gameName)
                    setResult(Activity.RESULT_OK, resultData)
                    finish()
                }
            }
    }

    /** save data to [FirebaseStorage]
     *
     * note: we need to downscale images before uploading
     *
     * reason:
     *
     * --> images used in this app are shown in small cards, no need for original size.
     *
     * --> to use less storage at Firebase (free storage limit: 5GB = 5000 images of 1MB each. So even if the user uploaded hundreds of photos, they will be of minimal size anyway and won't exceed the Firebase storage limit)
     *
     * --> also less size means faster download */
    private fun saveDataToFirebase() {
        Log.i(TAG, "saveDataToFirebase")

        btnSave.isEnabled = false

        val customGameName = etGameName.text.toString()

        // check that we are not overwriting someone else's data
        db
            .collection("games")
            .document(customGameName)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.data != null) {
                    showAlertDialog(this,
                        "Name taken",
                        "A game already exists with the name '$customGameName'.\nPlease choose another name.",
                        null) {}
                    btnSave.isEnabled = true
                } else {
                    handleImageUploading(customGameName)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Encountered an error while saving memory game", exception)
                Toast.makeText(this,
                    "Encountered an error while saving memory game",
                    Toast.LENGTH_SHORT).show()
                btnSave.isEnabled = false
            }
    }

    /** [btnSave] will be enabled only if:
     *
     * 1- number of chosen images by user equals number of images required for the board size.
     *
     * 2- game name editText is not blank.
     *
     * 3- the game name in the editText is not less than [MIN_GAME_NAME_LENGTH].*/
    private fun shouldEnableSaveButton(): Boolean {
        return !(chosenImageUris.size != numImagesRequired || etGameName.text.isBlank() || etGameName.text.length < MIN_GAME_NAME_LENGTH)
    }
}