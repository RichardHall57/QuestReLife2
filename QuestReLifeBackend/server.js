require('dotenv').config();
const express = require('express');
const axios = require('axios');
const bodyParser = require('body-parser');

const app = express();
app.use(bodyParser.json());

const { GCORE_API_TOKEN, PORT } = process.env;

// ---- IMAGE GENERATION ENDPOINT ----
app.post("/api/generate-image", async (req, res) => {
  try {
    const { prompt } = req.body;
    if (!prompt || prompt.trim() === "") {
      return res.status(400).json({ error: "Prompt is required" });
    }

    // Gcore AI endpoint
    const imageGenUrl = "https://api.gcore.com/ai/image/generate";

    // Payload with required fields
    const payload = {
      prompt,
      model: "fantasy-v1",  // check your Gcore API docs for available models
      size: "1024x1024"     // example size
    };

    // Make API request using APIKey
    const genResp = await axios.post(imageGenUrl, payload, {
      headers: {
        Authorization: `APIKey ${GCORE_API_TOKEN}`,
        "Content-Type": "application/json"
      }
    });

    // Log full API response for debugging
    console.log("Full Gcore API response:", JSON.stringify(genResp.data, null, 2));

    // Extract image URL from response
    let imageUrl = null;
    if (genResp.data?.image_url) imageUrl = genResp.data.image_url;
    else if (genResp.data?.result?.url) imageUrl = genResp.data.result.url;
    else if (Array.isArray(genResp.data?.images) && genResp.data.images.length > 0) {
      imageUrl = genResp.data.images[0].url || genResp.data.images[0].image_url;
    }

    if (!imageUrl) {
      return res.status(500).json({ error: "No image URL returned", fullResponse: genResp.data });
    }

    return res.json({ image_url: imageUrl });

  } catch (err) {
    console.error("Image generation error:", err.response?.data || err.message);
    return res.status(500).json({ error: "Failed to generate image", details: err.response?.data || err.message });
  }
});

// ---- START SERVER ----
app.listen(PORT || 3000, () => {
  console.log(`Server running on port ${PORT || 3000}`);
});
