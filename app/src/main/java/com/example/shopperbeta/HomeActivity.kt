package com.example.shopperbeta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopperbeta.adapters.UserListAdapter
import com.example.shopperbeta.models.UserList
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val currentUserEmail: String = FirebaseAuth.getInstance().currentUser?.email.toString()
    private val collectionReference: CollectionReference = db.collection("userLists").document(currentUserEmail).collection("lists")
    private var currentUser = FirebaseAuth.getInstance().currentUser
    var userListAdapter : UserListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (currentUser != null) {
            if(currentUser!!.displayName.isNullOrEmpty()){
                // Configure Builder and show alertDialog
                val builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.builderSetDisplayNameText))
                val editName = EditText(this)
                editName.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
                editName.hint = getString(R.string.builderSetDisplayNameHint)
                builder.setView(editName)

                // Builder buttons config
                builder.setPositiveButton(getString(R.string.builderSetDisplayNameButtonText)) { _, _ ->
                    val setDisplayName = editName.text.toString()
                    val profileUpdates = userProfileChangeRequest {
                        displayName = setDisplayName
                    }

                    currentUser!!.updateProfile(profileUpdates)
                    lblHomeUsername.text = currentUser?.displayName.toString()

                }
                builder.setNegativeButton(R.string.builderNegativeButtonText) { dialog, _ -> dialog.cancel() }

                // Show builder
                val alertDialog = builder.create()
                alertDialog.show()
            }
        }

        buttonHomeLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val loginIntent = Intent(this, MainActivity::class.java)
            startActivity(loginIntent)
            finish()
        }

        lblHomeUsername.text = currentUser?.displayName.toString()

        configureRecyclerView()
        buttonHomeAddList.setOnClickListener {
            // Configure Builder and show alertDialog
            val builder = AlertDialog.Builder(this)

            builder.setTitle(getString(R.string.builderNewListText))

            val editName = EditText(this)
            editName.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            editName.hint = getString(R.string.builderAddEditViewHintText)
            builder.setView(editName)

            // Builder buttons config
            builder.setPositiveButton(getString(R.string.builderPositiveAddButtonText)
            ) { _, _ ->
                val editListName = editName.text.toString()


                val addList = hashMapOf(
                    "listid" to collectionReference.document().id,
                    "listName" to editListName
                )
                collectionReference.document(addList["listid"].toString()).set(addList)

            }
            builder.setNegativeButton(getString(R.string.builderNegativeButtonText)
            ) { dialog, _ -> dialog.cancel() }

            // Show builder
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    private fun configureRecyclerView(){
        val query: Query = collectionReference
        val firestoreRecyclerOptions : FirestoreRecyclerOptions<UserList> = FirestoreRecyclerOptions.Builder<UserList>().setQuery(query,UserList::class.java).build()

        userListAdapter = UserListAdapter(firestoreRecyclerOptions)

        userListRecycler.layoutManager = LinearLayoutManager(this)
        userListRecycler.adapter = userListAdapter

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                userListAdapter!!.deleteItem(viewHolder.bindingAdapterPosition)
            }

        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(userListRecycler)

    }

    override fun onStart() {
        super.onStart()
        userListAdapter!!.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        userListAdapter!!.stopListening()
    }
}