package com.example.shopperbeta.adapters

import android.content.DialogInterface
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.shopperbeta.R
import com.example.shopperbeta.models.ListElement
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.item_element.view.*


class ElementAdapter(options: FirestoreRecyclerOptions<ListElement>) :
    FirestoreRecyclerAdapter<ListElement, ElementAdapter.ListElementVH>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListElementVH {
        return ListElementVH(LayoutInflater.from(parent.context).inflate(R.layout.item_element,parent,false))
    }

    override fun onBindViewHolder(holder: ListElementVH, position: Int, model: ListElement) {
        holder.elementName.text = model.elementName
        holder.elementAuthor.text = model.elementAuthor

        // If element is pressed longer than normal then update dialog will pop up
        holder.itemView.setOnLongClickListener{
            // Configure Builder and show alertDialog
            var builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Update Element")
            var editName = EditText(holder.itemView.context)
            editName.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            editName.setText(model.elementName)
            editName.setSelection(editName.text.length)
            editName.hint = "Rename this list"
            builder.setView(editName)

            // Builder buttons config
            builder.setPositiveButton("Update", DialogInterface.OnClickListener{ dialog, which ->
                var editElementName = editName.text.toString()

                val updateList = hashMapOf(
                    "elementName" to editElementName,
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
        snapshots.getSnapshot(position).reference.delete()
    }

    class ListElementVH(itemView: View) : RecyclerView.ViewHolder(itemView)  {
        var elementName : TextView = itemView.itemLblElementName
        var elementAuthor: TextView = itemView.itemLblElementAuthor
    }
}