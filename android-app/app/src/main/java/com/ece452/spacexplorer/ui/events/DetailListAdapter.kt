import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.ece452.spacexplorer.R

// custom data class for detail listView items
data class DetailItem(val mainText: String, val subText: String)

fun formatString(input: String): String {
    return input.replace("_", " ").replaceFirstChar { it.uppercase() }
}

class DetailListAdapter(context: Context, private val resource: Int, private val items: List<DetailItem>)
    : ArrayAdapter<DetailItem>(context, resource, items) {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // inflate the layout for each item, set the text, and return the inflated view
        val itemView = LayoutInflater.from(context).inflate(resource, parent, false)
        val item = items[position]

        val mainTextView = itemView.findViewById<TextView>(R.id.mainTextView)
        val subTextView = itemView.findViewById<TextView>(R.id.subTextView)

        mainTextView.text = formatString(item.subText)
        subTextView.text = formatString(item.mainText)

        return itemView
    }


}
