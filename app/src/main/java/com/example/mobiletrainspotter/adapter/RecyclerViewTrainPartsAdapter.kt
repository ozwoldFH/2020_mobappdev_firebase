package com.example.mobiletrainspotter.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiletrainspotter.R
import com.example.mobiletrainspotter.models.TrainPart

class RecyclerViewTrainPartsAdapter(private val parts: ArrayList<TrainPart>, private val isReadonly: Boolean = false) :
    RecyclerView.Adapter<RecyclerViewTrainPartsAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val model: TextView = item.findViewById(R.id.editTextModel)
        val no: TextView = item.findViewById(R.id.editTextNo)
        val delete: ImageButton = item.findViewById(R.id.buttonDeletePart)

        var part: TrainPart = TrainPart()
            get() = field
            set(value) {
                field = value
                model.setText(value.model)
                no.setText(value.no)
            }

        init {
            model.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    part.model = model.text.toString()
                }
            })

            no.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    part.no = no.text.toString()
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.train_part_cardview_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return parts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.part = parts[position]

        if (isReadonly) {
            holder.delete.visibility = View.GONE
            holder.model.isEnabled = false
            holder.no.isEnabled = false
        } else {
            holder.delete.setOnClickListener { _ ->
                if (parts.size == 1) {
                    // A train without a part does not make sense, so replace the only part left with a new part
                    parts[0] = TrainPart("", "")
                    notifyItemChanged(0)
                } else {
                    parts.removeAt(holder.adapterPosition)
                    notifyItemRemoved(holder.adapterPosition)
                }
            }
        }

    }
}
