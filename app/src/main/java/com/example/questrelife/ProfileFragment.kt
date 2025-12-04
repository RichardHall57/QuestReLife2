package com.example.questrelife

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import coil.ImageLoader
import coil.request.ImageRequest
import java.net.URLEncoder

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var generateButton: Button
    private lateinit var profileImageView: ImageView
    private lateinit var progressBar: ProgressBar

    // Spinners
    private lateinit var classSpinner: Spinner
    private lateinit var gearSpinner: Spinner
    private lateinit var poseSpinner: Spinner
    private lateinit var styleSpinner: Spinner

    private var currentBitmap: Bitmap? = null
    private var currentImageUrl: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        generateButton = view.findViewById(R.id.button_generate_image)
        profileImageView = view.findViewById(R.id.profile_image_view)
        progressBar = view.findViewById(R.id.progress_bar)

        // Connect spinners
        classSpinner = view.findViewById(R.id.profile_class_spinner)
        gearSpinner = view.findViewById(R.id.profile_gear_spinner)
        poseSpinner = view.findViewById(R.id.profile_pose_spinner)
        styleSpinner = view.findViewById(R.id.profile_style_spinner)

        setupSpinners()

        generateButton.setOnClickListener { testPollinationsAI() }
    }

    private fun setupSpinners() {
        // Example data
        val classOptions = listOf("Warrior", "Mage", "Archer")
        val gearOptions = listOf("Sword", "Staff", "Bow")
        val poseOptions = listOf("Standing", "Attack", "Casting")
        val styleOptions = listOf("Anime", "Realistic", "Cartoon")

        classSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, classOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        gearSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, gearOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        poseSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, poseOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        styleSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, styleOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Optional: react to selections
        classSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                Toast.makeText(requireContext(), "Class: ${classOptions[position]}", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun testPollinationsAI() {
        // Build prompt using spinner values
        val selectedClass = classSpinner.selectedItem.toString()
        val selectedGear = gearSpinner.selectedItem.toString()
        val selectedPose = poseSpinner.selectedItem.toString()
        val selectedStyle = styleSpinner.selectedItem.toString()

        val testPrompt = "A $selectedClass with $selectedGear, pose: $selectedPose, style: $selectedStyle, anime art style, detailed, digital art, colorful"
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
                    Toast.makeText(requireContext(), "Failed to load image.", Toast.LENGTH_SHORT).show()
                }
            )
            .build()
        loader.enqueue(request)
    }
}
