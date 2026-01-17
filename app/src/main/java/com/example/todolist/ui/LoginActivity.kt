package com.example.todolist.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.todolist.TodoApplication
import com.example.todolist.databinding.ActivityLoginBinding
import com.example.todolist.viewmodel.LoginViewModel
import com.example.todolist.viewmodel.LoginViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViewModel()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupViewModel() {
        val app = application as TodoApplication
        viewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(app.userRepository)
        )[LoginViewModel::class.java]
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            if (isRegisterMode) {
                registerUser()
            } else {
                loginUser()
            }
        }

        binding.btnRegister.setOnClickListener {
            toggleMode()
        }
    }

    private fun toggleMode() {
        isRegisterMode = !isRegisterMode

        if (isRegisterMode) {
            binding.btnLogin.text = "Create Account"
            binding.btnRegister.text = "Back to Login"
            binding.tilEmail.visibility = View.VISIBLE
        } else {
            binding.btnLogin.text = "Login"
            binding.btnRegister.text = "Create Account"
            binding.tilEmail.visibility = View.GONE
        }
    }

    private fun loginUser() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        viewModel.login(username, password)
    }

    private fun registerUser() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        viewModel.register(username, password, email)
    }

    private fun observeViewModel() {
        viewModel.currentUser.observe(this) { user ->
            if (user != null) {
                navigateToMain()
            }
        }

        viewModel.loginResult.observe(this) { result ->
            result.onSuccess {
                navigateToMain()
            }.onFailure { error ->
                Toast.makeText(this, error.message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.registerResult.observe(this) { result ->
            result.onSuccess {
                navigateToMain()
            }.onFailure { error ->
                Toast.makeText(this, error.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
            binding.btnRegister.isEnabled = !isLoading
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

