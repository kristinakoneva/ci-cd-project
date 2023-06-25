package infinumacademy.showsapp.kristinakoneva.showsscreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import infinumacademy.showsapp.kristinakoneva.R
import infinumacademy.showsapp.kristinakoneva.databinding.ViewShowItemBinding
import infinumacademy.showsapp.kristinakoneva.model.Show

class ShowsAdapter(
    private var items: List<Show>,
    private val onItemClickCallback: (Show) -> Unit
) : RecyclerView.Adapter<ShowsAdapter.ShowsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowsViewHolder {
        val binding = ViewShowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShowsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShowsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun addAllItems(shows: List<Show>) {
        items = shows
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    inner class ShowsViewHolder(private val binding: ViewShowItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Show) {
            binding.showName.text = item.title
            binding.showImage.load(item.imageUrl) {
                placeholder(R.drawable.show_image_placeholder)
                error(R.drawable.show_image_placeholder)
                R.drawable.show_image_placeholder
            }
            binding.showDescription.text = item.description
            binding.showCardContainer.setOnClickListener {
                onItemClickCallback(item)
            }
        }
    }
}