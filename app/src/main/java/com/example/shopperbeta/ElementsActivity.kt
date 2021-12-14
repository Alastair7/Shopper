package com.example.shopperbeta


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopperbeta.adapters.ElementAdapter
import com.example.shopperbeta.models.ListElement
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_elements.*

class ElementsActivity : AppCompatActivity() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var elementAdapter : ElementAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elements)
        enableBackButton()

        val bundle: Bundle? = intent.extras
        val listid = bundle?.getString("listid").toString()
        val listName = bundle?.getString("listName").toString()

        lblElementListName.text = listName

        val collectionReference: CollectionReference = db.collection("listElements").document(listid).collection("elements")
        buttonElementShare.setOnClickListener{
            // Configure Builder and show alertDialog
            val builder = AlertDialog.Builder(this)

            builder.setTitle(getString(R.string.builderShareListText))

            val editEmail = EditText(this)
            editEmail.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            editEmail.hint = getString(R.string.builderShareEditHintText)
            builder.setView(editEmail)

            // Builder buttons config
            builder.setPositiveButton(getString(R.string.builderPositiveShareButtonText)) { _, _ ->
                val editListEmail = editEmail.text.toString()
                shareList(editListEmail)

            }
            builder.setNegativeButton(getString(R.string.builderNegativeButtonText)) { dialog, _ -> dialog.cancel() }

            // Show builder
            val alertDialog = builder.create()
            alertDialog.show()
        }



        configureRecyclerView()

        buttonElementAdd.setOnClickListener {
            // Configure Builder and show alertDialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.builderNewElementText))
            val editName = EditText(this)
            editName.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            editName.hint = getString(R.string.buildNewElementHintText)
            builder.setView(editName)

            // Builder buttons config
            builder.setPositiveButton(getString(R.string.builderAddElementPositiveButtonText)
            ) { _, _ ->
                val elementName = editName.text.toString()
                val addElement = hashMapOf(
                    "elementName" to elementName,
                    "elementAuthor" to FirebaseAuth.getInstance().currentUser?.displayName.toString(),
                    "elementid" to collectionReference.document().id
                )
                collectionReference.document(addElement["elementid"].toString()).set(addElement)

            }
            builder.setNegativeButton(getString(R.string.builderNegativeButtonText)
            ) { dialog, _ -> dialog.cancel() }

            // Show builder
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    private fun configureRecyclerView(){
        val bundle: Bundle? = intent.extras
        val listid = bundle?.getString("listid").toString()

        val collectionReference: CollectionReference = db.collection("listElements").document(listid).collection("elements")
        val query: Query = collectionReference
        val firestoreRecyclerOptions : FirestoreRecyclerOptions<ListElement> = FirestoreRecyclerOptions.Builder<ListElement>().setQuery(query,
            ListElement::class.java).build()

        elementAdapter = ElementAdapter(firestoreRecyclerOptions)

       recyclerElementView.layoutManager = LinearLayoutManager(this)
        recyclerElementView.isNestedScrollingEnabled = false
        recyclerElementView.adapter = elementAdapter

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                elementAdapter!!.deleteItem(viewHolder.bindingAdapterPosition)
            }

        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerElementView)
    }

    private fun shareList(userEmail: String){
        val bundle: Bundle? = intent.extras
        val listid = bundle?.getString("listid").toString()
        val listName = bundle?.getString("listName").toString()

        // Check if user exists
        if(userEmail.isNotEmpty()) {
            db.collection("users").document(userEmail).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val shareList = hashMapOf(
                        "listid" to listid,
                        "listName" to listName
                    )
                    // Share List
                    db.collection("userLists").document(userEmail).collection("lists")
                        .document(listid).set(shareList)
                    Toast.makeText(this, getString(R.string.toastShareSuccessShared), Toast.LENGTH_SHORT).show()

                } else {
                    // user does not exist
                    Toast.makeText(this, getString(R.string.toastShareUserNotFound), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }else {
            Toast.makeText(this, getString(R.string.toastShareEmptyEmail), Toast.LENGTH_SHORT)
                .show()

        }

    }

    private fun enableBackButton(){
        val actionBar = supportActionBar
        actionBar!!.title = getString(R.string.app_name)
        actionBar.setDisplayHomeAsUpEnabled(true)

    }

    override fun onStart() {
        super.onStart()
        elementAdapter?.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        elementAdapter?.stopListening()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}