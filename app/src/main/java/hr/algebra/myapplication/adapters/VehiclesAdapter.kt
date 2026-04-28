package hr.algebra.myapplication.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hr.algebra.myapplication.databinding.ItemVehicleBinding
import hr.algebra.myapplication.models.VehicleProfile

class VehiclesAdapter(
    private var vehicles: List<VehicleProfile>,
    private val onUpdateClick: (VehicleProfile) -> Unit,
    private val onDeleteClick: (VehicleProfile) -> Unit
) : RecyclerView.Adapter<VehiclesAdapter.VehicleViewHolder>() {

    fun updateData(newVehicles: List<VehicleProfile>) {
        vehicles = newVehicles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val binding = ItemVehicleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehicleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val v = vehicles[position]
        holder.binding.tvBrandModel.text = "${v.brand} ${v.model}"
        holder.binding.tvPlate.text = v.registrationPlate

        holder.binding.btnUpdate.setOnClickListener { onUpdateClick(v) }
        holder.binding.btnDelete.setOnClickListener { onDeleteClick(v) }
    }

    override fun getItemCount(): Int = vehicles.size

    class VehicleViewHolder(val binding: ItemVehicleBinding) : RecyclerView.ViewHolder(binding.root)
}