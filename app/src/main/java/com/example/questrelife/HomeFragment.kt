package com.example.questrelife

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.questrelife.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.example.questrelife.utils.ImageUtils

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initially show default avatar
        binding.ivProfileImage.setImageResource(R.drawable.ic_default_avatar)

        // Make image clickable to replace
        binding.ivProfileImage.setOnClickListener {
            Snackbar.make(binding.root, "Image clicked! Loading AI portrait...", Snackbar.LENGTH_SHORT).show()
            replaceProfileImage()
        }

        // Simulated stats
        val xp = 720
        val nextLevelXP = 1000
        val level = 7
        val activeQuests = 3
        val completedQuests = 12
        val quotes = listOf(
            "One step closer to greatness.",
            "Small progress is still progress.",
            "Every quest shapes your legacy."
        )

        binding.tvWelcome.text = "Welcome back, Adventurer!"
        binding.tvStats.text = """
            Level: $level
            XP: $xp / $nextLevelXP
            Active Quests: $activeQuests
            Completed Quests: $completedQuests
        """.trimIndent()
        binding.tvQuote.text = quotes.random()

        binding.btnOpenLog.setOnClickListener {
            Snackbar.make(binding.root, "Opening Quest Log…", Snackbar.LENGTH_SHORT).show()
        }

        return binding.root
    }

    private fun replaceProfileImage() {
        // Load the last saved AI-generated image from ProfileFragment
        val prefs = requireContext().getSharedPreferences("MindSpirePrefs", 0)
        val imageIdentifier = prefs.getString("profile_image_identifier", null)

        val bitmap = if (imageIdentifier != null) {
            ImageUtils.loadBitmap(requireContext(), imageIdentifier)
        } else {
            null
        }

        if (bitmap != null) {
            binding.ivProfileImage.setImageBitmap(bitmap)
            Snackbar.make(binding.root, "Profile image updated!", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root, "No AI image found. Generate one in Profile.", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


