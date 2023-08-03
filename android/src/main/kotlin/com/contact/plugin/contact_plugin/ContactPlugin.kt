package com.contact.plugin.contact_plugin

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.ContactsContract
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import java.io.ByteArrayOutputStream
import java.io.IOException
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import io.flutter.plugin.common.MethodChannel.Result


/** ContactPluginAvadheshPlugin */
class ContactPlugin : FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware,
    ActivityResultListener {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var activity: Activity? = null
    private val logName = "PhoneNumberSuggestionPlugin"
    private val REQUEST_CONTACTS_PERMISSION = 112


    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "contact_plugin")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {

        if (call.method == "getContacts") {
            // Check if we have the contact permission
            val hasContactsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity!!.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
            } else {
                false
            }
            print(hasContactsPermission);

            if (!hasContactsPermission) {
                // We don't have the permission, so request it
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity!!.requestPermissions(
                        arrayOf(Manifest.permission.READ_CONTACTS),
                        REQUEST_CONTACTS_PERMISSION
                    )
                }
            } else {
                // The user granted the permission, so call the method channel
                val batteryLevel = getContactNumbers();
                result.success(batteryLevel)
            }
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity as FlutterActivity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {

    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity as FlutterActivity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivity() {
        activity = null

    }

    @SuppressLint("LongLogTag")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        Log.d(logName, "onActivityResult: requestCode: $requestCode")
        when (requestCode) {
            REQUEST_CONTACTS_PERMISSION -> {
                val hasContactsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity!!.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

                } else {
                    false
                }

                if (hasContactsPermission) {
                    Log.d(logName, "Not onActivity")
                    getContactNumbers();
                }else {
                    Log.d(logName, "onActivity")

                }
            }
        }
        return true
    }

    private fun getContactNumbers(): ArrayList<HashMap<Any, Any>> {
        val contactsNumberMap = ArrayList<HashMap<Any, Any>>()


        val phoneCursor: Cursor? = activity!!.applicationContext.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        if (phoneCursor != null && phoneCursor.count > 0) {
            val numberIndex =
                phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val nameIndex =
                phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val contactIndex =
                phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val photoIndex =
                phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (phoneCursor.moveToNext()) {
                val number: String = phoneCursor.getString(numberIndex)
                val name: String = phoneCursor.getString(nameIndex)
                val contactID: Long = phoneCursor.getLong(contactIndex)

                val hashMap: HashMap<Any, Any> = HashMap()
                hashMap["name"] = name
                hashMap["phones"] = number
                var photo: Bitmap? = null
                try {

                    if (phoneCursor.getString(photoIndex) != null) {
                        val inputStream = ContactsContract.Contacts.openContactPhotoInputStream(
                            activity!!.contentResolver,
                            ContentUris.withAppendedId(
                                ContactsContract.Contacts.CONTENT_URI,
                                contactID
                            )
                        )
                        println(
                            ContentUris.withAppendedId(
                                ContactsContract.Contacts.CONTENT_URI,
                                contactID
                            )
                        )
                        if (inputStream != null) {
                            photo = BitmapFactory.decodeStream(inputStream)
                            val outputStream = ByteArrayOutputStream()
                            photo.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

                            val byteArray = outputStream.toByteArray()
                            hashMap["photo"] = byteArray
                        }
                        assert(inputStream != null)
                        inputStream!!.close()
                    } else {
                        val outputStream = ByteArrayOutputStream()
                        val byteArray = outputStream.toByteArray()
                        hashMap["photo"] = byteArray
                    }


                } catch (e: IOException) {
                    e.printStackTrace()
                }

                if (!contactsNumberMap.contains(hashMap)) {
                    contactsNumberMap.add(hashMap);
                }
            }
            //contact contains all the number of a particular contact
            phoneCursor.close()
        }
        print(contactsNumberMap.size);
        return contactsNumberMap
    }

}
