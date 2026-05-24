package hr.algebra.myapplication.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hr.algebra.myapplication.databinding.ItemCaseBinding
import hr.algebra.myapplication.models.CaseProfile

class CasesAdapter(
    private var cases: List<CaseProfile>,
    private val onClick: (CaseProfile) -> Unit,
) : RecyclerView.Adapter<CasesAdapter.CaseViewHolder>() {

    fun updateData(newCases: List<CaseProfile>) {
        cases = newCases
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaseViewHolder {
        val binding = ItemCaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CaseViewHolder, position: Int) {
        val c = cases[position]
        holder.binding.tvVehicleInfo.text = c.vehicleInfo
        holder.binding.tvStatus.text = c.status
        holder.binding.tvCreatedAt.text = c.createdAt
        holder.binding.root.setOnClickListener { onClick(c) }
    }

    override fun getItemCount(): Int = cases.size

    class CaseViewHolder(val binding: ItemCaseBinding) : RecyclerView.ViewHolder(binding.root)
}
