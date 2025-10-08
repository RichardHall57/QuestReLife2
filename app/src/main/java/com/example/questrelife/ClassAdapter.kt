import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.questrelife.ClassItem

class ClassAdapter(
    private val onItemClick: (ClassItem) -> Unit
) : RecyclerView.Adapter<ClassAdapter.ClassViewHolder>() {

    private val classList = mutableListOf<ClassItem>()

    fun submitList(list: List<ClassItem>) {
        classList.clear()
        classList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val classItem = classList[position]
        holder.nameView.text = classItem.name
        holder.teacherView.text = "Teacher: ${classItem.teacher}"
        holder.itemView.setOnClickListener {
            onItemClick(classItem)
        }
    }

    override fun getItemCount(): Int = classList.size

    class ClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameView: TextView = itemView.findViewById(android.R.id.text1)
        val teacherView: TextView = itemView.findViewById(android.R.id.text2)
    }
}
