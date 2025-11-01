package com.example.hhhapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.hhhapp.R

class LoginFragment: Fragment(R.layout.fragment_login) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginButton = view.findViewById<Button>(R.id.loginBtn)
        val signupButton = view.findViewById<Button>(R.id.goToSignupBtn)

        loginButton.setOnClickListener {
            // later: check user login
        }

        signupButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SignUpFragment())
                .commit()
        }
    }
}