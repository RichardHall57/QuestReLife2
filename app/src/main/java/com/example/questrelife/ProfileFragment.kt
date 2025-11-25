package com.example.questrelife

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import coil.ImageLoader
import coil.request.ImageRequest
import java.net.URLEncoder

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var generateButton: Button
    private lateinit var profileImageView: ImageView
    private lateinit var progressBar: ProgressBar

    private var currentBitmap: Bitmap? = null
    private var currentImageUrl: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        generateButton = view.findViewById(R.id.button_generate_image)
        profileImageView = view.findViewById(R.id.profile_image_view)
        progressBar = view.findViewById(R.id.progress_bar)

        // Test Pollinations AI
        generateButton.setOnClickListener { testPollinationsAI() }
    }

    private fun testPollinationsAI() {
        val testPrompt = "A cute fantasy cat, pixel art style"
        val encodedPrompt = URLEncoder.encode(testPrompt, "UTF-8")
        val url = "https://image.pollinations.ai/prompt/$encodedPrompt?width=512&height=512"

        Log.d("ProfileFragment", "Testing Pollinations URL: $url")
        loadImageFromUrl(url)
    }

    private fun loadImageFromUrl(url: String) {
        progressBar.visibility = View.VISIBLE

        val loader = ImageLoader(requireContext())
        val request = ImageRequest.Builder(requireContext())
            .data(url)
            .target(
                onStart = { progressBar.visibility = View.VISIBLE },
                onSuccess = { drawable ->
                    progressBar.visibility = View.GONE
                    profileImageView.setImageDrawable(drawable)
                    currentBitmap = (drawable as? BitmapDrawable)?.bitmap
                    currentImageUrl = url
                    Toast.makeText(requireContext(), "Image loaded!", Toast.LENGTH_SHORT).show()
                },
                onError = { _ ->
                    progressBar.visibility = View.GONE
                    Log.e("ProfileFragment", "Image load failed for URL: $url")
                    Toast.makeText(requireContext(), "Failed to load image.", Toast.LENGTH_SHORT).show()
                }
            )
            .build()

        loader.enqueue(request)
    }
}
