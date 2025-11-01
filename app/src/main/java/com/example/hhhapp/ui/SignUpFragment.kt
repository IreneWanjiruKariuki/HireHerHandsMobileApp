package com.example.hhhapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.hhhapp.R

class SignUpFragment : Fragment(R.layout.fragment_signup) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val signupButton = view.findViewById<Button>(R.id.signupBtn)
        val goToLoginButton = view.findViewById<Button>(R.id.goToLoginBtn)

        signupButton.setOnClickListener {

        }

        goToLoginButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LoginFragment())
                .commit()
        }
    }
}
