package com.example.cardiorisk
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrdersAdapter(var orders: MutableList<Order>) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderStatus: TextView = itemView.findViewById(R.id.orderStatus)
        val orderID: TextView = itemView.findViewById(R.id.orderID)
        val openOrderIcon: ImageView = itemView.findViewById(R.id.openOrderIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycle_item, parent, false)
        return OrderViewHolder(view)
    }
    fun addOrder(order: Order) {
        this.orders.add(order)
        notifyItemInserted(orders.size - 1)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        println("orderAdapter: ${orders.size}")
        holder.orderID.text = order.id
        holder.orderStatus.text = order.status

        holder.openOrderIcon.setOnClickListener {
            val intent = Intent(holder.itemView.context, GeneratedOrderActivity::class.java)
            intent.putExtra("orderID", order.id)
            intent.putExtra("orderAge", order.age)
            intent.putExtra("orderGender", order.gender)
            intent.putExtra("orderHDL", order.hdlCholesterol)
            intent.putExtra("orderTotal", order.totalCholesterol)
            intent.putExtra("orderSystolicBP", order.systolicBloodPressure)
            intent.putExtra("orderDiabetes", order.diabetes)
            intent.putExtra("orderSmoker", order.smoker)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = orders.size
}