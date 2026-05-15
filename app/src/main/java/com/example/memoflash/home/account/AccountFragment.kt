package com.example.memoflash.home.account

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.memoflash.R
import com.example.memoflash.core.SessionStore
import com.example.memoflash.databinding.FragmentAccountBinding
import com.example.memoflash.onboarding.personal.model.UserProfile
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountFragment : Fragment(R.layout.fragment_account) {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAccountBinding.bind(view)
        loadUserData()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            binding.txtProfileName.text = getString(R.string.no_session)
            binding.txtProfileUsername.text = getString(R.string.no_session_username)
            return
        }

        val currentEmail = currentUser.email.orEmpty()
        binding.txtProfileEmail.text = getString(R.string.profile_email_format, currentEmail)
        SessionStore.currentProfile
            ?.takeIf { it.id == currentUser.uid }
            ?.let { renderProfile(it, currentEmail) }

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    binding.txtProfileName.text =
                        currentUser.email ?: getString(R.string.memo_user_fallback)
                    binding.txtProfileUsername.text = getString(R.string.pending_profile_username)
                    Snackbar.make(
                        binding.root,
                        getString(R.string.profile_not_saved),
                        Snackbar.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                val profile = UserProfile(
                    id = document.getString("id") ?: currentUser.uid,
                    firstName = document.getString("firstName").orEmpty(),
                    lastName = document.getString("lastName").orEmpty(),
                    userName = document.getString("userName").orEmpty(),
                    phone = document.getString("phone").orEmpty(),
                    birthDate = document.getString("birthDate").orEmpty()
                )
                SessionStore.currentProfile = profile
                renderProfile(profile, currentEmail)
            }
            .addOnFailureListener {
                Snackbar.make(
                    binding.root,
                    it.localizedMessage ?: getString(R.string.profile_load_error),
                    Snackbar.LENGTH_LONG
                ).show()
            }
    }

    private fun renderProfile(profile: UserProfile, email: String) {
        val fullName = "${profile.firstName} ${profile.lastName}".trim()
        val emptyValue = getString(R.string.empty_value)
        binding.txtProfileName.text = fullName.ifBlank { getString(R.string.memo_user_fallback) }
        binding.txtProfileUsername.text = getString(
            R.string.profile_username_format,
            profile.userName.ifBlank { getString(R.string.memoflash_username_fallback) }
        )
        binding.txtProfileEmail.text = getString(
            R.string.profile_email_format,
            email.ifBlank { emptyValue }
        )
        binding.txtProfilePhone.text = getString(
            R.string.profile_phone_format,
            profile.phone.ifBlank { emptyValue }
        )
        binding.txtProfileBirthDate.text = getString(
            R.string.profile_birth_date_format,
            profile.birthDate.ifBlank { emptyValue }
        )
        binding.txtProfileInitials.text = initialsFrom(profile.firstName, profile.lastName)
    }

    private fun initialsFrom(firstName: String, lastName: String): String {
        val first = firstName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        val last = lastName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        return (first + last).ifBlank { getString(R.string.default_initials) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
