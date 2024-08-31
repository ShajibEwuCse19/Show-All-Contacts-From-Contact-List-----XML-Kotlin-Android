package com.shajib.chooseandselectcontract

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shajib.chooseandselectcontract.databinding.ActivityMainBinding
import java.util.Objects

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val REQUEST_CODE = 1
    private val REQUEST_CODE_CONTACT = 2
    private lateinit var contactInformationAdapter: ContactInformationAdapter
    private var myContactList = ArrayList<Pair<String, String>>()

    private var isLoading = false
    private var PAGE_SIZE = 20
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up click listener for the choose contact button [step-1]
        binding.btnChooseContact.setOnClickListener {
            // Check for permission to access contacts
            checkContactPermission()
        }
        initRecyclerView()
    }

    private fun initRecyclerView() {
        contactInformationAdapter = ContactInformationAdapter(myContactList)

        binding.rvContact.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = contactInformationAdapter
            setHasFixedSize(true)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    if(!isLoading && totalItemCount <= lastVisibleItem + 5) {
                        loadMoreContacts()
                    }
                }
            })
        }
        //contactInformationAdapter.notifyDataSetChanged()
        loadMoreContacts()
    }

    private fun loadMoreContacts() {
        isLoading = true

        Thread {
            val newContact = getPaginatedContacts(currentPage)
            runOnUiThread {
                contactInformationAdapter.addContacts(newContact)
                isLoading = false
                currentPage++
            }
        }.start()
    }

    // Get paginated contacts [step-6]
    @SuppressLint("Range")
    private fun getPaginatedContacts(page: Int): List<Pair<String, String>> {
        val contentResolver = contentResolver
        val contactUri = ContactsContract.Contacts.CONTENT_URI
        val offset = page * PAGE_SIZE

        val contactCursor = contentResolver.query(
            contactUri,
            null,
            null,
            null,
            "${ContactsContract.Contacts._ID} LIMIT $PAGE_SIZE OFFSET $offset"
        )

        if (contactCursor != null && contactCursor.moveToFirst()) {
            do {
                val contactId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID))
                val numberCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                    arrayOf(contactId),
                    null
                )

                if (numberCursor != null && numberCursor.moveToFirst()) {
                    do {
                        val name = numberCursor.getString(
                            numberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        )
                        val number = numberCursor.getString(
                            numberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        )
                        myContactList.add(Pair(first = name, second = number))
                    } while (numberCursor.moveToNext())
                }
                numberCursor?.close()

            } while (contactCursor.moveToNext())
        }
        contactCursor?.close()

        return myContactList
    }


    // Check for permission to access contacts [step-2]
    private fun checkContactPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS),
                REQUEST_CODE
            )
        } else {
            chooseContact()
        }
    }

    // Handle the result of the permission request [step-3]
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            chooseContact()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    // Handle the result of the contact picker activity [step-5]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_CONTACT && resultCode == RESULT_OK) {
            loadMoreContacts()
        } else {
            Toast.makeText(this, "No Contact Selected", Toast.LENGTH_SHORT).show()
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Launch the contact picker [step-4]
    private fun chooseContact() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE)
        startActivityForResult(intent, REQUEST_CODE_CONTACT)
    }
}