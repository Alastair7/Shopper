package com.example.shopperbeta.adapters

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.shopperbeta.ElementsActivity
import com.example.shopperbeta.R
import com.example.shopperbeta.models.UserList
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.item_userlist.view.*

class UserListAdapter(options: FirestoreRecyclerOptions<UserList>) :
    FirestoreRecyclerAdapter<UserList, UserListAdapter.UserListVH>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_userlist,parent,false)

        return UserListVH(itemView)
    }

    override fun onBindViewHolder(holder: UserListVH, position: Int, model: UserList) {
        holder.listName.text = model.listName
        holder.itemView.setOnClickListener {
            var userListid:String = snapshots[position].listid
            var userListname: String = snapshots[position].listName
            val itemIntent : Intent = Intent(holder.itemView.context, ElementsActivity::class.java).apply {
                putExtra("listid", userListid)
                putExtra("listName", userListname)
            }

            holder.itemView.context.startActivity(itemIntent)
        }
        // If element is pressed longer than normal then update dialog will pop up
        holder.itemView.setOnLongClickListener{
            // Configure Builder and show alertDialog
            var builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("New List")
            var editName = EditText(holder.itemView.context)
            editName.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            editName.setText(model.listName)
            editName.setSelection(editName.text.length)
            editName.hint = "Rename this list"
            builder.setView(editName)

            // Builder buttons config
            builder.setPositiveButton("Update", DialogInterface.OnClickListener{ dialog, which ->
                var editListName = editName.text.toString()
                val currentUser: String = FirebaseAuth.getInstance().currentUser?.email.toString()
                //val collectionReference = FirebaseFirestore.getInstance().collection("userLists").document(currentUser).collection("lists").document(model.listid)
                val updateList = hashMapOf(
                    "listName" to editListName,
                )

                val collectionReference = snapshots.getSnapshot(holder.bindingAdapterPosition).reference
                collectionReference.update(updateList as Map<String, Any>)

            })
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener{ dialog, which -> dialog.cancel() })

            // Show builder
            var alertDialog = builder.create()
            alertDialog.show()
            true
        }

    }

    fun deleteItem(position: Int){
        // Get list ID and elementList reference then iterate through it elements collection and delete every item, after that delete the document
        var listid: String = snapshots.getSnapshot(position).id
        var elementListRef: DocumentReference = FirebaseFirestore.getInstance().collection("listElements").document(listid)
        elementListRef.collection("elements").get().addOnSuccessListener {elements ->
            for(element in elements){
                element.reference.delete()
            }
        }
        elementListRef.delete()

        // Delete item
        snapshots.getSnapshot(position).reference.delete()
    }

    class UserListVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var listName: TextView = itemView.lblUserListItemName

    }
}