package com.example.cardiorisk

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var ordersAdapter: OrdersAdapter

    private lateinit var createOrderButton: ImageView
    private lateinit var dialog: Dialog
    private lateinit var avatarImageView: ImageView

    private lateinit var originalOrdersList: MutableList<Order>
    private var isFadeInAnimationShown = false
    private var isLayoutAnimationShown = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val ordersList = mutableListOf<Order>()

        val recyclerView: RecyclerView = findViewById(R.id.recycleviewOrders)
        recyclerView.layoutManager = LinearLayoutManager(this)
        ordersAdapter = OrdersAdapter(ordersList)
        recyclerView.adapter = ordersAdapter

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)

        val spacesItemDecoration = SpacesItemDecoration(12)
        recyclerView.addItemDecoration(spacesItemDecoration)

        val welcomeText = findViewById<TextView>(R.id.welcomeText)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.getString("name")
                        welcomeText.text = "Welcome, $name!"
                        welcomeText.startAnimation(fadeInAnimation)
                    }
                }
        }
        createOrderButton = findViewById(R.id.createOrder)
        createOrderButton.setOnClickListener {
            showDialog()
        }
        avatarImageView = findViewById(R.id.avatarImageView)

        fetchOrdersFromFirebase()

        originalOrdersList = mutableListOf()
        originalOrdersList.addAll(ordersList)

        val searchView: SearchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    ordersAdapter.orders = originalOrdersList.toMutableList()
                } else {
                    val filteredList = originalOrdersList.filter { order ->
                        order.id.contains(newText, ignoreCase = true)
                    }
                    ordersAdapter.orders = filteredList.toMutableList()
                }
                ordersAdapter.notifyDataSetChanged()
                return false
            }
        })
        val logoutButton = findViewById<TextView>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.left = space
            outRect.right = space
            outRect.bottom = space

            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = space
            }
        }
    }

    private fun saveOrderToFirebase(order: Order) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("orders")
                .add(order)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                    fetchOrdersFromFirebase()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
        }
    }

    private fun fetchOrdersFromFirebase() {
        val userId = auth.currentUser?.uid
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        if (userId != null) {
            db.collection("users").document(userId).collection("orders")
                .get()
                .addOnSuccessListener { documents ->
                    ordersAdapter.orders.clear()
                    for (document in documents) {
                        val order = Order(
                            id = document.getString("id") ?: "",
                            status = document.getString("status") ?: "",
                            age = document.getString("age") ?: "",
                            gender = document.getString("gender") ?: "",
                            hdlCholesterol = document.getString("hdlCholesterol") ?: "",
                            totalCholesterol = document.getString("totalCholesterol") ?: "",
                            systolicBloodPressure = document.getString("systolicBloodPressure") ?: "",
                            smoker = document.getString("smoker") ?: "",
                            diabetes = document.getString("diabetes") ?: ""
                        )
                        ordersAdapter.addOrder(order)
                        originalOrdersList.add(order)
                        if (!isFadeInAnimationShown) {
                            findViewById<RecyclerView>(R.id.recycleviewOrders).startAnimation(fadeInAnimation)
                            isFadeInAnimationShown = true
                        }
                    }

                    ordersAdapter.notifyDataSetChanged()
                    findViewById<RecyclerView>(R.id.recycleviewOrders).scheduleLayoutAnimation()
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        }
    }

    private fun showDialog() {
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.order_creation)

        val submitOrderButton = dialog.findViewById<Button>(R.id.submitOrderButton)
        val genderSpinner: Spinner = dialog.findViewById(R.id.genderSpinner)
        val smokerSpinner: Spinner = dialog.findViewById(R.id.smokerInput)
        val diabetesSpinner: Spinner = dialog.findViewById(R.id.diabetesInput)
        val ageOfPatient: EditText = dialog.findViewById(R.id.AgePatient)
        val hdlCholesterol: EditText = dialog.findViewById(R.id.hdlCholesterolInput)
        val totalCholesterol: EditText = dialog.findViewById(R.id.totalCholesterolInput)
        val systolicBloodPressure: EditText = dialog.findViewById(R.id.systolicBPInput)

        ArrayAdapter.createFromResource(
            this,
            R.array.gender,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            genderSpinner.adapter = adapter
        }
        ArrayAdapter.createFromResource(
            this,
            R.array.smoker,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            smokerSpinner.adapter = adapter
        }
        ArrayAdapter.createFromResource(
            this,
            R.array.diabetes,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            diabetesSpinner.adapter = adapter
        }

        val ageField: ImageView = dialog.findViewById(R.id.ageField)
        val hdlCholesterolField: ImageView = dialog.findViewById(R.id.hdlCholField)
        val totalCholesterolField: ImageView = dialog.findViewById(R.id.totalCholField)
        val systolicBloodPressureField: ImageView = dialog.findViewById(R.id.systolicBPField)

        submitOrderButton.setOnClickListener {
            val agePatient = ageOfPatient.text.toString()
            val agePatientValue = ageOfPatient.text.toString().toIntOrNull()
            val selectedGender = genderSpinner.selectedItem.toString()
            val hdlCholesterol = hdlCholesterol.text.toString()
            val smoker = smokerSpinner.selectedItem.toString()
            val diabetes = diabetesSpinner.selectedItem.toString()
            val totalCholesterol = totalCholesterol.text.toString()
            val systolicBloodPressure = systolicBloodPressure.text.toString()
            val hdlCholesterolValue = hdlCholesterol.toDoubleOrNull()
            val totalCholesterolValue = totalCholesterol.toDoubleOrNull()
            val systolicBloodPressureValue = systolicBloodPressure.toDoubleOrNull()

            var isValid = true

            if (agePatient.isEmpty()) {
                ageField.setImageResource(R.drawable.field_size1_error)
                Toast.makeText(
                    applicationContext, "Please enter age",
                    Toast.LENGTH_SHORT
                ).show()
                isValid = false
            }

            if (hdlCholesterol.isEmpty()) {
                hdlCholesterolField.setImageResource(R.drawable.field_size3_error)
                Toast.makeText(
                    applicationContext, "Please enter HDL Cholesterol",
                    Toast.LENGTH_SHORT
                ).show()
                isValid = false
            }

            if (hdlCholesterolValue != null) {
                if (hdlCholesterolValue < 0 || hdlCholesterolValue > 3) {
                    hdlCholesterolField.setImageResource(R.drawable.field_size3_error)
                    Toast.makeText(
                        applicationContext, "HDL Cholesterol must be between 0 and 3 mmol/L",
                        Toast.LENGTH_SHORT
                    ).show()
                    isValid = false
                }
            }

            if (totalCholesterol.isEmpty()) {
                totalCholesterolField.setImageResource(R.drawable.field_size3_error)
                Toast.makeText(
                    applicationContext, "Please enter Total Cholesterol",
                    Toast.LENGTH_SHORT
                ).show()
                isValid = false
            }

            if (totalCholesterolValue != null) {
                if (totalCholesterolValue < 2.5 || totalCholesterolValue > 7) {
                    totalCholesterolField.setImageResource(R.drawable.field_size3_error)
                    Toast.makeText(
                        applicationContext, "HDL Cholesterol must be between 2.5 and 7 mmol/L",
                        Toast.LENGTH_SHORT
                    ).show()
                    isValid = false
                }
            }

            if (systolicBloodPressure.isEmpty()) {
                systolicBloodPressureField.setImageResource(R.drawable.field_size3_error)
                Toast.makeText(
                    applicationContext, "Please enter Systolic Blood Pressure",
                    Toast.LENGTH_SHORT
                ).show()
                isValid = false
            }

            if (systolicBloodPressureValue != null) {
                if (systolicBloodPressureValue < 80 || systolicBloodPressureValue > 300) {
                    systolicBloodPressureField.setImageResource(R.drawable.field_size3_error)
                    Toast.makeText(
                        applicationContext, "Systolic Blood Pressure must be between 80 and 300 mmHg",
                        Toast.LENGTH_SHORT
                    ).show()
                    isValid = false
                }
            }

            if (isValid) {
                val allowedChars = ('a'..'z') + ('0'..'9')
                val uniqueId = (1..3)
                    .map { allowedChars.random() }
                    .joinToString("")
                val orderNumber = "#$uniqueId"
                val status = "status: $selectedGender"
                val newOrder = Order(
                    orderNumber,
                    status,
                    agePatient,
                    selectedGender,
                    hdlCholesterol,
                    totalCholesterol,
                    systolicBloodPressure,
                    smoker,
                    diabetes
                )
                saveOrderToFirebase(newOrder)
                ordersAdapter.addOrder(newOrder)
                dialog.dismiss()
            }

            }
        val closeWindowButton = dialog.findViewById<TextView>(R.id.closeWindow)
        closeWindowButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.99).toInt(),
            (resources.displayMetrics.heightPixels * 0.70).toInt()
        )
    }
}
