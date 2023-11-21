package com.example.parkingfinder

import android.util.Patterns
import com.google.android.material.textfield.TextInputLayout

class ValidationUtils {

    companion object {
        fun validateEmail(ti: TextInputLayout): Boolean {

            val email = ti.editText?.text.toString()

            if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                ti.error = "Invalid email address"
                return false
            }

            ti.error = null
            return true
        }

        fun validatePassword(ti: TextInputLayout): Boolean {

            val pwd = ti.editText?.text.toString()

            if (pwd.isBlank()) {
                ti.error = "Password cannot be empty"
                return false
            }
            if (pwd.length < 5) {
                ti.error = "Password must be at least 5 characters long"
                return false
            }

            ti.error = null
            return true
        }

        fun checkConfirmation(first: TextInputLayout, second: TextInputLayout): Boolean {

            val pwd1 = first.editText?.text.toString()
            val pwd2 = second.editText?.text.toString()

            if (pwd1 != pwd2) {
                second.error = "Passwords do not match"
                return false
            }

            return true
        }

        fun validateName(ti: TextInputLayout): Boolean {

            val name = ti.editText?.text.toString()

            if (name.isBlank()) {
                ti.error = "Name cannot be empty"
                return false
            }
            if(name.length < 3) {
                ti.error = "Name is too short"
                return false
            }
            if(name.length > 30) {
                ti.error = "Name is too long"
                return false
            }

            return true
        }
    }
}