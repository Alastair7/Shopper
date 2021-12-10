package com.example.shopperbeta.adapters

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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
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
            val userListid:String = snapshots[position].listid
            val userListname: String = snapshots[position].listName
            val itemIntent : Intent = Intent(holder.itemView.context, ElementsActivity::class.java).apply {
                putExtra("listid", userListid)
                putExtra("listName", userListname)
            }

            holder.itemView.context.startActivity(itemIntent)
        }
        // If element is pressed longer than normal then update dialog will pop up
        holder.itemView.setOnLongClickListener{
            // Configure Builder and show alertDialog
            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle(holder.itemView.context.getString(R.string.builderUpdateListText))
            val editName = EditText(holder.itemView.context)
            editName.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            editName.setText(model.listName)
            editName.setSelection(editName.text.length)
            editName.hint = holder.itemView.context.getString(R.string.builderUpdateEditViewHintText)
            builder.setView(editName)

            // Builder buttons config
            builder.setPositiveButton(holder.itemView.context.getString(R.string.builderPositiveUpdateButtonText)
            ) { _, _ ->
                val editListName = editName.text.toString()
                //val currentUser: String = FirebaseAuth.getInstance().currentUser?.email.toString()
                val updateList = hashMapOf(
                    "listName" to editListName,
                )

                val collectionReference =
                    snapshots.getSnapshot(holder.bindingAdapterPosition).reference
                collectionReference.update(updateList as Map<String, Any>)

            }
            builder.setNegativeButton(holder.itemView.context.getString(R.string.builderNegativeButtonText)
            ) { dialog, _ -> dialog.cancel() }

            // Show builder
            val alertDialog = builder.create()
            alertDialog.show()
            true
        }

    }

    fun deleteItem(position: Int){
        // Get list ID and elementList reference then iterate through it elements collection and delete every item, after that delete the document
        val listid: String = snapshots.getSnapshot(position).id
        val elementListRef: DocumentReference = FirebaseFirestore.getInstance().collection("listElements").document(listid)
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