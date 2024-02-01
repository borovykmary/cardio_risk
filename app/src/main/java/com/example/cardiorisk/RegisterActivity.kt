package com.example.cardiorisk

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isOver18: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameEditText = findViewById<EditText>(R.id.nameRegister)
        val surnameEditText = findViewById<EditText>(R.id.surnameRegister)
        val emailEditText = findViewById<EditText>(R.id.emailRegister)
        val passwordEditText = findViewById<EditText>(R.id.passwordRegister)
        val repeatPasswordEditText = findViewById<EditText>(R.id.passwordRepeatRegister)
        val signUpButton = findViewById<Button>(R.id.SignUpButton)
        val editTextDate = findViewById<EditText>(R.id.editTextDate)

        editTextDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "${selectedDay}/${selectedMonth + 1}/$selectedYear"
                editTextDate.setText(selectedDate)

                isOver18 = isOver18(selectedYear, selectedMonth, selectedDay)
                if (!isOver18) {
                    Toast.makeText(this, "You must be over 18 to register.", Toast.LENGTH_SHORT).show()
                }
            }, year, month, day)

            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()
        }


        signUpButton.setOnClickListener {
            if (!isOver18) {
                Toast.makeText(this, "You must be over 18 to register.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val surname = surnameEditText.text.toString()
            val fullName = "$name $surname"
            val password = passwordEditText.text.toString()
            val repeatPassword = repeatPasswordEditText.text.toString()

            if (password == repeatPassword) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = hashMapOf(
                                "name" to fullName,
                                "email" to email
                            )

                            db.collection("users")
                                .document(auth.currentUser?.uid.toString())
                                .set(user)
                                .addOnSuccessListener {
                                    Toast.makeText(baseContext, "User registered successfully.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(baseContext, "Error adding document: $e", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(baseContext, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            }
        }
        val loginTextView = findViewById<TextView>(R.id.loginTextView)
        loginTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
    private fun isOver18(year: Int, month: Int, day: Int): Boolean {
        val dob = Calendar.getInstance()
        dob.set(year, month, day)

        val today = Calendar.getInstance()

        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return age >= 18
    }
}