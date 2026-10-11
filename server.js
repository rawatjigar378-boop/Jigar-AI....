import express from "express";
import OpenAI from "openai";

const app = express();
app.use(express.json({ limit: "1mb" }));
const port = process.env.PORT || 3000;
const apiKey = process.env.OPENAI_API_KEY;
const client = apiKey ? new OpenAI({ apiKey }) : null;

app.get("/", (_req, res) => res.json({ service: "JIGAR AI", ok: true }));

app.post("/chat", async (req, res) => {
  try {
    if (!client) return res.status(500).json({ error: "Server पर OPENAI_API_KEY सेट नहीं है।" });
    const message = String(req.body?.message || "").trim();
    if (!message) return res.status(400).json({ error: "message ज़रूरी है।" });
    if (message.length > 4000) return res.status(413).json({ error: "सवाल बहुत लंबा है।" });
    const result = await client.chat.completions.create({
      model: process.env.OPENAI_MODEL || "gpt-4o-mini",
      messages: [
        { role: "system", content: "आप JIGAR AI हैं। सरल हिंदी में मददगार और स्पष्ट जवाब दें। यदि किसी काम को सचमुच नहीं कर सकते, तो साफ़ बताएं।" },
        { role: "user", content: message }
      ],
      max_tokens: 700
    });
    res.json({ reply: result.choices[0]?.message?.content || "माफ़ कीजिए, जवाब नहीं मिला।" });
  } catch (e) {
    console.error("chat error:", e?.message || e);
    res.status(500).json({ error: "AI जवाब लेते समय समस्या हुई।" });
  }
});

app.post("/image", async (req, res) => {
  try {
    if (!client) return res.status(500).json({ error: "Server पर OPENAI_API_KEY सेट नहीं है।" });
    const prompt = String(req.body?.prompt || "").trim();
    if (!prompt) return res.status(400).json({ error: "फोटो का विवरण ज़रूरी है।" });
    if (prompt.length > 3000) return res.status(413).json({ error: "विवरण बहुत लंबा है।" });
    const result = await client.images.generate({
      model: process.env.OPENAI_IMAGE_MODEL || "gpt-image-1",
      prompt,
      size: "1024x1024"
    });
    const item = result.data?.[0];
    if (item?.url) return res.json({ imageUrl: item.url });
    if (item?.b64_json) {
      return res.json({ error: "Image API ने base64 image दी है। इस starter में hosted image URL अपेक्षित है; backend में सुरक्षित image storage जोड़ें।" });
    }
    res.status(502).json({ error: "Image result खाली मिला।" });
  } catch (e) {
    console.error("image error:", e?.message || e);
    res.status(500).json({ error: "फोटो बनाते समय समस्या हुई। अपने API access और billing की जाँच करें।" });
  }
});

app.post("/video", (_req, res) => {
  res.status(501).json({
    error: "इस starter में video generation provider अभी configured नहीं है। वीडियो बनाने के लिए समर्थित video-generation API, polling और output storage अलग से जोड़ना होगा।"
  });
});

app.listen(port, () => console.log(`JIGAR AI backend listening on ${port}`));
