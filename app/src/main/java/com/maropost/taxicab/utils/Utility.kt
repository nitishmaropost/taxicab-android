package com.maropost.taxicab.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import android.content.DialogInterface
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.Log
import android.view.Window
import java.io.File
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Environment
import android.webkit.MimeTypeMap
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import android.media.ExifInterface
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.maropost.taxicab.application.MyApplication


class Utility {

    var alertDialog : AlertDialog ?= null

    companion object {
        private var instance: Utility? = null
        fun getInstance() : Utility {
            if (instance == null)
                instance = Utility()
            return instance as Utility
        }
    }

    enum class MediaFileType (val stringValue: String) {
        IMAGE("IMAGE"),
        VIDEO("VIDEO");
        override fun toString(): String {
            return stringValue
        }
    }

    /**
     * Standard toast method
     */
    fun showToast(context: Context, message: String) {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }

    /**
     * Standard method to display a prompt
     */
    fun showAlertDialog(context: Context,
                        title: String,
                        message: String,
                        positiveButtonText: String,
                        negativeButtonText: String,
                        isCancellable:Boolean,
                        onPositiveClickEvent: DialogInterface.OnClickListener?,
                        onNegativeClickEvent: DialogInterface.OnClickListener?) {
        try {
            if(alertDialog == null) {
                val alertBuilder = AlertDialog.Builder(context)
                alertBuilder.setMessage(message)
                if(onPositiveClickEvent != null && !TextUtils.isEmpty(positiveButtonText))
                    alertBuilder.setPositiveButton(positiveButtonText, onPositiveClickEvent)
                if(onNegativeClickEvent != null && !TextUtils.isEmpty(negativeButtonText))
                    alertBuilder.setNegativeButton(negativeButtonText, onNegativeClickEvent)
                alertBuilder.setCancelable(isCancellable)
                alertDialog = alertBuilder.create()
                alertDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
                alertDialog!!.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(alertDialog != null && alertDialog!!.isShowing)
                alertDialog!!.dismiss()
            alertDialog = null
        }
    }

    /**
     * Dismiss the alert if opened
     */
    fun dismissAlertDialog(){
        if(alertDialog != null){
            alertDialog!!.dismiss()
            alertDialog = null
        }
    }

    /**
     * Common method to print logs throughout the application
     */
    fun printLog(tag: String, message: String, extraData: Any){
        Log.d(tag, "$message : $extraData")
    }

    /**
     * Get file mimetype from file extension
     */
    fun getFileMimeType(filePath: String): String? {
        // Get file extension from file path
        val fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1)
        // Get file mime type from extension
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension) ?: return null
        return mimeType
    }

    /**
     * Compress image file picked from gallery
     */
    fun compressBitmap(file: File) : File{
        var compressedFile: File ?= null
        try {
            val fileInputStream = FileInputStream(file)
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.RGB_565; // Less memory intensive color format
            var mBitmap = BitmapFactory.decodeStream(fileInputStream, null, options )
            // check if image from gallery is rotated by some angle
            mBitmap = rotateImageBy90Degree(file.absolutePath,mBitmap)
            val byteArrayOutputStream = ByteArrayOutputStream()
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val mByteArray = byteArrayOutputStream.toByteArray()
            compressedFile = createFileInDirectory(MediaFileType.IMAGE)
            val outStream = FileOutputStream(compressedFile)
            outStream.write(mByteArray)
            outStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return compressedFile!!
    }

    /**
     * Create a directory in file storage with app name
     */
    private fun createFileInDirectory(mediaFileType: MediaFileType):File{
        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Constants.DIRECTORY_NAME)
        val timestamp = System.currentTimeMillis()
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs()
        }
        var mediaFile: File? = null
        when (mediaFileType) {
            MediaFileType.IMAGE -> {
                // Create sub directory under app name folder i.e. Pictures -> Your app name -> media -> images
                var mediaInnerStoragePath = createInnerDirectory(MediaFileType.IMAGE)
                mediaFile = File(mediaInnerStoragePath.path + File.separator + "IMG_" + timestamp + ".jpg")
            }
            MediaFileType.VIDEO -> {
                // Create sub directory under app name folder i.e. Pictures -> Your app name -> media -> videos
                var mediaInnerStoragePath = createInnerDirectory(MediaFileType.VIDEO)
                mediaFile = File(mediaInnerStoragePath.path + File.separator + "VID_" + timestamp + ".mp4")
            }
        }
        return mediaFile
    }

    /**
     * Create inner directory under app name folder present in picture folder
     * i.e. Pictures -> Your app name -> media -> images or videos
     */
    private fun createInnerDirectory(mediaFileType: MediaFileType) : File{
        var mediaInnerStorageDir: File ?=null
        when(mediaFileType){
            MediaFileType.IMAGE -> {
                mediaInnerStorageDir = File(Environment.getExternalStoragePublicDirectory(Constants.DIRECTORY_NAME), Constants.DIRECTORY_MEDIA + File.pathSeparator + Constants.IMAGE_DIRECTORY_NAME)
            }
            MediaFileType.VIDEO -> {
                mediaInnerStorageDir = File(Environment.getExternalStoragePublicDirectory(Constants.DIRECTORY_NAME), Constants.DIRECTORY_MEDIA + File.pathSeparator + Constants.VIDEO_DIRECTORY_NAME)
            }
        }
        // Create the storage directory if it does not exist
        if (!mediaInnerStorageDir.exists()) {
            mediaInnerStorageDir.mkdirs()
        }
        return mediaInnerStorageDir
    }

    /**
     * Check if file is rotated by some angle, then re correct its orientation
     */
    private fun rotateImageBy90Degree(filePath: String,bitmap: Bitmap):Bitmap {
        var scaledBitmap: Bitmap ?= null
        var matrixBitmap: Bitmap ?= null
        try {
            val exifInterface = ExifInterface(filePath)
            val exifOrientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL)
            var rotate = 0
            when (exifOrientation) {
                ExifInterface.ORIENTATION_ROTATE_90  -> rotate = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
            }
            if (rotate != 0) {
                val width = bitmap.getWidth()
                val height = bitmap.getHeight()
                // Setting pre rotate
                val matrix = Matrix();
                matrix.preRotate(rotate.toFloat())
                // Rotating Bitmap & convert to ARGB_8888
                matrixBitmap = Bitmap.createBitmap(bitmap, 0, 0,width, height, matrix, true);
            }
            if(matrixBitmap != null)
                scaledBitmap = matrixBitmap.copy(Bitmap.Config.ARGB_8888, true);
            else scaledBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return scaledBitmap!!
    }

    fun hideKeyboard(editText: EditText) {
        val imm = MyApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    fun showKeyboard(editText: EditText) {
        val imm = MyApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

}